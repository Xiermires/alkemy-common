/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy.common.parse.impl;

import org.alkemy.common.visitor.AlkemyElementVisitor;
import org.alkemy.common.visitor.AlkemyNodeReader;
import org.alkemy.common.visitor.impl.AlkemyPostorderReader;
import org.alkemy.common.visitor.impl.AlkemyPreorderReader;
import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.util.Node;

public class VisitableAlkemyElement extends AlkemyElement
{
    public VisitableAlkemyElement(AlkemyElement other)
    {
        super(other);
    }

    /**
     * Use this apply method to work on an element which is not linked to any concrete instance of
     * a class and return a value associated to it.
     * <p>
     * If the visitor doesn't {@link AlkemyElementVisitor#accepts(Class)} this element, element is
     * not processed and returns null.
     * <p>
     * From the returned value, it is impossible to know whenever a null value is due to the
     * {@link AlkemyElementVisitor#accepts(Class)} returning false, or the visitor itself returning
     * null. If that information is required, the caller itself should invoke the
     * {@link AlkemyElementVisitor#accepts(Class)}.
     * <p>
     * The apply methods are used to bridge between {@link AlkemyNodeReader}, which traverses a tree
     * of unknown {@link AlkemyElement} implementations, and an {@link AlkemyElementVisitor} which
     * requires an explicit {@link AlkemyElement} type. Multi-purpose node visitors, such as the
     * {@link AlkemyPreorderReader} and the {@link AlkemyPostorderReader}, use this method to avoid
     * any casting between {@link AlkemyElement} types.
     * <p>
     * Whenever a {@link AlkemyNodeReader} handles known {@link AlkemyElement} implementations,
     * these methods are not required and the node visitor can directly
     * <code>alkemyElementVisitor.visit(new ConcreteAlkemyElement(node<impl>.data())</code>.
     */
    public <P, T extends VisitableAlkemyElement> Object apply(AlkemyElementVisitor<P, T> v)
    {
        if (useMappedRefCaching())
        {
            final T t = mapFromCache(v);
            return t != null ? v.create(t) : null;
        }
        else if (isNode())
            return v.create(v.map(this));
        else if (v.accepts(alkemyType()))
            return v.create(v.map(this));
        else return null;
    }

    /**
     * As {@link #apply(AlkemyElementVisitor)} but accepting an extra parameter.
     */
    public <P, T extends VisitableAlkemyElement> Object apply(AlkemyElementVisitor<P, T> v, P parameter)
    {
        if (useMappedRefCaching())
        {
            final T t = mapFromCache(v);
            return t != null ? v.create(t) : null;
        }
        else if (isNode())
            return v.create(v.map(this), parameter);
        else if (v.accepts(alkemyType()))
            return v.create(v.map(this));
        else return null;
    }

    /**
     * Use this accept method to work on an element which is not linked to any concrete instance of
     * a class and return a value associated to it.
     * <p>
     * This method can return:
     * <ul>
     * <li>True if the visitor accepts {@link AlkemyElementVisitor#accepts(Class)} this element
     * {@link #alkemyType}.
     * <li>False if the visitor doesn't accept this element alkemyType.
     * <p>
     * The accept methods are used to bridge between {@link AlkemyNodeReader}, which traverses a
     * tree of unknown {@link AlkemyElement} implementations, and an {@link AlkemyElementVisitor}
     * which requires an explicit {@link AlkemyElement} type. Multi-purpose node visitors, such as
     * the {@link AlkemyPreorderReader} and the {@link AlkemyPostorderReader}, use this method to
     * avoid any casting between {@link AlkemyElement} types.
     * <p>
     * Whenever a {@link AlkemyNodeReader} handles known {@link AlkemyElement} implementations,
     * these methods are not required and the node visitor can directly
     * <code>alkemyElementVisitor.visit(new ConcreteAlkemyElement(node<impl>.data())</code>.
     */
    public <P, T extends VisitableAlkemyElement> boolean accept(AlkemyElementVisitor<P, T> v, Object parent)
    {
        if (useMappedRefCaching())
        {
            final T t = mapFromCache(v);
            if (t != null)
            {
                v.visit(t, parent);
            }
        }
        else if (isNode())
            v.visit(v.map(this), parent);
        else if (v.accepts(alkemyType()))
            v.visit(v.map(this), parent);
        else return false;
        return true;
    }

    /**
     * As {@link #accept(AlkemyElementVisitor, Object)} but accepting an extra parameter.
     * <p>
     * Returns true if the visitor could accept this element.
     */
    public <P, T extends VisitableAlkemyElement> boolean accept(AlkemyElementVisitor<P, T> v, Object parent, P parameter)
    {
        if (useMappedRefCaching())
        {
            final T t = mapFromCache(v);
            if (t != null)
            {
                v.visit(t, parent, parameter);
            }
        }
        else if (isNode())
            v.visit(v.map(this), parent, parameter);
        else if (v.accepts(alkemyType()))
            v.visit(v.map(this), parent, parameter);
        else return false;
        return true;
    }

    /**
     * See {@link #useMappedRefCaching()}
     */
    @SuppressWarnings("unchecked")
    protected <P, T extends VisitableAlkemyElement> T mapFromCache(AlkemyElementVisitor<P, T> v)
    {
        if (v.accepts(alkemyType()))
        {
            return (T) (cacheRef != null ? cacheRef : initializeCacheRef(v));
        }
        else if (isNode())
        {
            return v.map(this); // do not cache nodes. Different AEVs might be
                                // visiting this node.
        }
        else return null;
    }

    private synchronized <P, T extends VisitableAlkemyElement> Object initializeCacheRef(AlkemyElementVisitor<P, T> v)
    {
        if (cacheRef == null)
        {
            cacheRef = v.map(this);
        }
        return cacheRef;
    }

    private Object cacheRef = null;

    /**
     * By default, mapped elements are cached to enhance the visiting performance.
     * <p>
     * That is safe so long:
     * <ul>
     * <li>v#map() maps statically for all visitors accepting this element <br>
     * { (consider) AlkemyElement e = ... (then) v.map(e) = v.map(e) = v.map(e) = ...
     * <li>Further visitors accepting this element are consistent with the mapped type (either using
     * it directly, or use a super class from it).
     * <li>Visitors work on isolated trees generated by the {@link NodeFactory} or copied from a raw
     * node (not visited) using
     * {@link Node#copy(Node, org.alkemy.util.Node.Builder, java.util.function.Function)}.
     * </ul>
     * <p>
     * Extend this method to return false otherwise.
     */
    protected boolean useMappedRefCaching()
    {
        return true;
    }
}
