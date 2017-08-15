package net.digitalid.core.handler.method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.handler.method.query.InternalQuery;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @see InternalAction
 * @see InternalQuery
 */
@Immutable
public interface InternalMethod extends Method<NonHostEntity> {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    public @Nonnull Entity getProvidedEntity();
    
    @Pure
    @Override
    @Derive("castEntity(providedEntity)")
    public @Nonnull NonHostEntity getEntity();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nullable InternalIdentifier getProvidedSubject() {
        return null;
    }
    
    @Pure
    @Override
    @OrderOfAssignment(1)
    @Derive("getEntity().getIdentity().getAddress()")
    public @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    /**
     * Returns the restrictions required for this internal method.
     */
    @Pure
    public default @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return Restrictions.MIN;
    }
    
}
