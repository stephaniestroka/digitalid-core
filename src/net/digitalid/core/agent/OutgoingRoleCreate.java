package net.digitalid.core.agent;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.contact.Context;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreServiceInternalAction;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Creates an {@link OutgoingRole outgoing role}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
final class OutgoingRoleCreate extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code create.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("create.outgoing.role@core.digitalid.net").load(TupleWrapper.TYPE, Agent.TYPE, Identity.IDENTIFIER, Context.TYPE);
    
    
    /**
     * Stores the outgoing role of this action.
     */
    private final @Nonnull OutgoingRole outgoingRole;
    
    /**
     * Stores the relation of the outgoing role.
     * 
     * @invariant relation.isRoleType() : "The relation is a role type.";
     */
    private final @Nonnull SemanticType relation;
    
    /**
     * Stores the context of the outgoing role.
     * 
     * @invariant context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
     */
    private final @Nonnull Context context;
    
    /**
     * Creates an internal action to create the given outgoing role.
     * 
     * @param outgoingRole the outgoing role which is to be created.
     * @param relation the relation of the given outgoing role.
     * @param context the context of the given outgoing role.
     * 
     * @require outgoingRole.isOnClient() : "The outgoing role is on a client.";
     * @require relation.isRoleType() : "The relation is a role type.";
     * @require context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
     */
    OutgoingRoleCreate(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType relation, @Nonnull Context context) {
        super(outgoingRole.getRole());
        
        assert relation.isRoleType() : "The relation is a role type.";
        assert context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
        
        this.outgoingRole = outgoingRole;
        this.relation = relation;
        this.context = context;
    }
    
    /**
     * Creates an internal action that decodes the given block.
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
    private OutgoingRoleCreate(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull NonHostEntity nonHostEntity = entity.toNonHostEntity();
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.outgoingRole = Agent.get(nonHostEntity, elements.getNonNullable(0)).toOutgoingRole();
        this.relation = IdentityClass.create(elements.getNonNullable(1)).toSemanticType().checkIsRoleType();
        this.context = Context.get(nonHostEntity, elements.getNonNullable(2));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, outgoingRole, relation, context).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Creates the outgoing role with the number " + outgoingRole + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissions() {
        return new FreezableAgentPermissions(relation, true).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return new Restrictions(false, false, true, context);
    }
    
    @Pure
    @Override
    public @Nonnull Agent getAuditAgent() {
        return outgoingRole;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws SQLException {
        outgoingRole.createForActions(relation, context);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return false;
    }
    
    @Pure
    @Override
    public @Nonnull AgentRemove getReverse() {
        return new AgentRemove(outgoingRole);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof OutgoingRoleCreate) {
            final @Nonnull OutgoingRoleCreate other = (OutgoingRoleCreate) object;
            return this.outgoingRole.equals(other.outgoingRole) && this.relation.equals(other.relation) && this.context.equals(other.context);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + outgoingRole.hashCode();
        hash = 89 * hash + relation.hashCode();
        hash = 89 * hash + context.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return AgentModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new OutgoingRoleCreate(entity, signature, recipient, block);
        }
        
    }
    
}
