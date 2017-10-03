/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.alkemy.common.visitor.impl;

import org.alkemy.common.parse.impl.VisitableAlkemyElement;
import org.alkemy.common.visitor.AlkemyElementVisitor;
import org.alkemy.common.visitor.AlkemyNodeReader;
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypedNode;

/**
 * Common functionality of some traverses.
 */
public abstract class AbstractTraverser<R, P> implements AlkemyNodeReader<R, P>
{
    /**
     * Branches descending from a null node are also evaluated.
     */
    public static final int INCLUDE_NULL_BRANCHES = 0x1;
    
    /**
     * Instantiate nodes which are null.
     */
    public static final int INSTANTIATE_NODES = 0x2;
    
    /**
     * Nodes are also evaluated by the {@link AlkemyElementVisitor}
     */
    public static final int VISIT_NODES = 0x4;
    
    /**
     * Leafs are ignored.
     */
    public static final int IGNORE_LEAFS = 0x8;
    
    protected boolean visitNodes;
    
    protected AbstractTraverser(boolean visitNodes)
    {
        this.visitNodes = visitNodes;
    }
    
    @Override
    public R create(AlkemyElementVisitor<?, ?> aev, TypedNode<R, ? extends VisitableAlkemyElement> root)
    {
        Assertions.nonNull(root);
        
        final R instance = root.data().newInstance(root.type());
        if (instance != null)
        {
            root.data().set(instance, null);
            processBranch(aev, root, instance);
        }
        return instance;
    }

    @Override
    public R create(AlkemyElementVisitor<P, ?> aev, TypedNode<R, ? extends VisitableAlkemyElement> root, P parameter)
    {
        Assertions.nonNull(root);
        
        final R instance = root.data().newInstance(root.type());
        if (instance != null)
        {
            root.data().set(instance, null);
            processBranch(aev, root, instance, parameter);
        }
        return instance;
    }

    @Override
    public R accept(AlkemyElementVisitor<?, ?> aev, TypedNode<R, ? extends VisitableAlkemyElement> root, R parameter)
    {
        Assertions.nonNull(root);

        root.data().set(parameter, null);
        processBranch(aev, root, parameter);
        return parameter;
    }

    @Override
    public R accept(AlkemyElementVisitor<P, ?> aev, TypedNode<R, ? extends VisitableAlkemyElement> root, R param1, P param2)
    {
        Assertions.nonNull(root);

        root.data().set(param1, null);
        processBranch(aev, root, param1, param2);
        return param1;
    }
    
    protected abstract void processBranch(AlkemyElementVisitor<P, ?> aev, Node<? extends VisitableAlkemyElement> e,
            Object parent, P parameter);

    protected abstract void processBranch(AlkemyElementVisitor<?, ?> aev, Node<? extends VisitableAlkemyElement> e,
            Object parent);
}
