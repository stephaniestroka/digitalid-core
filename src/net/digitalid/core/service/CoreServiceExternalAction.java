package net.digitalid.core.service;

import net.digitalid.core.data.Service;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.ExternalAction;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class models the {@link ExternalAction external actions} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject and the recipient are the same for external actions of the core service.");
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public abstract class CoreServiceExternalAction extends ExternalAction {
    
    /**
     * Creates an external action that encodes the content of a packet about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected CoreServiceExternalAction(@Nonnull NonHostEntity entity, @Nonnull InternalIdentity subject) {
        super(entity, subject.getAddress(), subject.getAddress().getHostIdentifier());
    }
    
    /**
     * Creates an external action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected CoreServiceExternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new InvalidEncodingException("The host of the subject and the recipient have to be the same for external actions of the core service.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return true;
    }
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return true;
    }
    
    
    @Override
    @NonCommitting
    public abstract @Nullable CoreServiceActionReply executeOnHost() throws PacketException, SQLException;
    
}
