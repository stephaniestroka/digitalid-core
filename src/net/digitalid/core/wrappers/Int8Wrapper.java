package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * Wraps a block with the syntactic type {@code int8@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class Int8Wrapper extends Wrapper {
    
    /**
     * Stores the syntactic type {@code int8@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("int8@core.digitalid.net").load(0);
    
    /**
     * The byte length of an int8.
     */
    public static final int LENGTH = 1;
    
    
    /**
     * Stores the value of this wrapper.
     */
    private final byte value;
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     */
    public Int8Wrapper(@Nonnull SemanticType type, byte value) {
        super(type);
        
        this.value = value;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public Int8Wrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
        
        value = block.getByte(0);
    }
    
    /**
     * Returns the value of the wrapped block.
     * 
     * @return the value of the wrapped block.
     */
    @Pure
    public byte getValue() {
        return value;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        block.setByte(0, value);
    }
    
}
