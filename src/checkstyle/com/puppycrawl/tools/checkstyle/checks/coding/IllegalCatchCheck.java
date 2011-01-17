////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2010  Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.CheckUtils;

/**
 * Catching java.lang.Exception, java.lang.Error or java.lang.RuntimeException
 * is almost never acceptable.
 * @author <a href="mailto:simon@redhillconsulting.com.au">Simon Harris</a>
 */
public final class IllegalCatchCheck extends AbstractIllegalCheck
{

    /** disable warning for "catch" blocks containing
     * throwing an exception. */
    private boolean mThrowPermit;

    /** disable warning for "catch" blocks containing
     * rethrowing an exception. */
    private boolean mRethrowPermit;

    /**
     * Enable(false) | Disable(true) warning for "catch" blocks containing
     * throwing an exception.
     * @param aValue Disable warning for throwing
     */
    public void setThrowPermit(final boolean aValue)
    {
        mThrowPermit = aValue;
    }

    /**
     * Enable(false) | Disable(true) warning for "catch" blocks containing
     * rethrowing an exception.
     * @param aValue Disable warning for rethrowing
     */
    public void setRethrowPermit(final boolean aValue)
    {
        mRethrowPermit = aValue;
    }

    /** Creates new instance of the check. */
    public IllegalCatchCheck()
    {
        super(new String[]{"Exception", "Error", "RuntimeException",
            "Throwable", "java.lang.Error", "java.lang.Exception",
            "java.lang.RuntimeException", "java.lang.Throwable", });
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[]{TokenTypes.LITERAL_CATCH};
    }

    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(DetailAST aDetailAST)
    {
        final DetailAST paramDef = aDetailAST
                .findFirstToken(TokenTypes.PARAMETER_DEF);

        final DetailAST throwAST = getThrowAST(aDetailAST);
        final boolean noWarning = (throwAST != null
             && throwAST.getFirstChild().getFirstChild() != null
             && ((mThrowPermit && throwAST.getFirstChild().getType()
                     == TokenTypes.EXPR
            && throwAST.getFirstChild().getFirstChild().getType()
                     == TokenTypes.IDENT)
             | (mRethrowPermit && throwAST.getFirstChild().getType()
                     == TokenTypes.EXPR
            && throwAST.getFirstChild().getFirstChild().getType()
                     == TokenTypes.LITERAL_NEW)));

        final DetailAST excType = paramDef.findFirstToken(TokenTypes.TYPE);
        final FullIdent ident = CheckUtils.createFullType(excType);

        if (!noWarning && isIllegalClassName(ident.getText())) {
            log(aDetailAST, "illegal.catch", ident.getText());
        }
    }

    /** Looking for the keyword "throw" among current (aParentAST) node childs.
     * @param aParentAST - the current parent node.
     * @return null if the "throw" keyword was not found
     * or the LITERAL_THROW DetailAST otherwise
     */
    public DetailAST getThrowAST(DetailAST aParentAST)
    {

        final DetailAST asts[] = getChilds(aParentAST);

        for (DetailAST currentNode : asts) {

            if (currentNode.getType() != TokenTypes.PARAMETER_DEF
                    && currentNode.getNumberOfChildren() > 0)
            {
                final DetailAST astResult = (getThrowAST(currentNode));
                if (astResult != null) {
                    return astResult;
                }
            }

            if (currentNode.getType() == TokenTypes.LITERAL_THROW) {
                return currentNode;
            }

            if (currentNode.getNextSibling() != null) {
                currentNode = currentNode.getNextSibling();
            }
        }
        return null;
    }

    /** Gets all the children one level below on the current top node.
     * @param aNode - current parent node.
     * @return an array of childs one level below
     * on the current parent node aNode. */
    public DetailAST[] getChilds(DetailAST aNode)
    {
        final DetailAST[] result = new DetailAST[aNode.getChildCount()];

        DetailAST currNode = aNode.getFirstChild();

        for (int i = 0; i < aNode.getNumberOfChildren(); i++) {
            result[i] = currNode;
            currNode = currNode.getNextSibling();
        }

        return result;
    }

}