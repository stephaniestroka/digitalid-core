package net.digitalid.service.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.sql.XDFConverterBasedSQLConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.declaration.ColumnDeclaration;

/**
 * This class models a {@link KeyChain key chain} of {@link PrivateKey private keys}.
 */
@Immutable
public final class PrivateKeyChain extends KeyChain<PrivateKeyChain, PrivateKey> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    private PrivateKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
        super(items);
    }
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @return a new key chain with the given items.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    @Pure
    public static @Nonnull PrivateKeyChain get(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
        return new PrivateKeyChain(items);
    }
    
    /**
     * Creates a new key chain with the given time and key.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @return a new key chain with the given time and key.
     * 
     * @require time.isLessThanOrEqualTo(Time.getCurrent()) : "The time lies in the past.";
     */
    @Pure
    public static @Nonnull PrivateKeyChain get(@Nonnull Time time, @Nonnull PrivateKey key) {
        assert time.isLessThanOrEqualTo(Time.getCurrent()) : "The time lies in the past.";
        
        final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, PrivateKey>> items = FreezableLinkedList.get();
        items.add(FreezablePair.get(time, key).freeze());
        return new PrivateKeyChain(items.freeze());
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code item.private.key.chain.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.map("item.private.key.chain.host@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Time.TYPE, PrivateKey.TYPE);
    
    /**
     * Stores the semantic type {@code private.key.chain.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("private.key.chain.host@core.digitalid.net").load(ListWrapper.XDF_TYPE, ITEM);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull KeyChain.XDFConverter<PrivateKeyChain, PrivateKey> XDF_CONVERTER = new KeyChain.XDFConverter<PrivateKeyChain, PrivateKey>(TYPE, ITEM, PrivateKey.XDF_CONVERTER) {
        @Pure
        @Override
        protected @Nonnull PrivateKeyChain createKeyChain(@Nonnull @NonNullableElements @Frozen @NonEmpty ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
            return new PrivateKeyChain(items);
        }
    };
    
    @Pure
    @Override
    public @Nonnull KeyChain.XDFConverter<PrivateKeyChain, PrivateKey> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Block.DECLARATION.renamedAs("private_key_chain");
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<PrivateKeyChain, Object> SQL_CONVERTER = XDFConverterBasedSQLConverter.get(DECLARATION, XDF_CONVERTER);
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<PrivateKeyChain, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<PrivateKeyChain, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
