package net.digitalid.service.core.expression;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.Blockable;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models abstract expressions.
 * 
 * @see ActiveExpression
 * @see PassiveExpression
 * @see PersonalExpression
 * @see ImpersonalExpression
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
abstract class AbstractExpression extends NonHostConcept implements Blockable, SQLizable {
    
    /**
     * Stores the expression of this abstract expression.
     */
    private final @Nonnull Expression expression;
    
    /**
     * Creates a new abstract expression with the given entity and string.
     * 
     * @param entity the entity to which this abstract expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    @NonCommitting
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity);
        
        this.expression = Expression.parse(entity, string);
        if (!isValid()) throw new InvalidEncodingException("The expression '" + string + "' is invalid as " + getClass().getSimpleName() + ".");
    }
    
    /**
     * Creates a new abstract expression from the given entity and block.
     * 
     * @param entity the entity to which this abstract expression belongs.
     * @param block the block which contains the abstract expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    @NonCommitting
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        this(entity, new StringWrapper(block).getString());
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return new StringWrapper(getType(), expression.toString()).toBlock();
    }
    
    
    /**
     * Returns the expression of this abstract expression.
     * 
     * @return the expression of this abstract expression.
     */
    @Pure
    final @Nonnull Expression getExpression() {
        return expression;
    }
    
    /**
     * Returns whether the expression is valid.
     * 
     * @return whether the expression is valid.
     */
    @Pure
    abstract boolean isValid();
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof AbstractExpression)) return false;
        final @Nonnull AbstractExpression other = (AbstractExpression) object;
        return this.expression.equals(other.expression);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return expression.hashCode();
    }
    
    @Pure
    @Override
    public final String toString() {
        return expression.toString();
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "TEXT NOT NULL COLLATE " + Database.getConfiguration().BINARY();
    
    @Override
    @NonCommitting
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setString(parameterIndex, toString());
    }
    
}
