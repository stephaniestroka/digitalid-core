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
package net.digitalid.core.packet;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class decrypts, verifies and decompresses responses on the client-side.
 * 
 * @invariant getSize() == replies.size() && getSize() == exceptions.size() : "The number of elements equals the number of replies and the number of exceptions.";
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
// @GenerateConverter // TODO: Support generics.
public abstract class Response extends Packet {
    
//    /**
//     * Stores the replies of this response.
//     */
//    private @Nonnull @Frozen @NonEmpty FreezableList<Reply> replies;
//    
//    /**
//     * Stores the exceptions of this response.
//     */
//    private @Nonnull @Frozen @NonEmpty FreezableList<RequestException> exceptions;
//    
//    /**
//     * Stores the signer of this response.
//     */
//    private @Nullable HostIdentifier signer;
//    
//    /**
//     * Stores the request that caused this response.
//     */
//    private final @Nullable Request request;
//    
//    /**
//     * Packs the given packet exception as a response without signing.
//     * 
//     * @param request the corresponding request or null if not yet decoded.
//     * @param exception the packet exception that is to be packed as an unsigned response.
//     * 
//     * @ensure getSize() == 1 : "The size of this response is one.";
//     */
//    @NonCommitting
//    public Response(@Nullable Request request, @Nonnull RequestException exception) throws ExternalException {
//        super(new FreezablePair<>(FreezableArrayList.<Reply>getWithCapacity(1).freeze(), FreezableArrayList.get(exception).freeze()).freeze(), 1, null, null, request == null ? null : request.getEncryption().getSymmetricKey(), null, null);
//        
//        this.request = request;
//    }
//    
//    /**
//     * Packs the given replies and exceptions with the given arguments as a response to the given request.
//     * 
//     * @param request the corresponding request.
//     * @param replies the replies to the methods of the corresponding request.
//     * @param exceptions the exceptions to the methods of the corresponding request.
//     * @param audit the audit since the last audit or null if no audit is appended.
//     * 
//     * @require replies.size() == exceptions.size() : "The number of replies and exceptions are the same.";
//     * 
//     * @ensure hasRequest() : "This response has a request.";
//     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the request.";
//     */
//    @NonCommitting
//    public Response(@Nonnull Request request, @Nonnull @Frozen @NonEmpty ReadOnlyList<Reply> replies, @Nonnull @Frozen @NonEmpty ReadOnlyList<RequestException> exceptions, @Nullable ResponseAudit audit) throws ExternalException {
//        super(new FreezablePair<>(replies, exceptions).freeze(), replies.size(), request.getRecipient(), null, request.getEncryption().getSymmetricKey(), request.getSubject(), audit);
//        
//        Require.that(replies.isFrozen()).orThrow("The list of replies is frozen.");
//        Require.that(!replies.isEmpty()).orThrow("The list of replies is not empty.");
//        Require.that(exceptions.isFrozen()).orThrow("The list of exceptions is frozen.");
//        Require.that(replies.size() == exceptions.size()).orThrow("The number of replies and exceptions are the same.");
//        
//        this.request = request;
//    }
//    
//    
//    /**
//     * Reads and unpacks the response from the given input stream.
//     * 
//     * @param request the corresponding request.
//     * @param inputStream the input stream to read the response from.
//     * @param verified determines whether the signature is verified (if not, it needs to be checked by the caller).
//     * 
//     * @ensure hasRequest() : "This response has a request.";
//     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the given request.";
//     */
//    @NonCommitting
//    public Response(@Nonnull Request request, @Nonnull InputStream inputStream, boolean verified) throws ExternalException {
//        super(inputStream, request, verified);
//        
//        this.request = request;
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nullable ResponseAudit getAudit() {
//        final @Nullable Audit audit = super.getAudit();
//        return audit != null ? (ResponseAudit) audit : null;
//    }
//    
//    /**
//     * Returns the audit of this packet.
//     * 
//     * @return the audit of this packet.
//     * 
//     * @require getAudit() != null : "The audit is not null.";
//     */
//    @Pure
//    public @Nonnull ResponseAudit getAuditNotNull() {
//        final @Nullable ResponseAudit audit = getAudit();
//        Require.that(audit != null).orThrow("The audit is not null.");
//        return audit;
//    }
//    
//    /**
//     * Returns whether this response has a request.
//     * 
//     * @return whether this response has a request.
//     */
//    public boolean hasRequest() {
//        return request != null;
//    }
//    
//    /**
//     * Returns the request that caused this response.
//     * 
//     * @return the request that caused this response.
//     * 
//     * @require hasRequest() : "This response has a request.";
//     */
//    @Pure
//    public @Nonnull Request getRequest() {
//        Require.that(request != null).orThrow("This response has a request.");
//        
//        return request;
//    }
//    
//    
//    @Override
//    @RawRecipient
//    @SuppressWarnings("unchecked")
//    void setList(@Nonnull Object object) {
//        final @Nonnull ReadOnlyPair<ReadOnlyList<Reply>, ReadOnlyList<RequestException>> pair = (ReadOnlyPair<ReadOnlyList<Reply>, ReadOnlyList<RequestException>>) object;
//        this.replies = (FreezableList<Reply>) pair.getElement0();
//        this.exceptions = (FreezableList<RequestException>) pair.getElement1();
//    }
//    
//    @Override
//    @RawRecipient
//    void setField(@Nullable Object field) {
//        this.signer = (HostIdentifier) field;
//    }
//    
//    @Pure
//    @Override
//    @RawRecipient
//    @Nullable Block getBlock(int index) {
//        if (!exceptions.isNull(index)) { return exceptions.getNonNullable(index).toBlock(); }
//        if (!replies.isNull(index)) { return replies.getNonNullable(index).toBlock(); }
//        return null;
//    }
//    
//    @Pure
//    @Override
//    @RawRecipient
//    @Nonnull HostSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
//        Require.that(signer != null).orThrow("This method is only called if the element is not an exception.");
//        return HostSignatureWrapper.sign(Packet.SIGNATURE, compression, subject, audit, signer);
//    }
//    
//    
//    @Override
//    @RawRecipient
//    void initialize(int size) {
//        this.replies = FreezableArrayList.getWithCapacity(size);
//        this.exceptions = FreezableArrayList.getWithCapacity(size);
//        for (int i = 0; i < size; i++) {
//            replies.add(null);
//            exceptions.add(null);
//        }
//    }
//    
//    @Override
//    @RawRecipient
//    void freeze() {
//        replies.freeze();
//        exceptions.freeze();
//    }
//    
//    
//    /**
//     * Checks whether the reply at the given position in this response is a packet exception.
//     * 
//     * @param index the index of the reply which is to be checked.
//     * 
//     * @throws RequestException if the responding host encountered a packet error.
//     */
//    @Pure
//    public void checkReply(@Index int index) throws RequestException {
//        if (!exceptions.isNull(index)) { throw exceptions.getNonNullable(index); }
//    }
//    
//    /**
//     * Returns the reply at the given position in this response or null if there was none.
//     * 
//     * @param index the index of the reply which is to be returned.
//     * 
//     * @return the reply at the given position in this response or null if there was none.
//     * 
//     * @throws RequestException if the responding host encountered a packet error.
//     */
//    @Pure
//    public @Nullable Reply getReply(@Index int index) throws RequestException {
//        checkReply(index);
//        return replies.get(index);
//    }
//    
//    /**
//     * Returns the reply at the given position in this response.
//     * 
//     * @param index the index of the reply which is to be returned.
//     * 
//     * @return the reply at the given position in this response.
//     * 
//     * @throws RequestException if the responding host encountered a packet error.
//     * 
//     * @require getReply(index) != null : "The reply at the given position is not null.";
//     */
//    @Pure
//    @SuppressWarnings("unchecked")
//    public @Nonnull <T extends Reply> T getReplyNotNull(@Index int index) throws RequestException {
//        final @Nullable Reply reply = getReply(index);
//        Require.that(reply != null).orThrow("The reply is not null.");
//        return (T) reply;
//    }
//    
//    /**
//     * Sets the reply at the given position during the packet constructor.
//     * 
//     * @param index the index of the reply which is to be set.
//     * @param reply the reply which is to set at the index.
//     */
//    @RawRecipient
//    void setReply(@Index int index, @Nonnull Reply reply) {
//        replies.set(index, reply);
//    }
//    
//    /**
//     * Sets the exception at the given position during the packet constructor.
//     * 
//     * @param index the index of the exception which is to be set.
//     * @param exception the exception which is to set at the index.
//     */
//    @RawRecipient
//    void setException(@Index int index, @Nonnull RequestException exception) {
//        exceptions.set(index, exception);
//    }
    
}
