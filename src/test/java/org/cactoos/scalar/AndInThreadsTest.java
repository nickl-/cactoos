/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2018 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.cactoos.scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.func.FuncOf;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.matchers.MatcherOf;
import org.cactoos.matchers.ScalarHasValue;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AndInThreads}.
 * @author Vseslav Sekorin (vssekorin@gmail.com)
 * @author Mehmet Yildirim (memoyil@gmail.com)
 * @version $Id$
 * @since 0.25
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class AndInThreadsTest {

    @Test
    public void allTrue() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                new True(),
                new True(),
                new True()
            ).value(),
            Matchers.equalTo(true)
        );
    }

    @Test
    public void oneFalse() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                new True(),
                new False(),
                new True()
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void allFalse() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                new IterableOf<Scalar<Boolean>>(
                    new False(),
                    new False(),
                    new False()
                )
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void emptyIterator() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(Collections.emptyList()).value(),
            Matchers.equalTo(true)
        );
    }

    @Test
    public void iteratesList() {
        final List<String> list = new LinkedList<>();
        MatcherAssert.assertThat(
            "Can't iterate a list with a procedure",
            new AndInThreads(
                new Mapped<String, Scalar<Boolean>>(
                    new FuncOf<>(list::add, () -> true),
                    new IterableOf<>("hello", "world")
                )
            ),
            new ScalarHasValue<>(
                Matchers.allOf(
                    Matchers.equalTo(true),
                    new MatcherOf<>(
                        value -> list.size() == 2
                    )
                )
            )
        );
    }

    @Test
    public void iteratesEmptyList() {
        final List<String> list = new LinkedList<>();
        MatcherAssert.assertThat(
            "Can't iterate a list",
            new AndInThreads(
                new Mapped<String, Scalar<Boolean>>(
                    new FuncOf<>(list::add, () -> true), Collections.emptyList()
                )
            ),
            new ScalarHasValue<>(
                Matchers.allOf(
                    Matchers.equalTo(true),
                    new MatcherOf<>(
                        value -> {
                            return list.isEmpty();
                        }
                    )
                )
            )
        );
    }

    @Test
    public void testProc() throws Exception {
        final List<Integer> list = new LinkedList<>();
        new AndInThreads(
            (Proc<Integer>) list::add,
            1, 1
        ).value();
        MatcherAssert.assertThat(
            list.size(),
            Matchers.equalTo(2)
        );
    }

    @Test
    public void testFunc() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                input -> input > 0,
                1, -1, 0
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void testProcIterator() throws Exception {
        final List<Integer> list = Collections.synchronizedList(
            new ArrayList<Integer>(2)
        );
        new AndInThreads(
            new Proc.NoNulls<Integer>(list::add),
            Arrays.asList(1, 2).iterator()
        ).value();
        MatcherAssert.assertThat(
            list,
            Matchers.containsInAnyOrder(1, 2)
        );
    }

    @Test
    public void testProcIterable() throws Exception {
        final List<Integer> list = Collections.synchronizedList(
            new ArrayList<Integer>(2)
        );
        new AndInThreads(
            new Proc.NoNulls<Integer>(list::add),
            Arrays.asList(1, 2)
        ).value();
        MatcherAssert.assertThat(
            list,
            Matchers.containsInAnyOrder(1, 2)
        );
    }

    @Test
    public void testIteratorScalarBoolean() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                new ListOf<Scalar<Boolean>>(
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(true)
                    ),
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(false)
                    )
                ).iterator()
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void testIterableScalarBoolean() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                new ListOf<Scalar<Boolean>>(
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(true)
                    ),
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(false)
                    )
                )
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void testExecServiceProcValues() throws Exception {
        final List<Integer> list = Collections.synchronizedList(
            new ArrayList<Integer>(2)
        );
        final ExecutorService service = Executors.newSingleThreadExecutor();
        new AndInThreads(
            service,
            new Proc.NoNulls<Integer>(list::add),
            1, 2
        ).value();
        MatcherAssert.assertThat(
            list,
            Matchers.containsInAnyOrder(1, 2)
        );
    }

    @Test
    public void testExecServiceProcIterator() throws Exception {
        final List<Integer> list = Collections.synchronizedList(
            new ArrayList<Integer>(2)
        );
        new AndInThreads(
            Executors.newSingleThreadExecutor(),
            new Proc.NoNulls<Integer>(list::add),
            Arrays.asList(1, 2).iterator()
        ).value();
        MatcherAssert.assertThat(
            list,
            Matchers.containsInAnyOrder(1, 2)
        );
    }

    @Test
    public void testExecServiceProcIterable() throws Exception {
        final List<Integer> list = Collections.synchronizedList(
            new ArrayList<Integer>(2)
        );
        final ExecutorService service = Executors.newSingleThreadExecutor();
        new AndInThreads(
            service,
            new Proc.NoNulls<Integer>(list::add),
            Arrays.asList(1, 2)
        ).value();
        MatcherAssert.assertThat(
            list,
            Matchers.containsInAnyOrder(1, 2)
        );
    }

    @Test
    public void testExecServiceScalarBooleans() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                Executors.newSingleThreadExecutor(),
                new Scalar.NoNulls<Boolean>(
                    new Constant<Boolean>(true)
                ),
                new Scalar.NoNulls<Boolean>(
                    new Constant<Boolean>(false)
                )
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void testExecServiceIterableScalarBoolean() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                Executors.newSingleThreadExecutor(),
                new ListOf<Scalar<Boolean>>(
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(true)
                    ),
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(false)
                    )
                )
            ).value(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void testExecServiceIteratorScalarBoolean() throws Exception {
        MatcherAssert.assertThat(
            new AndInThreads(
                Executors.newSingleThreadExecutor(),
                new ListOf<Scalar<Boolean>>(
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(true)
                    ),
                    new Scalar.NoNulls<Boolean>(
                        new Constant<Boolean>(false)
                    )
                ).iterator()
            ).value(),
            Matchers.equalTo(false)
        );
    }

}
