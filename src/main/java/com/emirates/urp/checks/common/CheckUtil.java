package com.emirates.urp.checks.common;

import static com.puppycrawl.tools.checkstyle.utils.CheckUtil.getAccessModifierFromModifiersToken;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public final class CheckUtil {

    /**
     * Returns the access modifier of the surrounding "block".
     *
     * @param node the node to return the access modifier for
     * @return the access modifier of the surrounding block
     */
    public static AccessModifierOption getSurroundingAccessModifier(DetailAST node) {
        AccessModifierOption returnValue = null;
        for (DetailAST token = node;
             returnValue == null && !TokenUtil.isRootNode(token);
             token = token.getParent()) {
            final int type = token.getType();
            if (type == TokenTypes.CLASS_DEF
                || type == TokenTypes.INTERFACE_DEF
                || type == TokenTypes.ANNOTATION_DEF
                || type == TokenTypes.ENUM_DEF
                || type == TokenTypes.RECORD_DEF) {
                returnValue = getAccessModifierFromModifiersToken(token);
            } else if (type == TokenTypes.LITERAL_NEW) {
                break;
            }
        }
        return returnValue;
    }

}
