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
package net.digitalid.core.client.role;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.Client;
import net.digitalid.core.identification.annotations.RoleType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.outgoingrole.OutgoingRole;

/**
 * This class models a non-native role on the client.
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter
public abstract class NonNativeRole extends Role {
    
    /* -------------------------------------------------- Relation -------------------------------------------------- */
    
    /**
     * Returns the relation of this role.
     */
    @Pure
    public abstract @Nonnull @RoleType SemanticType getRelation();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient of this role.
     */
    @Pure
    public abstract @Nonnull Role getRecipient();
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    @Pure
    @Override
    @Derive("OutgoingRole.of(this, agentKey)")
    public abstract @Nonnull OutgoingRole getAgent();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * This constructor triggers that the {@link DatabaseException} is thrown in the constructor of the generated subclass.
     */
    @TODO(task = "Remove once you can declare exceptions in @Derive.", date = "2017-08-19", author = Author.KASPAR_ETTER)
    protected NonNativeRole() throws DatabaseException {}
    
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull NonNativeRole with(@Nonnull Client unit, long key) throws DatabaseException, RecoveryException {
        final @Nonnull Role role = Role.with(unit, key);
        if (role instanceof NonNativeRole) { return (NonNativeRole) role; }
        else { throw RecoveryExceptionBuilder.withMessage("The key " + key + " does not denote a non-native role.").build(); }
    }
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    // TODO: Figure out whether/how we can use the ConceptIndex for this.
    
//    /**
//     * Caches the non-native roles given their client and number.
//     */
//    private static final @Nonnull ConcurrentMap<Client, ConcurrentMap<Long, NonNativeRole>> index = new ConcurrentHashMap<>();
//    
//    static {
//        if (Database.isSingleAccess()) {
//            Instance.observeAspects(new Observer() {
//                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
//            }, Client.DELETED);
//        }
//    }
//    
//    /**
//     * Returns the potentially locally cached non-native role with the given arguments.
//     * 
//     * @param client the client that can assume the returned role.
//     * @param number the number that references the returned role.
//     * @param issuer the issuer of the returned role.
//     * @param relation the relation of the returned role.
//     * @param recipient the recipient of the returned role.
//     * @param agentNumber the agent number of the returned role.
//     * 
//     * @return a new or existing non-native role with the given arguments.
//     * 
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
//    public static @Nonnull NonNativeRole get(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, @Nonnull Role recipient, long agentNumber) {
//        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
//        
//        if (Database.isSingleAccess()) {
//            @Nullable ConcurrentMap<Long, NonNativeRole> map = index.get(client);
//            if (map == null) { map = index.putIfAbsentElseReturnPresent(client, new ConcurrentHashMap<Long, NonNativeRole>()); }
//            @Nullable NonNativeRole role = map.get(number);
//            if (role == null) { role = map.putIfAbsentElseReturnPresent(number, new NonNativeRole(client, number, issuer, relation, recipient, agentNumber)); }
//            return role;
//        } else {
//            return new NonNativeRole(client, number, issuer, relation, recipient, agentNumber);
//        }
//    }
//    
//    /**
//     * Returns a new or existing non-native role with the given arguments.
//     * 
//     * @param client the client that can assume the returned role.
//     * @param issuer the issuer of the returned role.
//     * @param relation the relation of the returned role.
//     * @param recipient the recipient of the returned role.
//     * @param agentNumber the agent number of the returned role.
//     * 
//     * @return a new or existing non-native role with the given arguments.
//     * 
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
//    @NonCommitting
//    static @Nonnull NonNativeRole add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, @Nonnull Role recipient, long agentNumber) throws DatabaseException {
//        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
//        
//        final @Nonnull NonNativeRole role = get(client, RoleModule.map(client, issuer, relation, recipient, agentNumber), issuer, relation, recipient, agentNumber);
//        role.notify(CREATED);
//        return role;
//    }
//    
//    @Override
//    @NonCommitting
//    public void remove() throws DatabaseException {
//        if (Database.isSingleAccess()) {
//            final @Nullable ConcurrentMap<Long, NonNativeRole> map = index.get(getClient());
//            if (map != null) { map.remove(getKey()); }
//        }
//        super.remove();
//    }
    
}
