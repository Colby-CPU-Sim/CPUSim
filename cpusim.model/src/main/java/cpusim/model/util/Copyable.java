/**
 * 
 */
package cpusim.model.util;

/**
 * Denotes that an instance can be copied to
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public interface Copyable<T extends Copyable<T>> {

	/**
     * Provides a field-by-field copy into <code>other</code>.
     * 
     * @param other Instance to copy into
     *
     * @throws NullPointerException if `other` is null
     */
    public <U extends T> void copyTo(final U other);
	
}
