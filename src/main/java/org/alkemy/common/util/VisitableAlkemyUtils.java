package org.alkemy.common.util;

import java.util.ArrayList;
import java.util.List;

import org.alkemy.common.parse.impl.VisitableAlkemyElement;
import org.alkemy.common.visitor.AlkemyElementVisitor;
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;

// TODO ?
public class VisitableAlkemyUtils
{
    /**
     * Use this method if you are to process the same flat node (node with no grand children)
     * several times and you want to enhance the processing performance.
     * <p>
     * This method is meant to be combined with:
     * <ul>
     * <li>
     * {@link #createFlatNodeInstance(VisitableAlkemyElement[], AlkemyElementVisitor, Node, Class)}
     * <li>{@link #processFlatNodeInstance(List, AlkemyElementVisitor, Node, Object)}
     * </ul>
     */
    public static <R, P, E extends VisitableAlkemyElement> List<E> mapFlatNodeLeafs(AlkemyElementVisitor<P, E> aev,
            Node<? extends VisitableAlkemyElement> e, Class<R> retType, boolean proceedIfNotSupported)
    {
        Assertions.noneNull(e, retType);
        Assertions.isTrue(e.branchDepth() == 1, "Provided AlkemyElement node '%s' is not flat", e.data().toString());

        boolean supported = true;
        final List<E> mapped = new ArrayList<E>();
        for (Node<? extends VisitableAlkemyElement> node : e.children())
        {
            final E map = aev.map(new VisitableAlkemyElement(node.data()));
            if (aev.accepts(node.data().alkemyType()))
            {
                mapped.add(map);
            }
            else supported = false;
        }
        return supported || proceedIfNotSupported ? mapped : null;
    }

    /**
     * Creates an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)}
     */
    public static <R, P, E extends VisitableAlkemyElement> R createFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends VisitableAlkemyElement> e, Class<R> retType)
    {
        final Object[] args = new Object[mapped.size()];
        for (int i = 0; i < args.length; i++)
        {
            args[i] = aev.create(mapped.get(i));
        }
        return e.data().newInstance(retType, args);
    }

    /**
     * Creates an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)} and includes a
     * parameter.
     */
    public static <R, P, E extends VisitableAlkemyElement> R createFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends VisitableAlkemyElement> e, Class<R> retType, P parameter)
    {
        final Object[] args = new Object[mapped.size()];
        for (int i = 0; i < args.length; i++)
        {
            args[i] = aev.create(mapped.get(i), parameter);
        }
        return e.data().newInstance(retType, args);
    }

    /**
     * Process an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)}
     */
    public static <R, P, E extends VisitableAlkemyElement> R processFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends VisitableAlkemyElement> e, R instance)
    {
        for (E map : mapped)
        {
            aev.visit(map, instance);
        }
        return instance;
    }

    /**
     * Process an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)} and includes a
     * parameter.
     */
    public static <R, P, E extends VisitableAlkemyElement> R processFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends VisitableAlkemyElement> e, R instance, P parameter)
    {
        for (E map : mapped)
        {
            aev.visit(map, instance, parameter);
        }
        return instance;
    }
}
