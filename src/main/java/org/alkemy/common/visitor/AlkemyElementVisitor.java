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
package org.alkemy.common.visitor;

import org.alkemy.common.parse.impl.VisitableAlkemyElement;

public interface AlkemyElementVisitor<P, E extends VisitableAlkemyElement>
{
    default Object create(E e)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    default Object create(E e, P parameter)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    default void visit(E e, Object parent)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    default void visit(E e, Object parent, P parameter)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    E map(VisitableAlkemyElement e);

    default boolean accepts(Class<?> type)
    {
        return true;
    }
}
