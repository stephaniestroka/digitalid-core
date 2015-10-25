package net.digitalid.service.core.contact;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.annotations.OnlyForClients;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreServiceInternalAction;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Adds {@link FreezableContacts contacts} to a {@link Context context}.
 */
@Immutable
final class ContactsAdd extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code add.contacts.context@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("add.contacts.context@core.digitalid.net").load(TupleWrapper.TYPE, Context.TYPE, FreezableContacts.TYPE);
    
    
    /**
     * Stores the context of this action.
     */
    final @Nonnull Context context;
    
    /**
     * Stores the contacts which are to be added.
     */
    private final @Nonnull @Frozen ReadOnlyContacts contacts;
    
    /**
     * Creates an internal action to add the given contacts to the given context.
     * 
     * @param context the context whose contacts are to be extended.
     * @param contacts the contacts to be added to the given context.
     */
    @OnlyForClients
    ContactsAdd(@Nonnull Context context, @Nonnull @Frozen ReadOnlyContacts contacts) {
        super(context.getRole());
        
        this.context = context;
        this.contacts = contacts;
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
    private ContactsAdd(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getNonNullableElements(2);
        this.context = Context.get(entity.toNonHostEntity(), elements.getNonNullable(0));
        this.contacts = new FreezableContacts(entity.toNonHostEntity(), elements.getNonNullable(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, context, contacts).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Adds the contacts " + contacts + " to the context with the number " + context + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return new Restrictions(false, false, true, context);
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return new Restrictions(false, false, false, context);
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws AbortException {
        context.addContactsForActions(contacts);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ContactsAdd && ((ContactsAdd) action).context.equals(context) || action instanceof ContactsRemove && ((ContactsRemove) action).context.equals(context);
    }
    
    @Pure
    @Override
    public @Nonnull ContactsRemove getReverse() {
        return new ContactsRemove(context, contacts);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof ContactsAdd && this.context.equals(((ContactsAdd) object).context) && this.contacts.equals(((ContactsAdd) object).contacts);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * (89 * protectedHashCode() + context.hashCode()) + contacts.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return ContextModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException  {
            return new ContactsAdd(entity, signature, recipient, block);
        }
        
    }
    
}
