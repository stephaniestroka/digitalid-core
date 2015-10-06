package net.digitalid.core.wrappers.exceptions;

import java.io.IOException;
import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Pure;

/**
 * The end of the input stream has been reached before the indicated data could be read.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class UnexpectedEndOfFileException extends IOException {
    
    /**
     * Creates a new unexpected end-of-file exception.
     */
    private UnexpectedEndOfFileException() {}
    
    /**
     * Returns a new unexpected end-of-file exception.
     * 
     * @return a new unexpected end-of-file exception.
     */
    @Pure
    public static @Nonnull UnexpectedEndOfFileException get() {
        return new UnexpectedEndOfFileException();
    }
    
}
