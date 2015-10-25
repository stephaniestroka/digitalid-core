package net.digitalid.service.core.client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.FreezableAgentPermissions;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.contact.Context;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NativeRole;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.IdentifierClass;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identifier.NonHostIdentifier;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.Successor;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.service.CoreServiceInternalAction;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Closes the account and sets the given successor.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class AccountClose extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code close.account@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("close.account@core.digitalid.net").load(InternalNonHostIdentity.IDENTIFIER);
    
    
    /**
     * Stores the successor of the given account.
     */
    private final @Nullable InternalNonHostIdentifier successor;
    
    /**
     * Creates an action to close a given account.
     * <p>
     * <em>Important:</em> The successor may only be null
     * in case {@link AccountInitialize} is to be reversed.
     * 
     * @param role the role to which this handler belongs.
     * @param successor the successor of the given account.
     */
    AccountClose(@Nonnull NativeRole role, @Nullable InternalNonHostIdentifier successor) {
        super(role);
        
        this.successor = successor;
        this.restrictions = new Restrictions(true, true, true, Context.getRoot(role));
    }
    
    /**
     * Creates an action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private AccountClose(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        this.successor = IdentifierClass.create(block).toInternalNonHostIdentifier();
        this.restrictions = new Restrictions(true, true, true, Context.getRoot(entity.toNonHostEntity()));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        assert successor != null : "The successor may only be null to reverse account initialization.";
        return successor.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Closes the given account with the successor " + successor + ".";
    }
    
    
    /**
     * Stores the required restrictions.
     */
    private final @Nonnull Restrictions restrictions;
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return restrictions;
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return restrictions;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws SQLException {
        CoreService.SERVICE.removeState(getNonHostEntity());
        if (successor != null) Successor.set((NonHostIdentifier) getSubject(), successor, null);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return false;
    }
    
    @Pure
    @Override
    public @Nullable InternalAction getReverse() {
        return null;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AccountClose && Objects.equals(successor, ((AccountClose) object).successor);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + Objects.hashCode(successor);
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return CoreService.SERVICE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
            return new AccountClose(entity, signature, recipient, block);
        }
        
    }
    
}
