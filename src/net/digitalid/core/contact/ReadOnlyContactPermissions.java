package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.annotations.reference.Capturable;
import net.digitalid.collections.annotations.freezable.NonFrozen;
import net.digitalid.annotations.state.Pure;

/**
 * This interface provides read-only access to {@link FreezableContactPermissions contact permissions} and should <em>never</em> be cast away.
 * 
 * @see FreezableContactPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface ReadOnlyContactPermissions extends ReadOnlyAttributeTypeSet {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContactPermissions clone();
    
}
