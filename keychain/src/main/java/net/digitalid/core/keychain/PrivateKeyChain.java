package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.order.StrictlyDescending;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.PrivateKey;

/**
 * This class models a {@link KeyChain key chain} of {@link PrivateKey private keys}.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class PrivateKeyChain extends KeyChain<PrivateKey> {
    
    /**
     * Returns a new key chain with the given time and key.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @require time.isInPast() : "The time lies in the past.";
     */
    @Pure
    public static @Nonnull PrivateKeyChain with(@Nonnull Time time, @Nonnull PrivateKey key) {
        Require.that(time.isInPast()).orThrow("The time has to lie in the past but was $.", time);
        
        return new PrivateKeyChainSubclass(FreezableLinkedList.<Pair<Time, PrivateKey>>withElement(Pair.of(time, key)).freeze());
    }
    
    @Pure
    @Override
    protected @Nonnull PrivateKeyChain createKeyChain(@Nonnull @Frozen @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull Pair<@Nonnull Time, @Nonnull PrivateKey>> items) {
        return new PrivateKeyChainSubclass(items);
    }
    
    @Pure
    @Recover
    @TODO(task = "Remove this method once the read-only list can be properly converted.", date = "2016-12-14", author = Author.KASPAR_ETTER)
    public static @Nonnull PrivateKeyChain recover() {
        return new PrivateKeyChainSubclass(FreezableLinkedList.<@Nonnull Pair<@Nonnull Time, @Nonnull PrivateKey>>withNoElements().freeze());
    }
    
}
