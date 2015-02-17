package ch.virtualid.cryptography;

import ch.virtualid.annotations.ElementsNonNullable;
import ch.virtualid.annotations.Frozen;
import ch.virtualid.annotations.NonEmpty;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.collections.ReadonlyList;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.tuples.ReadonlyPair;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;

/**
 * This class models a {@link KeyChain key chain} of {@link PublicKey public keys}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class PublicKeyChain extends KeyChain<PublicKey> implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code item.public.key.chain.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.create("item.public.key.chain.host@virtualid.ch").load(TupleWrapper.TYPE, Time.TYPE, PublicKey.TYPE);
    
    /**
     * Stores the semantic type {@code public.key.chain.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("public.key.chain.host@virtualid.ch").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, ListWrapper.TYPE, ITEM);
    
    
    /**
     * Creates a new key chain with the given time and public key.
     * 
     * @param time the time from when on the given public key is valid.
     * @param publicKey the public key that is valid from the given time on.
     * 
     * @require time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
     */
    public PublicKeyChain(@Nonnull Time time, @Nonnull PublicKey publicKey) {
        super(time, publicKey);
    }
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    public PublicKeyChain(@Nonnull @Frozen @NonEmpty @ElementsNonNullable ReadonlyList<ReadonlyPair<Time, PublicKey>> items) {
        super(items);
    }
    
    /**
     * Creates a new key chain with the entries encoded in the given block.
     * 
     * @param block the block containing the key chain entries.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public PublicKeyChain(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Pure
    @Override
    protected @Nonnull SemanticType getItemType() {
        return ITEM;
    }
    
    @Pure
    @Override
    protected @Nonnull PublicKey createKey(@Nonnull Block block) throws InvalidEncodingException {
        return new PublicKey(block);
    }
    
    @Pure
    @Override
    protected @Nonnull KeyChain<PublicKey> createKeyChain(@Nonnull ReadonlyList<ReadonlyPair<Time, PublicKey>> items) {
        return new PublicKeyChain(items);
    }
    
}
