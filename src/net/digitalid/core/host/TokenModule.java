package net.digitalid.core.host;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.annotations.state.Stateless;
import net.digitalid.collections.freezable.FreezableLinkedList;
import net.digitalid.collections.freezable.FreezableList;
import net.digitalid.collections.readonly.ReadOnlyList;
import net.digitalid.database.configuration.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.data.HostModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.data.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class provides database access to the tokens of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Stateless
public final class TokenModule implements HostModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final TokenModule MODULE = new TokenModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS public (host BIGINT NOT NULL, public BOOLEAN NOT NULL, PRIMARY KEY (host), FOREIGN KEY (host) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS token (host BIGINT NOT NULL, token CHAR(19) BIGINT NOT NULL, PRIMARY KEY (host, token), FOREIGN KEY (host) REFERENCES general_identity (identity))");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.tokens.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.tokens.module@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code tokens.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("tokens.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
