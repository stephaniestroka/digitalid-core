package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.size.EmptyOrSingle;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Handlers that can be added to the audit have to implement this interface.
 * 
 * @see Action
 * @see ActionReply
 */
@Immutable
public interface Auditable {
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public default @Nonnull @EmptyOrSingle ReadOnlyAgentPermissions getRequiredPermissionsToSeeMethod() {
        return ReadOnlyAgentPermissions.NONE;
    }
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public default @Nonnull Restrictions getRequiredRestrictionsToSeeMethod() {
        return Restrictions.MIN;
    }
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public default @Nullable Agent getRequiredAgentToSeeMethod() {
        return null;
    }
    
}
