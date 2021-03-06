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
package net.digitalid.core.signature;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.exceptions.ExpiredSignatureException;
import net.digitalid.core.signature.exceptions.ExpiredSignatureExceptionBuilder;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class signs the wrapped object for encoding.
 * 
 * @see HostSignature
 * @see ClientSignature
 * TODO: CredentialsSignature
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class Signature<@Unspecifiable OBJECT> extends RootClass {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    /**
     * Returns the object converter that is used to calculate the client signature content hash.
     */
    @Pure
    @TODO(task = "Make protected as soon as the subclass generator does not take protected fields into consideration in the equals and hashCode methods.", assignee = Author.STEPHANIE_STROKA, author = Author.STEPHANIE_STROKA, date = "2017-08-25")
    public abstract @Nonnull Converter<OBJECT, Void> getObjectConverter();
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    /**
     * Returns the wrapped object that has been or will be signed.
     */
    @Pure
    public abstract @Nonnull OBJECT getObject();
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * Returns the time at which the object has been or will be signed.
     */
    @Pure
    @Default("net.digitalid.utility.time.TimeBuilder.build()")
    public abstract @Nonnull @Positive Time getTime();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Returns the subject about which a statement is made.
     */
    @Pure
    public abstract @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Returns whether this signature has been verified.
     */
    @Pure
    public boolean isVerified() {
        // TODO.
        return true;
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    /**
     * Creates a content hash with a given time, subject, signer, object converter and object.
     */
    @Pure
    public static <OBJECT> @Nonnull BigInteger getContentHash(@Nonnull Time time, @Nonnull InternalIdentifier subject, @Nonnull Converter<OBJECT, Void> objectConverter, @Nonnull OBJECT object) {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
            final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
                encoder.startHashing(messageDigest);
                encoder.encodeObject(TimeConverter.INSTANCE, time);
                encoder.encodeObject(InternalIdentifierConverter.INSTANCE, subject);
                encoder.encodeObject(objectConverter, object);
                return new BigInteger(1, encoder.stopHashing());
            } catch (@Nonnull MemoryException exception) {
                throw UncheckedExceptionBuilder.withCause(exception).build();
            }
    }
    
    /* -------------------------------------------------- Expiration -------------------------------------------------- */
    
    @Pure
    protected void checkExpiration() throws ExpiredSignatureException {
        if (getTime().isLessThan(Time.TROPICAL_YEAR.ago())) {
            throw ExpiredSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    public void verifySignature() throws InvalidSignatureException, ExpiredSignatureException, RecoveryException {
        checkExpiration();
    }
    
}
