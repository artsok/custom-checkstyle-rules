package com.emirates.urp.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * We should avoid empty line at the end of method or constructor.
 */
public class EmptyLineAtTheEndChecker extends AbstractCheck {

  public static final String MSG_EMPTY_LINE = "urp.methodEmptyLines.emptyLinesNotAllowedInTheEnd";

  @Override
  public int[] getDefaultTokens() {
    return new int[]{TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.COMPACT_CTOR_DEF};
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
    DetailAST openingBrace = ast.findFirstToken(TokenTypes.SLIST);
    DetailAST closingBrace =
        (openingBrace != null) ? findLastChildWhichHasType(openingBrace, TokenTypes.RCURLY) : null;

    if (openingBrace != null && closingBrace != null) {
      int open = openingBrace.getLineNo();
      int end = closingBrace.getLineNo();
      checkEmptyLinesAfter(open, end);
      checkEmptyLinesBefore(end, open);
    }
  }

  private DetailAST findLastChildWhichHasType(DetailAST ast, int type) {
    DetailAST child = ast.getLastChild();
    while (child != null && child.getType() != type) {
      child = child.getPreviousSibling();
    }
    return child;
  }


  private void checkEmptyLinesAfter(int startLine, int endLine) {
    String[] lines = getLines();
    for (int currentLine = startLine; currentLine < endLine; currentLine++) {
      if (lines[currentLine].trim().isEmpty()) {
        log(currentLine + 1, MSG_EMPTY_LINE);  // учитываем смещение на 1
      }
      if (!lines[currentLine].trim().isEmpty()) {
        break;
      }
    }
  }

  private void checkEmptyLinesBefore(int endLine, int startLine) {
    String[] lines = getLines();
    // -2 because =>  -1 it's our close braket, but we want to start from line that goes after breacket that way start from 2
    for (int currentLine = endLine - 2; currentLine > startLine; currentLine--) {
      if (lines[currentLine].trim().isEmpty()) {
        log(currentLine + 1, MSG_EMPTY_LINE);
      }
      if (!lines[currentLine].trim().isEmpty()) {
        break;
      }
    }
  }

}