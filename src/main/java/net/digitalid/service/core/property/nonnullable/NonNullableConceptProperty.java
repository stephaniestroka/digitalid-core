package net.digitalid.service.core.property.nonnullable;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.property.ConceptProperty;
import net.digitalid.service.core.synchronizer.Synchronizer;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This property belongs to a concept and stores a replaceable value that cannot be null.
 * 
 * @invariant (time == null) == (value == null) : "The time and value are either both null or both non-null.";
 */
public final class NonNullableConceptProperty<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends WriteableNonNullableProperty<V> implements ConceptProperty<V, C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the property factory that contains the required information.
     */
    private final @Nonnull NonNullableConceptPropertyFactory<V, C, E> propertyFactory;
    
    @Pure
    @Override
    public @Nonnull NonNullableConceptPropertyFactory<V, C, E> getPropertyFactory() {
        return propertyFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the concept to which this property belongs.
     */
    private final @Nonnull C concept;
    
    @Pure
    @Override
    public @Nonnull C getConcept() {
        return concept;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-nullable concept property with the given parameters.
     * 
     * @param concept the concept to which the new property belongs.
     * @param propertyFactory the property factory that contains the required information.
     */
    NonNullableConceptProperty(@Nonnull NonNullableConceptPropertyFactory<V, C, E> propertyFactory, @Nonnull C concept) {
        super(propertyFactory.getValueValidator());
        
        this.propertyFactory = propertyFactory;
        this.concept = concept;
        
        concept.register(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Loading –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Loads the time and value of this property from the database.
     */
    @Pure
    @Locked
    @NonCommitting
    private void load() throws AbortException {
        final @Nonnull @NonNullableElements ReadOnlyPair<Time, V> pair = propertyFactory.getPropertyTable().load(this);
        this.time = pair.getNonNullableElement0();
        this.value = pair.getNonNullableElement1();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Time –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the time of the last modification.
     */
    private @Nullable Time time;
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Time getTime() throws AbortException {
        if (time == null) load();
        assert time != null;
        return time;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this property.
     */
    private @Nullable @Validated V value;
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull @Validated V get() throws AbortException {
        if (value == null) load();
        assert value != null;
        return value;
    }
    
    @Locked
    @Override
    @Committing
    public void set(@Nonnull @Validated V newValue) throws AbortException {
        assert getValueValidator().isValid(newValue) : "The new value is valid.";
        
        final @Nonnull V oldValue = get();
        if (!newValue.equals(oldValue)) {
            Synchronizer.execute(NonNullableConceptPropertyInternalAction.get(this, oldValue, newValue));
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Action –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Replaces the time and value of this property from the {@link NonNullableConceptPropertyInternalAction}.
     * 
     * @param oldTime the old time of this property.
     * @param newTime the new time of this property.
     * @param oldValue the old value of this property.
     * @param newValue the new value of this property.
     * 
     * @require !oldValue.equals(newValue) : "The old and the new value are not the same.";
     */
    @Locked
    @NonCommitting
    void replace(@Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws AbortException {
        assert getValueValidator().isValid(oldValue) : "The old value is valid.";
        assert getValueValidator().isValid(newValue) : "The new value is valid.";
        
        propertyFactory.getPropertyTable().replace(this, oldTime, newTime, oldValue, newValue);
        this.time = newTime;
        this.value = newValue;
        notify(oldValue, newValue);
    }
    
    @Locked
    @Override
    @NonCommitting
    public void reset() throws AbortException {
        if (hasObservers() && value != null) {
            final @Nonnull V oldValue = value;
            this.value = null;
            final @Nonnull V newValue = get();
            if (!oldValue.equals(newValue)) notify(oldValue, newValue);
        } else {
            this.time = null;
            this.value = null;
        }
    }
    
}
