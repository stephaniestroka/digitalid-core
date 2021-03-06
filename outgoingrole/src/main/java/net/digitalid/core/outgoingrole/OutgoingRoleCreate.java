/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// TODO

//package net.digitalid.core.agent;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.collections.readonly.ReadOnlyArray;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.validation.annotations.type.Immutable;
//
//import net.digitalid.database.annotations.transaction.NonCommitting;
//
//import net.digitalid.core.context.Context;
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
//import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
//import net.digitalid.core.entity.Entity;
//import net.digitalid.core.entity.NonHostEntity;
//import net.digitalid.core.handler.Action;
//import net.digitalid.core.handler.Method;
//import net.digitalid.core.identification.identifier.HostIdentifier;
//import net.digitalid.core.identification.identity.Identity;
//import net.digitalid.core.identification.identity.IdentityImplementation;
//import net.digitalid.core.identification.identity.SemanticType;
//import net.digitalid.core.service.handler.CoreServiceInternalAction;
//
//import net.digitalid.service.core.dataservice.StateModule;
//
///**
// * Creates an {@link OutgoingRole outgoing role}.
// */
//@Immutable
//final class OutgoingRoleCreate extends CoreServiceInternalAction {
//    
//    /**
//     * Stores the semantic type {@code create.outgoing.role@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType TYPE = SemanticType.map("create.outgoing.role@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.TYPE, Identity.IDENTIFIER, Context.TYPE);
//    
//    
//    /**
//     * Stores the outgoing role of this action.
//     */
//    private final @Nonnull OutgoingRole outgoingRole;
//    
//    /**
//     * Stores the relation of the outgoing role.
//     * 
//     * @invariant relation.isRoleType() : "The relation is a role type.";
//     */
//    private final @Nonnull SemanticType relation;
//    
//    /**
//     * Stores the context of the outgoing role.
//     * 
//     * @invariant context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
//     */
//    private final @Nonnull Context context;
//    
//    /**
//     * Creates an internal action to create the given outgoing role.
//     * 
//     * @param outgoingRole the outgoing role which is to be created.
//     * @param relation the relation of the given outgoing role.
//     * @param context the context of the given outgoing role.
//     * 
//     * @require outgoingRole.isOnClient() : "The outgoing role is on a client.";
//     * @require relation.isRoleType() : "The relation is a role type.";
//     * @require context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
//     */
//    OutgoingRoleCreate(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType relation, @Nonnull Context context) {
//        super(outgoingRole.getRole());
//        
//        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
//        Require.that(context.getEntity().equals(outgoingRole.getEntity())).orThrow("The context belongs to the entity of the outgoing role.");
//        
//        this.outgoingRole = outgoingRole;
//        this.relation = relation;
//        this.context = context;
//    }
//    
//    /**
//     * Creates an internal action that decodes the given block.
//     * 
//     * @param entity the entity to which this handler belongs.
//     * @param signature the signature of this handler (or a dummy that just contains a subject).
//     * @param recipient the recipient of this method.
//     * @param block the content which is to be decoded.
//     * 
//     * @require signature.hasSubject() : "The signature has a subject.";
//     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
//     * 
//     * @ensure hasSignature() : "This handler has a signature.";
//     */
//    @NonCommitting
//    private OutgoingRoleCreate(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
//        super(entity, signature, recipient);
//        
//        final @Nonnull NonHostEntity nonHostEntity = entity.castTo(NonHostEntity.class);
//        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
//        this.outgoingRole = Agent.get(nonHostEntity, elements.getNonNullable(0)).castTo(OutgoingRole.class);
//        this.relation = IdentityImplementation.create(elements.getNonNullable(1)).castTo(SemanticType.class).checkIsRoleType();
//        this.context = Context.get(nonHostEntity, elements.getNonNullable(2));
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Block toBlock() {
//        return TupleWrapper.encode(TYPE, outgoingRole, relation, context);
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String getDescription() {
//        return "Creates the outgoing role with the number " + outgoingRole + ".";
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
//        return new FreezableAgentPermissions(relation, true).freeze();
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
//        return new Restrictions(false, false, true, context);
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Agent getRequiredAgentToSeeAudit() {
//        return outgoingRole;
//    }
//    
//    
//    @Override
//    @NonCommitting
//    protected void executeOnBoth() throws DatabaseException {
//        outgoingRole.createForActions(relation, context);
//    }
//    
//    @Pure
//    @Override
//    public boolean interferesWith(@Nonnull Action action) {
//        return false;
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull AgentRemove getReverse() {
//        return new AgentRemove(outgoingRole);
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean equals(@Nullable Object object) {
//        if (protectedEquals(object) && object instanceof OutgoingRoleCreate) {
//            final @Nonnull OutgoingRoleCreate other = (OutgoingRoleCreate) object;
//            return this.outgoingRole.equals(other.outgoingRole) && this.relation.equals(other.relation) && this.context.equals(other.context);
//        }
//        return false;
//    }
//    
//    @Pure
//    @Override
//    public int hashCode() {
//        int hash = protectedHashCode();
//        hash = 89 * hash + outgoingRole.hashCode();
//        hash = 89 * hash + relation.hashCode();
//        hash = 89 * hash + context.hashCode();
//        return hash;
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull SemanticType getType() {
//        return TYPE;
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull StateModule getModule() {
//        return AgentModule.MODULE;
//    }
//    
//    /**
//     * The factory class for the surrounding method.
//     */
//    private static final class Factory extends Method.Factory {
//        
//        static { Method.add(TYPE, new Factory()); }
//        
//        @Pure
//        @Override
//        @NonCommitting
//        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
//            return new OutgoingRoleCreate(entity, signature, recipient, block);
//        }
//        
//    }
//    
//}
