package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.ClientSignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a client signature is invalid.
 */
@Immutable
public class InvalidClientSignatureException extends InvalidSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid client signature exception.
     * 
     * @param signature the client signature that is invalid.
     */
    protected InvalidClientSignatureException(@Nonnull ClientSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new invalid client signature exception.
     * 
     * @param signature the client signature that is invalid.
     * 
     * @return a new invalid client signature exception.
     */
    @Pure
    public static @Nonnull InvalidClientSignatureException get(@Nonnull ClientSignatureWrapper signature) {
        return new InvalidClientSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ClientSignatureWrapper getSignature() {
        return (ClientSignatureWrapper) super.getSignature();
    }
    
}
