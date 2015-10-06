package net.digitalid.core.synchronizer;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.collections.freezable.FreezableArray;
import net.digitalid.collections.readonly.ReadOnlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.packet.Packet;
import net.digitalid.core.factory.Storable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models an audit with a time and trail.
 * 
 * @see RequestAudit
 * @see ResponseAudit
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class Audit implements Storable<Audit> {
    
    /**
     * Stores the semantic type {@code last.time.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType LAST_TIME = SemanticType.map("last.time.audit@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code this.time.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType THIS_TIME = SemanticType.map("this.time.audit@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code trail.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TRAIL = SemanticType.map("trail.audit@core.digitalid.net").load(Packet.SIGNATURES);
    
    /**
     * Stores the semantic type {@code audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("audit@core.digitalid.net").load(TupleWrapper.TYPE, LAST_TIME, THIS_TIME, TRAIL);
    
    
    /**
     * Stores the time of the last audit.
     */
    private final @Nonnull Time lastTime;
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    Audit(@Nonnull Time lastTime) {
        this.lastTime = lastTime;
    }
    
    /**
     * Creates a new audit from the given block.
     * 
     * @param block the block containing the audit.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public static @Nonnull Audit get(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        final @Nonnull Time lastTime = new Time(tuple.getNonNullableElement(0));
        if (tuple.isElementNull(1)) {
            return new RequestAudit(lastTime);
        } else {
            final @Nonnull Time thisTime = new Time(tuple.getNonNullableElement(1));
            final @Nonnull ReadOnlyList<Block> trail = new ListWrapper(tuple.getNonNullableElement(2)).getElementsNotNull();
            return new ResponseAudit(lastTime, thisTime, trail);
        }
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(3);
        elements.set(0, lastTime.toBlock().setType(LAST_TIME));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the time of the last audit.
     * 
     * @return the time of the last audit.
     */
    @Pure
    public final @Nonnull Time getLastTime() {
        return lastTime;
    }
    
    
    /**
     * Returns this audit as a {@link RequestAudit}.
     * 
     * @return this audit as a {@link RequestAudit}.
     * 
     * @throws InvalidEncodingException if this audit is not an instance of {@link RequestAudit}.
     */
    @Pure
    public final @Nonnull RequestAudit toRequestAudit() throws InvalidEncodingException {
        if (this instanceof RequestAudit) return (RequestAudit) this;
        throw new InvalidEncodingException("This audit is not a request audit.");
    }
    
    /**
     * Returns this audit as a {@link ResponseAudit}.
     * 
     * @return this audit as a {@link ResponseAudit}.
     * 
     * @throws InvalidEncodingException if this audit is not an instance of {@link ResponseAudit}.
     */
    @Pure
    public final @Nonnull ResponseAudit toResponseAudit() throws InvalidEncodingException {
        if (this instanceof ResponseAudit) return (ResponseAudit) this;
        throw new InvalidEncodingException("This audit is not a response audit.");
    }
    
}
