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

import java.util.function.Supplier;

import org.alkemy.common.parse.impl.VisitableAlkemyElement;
import org.alkemy.common.visitor.AlkemyElementVisitor;
import org.alkemy.common.visitor.AlkemyNodeReader;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypedNode;

public class AlkemyElementWriter<R, P> implements AlkemyNodeReader<R, P>
{
    @Override
    public R create(AlkemyElementVisitor<?, ?> aev, TypedNode<R, ? extends VisitableAlkemyElement> root)
    {
        final Parameters<P> params = new Parameters<>(aev, root.children().size());
        root.children().forEach(c -> processNode(aev, c, params));
        return root.data().newInstance(root.type(), params.get());
    }

    private void processNode(AlkemyElementVisitor<?, ?> aev, Node<? extends VisitableAlkemyElement> e, Parameters<P> params)
    {
        if (e.hasChildren())
        {
            final Parameters<P> childParams = new Parameters<>(aev, e.children().size());
            e.children().forEach(c -> processNode(aev, c, childParams));
            if (!childParams.unsupported)
            {
                params.add(e.data().newInstance(childParams.get()));
            }
        }
        else
        {
            params.tryAdd(e);
        }
    }

    static class Parameters<P> implements Supplier<Object[]>
    {
        int c = 0;
        Object[] params;
        boolean unsupported = false;
        AlkemyElementVisitor<?, ?> aev;

        Parameters(AlkemyElementVisitor<?, ?> aev, int size)
        {
            this.aev = aev;
            params = new Object[size];
        }

        void add(Object param)
        {
            params[c++] = param;
        }

        void tryAdd(Node<? extends VisitableAlkemyElement> e)
        {
            final Object v = e.data().apply(aev);
            if (unsupported)
            {
                return; // no need to visit further elements.
            }
            else if (v != null)
            {
                params[c++] = v;
            }
            else
            {
                params[c++] = null;
                unsupported = aev.accepts(e.data().alkemyType());
            }
        }

        @Override
        public Object[] get()
        {
            return params;
        }
    }
}
