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
package net.digitalid.core.client;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.file.existence.ExistentParent;
import net.digitalid.utility.validation.annotations.file.path.Absolute;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.ExponentConverter;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.parameters.Parameters;

/**
 * The client secret loader loads and stores the secret of a client.
 */
@Mutable
public class ClientSecretLoader {
    
    /* -------------------------------------------------- File -------------------------------------------------- */
    
    /**
     * Returns the file in which the secret of the client with the given identifier is stored.
     */
    @PureWithSideEffects
    public static @Nonnull @Absolute @ExistentParent File getFile(@Nonnull @DomainName @MaxSize(63) String identifier) {
        return Files.relativeToConfigurationDirectory(identifier + ".client.xdf");
    }
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the secret of the client with the given identifier.
     */
    @Pure
    public @Nonnull Exponent getClientSecret(@Nonnull @DomainName @MaxSize(63) String identifier) throws FileException, RecoveryException {
        final @Nonnull File file = getFile(identifier);
        if (file.exists()) {
            // TODO: Check the type of the loaded pack?
            return Pack.loadFrom(file).unpack(ExponentConverter.INSTANCE, null);
        } else {
            final @Nonnull Exponent secret = ExponentBuilder.withValue(new BigInteger(Parameters.EXPONENT.get(), new SecureRandom())).build();
            setClientSecret(identifier, secret);
            return secret;
        }
    }
    
    /**
     * Sets the secret of the client with the given identifier.
     */
    @Impure
    public void setClientSecret(@Nonnull @DomainName @MaxSize(63) String identifier, @Nonnull Exponent secret) throws FileException {
        final @Nonnull File file = getFile(identifier);
        Pack.pack(ExponentConverter.INSTANCE, secret).storeTo(file);
    }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configured client secret loader.
     */
    public static final @Nonnull Configuration<ClientSecretLoader> configuration = Configuration.with(new ClientSecretLoader()).addDependency(Files.directory);
    
    /* -------------------------------------------------- Type Mapping -------------------------------------------------- */
    
    /**
     * Maps the converter with which a client secret is unpacked.
     */
    @PureWithSideEffects
    @Initialize(target = ClientSecretLoader.class, dependencies = IdentifierResolver.class)
    @TODO(task = "Provide the correct parameters for the loading of the type.", date = "2017-08-30", author = Author.KASPAR_ETTER)
    public static void mapConverter() {
        SemanticType.map(ExponentConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build());
    }
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the secret of the client with the given identifier.
     */
    @Pure
    public static @Nonnull Exponent load(@Nonnull @DomainName @MaxSize(63) String identifier) throws FileException, RecoveryException {
        return configuration.get().getClientSecret(identifier);
    }
    
    /**
     * Stores the secret of the client with the given identifier.
     */
    @Impure
    public static void store(@Nonnull @DomainName @MaxSize(63) String identifier, @Nonnull Exponent secret) throws FileException {
        configuration.get().setClientSecret(identifier, secret);
    }
    
}
