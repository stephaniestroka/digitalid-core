package ch.virtualid.handler.action.external;

import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.ContactPermissions;
import ch.virtualid.contact.ReadonlyContactPermissions;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Requests access to the given attributes of the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.9
 */
public final class AccessRequest extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code request.access@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("request.access@virtualid.ch").load(ContactPermissions.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the attributes of this access request.
     * 
     * @invariant !attributes.isEmpty() : "The set of attributes is not empty.";
     */
    private final @Nonnull ReadonlyContactPermissions attributes;
    
    /**
     * Creates an external action to request access to the given attributes of the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param attributes the set of attributes.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     * @require !attributes.isEmpty() : "The set of attributes is not empty.";
     */
    protected AccessRequest(@Nonnull Entity entity, @Nonnull Identity subject, @Nonnull ReadonlyContactPermissions attributes) {
        super(entity, subject);
        
        assert !attributes.isEmpty() : "The set of attributes is not empty.";
        
        this.attributes = attributes;
        this.requiredPermissions = attributes.toAgentPermissions().freeze();
    }
    
    /**
     * Creates an external action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected AccessRequest(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        this.attributes = new ContactPermissions(block).freeze();
        this.requiredPermissions = attributes.toAgentPermissions().freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return attributes.toBlock().setType(TYPE);
    }
    
    
    /**
     * Returns the attributes of this access request.
     * 
     * @return the attributes of this access request.
     * 
     * @ensure !return.isEmpty() : "The set of attributes is not empty.";
     */
    public @Nonnull ReadonlyContactPermissions getAttributes() {
        return attributes;
    }
    
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    /**
     * Stores the required permissions for this method.
     */
    private final @Nonnull ReadonlyAgentPermissions requiredPermissions;
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return requiredPermissions;
    }
    
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert getSignature() != null : "The signature of this handler is not null.";
        
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (signature instanceof CredentialsSignatureWrapper) {
            ((CredentialsSignatureWrapper) signature).checkCover(getRequiredPermissions());
        } else if (!(signature instanceof HostSignatureWrapper)) {
            throw new PacketException(PacketError.AUTHORIZATION);
        }
        return null;
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        assert isOnClient() : "This method is called on a client.";
        
        // TODO: Add this access request to a list of pending access requests.
    }
    
    
    /**
     * Stores the audit restrictions for this action.
     */
    private static final @Nonnull Restrictions auditRestrictions = new Restrictions(false, false, true);
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return auditRestrictions;
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Method.Factory {
        
        static { Method.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
            return new AccessRequest(entity, signature, recipient, block);
        }
        
    }
    
}
