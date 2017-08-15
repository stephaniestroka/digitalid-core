package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.storage.Storage;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.property.map.MapPropertyInternalAction;
import net.digitalid.core.property.set.SetPropertyInternalAction;
import net.digitalid.core.property.value.ValuePropertyInternalAction;
import net.digitalid.core.service.Service;
import net.digitalid.core.subject.CoreSubject;

/**
 * Internal actions are used to synchronize properties across sites.
 * 
 * @see MapPropertyInternalAction
 * @see SetPropertyInternalAction
 * @see ValuePropertyInternalAction
 */
@Immutable
public abstract class PropertyInternalAction<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable PROPERTY extends SynchronizedProperty<ENTITY, KEY, SUBJECT, ?, ?>> extends InternalAction {
    
    /* -------------------------------------------------- Property -------------------------------------------------- */
    
    /**
     * Returns the property that is modified by this internal action.
     */
    @Pure
    public abstract @Nonnull PROPERTY getProperty();
    
    /* -------------------------------------------------- Handler -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable Entity getProvidedEntity() {
        return null;
    }
    
    @Pure
    @Override
    public @Nonnull NonHostEntity getEntity() {
        return (NonHostEntity) getProperty().getSubject().getEntity();
    }
    
    @Pure
    @Override
    public @Nullable InternalIdentifier getProvidedSubject() {
        return getProperty().getSubject().getEntity().getIdentity().getAddress();
    }
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return getProperty().getTable().getParentModule().getService();
    }
    
//    @Pure
//    @Override
//    public @Nonnull String getDescription() {
//        return "Synchronized the " + getProperty().getTable().getName() + " property of the " + getProperty().getTable().getParentModule().getName() + " concept of the identity " + getProperty().getConcept().getEntity().getIdentity().getAddress().getString() + ".";
//    }
    
    /* -------------------------------------------------- Method -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient() {
        try {
            return getService().getRecipient(getEntity());
        } catch (@Nonnull DatabaseException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build(); // TODO: How to handle this? Maybe use @Derive instead once exceptions can be indicated.
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Storage getStorage() {
        return getProperty().getTable();
    }
    
    /* -------------------------------------------------- Packable -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Remove this method as soon as the property actions have converters.", date = "2016-12-20", author = Author.KASPAR_ETTER)
    public @Nonnull Pack pack() {
        return null;
    }
    
}
