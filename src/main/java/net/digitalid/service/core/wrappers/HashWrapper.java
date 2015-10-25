package net.digitalid.service.core.wrappers;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.BasedOn;
import net.digitalid.service.core.annotations.Encoding;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.cryptography.Parameters;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.factories.Factories;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.wrappers.ValueWrapper.ValueEncodingFactory;
import net.digitalid.service.core.wrappers.ValueWrapper.ValueStoringFactory;
import net.digitalid.utility.annotations.math.NonNegative;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;

/**
 * This class wraps a {@link BigInteger} for encoding and decoding a block of the syntactic type {@code hash@core.digitalid.net}.
 */
@Immutable
public final class HashWrapper extends Wrapper<HashWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code hash@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("hash@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this wrapper.
     */
    private final @Nonnull @NonNegative BigInteger value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     * 
     * @ensure value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
     */
    @Pure
    public @Nonnull @NonNegative BigInteger getValue() {
        return value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     * 
     * @require value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
     */
    private HashWrapper(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type, @Nonnull @NonNegative BigInteger value) {
        super(type);
        
        assert value.signum() >= 0 : "The value is positive.";
        assert value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
        
        this.value = value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the given non-nullable value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new non-nullable block containing the given value.
     * 
     * @require value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type, @Nonnull @NonNegative BigInteger value) {
        return new EncodingFactory(type).encodeNonNullable(new HashWrapper(type, value));
    }
    
    /**
     * Encodes the given nullable value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new nullable block containing the given value.
     * 
     * @require value == null || value.bitLength() <= Parameters.HASH : "The value is either null or its length is at most Parameters.HASH.";
     */
    @Pure
    public static @Nullable @NonEncoding Block encodeNullable(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type, @Nullable @NonNegative BigInteger value) {
        return value == null ? null : encodeNonNullable(type, value);
    }
    
    /**
     * Decodes the given non-nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     * 
     * @ensure return.bitLength() <= Parameters.HASH : "The length of the returned value is at most Parameters.HASH.";
     */
    @Pure
    public static @Nonnull @NonNegative BigInteger decodeNonNullable(@Nonnull @NonEncoding @BasedOn("hash@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new EncodingFactory(block.getType()).decodeNonNullable(None.OBJECT, block).value;
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     * 
     * @ensure return == null ||  return.bitLength() <= Parameters.HASH : "The returned value is either null or its length is at most Parameters.HASH.";
     */
    @Pure
    public static @Nullable @NonNegative BigInteger decodeNullable(@Nullable @NonEncoding @BasedOn("hash@core.digitalid.net") Block block) throws InvalidEncodingException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The byte length of a hash.
     */
    public static final int LENGTH = 64;
    
    @Pure
    @Override
    protected int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        final @Nonnull byte[] bytes = value.toByteArray();
        final int offset = bytes.length > LENGTH ? 1 : 0;
        block.setBytes(LENGTH - bytes.length + offset, bytes, offset, bytes.length - offset);
    }

    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends Wrapper.NonRequestingEncodingFactory<HashWrapper> {
        
        /**
         * Creates a new encoding factory with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private EncodingFactory(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull HashWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("hash@core.digitalid.net") Block block) throws InvalidEncodingException {
            if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
            
            return new HashWrapper(block.getType(), new BigInteger(1, block.getBytes()));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return new EncodingFactory(getSemanticType());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory extends Wrapper.StoringFactory<HashWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.HASH);
        
        /**
         * Creates a new storing factory with the given type.
         * 
         * @param type the semantic type of the restored wrappers.
         */
        private StoringFactory(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type) {
            super(COLUMN, type);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull HashWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setBytes(parameterIndex, wrapper.value.toByteArray());
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable HashWrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable byte[] bytes = resultSet.getBytes(columnIndex);
            return bytes == null || bytes.length > LENGTH ? null : new HashWrapper(getType(), new BigInteger(1, bytes));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull StoringFactory getStoringFactory() {
        return new StoringFactory(getSemanticType());
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Block.toString(value.toByteArray());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for the value type of this wrapper.
     */
    @Immutable
    public static class Factory extends ValueWrapper.Factory<BigInteger, HashWrapper> {
        
        @Pure
        @Override
        protected boolean isValid(@Nonnull BigInteger value) {
            return value.signum() >= 0 && value.bitLength() <= Parameters.HASH;
        }
        
        @Pure
        @Override
        protected @Nonnull HashWrapper wrap(@Nonnull SemanticType type, @Nonnull BigInteger value) {
            assert isValid(value) : "The value is valid.";
            
            return new HashWrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull BigInteger unwrap(@Nonnull HashWrapper wrapper) {
            return wrapper.value;
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value encoding factory of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value encoding factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueEncodingFactory<BigInteger, HashWrapper> getValueEncodingFactory(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type) {
        return new ValueEncodingFactory<>(FACTORY, new EncodingFactory(type));
    }
    
    /**
     * Returns the value storing factory of this wrapper.
     * 
     * @param type any semantic type that is based on the syntactic type of this wrapper.
     * 
     * @return the value storing factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueStoringFactory<BigInteger, HashWrapper> getValueStoringFactory(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type) {
        return new ValueStoringFactory<>(FACTORY, new StoringFactory(type));
    }
    
    /**
     * Returns the value factories of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value factories of this wrapper.
     */
    @Pure
    public static @Nonnull Factories<BigInteger, Object> getValueFactories(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type) {
        return Factories.get(getValueEncodingFactory(type), getValueStoringFactory(type));
    }
    
}
