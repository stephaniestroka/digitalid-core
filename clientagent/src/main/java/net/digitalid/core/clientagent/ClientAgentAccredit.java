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
package net.digitalid.core.clientagent;

// TODO: Do we still have to write an action like this manually?

//import java.sql.SQLException;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.collections.list.FreezableArrayList;
//import net.digitalid.utility.collections.readonly.ReadOnlyArray;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.validation.annotations.type.Immutable;
//
//import net.digitalid.database.annotations.transaction.NonCommitting;
//import net.digitalid.database.core.exceptions.DatabaseException;
//
//import net.digitalid.core.client.Client;
//import net.digitalid.core.client.Commitment;
//import net.digitalid.core.context.Context;
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.conversion.wrappers.signature.ClientSignatureWrapper;
//import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
//import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
//import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
//import net.digitalid.core.entity.Entity;
//import net.digitalid.core.entity.NativeRole;
//import net.digitalid.core.entity.NonHostEntity;
//import net.digitalid.core.handler.Action;
//import net.digitalid.core.handler.InternalAction;
//import net.digitalid.core.handler.Method;
//import net.digitalid.core.identification.identifier.HostIdentifier;
//import net.digitalid.core.identification.identity.SemanticType;
//import net.digitalid.core.packet.Response;
//import net.digitalid.core.packet.exceptions.RequestErrorCode;
//import net.digitalid.core.packet.exceptions.RequestException;
//import net.digitalid.core.service.handler.CoreServiceInternalAction;
//import net.digitalid.core.settings.Settings;
//
//import net.digitalid.service.core.dataservice.StateModule;
//import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;
//import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
//
///**
// * Accredits a {@link ClientAgent client agent}.
// */
//@Immutable
public final class ClientAgentAccredit {// extends CoreServiceInternalAction {
    
//    /**
//     * Stores the semantic type {@code accredit.client.agent@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType TYPE = SemanticType.map("accredit.client.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.TYPE, FreezableAgentPermissions.TYPE, Client.NAME, Settings.TYPE);
//    
//    
//    /**
//     * Stores the client agent of this action.
//     * 
//     * @invariant clientAgent.isRemoved() : "The client agent is removed.";
//     */
//    private final @Nonnull ClientAgent clientAgent;
//    
//    /**
//     * Stores the permissions of the client agent.
//     * 
//     * @invariant permissions.isFrozen() : "The permissions are frozen.";
//     */
//    private final @Nonnull ReadOnlyAgentPermissions permissions;
//    
//    /**
//     * Stores the commitment of the client agent.
//     */
//    private final @Nonnull Commitment commitment;
//    
//    /**
//     * Stores the name of the client agent.
//     * 
//     * @invariant Client.isValid(name) : "The name is valid.";
//     */
//    private final @Nonnull String name;
//    
//    /**
//     * Stores the password of the subject.
//     * 
//     * @invariant Password.isValid(password) : "The password is valid.";
//     */
//    private final @Nonnull String password;
//    
//    /**
//     * Creates an internal action to accredit the client agent given by the role.
//     * 
//     * @param role the role whose client agent is to be accredited.
//     * @param password the password needed to accredit the client agent.
//     * 
//     * @require Password.isValid(password) : "The password is valid.";
//     */
//    @NonCommitting
//    public ClientAgentAccredit(@Nonnull NativeRole role, @Nonnull String password) throws ExternalException {
//        super(role);
//        
//        Require.that(Settings.isValid(password)).orThrow("The password is valid.");
//        
//        final @Nonnull Client client = role.getClient();
//        this.clientAgent = new ClientAgent(role, role.getAgent().getNumber(), true);
//        this.permissions = client.getPreferredPermissions();
//        this.commitment = client.getCommitment(role.getIdentity().getAddress());
//        this.name = client.getName();
//        this.password = password;
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
//    private ClientAgentAccredit(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
//        super(entity, signature, recipient);
//        
//        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(4);
//        
//        this.clientAgent = Agent.get(entity.castTo(NonHostEntity.class), elements.getNonNullable(0)).castTo(ClientAgent.class);
//        if (!clientAgent.isRemoved()) { throw InvalidParameterValueCombinationException.get("The client agent has to be removed."); }
//        
//        this.permissions = new FreezableAgentPermissions(elements.getNonNullable(1)).freeze();
//        
//        if (!(signature instanceof ClientSignatureWrapper)) { throw InvalidParameterValueCombinationException.get("The action to accredit a client agent has to be signed by a client."); }
//        this.commitment = ((ClientSignatureWrapper) signature).getCommitment();
//        
//        this.name = StringWrapper.decodeNonNullable(elements.getNonNullable(2));
//        if (!Client.isValidName(name)) { throw InvalidParameterValueException.get("name", name); }
//        
//        this.password = StringWrapper.decodeNonNullable(elements.getNonNullable(3));
//        if (!Settings.isValid(password)) { throw InvalidParameterValueException.get("password", password); }
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Block toBlock() {
//        return TupleWrapper.encode(TYPE, clientAgent.toBlock(), permissions.toBlock(), StringWrapper.encodeNonNullable(Client.NAME, name), StringWrapper.encodeNonNullable(Settings.TYPE, password));
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String getDescription() {
//        return "Accredits the client agent with the number " + clientAgent + ".";
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean isSimilarTo(@Nonnull Method other) {
//        return false;
//    }
//    
//    @Override
//    @NonCommitting
//    public @Nonnull Response send() throws ExternalException {
//        final @Nonnull Response response = Method.send(new FreezableArrayList<Method>(this).freeze(), null);
//        response.checkReply(0);
//        return response;
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull Agent getRequiredAgentToSeeAudit() {
//        return clientAgent;
//    }
//    
//    
//    @Override
//    @NonCommitting
//    protected void executeOnBoth() throws DatabaseException {
//        clientAgent.createForActions(permissions, new Restrictions(true, true, true, Context.getRoot(getNonHostEntity())), commitment, name);
//    }
//    
//    @Override
//    @NonCommitting
//    public void executeOnHostInternalAction() throws RequestException, SQLException {
//        if (!Settings.get(getNonHostAccount()).getValue().equals(password)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The password is not correct."); }
//        executeOnBoth();
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
//    public @Nullable InternalAction getReverse() {
//        return null;
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean equals(@Nullable Object object) {
//        if (protectedEquals(object) && object instanceof ClientAgentAccredit) {
//            final @Nonnull ClientAgentAccredit other = (ClientAgentAccredit) object;
//            return this.clientAgent.equals(other.clientAgent) && this.permissions.equals(other.permissions) && this.commitment.equals(other.commitment) && this.name.equals(other.name) && this.password.equals(other.password);
//        }
//        return false;
//    }
//    
//    @Pure
//    @Override
//    public int hashCode() {
//        int hash = protectedHashCode();
//        hash = 89 * hash + clientAgent.hashCode();
//        hash = 89 * hash + permissions.hashCode();
//        hash = 89 * hash + commitment.hashCode();
//        hash = 89 * hash + name.hashCode();
//        hash = 89 * hash + password.hashCode();
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
//            return new ClientAgentAccredit(entity, signature, recipient, block);
//        }
//        
//    }
    
}
