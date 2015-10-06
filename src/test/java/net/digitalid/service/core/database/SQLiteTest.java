package net.digitalid.service.core.database;

import java.sql.SQLException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.configuration.SQLiteConfiguration;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link SQLiteConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class SQLiteTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws SQLException {
        Database.initialize(new SQLiteConfiguration(true), false);
        createTables();
    }
    
}
