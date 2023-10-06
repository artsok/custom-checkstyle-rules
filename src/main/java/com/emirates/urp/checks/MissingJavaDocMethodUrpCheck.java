package com.emirates.urp.checks;

import static com.emirates.urp.checks.common.CheckUtil.getSurroundingAccessModifier;
import static com.emirates.urp.checks.common.DetailASTUtil.getRootClassName;
import static java.util.stream.Collectors.toSet;

import com.emirates.urp.util.CheckCodeStyleUtils;
import com.emirates.urp.util.DiffParser;
import com.emirates.urp.util.GitChange;
import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * "This class is derived from the MissingJavadocMethodCheck class in Checkstyle. Additionally, it
 * is enhanced with a regular expression feature for class names, allowing you to ignore classes
 * that don't match your specified pattern. Furthermore, this check is only applied to git changes
 * made in your branch."
 */
@FileStatefulCheck
@Slf4j
public class MissingJavaDocMethodUrpCheck extends AbstractCheck {


  /**
   * A key is pointing to the warning message text in "messages.properties" file.
   */
  public static final String MSG_JAVADOC_MISSING = "urp.javadoc.missing";

  /**
   * Default value of minimal amount of lines in method to allow no documentation.
   */
  private static final int DEFAULT_MIN_LINE_COUNT = -1;

  /**
   * Specify the access modifiers where Javadoc comments are checked.
   */
  private AccessModifierOption[] accessModifiers = {AccessModifierOption.PUBLIC,
      AccessModifierOption.PROTECTED, AccessModifierOption.PACKAGE, AccessModifierOption.PRIVATE,};

  /**
   * Specify the file type extension of files to process. Default is uninitialized as the value is
   * inherited from the parent module.
   */
  private String[] fileExtensions;

  /**
   * Control the minimal amount of lines in method to allow no documentation.
   */
  private int minLineCount = DEFAULT_MIN_LINE_COUNT;

  /**
   * Control whether to allow missing Javadoc on accessor methods for properties (setters and
   * getters).
   */
  private boolean allowMissingPropertyJavadoc;


  /**
   * Ignore class whose names are matching specified regex.
   */
  private String mainBranch = "main";

  /**
   * Ignore class whose names are matching specified regex.
   */
  private Pattern ignoreClassNamesRegex;

  /**
   * Configure annotations that allow missed documentation.
   */
  private Set<String> allowedAnnotations = Set.of("");

  private List<String> changedFileSet = new ArrayList<>();

  private List<GitChange> changes = new ArrayList<>();
  private boolean enabledGit = true;

  public void setEnabledGit(boolean enabledGit) {
    this.enabledGit = enabledGit;
  }

  public void setChangedFileSet(String... changedFileSet) {
    this.changedFileSet.clear();
    this.changedFileSet.addAll(Arrays.stream(changedFileSet).collect(toSet()));
  }

  /**
   * Setter to configure main git branch.
   *
   * @param mainBranch - main git branch.
   */
  public void setMainBranch(String mainBranch) {
    this.mainBranch = mainBranch;
  }

  /**
   * Setter to configure annotations that allow missed documentation.
   *
   * @param userAnnotations user's value.
   */
  public void setAllowedAnnotations(String... userAnnotations) {
    allowedAnnotations = Set.of(userAnnotations);
  }


  /**
   * Setter to specify the access modifiers where Javadoc comments are checked.
   *
   * @param accessModifiers access modifiers.
   */
  public void setAccessModifiers(AccessModifierOption... accessModifiers) {
    this.accessModifiers = Arrays.copyOf(accessModifiers, accessModifiers.length);
  }

  /**
   * Setter to ignore class whose names are matching specified regex.
   *
   * @param pattern a pattern.
   */
  public void setIgnoreClassNamesRegex(Pattern pattern) {
    this.ignoreClassNamesRegex = pattern;
  }

  /**
   * Setter to control the minimal amount of lines in method to allow no documentation.
   *
   * @param value user's value.
   */
  public void setMinLineCount(int value) {
    minLineCount = value;
  }

  /**
   * Setter to control whether to allow missing Javadoc on accessor methods for properties (setters
   * and getters).
   *
   * @param flag a {@code Boolean} value
   */
  public void setAllowMissingPropertyJavadoc(final boolean flag) {
    allowMissingPropertyJavadoc = flag;
  }

  /**
   * Setter to specify the file type extension of files to process.
   *
   * @param extensions the set of file extensions. A missing initial '.' character of an extension
   *                   is automatically added.
   * @throws IllegalArgumentException is argument is null
   */
  public final void setFileExtensions(String... extensions) {
    if (extensions == null) {
      throw new IllegalArgumentException("Extensions array can not be null");
    }

    fileExtensions = new String[extensions.length];
    for (int i = 0; i < extensions.length; i++) {
      final String extension = extensions[i];
      if (CommonUtil.startsWithChar(extension, '.')) {
        fileExtensions[i] = extension;
      } else {
        fileExtensions[i] = "." + extension;
      }
    }
  }

  /**
   * Initiate changed files from git.
   */
  @Override
  public void init() {
    try {
      final String currentBranchName = CheckCodeStyleUtils.findCurrentBranchName();
      final String currentRepo = CheckCodeStyleUtils.getCurrentRepo();

      if (currentRepo.equalsIgnoreCase(currentBranchName)) {
        log.warn("You try to run check on the same branches");
        return;
      }

      log.debug("currentBranchName - '{}',  currentRepo - '{}, mainBranch - '{}'",
          currentBranchName, currentRepo, mainBranch);
      changes = DiffParser.parse(currentRepo, currentBranchName, mainBranch);
    } catch (IOException | GitAPIException e) {
      log.error("Couldn't get git diff in init method");
      return;
    }

    changedFileSet = Optional.ofNullable(fileExtensions).filter(it -> enabledGit)
        .map(CheckCodeStyleUtils::getChangedFileList).orElse(changedFileSet);
    log.debug("changedFileSet contains '{}'", changedFileSet);
  }


  @Override
  public final int[] getRequiredTokens() {
    return CommonUtil.EMPTY_INT_ARRAY;
  }

  @Override
  public int[] getDefaultTokens() {
    return getAcceptableTokens();
  }

  /**
   * This check applicable only to Java methods.
   *
   * @return {@link int[]}.
   */
  @Override
  public int[] getAcceptableTokens() {
    return new int[]{TokenTypes.METHOD_DEF};
  }


  /**
   * Firstly, we get changes from git (all files that we want to commit). When checkstyle is
   * proceeding checks, it each time apply his checks all files in whole project. First our
   * condition just apply our check only for specific files not for all project.
   *
   * @param ast the token to process
   */
  @SuppressWarnings("deprecation")
  @Override
  public final void visitToken(DetailAST ast) {

    //1. Получим открывающую скобку и закрывающуюся скобку для метода
    //2. Далее получим их строки
    //Get open curley bracket from first method

//    DetailAST openingBrace = ast.findFirstToken(SLIST);
//
//    if (openingBrace != null) {
//      DetailAST closingBrace = openingBrace.findFirstToken(RCURLY);

    //3.Далее вычислим диапозон (и будем смотрет входит ли в этот диапозон те строки которые мы поменяли в ФАЙЛЕ)

    //4. Если входит, то будем запускать наше правило, чтобы добавили документацию
    //5. Если не входит то скипаем

    final Path path = Paths.get(getFilePath());
    final String filename = path.getFileName().getFileName().toString();
    final var rootClassName = getRootClassName(ast);

    log.debug("File path -'{}', filename - '{}', rootClassName - '{}'", path, filename,
        rootClassName);

    boolean result = false;
    if (ignoreClassNamesRegex != null) {
      result = ignoreClassNamesRegex.matcher(rootClassName).matches();
      log.debug("ignoreClassNamesRegex is not null and result - '{}'", result);
    }

    //This condition will only be true if both conditions are true:
    // the file is present in the modified fileset and
    // the filename does not match the given regular expression.
    if (changedFileSet.contains(filename) && !result) {
      //Если измененный файл содержится в списке обновленных файлов от GIT

      log.debug("Proceeding filename - '{}'", filename);

      final DetailAST openingBrace = Optional.ofNullable(ast)
          .map(it -> it.findFirstToken(TokenTypes.SLIST)).orElse(null);
      final DetailAST closingBrace = Optional.ofNullable(openingBrace)
          .map(this::findLastRCurly).orElse(null);

      if (Objects.isNull(openingBrace) || Objects.isNull(closingBrace)) {
        log.debug("opening brace or closing brace is null");
        return;
      }

      final Set<Integer> methodLines = IntStream.range(openingBrace.getLineNo(),
          closingBrace.getLineNo() + 1).boxed().collect(Collectors.toSet());

      //Нашли файл который сейчас обрабатываем и изминения к нему взяли от GIT (Изминения это строки у файла, которые правили)

      final Optional<GitChange> gitChange = changes.stream()
          .filter(it -> it.path().contains(filename)).findFirst();

      if (gitChange.isEmpty()) {
        log.debug("Couldn't get git changes for specific class or file.");
        return;
      }

      final List<Integer> addedLines = gitChange.get().addedLines().stream().map(it -> it + 1)
          .toList();
      final List<Integer> deletedLines = gitChange.get().deletedLines().stream().map(it -> it + 1)
          .toList();

      //Проверяем входят ли в диапозон метода который сейчас проверяем любая из строк, где мы сделали изминения.
      if (addedLines.stream().anyMatch(methodLines::contains)
          || deletedLines.stream().anyMatch(methodLines::contains) && (checkModifierOption(ast))) {
        final FileContents contents = getFileContents();
        final TextBlock textBlock = contents.getJavadocBefore(ast.getLineNo());
        if (textBlock == null && !isMissingJavadocAllowed(ast)) {
          this.log(ast, MSG_JAVADOC_MISSING);
        }
      }
    }
  }

  /**
   * Get number of lines for method.
   *
   * @param methodDef - {@link DetailAST}
   * @return - number of lines.
   */
  private int getMethodsNumberOfLine(DetailAST methodDef) {
    final int numberOfLines;
    final DetailAST lCurly = methodDef.getLastChild();
    final DetailAST rCurly = lCurly.getLastChild();

    if (lCurly.getFirstChild() == rCurly) {
      numberOfLines = 1;
    } else {
      numberOfLines = rCurly.getLineNo() - lCurly.getLineNo() - 1;
    }
    return numberOfLines;
  }

  /**
   * Checks if a missing Javadoc is allowed by the check's configuration.
   *
   * @param ast the tree node for the method or constructor.
   * @return True if this method or constructor doesn't need Javadoc.
   */
  private boolean isMissingJavadocAllowed(final DetailAST ast) {
    return
        allowMissingPropertyJavadoc && (CheckUtil.isSetterMethod(ast) || CheckUtil.isGetterMethod(
            ast)) || isContentsAllowMissingJavadoc(ast);
  }

  /**
   * Checks if the Javadoc can be missing if the method or constructor is below the minimum line
   * count or has a special annotation.
   *
   * @param ast the tree node for the method or constructor.
   * @return True if this method or constructor doesn't need Javadoc.
   */
  private boolean isContentsAllowMissingJavadoc(DetailAST ast) {
    return (ast.getType() == TokenTypes.METHOD_DEF || ast.getType() == TokenTypes.CTOR_DEF
        || ast.getType() == TokenTypes.COMPACT_CTOR_DEF) && (
        getMethodsNumberOfLine(ast) <= minLineCount || AnnotationUtil.containsAnnotation(ast,
            allowedAnnotations));
  }


  /**
   * Whether we should check this node.
   *
   * @param ast a given node.
   * @return whether we should check a given node.
   */
  private boolean checkModifierOption(final DetailAST ast) {
    final AccessModifierOption surroundingAccessModifier = getSurroundingAccessModifier(ast);
    final AccessModifierOption accessModifier = CheckUtil.getAccessModifierFromModifiersToken(ast);
    return surroundingAccessModifier != null && Arrays.stream(accessModifiers)
        .anyMatch(modifier -> modifier == surroundingAccessModifier) && Arrays.stream(
        accessModifiers).anyMatch(modifier -> modifier == accessModifier);
  }

  private DetailAST findLastRCurly(DetailAST ast) {
    DetailAST child = ast.getLastChild();
    while (child != null && child.getType() != TokenTypes.RCURLY) {
      child = child.getPreviousSibling();
    }
    return child;
  }
}
