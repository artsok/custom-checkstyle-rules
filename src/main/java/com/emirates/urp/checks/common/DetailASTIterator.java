package com.emirates.urp.checks.common;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Creating own iterator for {@link DetailAST}. AST - abstract syntax trie.
 */
class DetailASTIterator implements Iterator<DetailAST> {

  private DetailAST current;

  DetailASTIterator(DetailAST start) {
    current = start;
  }

  @Override
  public boolean hasNext() {
    return current != null;
  }

  @Override
  public DetailAST next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    DetailAST result = current;
    //set current node to next node
    current = current.getNextSibling();
    return result;
  }
}
