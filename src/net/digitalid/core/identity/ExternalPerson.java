package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.identifier.ExternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identifier.NonHostIdentifier;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class ExternalPerson extends Person implements ExternalIdentity {
    
    /**
     * Stores the semantic type {@code external.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("external.person@core.digitalid.net").load(Person.IDENTIFIER);
    
    
    /**
     * Stores the presumable address of this external person.
     * The address is updated when the person is relocated or merged.
     */
    private @Nonnull NonHostIdentifier address;
    
    /**
     * Creates a new external person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this external person.
     */
    ExternalPerson(long number, @Nonnull ExternalIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier getAddress() {
        return address;
    }
    
    @Override
    final void setAddress(@Nonnull InternalNonHostIdentifier address) {
        this.address = address;
    }
    
}
