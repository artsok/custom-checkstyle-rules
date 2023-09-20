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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
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
  private AccessModifierOption[] accessModifiers = {
      AccessModifierOption.PUBLIC,
      AccessModifierOption.PROTECTED,
      AccessModifierOption.PACKAGE,
      AccessModifierOption.PRIVATE,
  };

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
  private Pattern ignoreClassNamesRegex;

  /**
   * Configure annotations that allow missed documentation.
   */
  private Set<String> allowedAnnotations = Set.of("");

  private List<String> changedFileSet = new ArrayList<>();
  private boolean enabledGit = true;

  public void setEnabledGit(boolean enabledGit) {
    this.enabledGit = enabledGit;
  }

  public void setChangedFileSet(String... changedFileSet) {
    this.changedFileSet.clear();
    this.changedFileSet.addAll(Arrays.stream(changedFileSet).collect(toSet()));
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
    this.accessModifiers =
        Arrays.copyOf(accessModifiers, accessModifiers.length);
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
      final List<GitChange> changes = DiffParser.parse(CheckCodeStyleUtils.getCurrentRepo(),
          CheckCodeStyleUtils.findCurrentBranchName());

    } catch (IOException | GitAPIException e) {
      throw new RuntimeException("Happened something here");
    }

    changedFileSet = Optional.ofNullable(fileExtensions)
        .filter(it -> enabledGit)
        .map(CheckCodeStyleUtils::getChangedFileList)
        .orElse(changedFileSet);
  }


  @Override
  public final int[] getRequiredTokens() {
    return CommonUtil.EMPTY_INT_ARRAY;
  }

  @Override
  public int[] getDefaultTokens() {
    return getAcceptableTokens();
  }

  @Override
  public int[] getAcceptableTokens() {
    return new int[]{
        TokenTypes.METHOD_DEF,
        TokenTypes.CTOR_DEF,
        TokenTypes.ANNOTATION_FIELD_DEF,
        TokenTypes.COMPACT_CTOR_DEF,
    };
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
    final Path path = Paths.get(getFilePath());
    final String filename = path.getFileName().getFileName().toString();
    final var rootClassName = getRootClassName(ast);

    boolean result = false;
    if (ignoreClassNamesRegex != null) {
      result = ignoreClassNamesRegex.matcher(rootClassName).matches();
    }

    //This condition will only be true if both conditions are true:
    // the file is present in the modified fileset and
    // the filename does not match the given regular expression.
    if (changedFileSet.contains(filename) && !result) {
      if (shouldCheck(ast)) {
        final FileContents contents = getFileContents();
        final TextBlock textBlock = contents.getJavadocBefore(ast.getLineNo());
        if (textBlock == null && !isMissingJavadocAllowed(ast)) {
          this.log(ast, MSG_JAVADOC_MISSING);
        }
      }
    }
  }

  /**
   * Some javadoc.
   *
   * @param methodDef Some javadoc.
   * @return Some javadoc.
   */
  private static int getMethodsNumberOfLine(DetailAST methodDef) {
    final int numberOfLines;
    final DetailAST lcurly = methodDef.getLastChild();
    final DetailAST rcurly = lcurly.getLastChild();

    if (lcurly.getFirstChild() == rcurly) {
      numberOfLines = 1;
    } else {
      numberOfLines = rcurly.getLineNo() - lcurly.getLineNo() - 1;
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
    return allowMissingPropertyJavadoc
        && (CheckUtil.isSetterMethod(ast) || CheckUtil.isGetterMethod(ast))
        || isContentsAllowMissingJavadoc(ast);
  }

  /**
   * Checks if the Javadoc can be missing if the method or constructor is below the minimum line
   * count or has a special annotation.
   *
   * @param ast the tree node for the method or constructor.
   * @return True if this method or constructor doesn't need Javadoc.
   */
  private boolean isContentsAllowMissingJavadoc(DetailAST ast) {
    return (ast.getType() == TokenTypes.METHOD_DEF
        || ast.getType() == TokenTypes.CTOR_DEF
        || ast.getType() == TokenTypes.COMPACT_CTOR_DEF)
        && (getMethodsNumberOfLine(ast) <= minLineCount
        || AnnotationUtil.containsAnnotation(ast, allowedAnnotations));
  }


  /**
   * Whether we should check this node.
   *
   * @param ast a given node.
   * @return whether we should check a given node.
   */
  private boolean shouldCheck(final DetailAST ast) {
    final AccessModifierOption surroundingAccessModifier =
        getSurroundingAccessModifier(ast);
    final AccessModifierOption accessModifier = CheckUtil
        .getAccessModifierFromModifiersToken(ast);
    return surroundingAccessModifier != null
        && Arrays.stream(accessModifiers)
        .anyMatch(modifier -> modifier == surroundingAccessModifier)
        && Arrays.stream(accessModifiers).anyMatch(modifier -> modifier == accessModifier);
  }
}
