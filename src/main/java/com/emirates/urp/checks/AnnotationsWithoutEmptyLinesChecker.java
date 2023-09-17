package com.emirates.urp.checks;


import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.ArrayList;
import java.util.List;

/**
 * Если вы хотите убедиться, что между любыми двумя аннотациями или между последней аннотацией и
 * сигнатурой метода нет пустых строк или комментариев, вам придется немного изменить вашу логику.
 * <p>
 * Основная идея в том, чтобы проверить каждую аннотацию, а не только последнюю. После проверки
 * каждой аннотации вы будете переходить к следующей аннотации или к сигнатуре метода (если
 * аннотации закончились).
 */

/**
 * Проверка, которая проверяет наличие пустых строк и комментариев между аннотациями
 * и сигнаторой метода, конструктора и класса.
 */
public class AnnotationsWithoutEmptyLinesChecker extends AbstractCheck {

  public static final String MSG_EMPTY_LINE = "urp.methodEmptyLines.emptyLinesBetweenAnnotation";

  /**
   * Обрабатываем только конструкторы, модификаторы и record.
   *
   * @return - {@link int[]}
   */
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
    // Получаем список всех аннотаций у метода.
    final DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
    if (modifiers != null) {
      DetailAST annotation = modifiers.findFirstToken(TokenTypes.ANNOTATION);
      while (annotation != null) {
        checkForEmptyLinesOrComments(annotation);
        annotation = annotation.getNextSibling();
      }
    }
  }


  private void checkForEmptyLinesOrComments(DetailAST annotation) {
    List<DetailAST> annotations = new ArrayList<>();

    // Собираем все аннотации
    DetailAST currentAnnotation = annotation;
    while (currentAnnotation != null && currentAnnotation.getType() == TokenTypes.ANNOTATION) {
      annotations.add(currentAnnotation);
      currentAnnotation = currentAnnotation.getNextSibling();
    }

    // Проверяем промежутки между аннотациями
    for (int i = 0; i < annotations.size() - 1; i++) {
      validateGap(annotations.get(i), annotations.get(i + 1));
    }

    // Проверяем промежуток между последней аннотацией и сигнатурой метода/конструктора
    if (!annotations.isEmpty()) {
      DetailAST parent = annotations.get(annotations.size() - 1).getParent().getParent();
      validateGap(annotations.get(annotations.size() - 1), parent.findFirstToken(TokenTypes.TYPE));
    }
  }


  private void validateGap(final DetailAST start, final DetailAST end) {
    var current = start;

    while (current != null && current != end) {
      current = current.getNextSibling();

      if (current == null) {
        break;
      }

      if (isViolation(current)) {
        log(current.getLineNo(), 0, MSG_EMPTY_LINE);
        break;
      }
    }
  }


  /*
  Мы проверяем, является ли текущий токен однострочным комментарием.
  Мы проверяем, является ли текущий токен началом блочного комментария.
  Мы проверяем, была ли между текущим и предыдущим токеном пустая строка.
   */
  private boolean isViolation(DetailAST current) {
    return current.getType() == TokenTypes.SINGLE_LINE_COMMENT
        || current.getType() == TokenTypes.BLOCK_COMMENT_BEGIN
        || current.getLineNo() != current.getPreviousSibling().getLineNo() + 1;
  }

}