package com.emirates.urp.checks.common;

import com.google.common.collect.Streams;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DetailASTUtil {

  public static Set<DetailAST> traverse(DetailAST node) {
    Set<DetailAST> visitedNodes = new HashSet<>();
    collectNodes(node, visitedNodes);
    return visitedNodes;
  }

  //Обход дерева
  private void collectNodes(DetailAST node, Set<DetailAST> visitedNodes) {
    if (node == null) {
      return;
    }

    // Добавление текущего узла в Set.
    visitedNodes.add(node);

    // Рекурсивный обход всех дочерних узлов.
    collectNodes(node.getFirstChild(), visitedNodes);

    // Рекурсивный обход всех "братских" узлов.
    collectNodes(node.getNextSibling(), visitedNodes);
  }

  public static Stream<DetailAST> stream(DetailAST start) {
    return Streams.stream(new DetailASTIterator(start));
  }

  public static Stream<DetailAST> streamRecursively(DetailAST start) {
    if (start == null) {
      return Stream.empty();
    } else if (start.getFirstChild() == null) {
      return stream(start.getFirstChild());
    } else {
      return Stream.concat(
          stream(start.getFirstChild()),
          stream(start.getFirstChild()).flatMap(DetailASTUtil::streamRecursively)
      );
    }
  }

  public static DetailAST getFirstChild(DetailAST ast, int type) {
    return tryGetFirstChild(ast, type)
        .orElseThrow(() -> new NoSuchElementException(
            String.format(
                "Can't find element of type %s ant %s[%s:%s] AST",
                type,
                ast.getText(),
                ast.getLineNo(),
                ast.getColumnNo())));
  }

  public static Optional<DetailAST> tryGetFirstChild(DetailAST ast, int type) {
    return stream(ast.getFirstChild())
        .filter(it -> it.getType() == type)
        .findFirst();
  }

  public static Stream<DetailAST> streamAll(DetailAST start, int type) {
    return stream(start)
        .filter(c -> c.getType() == type);
  }

  /**
   * Get parent class for element.
   *
   * @param ast - {@link DetailAST}
   * @return - class name
   */
  public static String getRootClassName(DetailAST ast) {
    if (ast == null) {
      return null;
    }

    String className = null;
    DetailAST currentAst = ast;

    while (currentAst != null) {
      if (isClassType(currentAst.getType())) {
        DetailAST ident = currentAst.findFirstToken(TokenTypes.IDENT);
        if (ident != null) {
          className = ident.getText();
        }
      }
      currentAst = currentAst.getParent();
    }

    return className;
  }

  public static void printMethodName(DetailAST ast) {
    if (ast != null && ast.getType() == TokenTypes.METHOD_DEF) {
      DetailAST methodName = ast.findFirstToken(TokenTypes.IDENT);
    }
  }

  private static boolean isClassType(int type) {
    return type == TokenTypes.CLASS_DEF ||
        type == TokenTypes.INTERFACE_DEF ||
        type == TokenTypes.ENUM_DEF ||
        type == TokenTypes.RECORD_DEF;
  }
}