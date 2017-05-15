package cpusim.model.assembler;

import com.google.common.base.Strings;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * stores a file name and a line number.  Used to display the currently
 * executing line in an assembly file when stepping through execution in
 * debug mode.
 * 
 * @author Dale Skrien
 * @since 2010-05-27
 */
public class SourceLine {
	
    private final int line;
    private final String fileName;

    public SourceLine(int line, String fileName) {
    	checkArgument(line >= 0);
    	checkArgument(!Strings.isNullOrEmpty(fileName));
    	
        this.line = line;
        this.fileName = fileName;
    }

    /**
     * Line number within file. 
     * @return 
     */
    public int getLine() {
        return line;
    }

    public String getFileName() {
        return fileName;
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(line, fileName);
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof SourceLine))
            return false;
        
        SourceLine other = (SourceLine) obj;
        return other.line == this.line 
        		&& other.fileName.equals(this.fileName);
    }
}
