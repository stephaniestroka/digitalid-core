package net.digitalid.service.core.expression;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.contact.Contact;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models active expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class ActiveExpression extends AbstractExpression {
    
    /**
     * Stores the semantic type {@code active.expression@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("active.expression@core.digitalid.net").load(StringWrapper.TYPE);
    
    
    /**
     * Creates a new active expression with the given entity and string.
     * 
     * @param entity the entity to which this active expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    @NonCommitting
    public ActiveExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, string);
    }
    
    /**
     * Creates a new active expression from the given entity and block.
     * 
     * @param entity the entity to which this active expression belongs.
     * @param block the block which contains the active expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    @NonCommitting
    public ActiveExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    boolean isValid() {
        return getExpression().isActive();
    }
    
    
    /**
     * Returns the contacts denoted by this active expression.
     * 
     * @return the contacts denoted by this active expression.
     */
    @Pure
    @NonCommitting
    public @Nonnull @Capturable FreezableSet<Contact> getContacts() throws SQLException {
        return getExpression().getContacts();
    }
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the returned expression belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull ActiveExpression get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return new ActiveExpression(entity, resultSet.getString(columnIndex));
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("The expression returned by the database is invalid.", exception);
        }
    }
    
}
