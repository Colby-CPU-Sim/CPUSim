/*
 * File: ValidationException.java
 * Author: Dale Skrien
 * Date: Aug 14, 2007
 */
package cpusim.model.util;

public class ValidationException  extends RuntimeException
{
    public ValidationException(String s)
    {
        super(s);
    }
    
    public ValidationException(String s, Throwable cause) {
        super(s, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
