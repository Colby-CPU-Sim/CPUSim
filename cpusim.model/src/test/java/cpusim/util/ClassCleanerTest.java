package cpusim.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link ClassCleaner} value.
 *
 * @since 2016-12-05
 */
public class ClassCleanerTest {


    interface MockInterface {

    }

    private final MockInterface mockitoInstance = mock(MockInterface.class);


    @Test
    public void forName() throws Exception {
        Class<?> fromInstance = ClassCleaner.forName(mockitoInstance.getClass().getName());
        assertEquals(MockInterface.class, fromInstance);

        Class<?> fromStatic = ClassCleaner.forName(MockInterface.class.getName());
        assertEquals(MockInterface.class, fromStatic);
    }

    @Test
    public void cleanClass() throws Exception {
        Class<?> fromInstance = ClassCleaner.cleanClass(mockitoInstance.getClass());
        assertEquals(MockInterface.class, fromInstance);

        Class<?> fromStatic = ClassCleaner.cleanClass(MockInterface.class);
        assertEquals(MockInterface.class, fromStatic);
    }

}