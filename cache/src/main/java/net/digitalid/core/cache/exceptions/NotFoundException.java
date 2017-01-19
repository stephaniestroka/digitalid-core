package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This exception is thrown when something cannot be found.
 * 
 * @see AttributeNotFoundException
 * @see CertificateNotFoundException
 */
@Immutable
public abstract class NotFoundException extends ExternalException {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    private final @Nonnull InternalIdentity identity;
    
    /**
     * Returns the identity whose something could not be found.
     */
    @Pure
    public final @Nonnull InternalIdentity getIdentity() {
        return identity;
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    private final @Nonnull @AttributeType SemanticType type;
    
    /**
     * Returns the type of the something that could not be found.
     */
    @Pure
    public final @Nonnull SemanticType getType() {
        return type;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new something not found exception with the given identity and type.
     * 
     * @param something a string stating what the something actually is.
     * @param identity the identity whose something could not be found.
     * @param type the type of the something that could not be found.
     */
    protected NotFoundException(@Nonnull String something, @Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        super("The " + something + " " + type.getAddress() + " of " + identity.getAddress() + " could not be found.");
        
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
        this.identity = identity;
        this.type = type;
    }
    
}
