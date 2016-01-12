package net.digitalid.service.core.site.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;
import net.digitalid.service.core.CoreService;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.attribute.Attribute;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.concepts.attribute.CertifiedAttributeValue;
import net.digitalid.service.core.concepts.attribute.UncertifiedAttributeValue;
import net.digitalid.service.core.cryptography.KeyPair;
import net.digitalid.service.core.cryptography.PrivateKeyChain;
import net.digitalid.service.core.cryptography.PublicKeyChain;
import net.digitalid.service.core.entity.HostAccount;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.server.Server;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.directory.Directory;
import net.digitalid.utility.exceptions.external.ExternalException;

/**
 * A host stores a {@link KeyPair} and is run by a {@link Server}.
 * 
 * TODO: Make sure that the host keys get rotated!
 */
public final class Host extends Site {
    
    @Pure
    @Override
    public @Nonnull String getEntityReference() {
        return Mapper.REFERENCE;
    }
    
    
    /*
    TODO: Support:
    services
    provider
    tokens
    members
    open/close
    */
    
    /**
     * Stores the identifier of this host.
     */
    private final @Nonnull HostIdentifier identifier;
    
    /**
     * Stores the identity of this host.
     */
    private final @Nonnull HostIdentity identity;
    
    /**
     * Stores the private key chain of this host.
     */
    private @Nonnull PrivateKeyChain privateKeyChain;
    
    /**
     * Stores the public key chain of this host.
     */
    private @Nonnull PublicKeyChain publicKeyChain;
    
    /**
     * Stores the client associated with this host.
     */
    private final @Nonnull Client client;
    
    /**
     * Creates a new host with the given identifier by either reading the cryptographic keys from the file system or creating them.
     * 
     * @param identifier the identifier of the new host.
     */
    @Locked
    @Committing
    public Host(@Nonnull HostIdentifier identifier) throws IOException, DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(identifier.asHostName());
        
        this.identifier = identifier;
        this.identity = Mapper.mapHostIdentity(identifier);
        
        final @Nonnull String path = Directory.getHostsDirectory().getPath() + File.separator + identifier.getString();
        final @Nonnull File privateKeyFile = new File(path + ".private.xdf");
        final @Nonnull File publicKeyFile = new File(path + ".public.xdf");
        
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            this.privateKeyChain = PrivateKeyChain.XDF_CONVERTER.decodeNonNullable(None.OBJECT, SelfcontainedWrapper.decodeBlockFrom(new FileInputStream(privateKeyFile), true).checkType(PrivateKeyChain.TYPE));
            this.publicKeyChain = PublicKeyChain.XDF_CONVERTER.decodeNonNullable(None.OBJECT, SelfcontainedWrapper.decodeBlockFrom(new FileInputStream(publicKeyFile), true).checkType(PublicKeyChain.TYPE));
        } else {
            final @Nonnull KeyPair keyPair = KeyPair.getRandom();
            final @Nonnull Time time = Time.getCurrent();
            this.privateKeyChain = PrivateKeyChain.get(time, keyPair.getPrivateKey());
            this.publicKeyChain = PublicKeyChain.get(time, keyPair.getPublicKey());
        }
        
        final @Nonnull Block privateKeyBlock = SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, privateKeyChain);
        final @Nonnull Block publicKeyBlock = SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, publicKeyChain);
        
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            privateKeyBlock.writeTo(new FileOutputStream(privateKeyFile), true);
            publicKeyBlock.writeTo(new FileOutputStream(publicKeyFile), true);
        }
        
        CoreService.SERVICE.createTables(this);
        Server.addHost(this);
        Database.commit();
        
        final @Nonnull HostAccount account = HostAccount.get(this, identity);
        final @Nonnull Attribute attribute = Attribute.get(account, PublicKeyChain.TYPE);
        if (attribute.getValue() == null) {
            final @Nonnull AttributeValue value;
            if (Server.hasHost(HostIdentifier.DIGITALID) || identifier.equals(HostIdentifier.DIGITALID)) {
                // If the new host is running on the same server as 'core.digitalid.net', certify its public key immediately.
                value = new CertifiedAttributeValue(publicKeyChain, identity, PublicKeyChain.TYPE);
            } else {
                value = new UncertifiedAttributeValue(publicKeyChain);
            }
            attribute.replaceValue(null, value);
        }
        
        // TODO: Load which services this host runs and initialize them afterwards.
        Database.commit();
        
        // TODO: What are the right permissions to pass here? Probably an aggregation of all the services.
        this.client = new Client("_" + identifier.asHostName(), identifier.getString(), FreezableAgentPermissions.GENERAL_WRITE);
    }
    
    /**
     * Returns the identifier of this host.
     * 
     * @return the identifier of this host.
     */
    @Pure
    public @Nonnull HostIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the identity of this host.
     * 
     * @return the identity of this host.
     */
    @Pure
    public @Nonnull HostIdentity getIdentity() {
        return identity;
    }
    
    /**
     * Returns the account of this host.
     * 
     * @return the account of this host.
     */
    @Pure
    public @Nonnull HostAccount getAccount() {
        return HostAccount.get(this, identity);
    }
    
    /**
     * Returns the private key chain of this host.
     * 
     * @return the private key chain of this host.
     */
    @Pure
    public @Nonnull PrivateKeyChain getPrivateKeyChain() {
        return privateKeyChain;
    }
    
    /**
     * Returns the public key chain of this host.
     * 
     * @return the public key chain of this host.
     */
    @Pure
    public @Nonnull PublicKeyChain getPublicKeyChain() {
        return publicKeyChain;
    }
    
    /**
     * Returns the client associated with this host.
     * 
     * @return the client associated with this host.
     */
    @Pure
    public @Nonnull Client getClient() {
        return client;
    }
    
    
    /**
     * Returns whether this host hosts the given identity.
     * 
     * @param identity the identity to check.
     * 
     * @return whether this host hosts the given identity.
     */
    @Pure
    public boolean hosts(@Nonnull InternalIdentity identity) {
        return identity.getAddress().getHostIdentifier().equals(identifier);
    }
    
    
    @Pure
    public boolean supports(final @Nonnull Service service) {
        return true; // TODO!
    }
    
}
