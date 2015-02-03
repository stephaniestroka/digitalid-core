package ch.virtualid.packet;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.annotations.RawRecipient;
import ch.virtualid.attribute.CertifiedAttributeValue;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.synchronizer.Audit;
import ch.virtualid.synchronizer.RequestAudit;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.CompressionWrapper;
import ch.xdf.CredentialsSignatureWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Quartet;

/**
 * This class compresses, signs and encrypts requests with credentials.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class CredentialsRequest extends Request {
    
    /**
     * Stores the credentials with which the content is signed.
     * 
     * @invariant credentials.isFrozen() : "The credentials are frozen.";
     * @invariant CredentialsSignatureWrapper.credentialsAreValid(credentials) : "The credentials are valid.";
     */
    private @Nonnull ReadonlyList<Credential> credentials;
    
    /**
     * Stores the certificates that are appended to an identity-based authentication or null.
     * 
     * @invariant certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
     * @invariant CredentialsSignatureWrapper.certificatesAreValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
     */
    private @Nullable ReadonlyList<CertifiedAttributeValue> certificates;
    
    /**
     * Stores whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     */
    private boolean lodged;
    
    /**
     * Stores the value b' or null if the credentials are not to be shortened.
     */
    private @Nullable BigInteger value;
    
    /**
     * Packs the given methods with the given arguments signed with the given credentials.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param credentials the credentials with which the content is signed.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * 
     * @require credentials.isFrozen() : "The credentials are frozen.";
     * @require CredentialsSignatureWrapper.credentialsAreValid(credentials) : "The credentials are valid.";
     * @require certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
     * @require CredentialsSignatureWrapper.certificatesAreValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
     */
    @NonCommitting
    public CredentialsRequest(@Nonnull ReadonlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<CertifiedAttributeValue> certificates, boolean lodged, @Nullable BigInteger value) throws SQLException, IOException, PacketException, ExternalException {
        this(methods, recipient, subject, audit, credentials, certificates, lodged, value, 0);
    }
    
    /**
     * Packs the given methods with the given arguments signed with the given credentials.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param credentials the credentials with which the content is signed.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * @param iteration how many times this request was resent.
     */
    @NonCommitting
    private CredentialsRequest(@Nonnull ReadonlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<CertifiedAttributeValue> certificates, boolean lodged, @Nullable BigInteger value, int iteration) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, recipient, new SymmetricKey(), subject, audit, new Quartet<ReadonlyList<Credential>, ReadonlyList<CertifiedAttributeValue>, Boolean, BigInteger>(credentials, certificates, lodged, value), iteration);
    }
    
    
    @Override
    @RawRecipient
    @SuppressWarnings("unchecked")
    void setField(@Nullable Object field) {
        assert field != null : "See the constructor above.";
        final @Nonnull Quartet<ReadonlyList<Credential>, ReadonlyList<CertifiedAttributeValue>, Boolean, BigInteger> quartet = (Quartet<ReadonlyList<Credential>, ReadonlyList<CertifiedAttributeValue>, Boolean, BigInteger>) field;
        this.credentials = quartet.getValue0();
        this.certificates = quartet.getValue1();
        this.lodged = quartet.getValue2();
        this.value = quartet.getValue3();
    }
    
    @Pure
    @Override
    @RawRecipient
    @NonCommitting
    @Nonnull CredentialsSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) throws SQLException, IOException, PacketException, ExternalException {
        return new CredentialsSignatureWrapper(Packet.SIGNATURE, compression, subject, audit, credentials, certificates, lodged, value);
    }
    
    
    @Pure
    @Override
    public boolean isSigned() {
        return true;
    }
    
    @Override
    @NonCommitting
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        return new CredentialsRequest(methods, recipient, subject, getAudit(), credentials, certificates, lodged, value, iteration).send(verified);
    }
    
}
