package ch.virtualid.handler;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.action.external.CoreServiceExternalAction;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.pusher.Pusher;
import ch.xdf.SignatureWrapper;
import javax.annotation.Nonnull;

/**
 * External actions can be sent by both {@link Host hosts} and {@link Client clients}.
 * <p>
 * <em>Important:</em> Do not send external actions directly but always pass them to the {@link Pusher#send(ch.virtualid.handler.ExternalAction) Pusher}!
 * 
 * @invariant getEntity() != null : "The entity of this external action is not null.";
 * 
 * @see CoreServiceExternalAction
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalAction extends Action {
    
    /**
     * Creates an external action that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected ExternalAction(@Nonnull Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject, recipient);
    }
    
    /**
     * Creates an external action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected ExternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) {
        super(entity, signature, recipient);
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof ExternalAction;
    }
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @return the permission that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @ensure return.areEmptyOrSingle() : "The returned permissions are empty or single.";
     */
    @Pure
    public @Nonnull ReadonlyAgentPermissions getFailedAuditPermissions() {
        return AgentPermissions.NONE;
    }
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @return the restrictions that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nonnull Restrictions getFailedAuditRestrictions() {
        return Restrictions.NONE;
    }
    
}
