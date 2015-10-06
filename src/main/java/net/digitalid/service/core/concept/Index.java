package net.digitalid.service.core.concept;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.property.ConceptPropertyTable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnlyForSingleAccess;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class indexes the instances of a {@link Concept concept} by their {@link Entity entity} and key.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class Index<C extends Concept<C, E, K>, E extends Entity, K> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Removal –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores a list of all the indexes that were created.
     */
    private static final @Nonnull @NonNullableElements List<Index<?, ? extends Entity, ?>> indexes = new LinkedList<>();
    
    /**
     * Removes the entries of the given entity from all indexes.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @OnlyForSingleAccess
    public static void remove(@Nonnull Entity entity) {
        assert Database.isSingleAccess() : "The database is in single-access mode.";
        
        for (final @Nonnull Index<?, ? extends Entity, ?> index : indexes) {
            index.concepts.remove(entity);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory that can produce a new concept instance with a given entity and key.
     */
    private final @Nonnull Concept.IndexBasedGlobalFactory<C, E, K> factory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new index with the given concept factory.
     * 
     * @param factory the factory that can produce a new concept instance.
     */
    private Index(@Nonnull Concept.IndexBasedGlobalFactory<C, E, K> factory) {
        this.factory = factory;
        indexes.add(this);
    }
    
    /**
     * Creates a new index with the given concept factory.
     * 
     * @param factory the factory that can produce a new concept instance.
     * 
     * @return a new index with the given concept factory.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity, K> Index<C, E, K> get(@Nonnull Concept.IndexBasedGlobalFactory<C, E, K> factory) {
        return new Index<>(factory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concepts –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the concepts of this index.
     */
    private final @Nonnull ConcurrentMap<E, ConcurrentMap<K, C>> concepts = ConcurrentHashMap.get();
    
    /**
     * Returns a potentially cached concept that might not yet exist in the database.
     * 
     * @param entity the entity to which the concept belongs.
     * @param key the key that denotes the returned instance.
     * 
     * @return a new or existing concept with the given entity and key.
     */
    public @Nonnull C get(@Nonnull E entity, @Nonnull K key) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
            if (map == null) map = concepts.putIfAbsentElseReturnPresent(entity, ConcurrentHashMap.<K, C>get());
            @Nullable C concept = map.get(key);
            if (concept == null) concept = map.putIfAbsentElseReturnPresent(key, factory.create(entity, key));
            return concept;
        } else {
            return factory.create(entity, key);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Resetting –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Resets the concepts of the given entity.
     * 
     * @param entity the entity whose concepts are to be reset.
     * @param table the table which initiated the reset of its properties.
     */
    @Locked
    @NonCommitting
    public void reset(@Nonnull E entity, @Nonnull ConceptPropertyTable<?, C, E> table) throws SQLException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
            if (map != null) {
                for (final @Nonnull C concept : map.values()) concept.reset(table);
            }
        }
    }
    
}
