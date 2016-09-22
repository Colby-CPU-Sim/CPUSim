package cpusim.util;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
 
/**
 * General JavaFX utilities
 *  
 */
public abstract class FXUtilities {
	
	private FXUtilities() {
		// noop
	}
	
	/**
     * Converts a textual representation of a KeyCode into an actual KeyCode object
     * @param text textual representation of a KeyCode 
     * @return the KeyCode that the text represents
     */
    public static KeyCode charToKeyCode(String text) {
        if (text.equals("Close_Bracket")){
            return KeyCode.CLOSE_BRACKET;
        }
        else if (text.equals("Open_Bracket")){
            return KeyCode.OPEN_BRACKET;
        }
        else if (text.equals("Caps")){
            return KeyCode.CAPS;
        }
        else if (text.equals("Page_Up")){
            return KeyCode.PAGE_UP;
        }
        else if (text.equals("Page_Down")){
            return KeyCode.PAGE_DOWN;
        }
        else if (text.equals("Back_Slash")){
            return KeyCode.BACK_SLASH;
        }
        else if (text.equals("Context_Menu")){
            return KeyCode.CONTEXT_MENU;    
        }
        
        return null;
    }
    
	
	/**
	 * 
	 * @param runnable
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * 
	 * @author Tom Brus
	 * @see <a href="http://www.guigarage.com/2013/01/invokeandwait-for-javafx"/>Source</a>
	 */
	public static void runAndWait(Runnable runnable) throws InterruptedException, ExecutionException {
		FutureTask future = new FutureTask(runnable, null);
		Platform.runLater(future);
		future.get();
	}

	/**
	 * 
	 * @param callable
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * 
	 * @author Tom Brus
	 * @see <a href="http://www.guigarage.com/2013/01/invokeandwait-for-javafx"/>Source</a>
	 */
	public static <T> T runAndWait(Callable<T> callable) throws InterruptedException, ExecutionException {
		FutureTask<T> future = new FutureTask<T>(callable);
		Platform.runLater(future);
		return future.get();
	}
}