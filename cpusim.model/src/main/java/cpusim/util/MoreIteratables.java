package cpusim.util;

import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * Created by kevin on 01/02/2017.
 */
public abstract class MoreIteratables {

    private MoreIteratables() {
        throw new UnsupportedOperationException("no construction.");
    }

    private static void verifyIteratorsComplete(Iterator<?>... iters) {
        for (int i = 0; i < iters.length; ++i) {
            if (iters[i].hasNext()) {
                throw new IllegalStateException("Iterator " + i + " did not finish iteration.");
            }
        }
    }

    public static <A1, A2> void zip(Iterable<A1> iterable1, Iterable<A2> iterable2, BiConsumer<? super A1, ? super A2> consumer) {
        Iterator<A1> iter1 = iterable1.iterator();
        Iterator<A2> iter2 = iterable2.iterator();

        while (iter1.hasNext() && iter2.hasNext()) {
            consumer.accept(iter1.next(), iter2.next());
        }

        verifyIteratorsComplete(iter1, iter2);
    }


    @FunctionalInterface
    public interface Consumer3<A1, A2, A3> {

        void accept(A1 a1, A2 a2, A3 a3);
    }

    public static <A1, A2, A3> void zip(Iterable<A1> iterable1, Iterable<A2> iterable2, Iterable<A3> iterable3,
                                        Consumer3<? super A1, ? super A2, ? super A3> consumer) {
        Iterator<A1> iter1 = iterable1.iterator();
        Iterator<A2> iter2 = iterable2.iterator();
        Iterator<A3> iter3 = iterable3.iterator();

        while (iter1.hasNext() && iter2.hasNext() && iter3.hasNext()) {
            consumer.accept(iter1.next(), iter2.next(), iter3.next());
        }

        verifyIteratorsComplete(iter1, iter2, iter3);
    }
}
