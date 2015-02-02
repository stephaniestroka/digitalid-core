package ch.virtualid.password;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreServiceInternalAction;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replaces the value of a {@link Password password}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class PasswordValueReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.password@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_VALUE = SemanticType.create("old.password@virtualid.ch").load(Password.TYPE);
    
    /**
     * Stores the semantic type {@code new.password@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_VALUE = SemanticType.create("new.password@virtualid.ch").load(Password.TYPE);
    
    /**
     * Stores the semantic type {@code replace.password@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("replace.password@virtualid.ch").load(TupleWrapper.TYPE, OLD_VALUE, NEW_VALUE);
    
    
    /**
     * Stores the password whose value is to be replaced.
     */
    private final @Nonnull Password password;
    
    /**
     * Stores the old value of the password.
     * 
     * @invariant Password.isValid(oldValue) : "The old value is valid.";
     */
    private final @Nonnull String oldValue;
    
    /**
     * Stores the new value of the password.
     * 
     * @invariant Password.isValid(newValue) : "The new value is valid.";
     */
    private final @Nonnull String newValue;
    
    /**
     * Creates an internal action to replace the value of the given password.
     * 
     * @param password the password whose value is to be replaced.
     * @param oldValue the old value of the password.
     * @param newValue the new value of the password.
     * 
     * @require password.isOnClient() : "The password is on a client.";
     * @require Password.isValid(oldValue) : "The old value is valid.";
     * @require Password.isValid(newValue) : "The new value is valid.";
     */
    PasswordValueReplace(@Nonnull Password password, @Nonnull String oldValue, @Nonnull String newValue) {
        super(password.getRole());
        
        assert Password.isValid(oldValue) : "The old value is valid.";
        assert Password.isValid(newValue) : "The new value is valid.";
        
        this.password = password;
        this.oldValue = oldValue;
        this.newValue = newValue;
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
    @DoesNotCommit
    private PasswordValueReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.password = Password.get(entity.toNonHostEntity());
        this.oldValue = new StringWrapper(elements.getNotNull(0)).getString();
        this.newValue = new StringWrapper(elements.getNotNull(1)).getString();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, new StringWrapper(OLD_VALUE, oldValue).toBlock());
        elements.set(1, new StringWrapper(NEW_VALUE, newValue).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the password '" + oldValue + "' with '" + newValue + "'.";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return new Restrictions(true, false, true);
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return new Restrictions(true, false, false);
    }
    
    
    @Override
    @DoesNotCommit
    protected void executeOnBoth() throws SQLException {
        password.replaceName(oldValue, newValue);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof PasswordValueReplace;
    }
    
    @Pure
    @Override
    public @Nonnull PasswordValueReplace getReverse() {
        return new PasswordValueReplace(password, newValue, oldValue);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof PasswordValueReplace) {
            final @Nonnull PasswordValueReplace other = (PasswordValueReplace) object;
            return this.password.equals(other.password) && this.oldValue.equals(other.oldValue) && this.newValue.equals(other.newValue);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + password.hashCode();
        hash = 89 * hash + oldValue.hashCode();
        hash = 89 * hash + newValue.hashCode();
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
        return PasswordModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @DoesNotCommit
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new PasswordValueReplace(entity, signature, recipient, block);
        }
        
    }
    
}
