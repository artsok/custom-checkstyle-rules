package com.emirates.urp.checks;

import static com.emirates.urp.checks.common.DetailASTUtil.getFirstChild;
import static com.emirates.urp.checks.common.DetailASTUtil.streamRecursively;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Проверяем промежуточные пустые строки. Если больше пустых строк то кидаем ошибку.
 */
public class IntermediateEmptyLinesCheck extends AbstractCheck {

  public static final String MSG_EMPTY_LINE = "urp.methodEmptyLines.intermediateEmptyLinesCheck";

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
    Set<Integer> notEmptyLines = getNotEmptyLines(ast);
    int firstLine = findFirstLine(notEmptyLines);
    int lastLine = findLastLine(notEmptyLines);
    List<Integer> emptyLines = findIntermediateDoubleEmptyLines(notEmptyLines, firstLine, lastLine);
    emptyLines.forEach(emptyLine -> log(emptyLine, 0, MSG_EMPTY_LINE));
  }

  @Override
  public boolean isCommentNodesRequired() {
    return true;
  }

  private static Set<Integer> getNotEmptyLines(DetailAST ast) {
    return streamRecursively(ast)
        .flatMap(it -> getLineNumbers(it).stream())
        .collect(Collectors.toSet());
  }

  private static List<Integer> getLineNumbers(DetailAST ast) {
    if (ast.getType() == TokenTypes.BLOCK_COMMENT_BEGIN) {
      int commentFirstLine = ast.getLineNo();
      int commentLastLine = getFirstChild(ast, TokenTypes.BLOCK_COMMENT_END).getLineNo();
      return intRange(commentFirstLine, commentLastLine);
    } else if (ast.getType() == TokenTypes.TEXT_BLOCK_LITERAL_BEGIN) {
      int stringFirstLine = ast.getLineNo();
      int stringLastLine = getFirstChild(ast, TokenTypes.TEXT_BLOCK_LITERAL_END).getLineNo();
      return intRange(stringFirstLine, stringLastLine);
    } else if (ast.getType() == TokenTypes.ANNOTATION) {
      return Collections.emptyList();
    } else {
      return List.of(ast.getLineNo());
    }
  }

  private static int findFirstLine(Set<Integer> notEmptyLines) {
    return notEmptyLines.stream().mapToInt(it -> it).min()
        .orElseThrow(() -> new IllegalStateException(""));
  }

  private static int findLastLine(Set<Integer> notEmptyLines) {
    return notEmptyLines.stream().mapToInt(it -> it).max()
        .orElseThrow(() -> new IllegalStateException(""));
  }


  private static List<Integer> findIntermediateDoubleEmptyLines(Set<Integer> notEmptyLines,
      int firstLine, int lastLine) {
    var previousLineWasEmpty = new AtomicBoolean(false);
    var consecutiveEmptyLines = new ArrayList<Integer>();

    IntStream.range(firstLine, lastLine)
        .forEach(lineNumber -> {
          if (!notEmptyLines.contains(lineNumber)) {
            if (previousLineWasEmpty.get()) {
              consecutiveEmptyLines.add(lineNumber - 1); // добавляем предыдущую пустую строку
              consecutiveEmptyLines.add(lineNumber); // добавляем текущую пустую строку
            }
            previousLineWasEmpty.set(true);
          } else {
            previousLineWasEmpty.set(false);
          }
        });

    return consecutiveEmptyLines;
  }


  private static List<Integer> intRange(int stringFirstLine, int stringLastLine) {
    return IntStream.range(stringFirstLine, stringLastLine)
        .boxed()
        .toList();
  }
}