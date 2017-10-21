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
package org.alkemy.common;

import static org.alkemy.common.visitor.impl.AbstractTraverser.INSTANTIATE_NODES;
import static org.alkemy.common.visitor.impl.AbstractTraverser.VISIT_NODES;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.agenttools.Agents;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.common.parse.impl.VisitableAlkemyElement;
import org.alkemy.common.visitor.AlkemyElementVisitor;
import org.alkemy.common.visitor.AlkemyNodeHandler;
import org.alkemy.common.visitor.AlkemyNodeHandler.Entry;
import org.alkemy.common.visitor.AlkemyNodeReader;
import org.alkemy.common.visitor.impl.AlkemyPostorderReader;
import org.alkemy.common.visitor.impl.AlkemyPreorderReader;
import org.alkemy.common.visitor.impl.NodeReaderToVisitorAdapter;
import org.alkemy.instr.AlkemizerCTF;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypedNode;

/**
 * The Alkemy library allows applying user specific {@link AlkemyNodeReader},
 * {@link AlkemyNodeHandler} and {@link AlkemyElementVisitor} strategies to a set of alkemy
 * elements.
 * <p>
 * Alkemy elements are the result of parsing an "alkemized" type with an {@link AlkemyParser}, being
 * "alkemized" and the parser user defined.
 * <p>
 * For instance the {@link AlkemyParsers#typeParser()} searches fields "super" annotated as
 * {@link AlkemyLeaf} within the type hierarchy and groups them as a directed rooted tree starting
 * from the parsed type.
 * <p>
 * If a type define alkemizations which are not supported by the used visitors, those alkemy
 * elements are left unprocessed.
 */
public class AlkemyCommon
{
    private static boolean instrumenting = false;

    /**
     * This method set-up the instrumentation of classes.
     * <p>
     * It is very important that the method is called before any of the alkemized classes are loaded
     * by the class loader for the instrumentation to work. If instrumentation fails, the library
     * fallbacks to reflection which is considerably slower (~10x).
     * <p>
     * Only alkemized classes are instrumented, others are left untouched.
     */
    public static void start()
    {
        if (!instrumenting)
        {
            Agents.add(new AlkemizerCTF());
            instrumenting = true;
        }
    }

    /**
     * Simple alkemy method to parse, create an object and apply the provided visitor to all its
     * leaves.
     * <p>
     * Simple alkemy methods are provided as a quick access to some common, non-intensive uses. But
     * should be avoided in favor of creating specialized facades for the {@link AlkemyNodeHandler}s
     * and {@link AlkemyNodeReader}s.
     * <ol>
     * <li>Parses the type Class&lt;R&gt; into a Node.
     * <li>Creates an instance of type R.
     * <li>Traverses in pre-order.
     * <li>If any node found is null, it creates and assigns an instance of it.
     * <li>For each leaf calls {@link AlkemyElementVisitor#visit(AlkemyElement, Object)} on
     * the provided aev.
     * <li>Returns the object R.
     * </ol>
     */
    public static <R, P> R mature(Class<R> r, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(INSTANTIATE_NODES).create(aev, rootNode(r));
    }

    /**
     * As {@link #mature(Class, AlkemyElementVisitor)} but including a parameter.
     */
    public static <R, P> R mature(Class<R> r, AlkemyElementVisitor<P, ?> aev, P p)
    {
        return new AlkemyPreorderReader<R, P>(INSTANTIATE_NODES | VISIT_NODES).create(aev, rootNode(r), p);
    }

    /**
     * Simple alkemy method to parse an object type and apply the provided visitor to all leaves in
     * non-null branches.
     * <p>
     * Simple alkemy methods are provided as a quick access to some common, non-intensive uses. But
     * should be avoided in favor of creating specialized facades for the {@link AlkemyNodeHandler}s
     * and {@link AlkemyNodeReader}s.
     * <ol>
     * <li>Parses the type Class&lt;R&gt; into a Node.
     * <li>Traverses in pre-order.
     * <li>If any node found is null, any sub-nodes further in the branch are ignored.
     * <li>For each leaf in a non-null branch calls
     * {@link AlkemyElementVisitor#visit(AlkemyElement, Object)} on the provided aev.
     * <li>Returns the object R.
     * </ol>
     */
    @SuppressWarnings("unchecked")
    // safe
    public static <R, P> R mature(R r, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(0).accept(aev, rootNode((Class<R>) r.getClass()), r);
    }

    /**
     * As {@link #mature(Object, AlkemyElementVisitor)} but including a parameter.
     */
    @SuppressWarnings("unchecked")
    // safe
    public static <R, P> R mature(R r, P p, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(0).accept(aev, rootNode((Class<R>) r.getClass()), r, p);
    }

    public static <R> ReaderFactory<R, R> reader(Class<R> retType)
    {
        return new ReaderFactory<R, R>(rootNode(retType));
    }

    public static <R, P> ReaderFactory<R, P> reader(Class<R> retType, Class<P> paramType)
    {
        return new ReaderFactory<R, P>(rootNode(retType));
    }
    
    public static <R> TypedNode<R, VisitableAlkemyElement> rootNode(Class<R> r) {
        return TypedNode.create(r, f -> new VisitableAlkemyElement(f));
    }

    public static class ReaderFactory<R, P>
    {
        private final TypedNode<R, ? extends VisitableAlkemyElement> root;

        private ReaderFactory(TypedNode<R, ? extends VisitableAlkemyElement> root)
        {
            this.root = root;
        }

        public SingleTypeReader<R, P> preorder(int conf)
        {
            return new SingleTypeReader<R, P>(root, new AlkemyPostorderReader<R, P>(conf));
        }

        public SingleTypeReader<R, P> postorder(int conf)
        {
            return new SingleTypeReader<R, P>(root, new AlkemyPostorderReader<R, P>(conf));
        }
    }
    
    /**
     * Part of the simple Alkemy syntax sugar. See {@link AlkemyCommon}.
     * <p>
     * This class reproduces all Alkemy operations removing the Node parameter from the signature.
     */
    public static class SingleTypeReader<R, P>
    {
        private final TypedNode<R, ? extends VisitableAlkemyElement> root;
        private final AlkemyNodeReader<R, P> anv;

        SingleTypeReader(TypedNode<R, ? extends VisitableAlkemyElement> root, AlkemyNodeReader<R, P> anv)
        {
            this.root = root;
            this.anv = anv;
        }

        /**
         * Generates an element of type R.
         */
        public R create(AlkemyElementVisitor<?, ?> aev)
        {
            return anv.create(aev, root);
        }

        /**
         * Generates an element of type R using a parameter P.
         */
        public R create(AlkemyElementVisitor<P, ?> aev, P parameter)
        {
            return anv.create(aev, root, parameter);
        }

        /**
         * Generates an element of type R, or modifies and returns the received param1 of type R.
         */
        public R accept(AlkemyElementVisitor<?, ?> aev, R parameter)
        {
            return anv.accept(aev, root, parameter);
        }
        
        /**
         * Generates an element of type R, or modifies and returns the received param1 of type R, using
         * the param2 of type P.
         */
        public R accept(AlkemyElementVisitor<P, ?> aev, R param1, P param2)
        {
            return anv.accept(aev, root, param1, param2);
        }
        
        /* * STREAM SUPPORT * */

        /**
         * Stream of {@link #iterable(TypedNode, Iterable)}
         */
        public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Iterable<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #iterable(TypedNode, Iterator)}
         */
        public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Iterator<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #peekIterable(TypedNode, Iterable)}
         */
        public Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #iterable(TypedNode, Iterator)}
         */
        public Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #iterable(TypedNode, Supplier)}
         */
        public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, hasNext).iterator(), -1, 0), false);
        }

        /* * PARALLEL STREAM SUPPORT * */

        /**
         * Parallel stream of {@link #iterable(TypedNode, Iterable)}
         */
        public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Iterable<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #iterable(TypedNode, Iterator)}
         */
        public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Iterator<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #peekIterable(TypedNode, Iterable)}
         */
        public Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #peekIterable(TypedNode, Iterator)}
         */
        public Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #iterable(TypedNode, Supplier)}
         */
        public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, hasNext).iterator(), Long.MAX_VALUE, 0), false);
        }

        /* * ITERABLE SUPPORT * */

        /**
         * See {@link AlkemyNodeHandler#iterable(Node, Iterable)}
         */
        public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Iterable<R> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#iterable(Node, Iterator)}
         */
        public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Iterator<R> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#peekIterable(Node, Iterable)}
         */
        public Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).peekIterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#peekIterable(Node, Iterator)}
         */
        public Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).peekIterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#iterable(Node, Supplier, Class)}
         */
        public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, hasNext);
        }
    }
}
