package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when the type of a block is invalid.
 */
@Immutable
public class InvalidTypeException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Expected Type -------------------------------------------------- */
    
    /**
     * Stores the expected type of the block.
     */
    private final @Nonnull SemanticType expectedType;
    
    /**
     * Returns the expected type of the block.
     * 
     * @return the expected type of the block.
     */
    @Pure
    public final @Nonnull SemanticType getExpectedType() {
        return expectedType;
    }
    
    /* -------------------------------------------------- Encountered Type -------------------------------------------------- */
    
    /**
     * Stores the encountered type of the block.
     */
    private final @Nonnull SemanticType encounteredType;
    
    /**
     * Returns the encountered type of the block.
     * 
     * @return the encountered type of the block.
     */
    @Pure
    public final @Nonnull SemanticType getEncounteredType() {
        return encounteredType;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid type exception with the given expected and encountered types.
     * 
     * @param expectedType the expected type of the block.
     * @param encounteredType the encountered type of the block.
     */
    protected InvalidTypeException(@Nonnull SemanticType expectedType, @Nonnull SemanticType encounteredType) {
        super("A block whose type is based on " + expectedType.getAddress() + " was expected but a block of type " + encounteredType.getAddress() + " was encountered.");
        
        this.expectedType = expectedType;
        this.encounteredType = encounteredType;
    }
    
    /**
     * Returns a new invalid type exception with the given expected and encountered types.
     * 
     * @param expectedType the expected type of the block.
     * @param encounteredType the encountered type of the block.
     * 
     * @return a new invalid type exception with the given expected and encountered types.
     */
    @Pure
    public static @Nonnull InvalidTypeException get(@Nonnull SemanticType expectedType, @Nonnull SemanticType encounteredType) {
        return new InvalidTypeException(expectedType, encounteredType);
    }
    
}
