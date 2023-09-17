package com.emirates.urp.checks;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RCURLY;
import static com.puppycrawl.tools.checkstyle.grammar.java.JavaLanguageLexer.SLIST;

import com.emirates.urp.util.CheckCodeStyleUtils;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Check the minimal amount of lines at the method and constructor where class names are matching
 * specified regex.
 */
public class JavaDocMethodSizeCheck extends AbstractCheck {

  public static final String MSG_EMPTY_LINE = "urp.methodJavaDoc.javaDocMissedOnMethods";
  private int max = 150;
  private Pattern ignoreClassNamesRegex;

  public void setIgnoreClassNamesRegex(Pattern pattern) {
    ignoreClassNamesRegex = pattern;
  }

  public void setMax(int length) {
    this.max = length;
  }

  @Override
  public int[] getDefaultTokens() {
    return new int[]{TokenTypes.METHOD_DEF};
  }

  @Override
  public int[] getAcceptableTokens() {
    return getDefaultTokens();
  }

  @Override
  public int[] getRequiredTokens() {
    return getDefaultTokens();
  }

  @Override
  public void visitToken(DetailAST ast) {
    final List<String> changedFileSet = CheckCodeStyleUtils.getChangedFileList();

    System.out.println(changedFileSet.size() + " sizze");
    changedFileSet.forEach(it -> System.out.printf("Updated files" + it));

    //System.out.printf(getFileContents().getFileName());

    //System.out.println("!!asdsadasdasdasd");

    //Check class
    String[] split = getFilePath().split("/");
    String className = split[split.length - 1];
    System.out.println("filename:  " + className);

    System.out.println("sasdaasad " + ignoreClassNamesRegex.matcher(className).matches());

    if (ignoreClassNamesRegex.matcher(className).matches()) {

      System.out.println(" aasdasdas");

      //Get open curley bracket from first method
      DetailAST openingBrace = ast.findFirstToken(SLIST);

      if (openingBrace != null) {
        DetailAST closingBrace = openingBrace.findFirstToken(RCURLY);

        //Count current method length
        int currentLength = closingBrace.getLineNo() - openingBrace.getLineNo() + 1;

        if (currentLength > this.max) {
          FileContents contents = getFileContents();
          if (Objects.nonNull(contents)) {
            TextBlock textBlock = contents.getJavadocBefore(ast.getLineNo());
            if (Objects.isNull(textBlock)) {
            }
          }
          this.log(ast.getLineNo(), ast.getColumnNo(),
              "No JavaDoc for method where length more then {1}. Current size is {0}",
              currentLength, this.max);
        }
      }
    }
  }

  /**
   * Возвращает имя методов в классе.
   *
   * @param ast - {@link DetailAST}
   * @return - {@link String}
   */
  private static String getName(DetailAST ast) {
    return ast.findFirstToken(TokenTypes.IDENT).getText();
  }
}
