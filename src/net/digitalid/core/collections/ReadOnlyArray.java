package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.ValidIndex;

/**
 * This interface provides read-only access to arrays and should <em>never</em> be cast away.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArray
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyArray<E> extends ReadOnlyIterable<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieval –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the size of this array.
     * 
     * @return the size of this array.
     */
    @Pure
    public int size();
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     */
    @Pure
    public @Nullable E getNullable(@ValidIndex int index);
    
    /**
     * Returns whether the element at the given index is null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is null.
     */
    @Pure
    public boolean isNull(@ValidIndex int index);
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     * 
     * @require !isNull(index) : "The element at the given index is not null.";
     */
    @Pure
    public @Nonnull E getNonNullable(@ValidIndex int index);
    
    @Pure
    @Override
    public @Nonnull ReadOnlyArrayIterator<E> iterator();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conditions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether this array contains an element which is null.
     * If it does not, {@link #getNullable(int)} is guaranteed to return not null for every valid index.
     * 
     * @return {@code true} if this array contains null, {@code false} otherwise.
     */
    @Pure
    public boolean containsNull();
    
    /**
     * Returns whether this array contains duplicates (including null values).
     * 
     * @return {@code true} if this array contains duplicates, {@code false} otherwise.
     */
    @Pure
    public boolean containsDuplicates();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the elements of this array as an array.
     * 
     * @return the elements of this array as an array.
     */
    @Pure
    public @Capturable @Nonnull E[] toArray();
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableArray<E> clone();
    
    /**
     * Returns the elements of this array in a freezable list.
     * 
     * @return the elements of this array in a freezable list.
     */
    @Pure
    public @Capturable @Nonnull @NonFrozen FreezableList<E> toFreezableList();
    
}
