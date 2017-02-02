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

    public static <A1, A2> void zip(Iterable<A1> iterable1, Iterable<A2> iterable2, BiConsumer<? super A1, ? super A2> consumer) {
        Iterator<A1> iter1 = iterable1.iterator();
        Iterator<A2> iter2 = iterable2.iterator();

        while (iter1.hasNext() && iter2.hasNext()) {
            consumer.accept(iter1.next(), iter2.next());
        }

        if (iter1.hasNext()) {
            throw new IllegalStateException("iterable1 did not finish iteration, size mismatch.");
        }


        if (iter2.hasNext()) {
            throw new IllegalStateException("iterable2 did not finish iteration, size mismatch.");
        }
    }
}
