package cpusim.gui.util;

import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.Optional;

import static com.google.common.base.Preconditions.*;
import com.google.common.base.Strings;

/**
 * Factory for loading new instances of FXML files into an {@link javafx.fxml.FXMLLoader}.
 */
public abstract class FXMLLoaderFactory {

    private FXMLLoaderFactory() {
        // stop inheritance
    }

    /**
     * Get's the resource URL for a file based on the controller's package.
     * @param clazz Controller class
     * @param name Name of the file
     * @return Optional with an URL present if found, otherwise it will be empty
     */
    public static Optional<URL> getURL(final Class<?> clazz, final String name) {
        checkNotNull("controller == null");
        checkArgument(!Strings.isNullOrEmpty(name));

        Optional<URL> uri = Optional.ofNullable(clazz.getResource(name));

        if (!uri.isPresent()) {
            final String packageName = clazz.getPackage().getName();
            final String path = "" + packageName.replace('.', '/') + '/' + name;
            final URL uriURL = clazz.getResource(path);

            uri = Optional.ofNullable(uriURL);
        }

        return uri;
    }

    /**
     * Creates a new {@link FXMLLoader} instance. Setting
     * {@link FXMLLoader#setRoot(Object)} and {@link FXMLLoader#setController(Object)}.
     * @param controller The root for the new loader
     * @param name The name of the fxml file, it must be located in the same package as the
     *             root controller passed in is (as described by {@link Class#getPackage})
     * @return new FXMLLoader instance
     *
     * @throws IllegalStateException if the resource is not found
     * @throws NullPointerException if the root is {@code null}
     */
    public static FXMLLoader fromRootController(Object controller, String name) {
        checkNotNull(controller);
        checkArgument(!Strings.isNullOrEmpty(name));

        final FXMLLoader loader = fromController(controller, name);
        loader.setRoot(controller);

        return loader;
    }

    /**
     * Creates a new {@link FXMLLoader} instance. Sets
     * {@link FXMLLoader#setController(Object)} to the passed controller.
     *
     * @param controller The root for the new loader
     * @param name The name of the fxml file, it must be located in the same package as the
     *             root controller passed in is (as described by {@link Class#getPackage})
     * @return new FXMLLoader instance
     *
     * @throws IllegalStateException if the resource is not found
     * @throws NullPointerException if the root is {@code null}
     */
    public static FXMLLoader fromController(final Object controller, final String name) {
        checkNotNull(controller, "root == null");
        checkArgument(!Strings.isNullOrEmpty(name));

        final Optional<URL> uri = getURL(controller.getClass(), name);

        if (!uri.isPresent()) {
            throw new IllegalStateException("Could not load resource for root. " +
                    "{ root = " + controller + ", name = " + name + " }");
        }

        final FXMLLoader loader = new FXMLLoader(uri.get());
        loader.setController(controller);

        return loader;
    }

}
