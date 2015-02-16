package ch.virtualid.identity;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.cache.Cache;
import ch.virtualid.client.AccountInitialize;
import ch.virtualid.client.AccountOpen;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.host.Host;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import ch.virtualid.server.Server;
import ch.virtualid.collections.ReadonlyList;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Triplet;

/**
 * The mapper maps between {@link Identifier identifiers} and {@link Identity identities}.
 * <p>
 * <em>Important:</em> Every reference to general_identity.identity needs to be registered
 * with {@link #addReference(java.lang.String, java.lang.String, java.lang.String...)}
 * in order that the values can be updated when two {@link Person persons} have been
 * merged! (If the column can only contain {@link Type types}, this is not necessary.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Mapper {
    
    /**
     * Stores the data type used to store identities in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference identities.
     * <p>
     * <em>Important:</em> Every reference to general_identity.identity needs to be registered
     * with {@link #addReference(java.lang.String, java.lang.String, java.lang.String...)}
     * in order that the values can be updated when two {@link Person persons} have been
     * merged! (If the column can only contain {@link Type types}, this is not necessary.)
     * Additionally, it might be a good idea to establish an index on the referencing column.
     */
    public static final @Nonnull String REFERENCE = new String("REFERENCES general_identity (identity) ON DELETE RESTRICT ON UPDATE RESTRICT");
    
    
    /**
     * Stores the registered triplets of tables, columns and unique constraint that reference a person.
     */
    private static final @Nonnull Set<Triplet<String, String, String[]>> references = new LinkedHashSet<Triplet<String, String, String[]>>();
    
    /**
     * Adds the given table, columns and unique constraint to the list of registered references.
     * 
     * @param table the name of the table that references a person.
     * @param column the name of the column that references a person.
     * @param uniques the names of all the columns in the same unique constraint or nothing.
     */
    public static void addReference(@Nonnull String table, @Nonnull String column, @Nonnull String... uniques) {
        references.add(new Triplet<String, String, String[]>(table, column, uniques));
    }
    
    /**
     * Removes the given table, columns and unique constraint from the list of registered references.
     * 
     * @param table the name of the table that references a person.
     * @param column the name of the column that references a person.
     * @param uniques the names of all the columns in the same unique constraint or nothing.
     */
    public static void removeReference(@Nonnull String table, @Nonnull String column, @Nonnull String... uniques) {
        references.remove(new Triplet<String, String, String[]>(table, column, uniques));
    }
    
    /**
     * Updates the references from the old to the new number using the given statement.
     * 
     * @param statement the statement on which the updates are executed.
     * @param oldNumber the old number of the person which is updated.
     * @param newNumber the new number of the person which is updated.
     */
    @NonCommitting
    private static void updateReferences(@Nonnull Statement statement, long oldNumber, long newNumber) throws SQLException {
        final @Nonnull String IGNORE = Database.getConfiguration().IGNORE();
        for (final @Nonnull Triplet<String, String, String[]> reference : references) {
            final @Nonnull String table = reference.getValue0();
            final @Nonnull String column = reference.getValue1();
            if (IGNORE.isEmpty()) {
                final @Nonnull String[] uniques = reference.getValue2();
                if (uniques.length > 0) {
                    final @Nonnull StringBuilder SQL = new StringBuilder("DELETE FROM ").append(table).append(" AS a WHERE ").append(column).append(" = ").append(oldNumber);
                    SQL.append(" AND WHERE EXISTS (SELECT 1 FROM ").append(table).append(" AS b WHERE ");
                    boolean first = true;
                    for (final @Nonnull String unique : uniques) {
                        if (first) first = false;
                        else SQL.append(" AND ");
                        SQL.append("b.").append(unique).append(" = ");
                        if (unique.equals(column)) SQL.append(newNumber);
                        else SQL.append("a.").append(unique);
                    }
                    SQL.append(")");
                    statement.executeUpdate(SQL.toString());
                }
                statement.executeUpdate("UPDATE " + table + " SET " + column + " = " + newNumber + " WHERE " + column + " = " + oldNumber);
            } else {
                statement.executeUpdate("UPDATE" + IGNORE + " " + table + " SET " + column + " = " + newNumber + " WHERE " + column + " = " + oldNumber);
                statement.executeUpdate("DELETE FROM " + table + " WHERE " + column + " = " + oldNumber);
            }
        }
    }
    
    
    static {
        assert Database.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            // Make sure that no type initializations are triggered during the creation of the database tables! (This is why the format of the category column is not taken from the category class.)
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_identity (identity " + Database.getConfiguration().PRIMARY_KEY() + ", category " + Database.getConfiguration().TINYINT() + " NOT NULL, address " + IdentifierClass.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_identifier (identifier " + IdentifierClass.FORMAT + " NOT NULL, identity " + Mapper.FORMAT + " NOT NULL, PRIMARY KEY (identifier), FOREIGN KEY (identity) " + Mapper.REFERENCE + ")");
            addReference("general_identifier", "identity");
        } catch (@Nonnull SQLException exception) {
            try { Database.rollback(); } catch (@Nonnull SQLException exc) { throw new InitializationError("Could not rollback.", exc); }
            throw new InitializationError("The database tables of the mapper could not be created.", exception);
        }
    }
    
    
    /**
     * Stores the logger of the identity mapper.
     */
    private static final @Nonnull Logger LOGGER = new Logger("Mapper.log");
    
    
    /**
     * Creates a new identity of the given category with the given number and address.
     * 
     * @param category the category of the identity to be created.
     * @param number the number of the identity to be created.
     * @param address the address of the identity to be created.
     * 
     * @return the newly created identity of the right subtype.
     * 
     * @ensure return.getCategory().equals(category) : "The category of the returned identity equals the given category.";
     * @ensure !(return instanceof Type) || !((Type) return).isLoaded() : "If a type is returned, its declaration has not yet been loaded.";
     */
    @Pure
    @NonCommitting
    private static @Nonnull Identity createIdentity(@Nonnull Category category, long number, @Nonnull Identifier address) throws SQLException {
        try {
            switch (category) {
                case HOST: return new HostIdentity(number, address.toHostIdentifier());
                case SYNTACTIC_TYPE: return new SyntacticType(number, address.toInternalNonHostIdentifier());
                case SEMANTIC_TYPE: return new SemanticType(number, address.toInternalNonHostIdentifier());
                case NATURAL_PERSON: return new NaturalPerson(number, address.toInternalNonHostIdentifier());
                case ARTIFICIAL_PERSON: return new ArtificialPerson(number, address.toInternalNonHostIdentifier());
                case EMAIL_PERSON: return new EmailPerson(number, address.toEmailIdentifier());
                case MOBILE_PERSON: return new MobilePerson(number, address.toMobileIdentifier());
                default: throw new ShouldNeverHappenError("The category '" + category.name() + "' is not supported.");
            }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The address " + address + " does not match the category '" + category.name() + "'.", exception);
        }
    }
    
    
    /**
     * Maps numbers onto identities by caching the corresponding entries from the database.
     */
    private static final @Nonnull Map<Long, Identity> numbers = new ConcurrentHashMap<Long, Identity>();
    
    /**
     * Maps identifiers onto identities by caching the corresponding entries from the database.
     */
    private static final @Nonnull Map<Identifier, Identity> identifiers = new ConcurrentHashMap<Identifier, Identity>();
    
    /**
     * Clears the local maps of the mapper.
     */
    public static void clearLocalMaps() {
        numbers.clear();
        identifiers.clear();
    }
    
    /**
     * Removes the given identity from the local maps.
     * 
     * @param identity the identity which is to be removed.
     */
    public static void unmap(@Nonnull Identity identity) {
        numbers.remove(identity.getNumber());
        identifiers.remove(identity.getAddress());
        LOGGER.log(Level.INFORMATION, "The identity of " + identity.getAddress() + " was unmapped.");
    }
    
    /**
     * Loads the identity with the given number from the database into the local hash map.
     * 
     * @param number the number of the identity to load.
     * 
     * @return the identity with the given number.
     * 
     * @require !numbers.containsKey(number) : "The given number is not yet loaded.";
     */
    @NonCommitting
    private static @Nonnull Identity loadIdentity(long number) throws SQLException {
        assert !numbers.containsKey(number) : "The given number is not yet loaded.";
        
        final @Nonnull String SQL = "SELECT category, address FROM general_identity WHERE identity = " + number;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull Category category = Category.get(resultSet.getByte(1));
                final @Nonnull Identifier address = IdentifierClass.get(resultSet, 2);
                final @Nonnull Identity identity = createIdentity(category, number, address);
                
                numbers.put(number, identity);
                identifiers.put(address, identity);
                
                return identity;
            } else {
                throw new SQLException("There exists no identity with the number " + number + ".");
            }
        }
    }
    
    /**
     * Returns the identity with the given number.
     * 
     * @param number the number of the identity.
     * 
     * @return the identity with the given number.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @NonCommitting
    static @Nonnull Identity getIdentity(long number) throws SQLException {
        @Nullable Identity identity = numbers.get(number);
        if (identity == null) identity = loadIdentity(number);
        try {
            if (identity instanceof Type) ((Type) identity).ensureLoaded();
        } catch (@Nonnull IOException | PacketException | ExternalException  exception) {
            throw new ShouldNeverHappenError("The type declaration and the referenced identities should already be cached.", exception);
        }
        return identity;
    }
    
    
    /**
     * Loads the identity and address of the given identifier from the database into the local hash map.
     * 
     * @param identifier the identifier of the identity to load.
     * 
     * @return whether the identity was successfully loaded.
     * 
     * @require !identifiers.containsKey(identifier) : "The given identifier is not yet loaded.";
     */
    @NonCommitting
    private static boolean loadIdentity(@Nonnull Identifier identifier) throws SQLException {
        assert !identifiers.containsKey(identifier) : "The given identifier is not yet loaded.";
        
        final @Nonnull String SQL = "SELECT general_identity.category, general_identity.identity, general_identity.address FROM general_identifier INNER JOIN general_identity ON general_identifier.identity = general_identity.identity WHERE general_identifier.identifier = " + identifier;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull Category category = Category.get(resultSet, 1);
                final long number = resultSet.getLong(2);
                final @Nonnull Identifier address = IdentifierClass.get(resultSet, 3);
                
                @Nullable Identity identity = numbers.get(number);
                if (identity instanceof InternalNonHostIdentity) {
                    if (!(address instanceof InternalNonHostIdentifier)) throw new SQLException("The address " + address + " should be an internal non-host identifier.");
                    if (identity instanceof InternalPerson) ((InternalPerson) identity).setAddress((InternalNonHostIdentifier) address);
                    else if (identity instanceof Type) ((Type) identity).setAddress((InternalNonHostIdentifier) address);
                } else {
                    final @Nonnull Identity newIdentity = createIdentity(category, number, address);
                    numbers.put(number, newIdentity);
                    if (identity instanceof ExternalPerson) {
                        if (!(address instanceof InternalNonHostIdentifier)) throw new SQLException("The address " + address + " should be an internal non-host identifier.");
                        ((ExternalPerson) identity).setAddress((InternalNonHostIdentifier) address);
                    }
                    identity = newIdentity;
                }
                
                identifiers.put(identifier, identity);
                if (!address.equals(identifier))
                    identifiers.put(address, identity);
                
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Returns whether the given identifier is mapped.
     * 
     * @param identifier the identifier of interest.
     * 
     * @return whether the given identifier is mapped.
     */
    @NonCommitting
    public static boolean isMapped(@Nonnull Identifier identifier) throws SQLException {
        return identifiers.containsKey(identifier) || loadIdentity(identifier);
    }
    
    /**
     * Returns whether the given identifier is not mapped.
     * 
     * @param identifier the identifier of interest.
     * 
     * @return whether the given identifier is not mapped.
     */
    @NonCommitting
    public static boolean isNotMapped(@Nonnull Identifier identifier) throws SQLException {
        return !isMapped(identifier);
    }
    
    /**
     * Returns the mapped identity of the given identifier.
     * 
     * @param identifier the identifier whose identity is to be returned.
     * 
     * @return the mapped identity of the given identifier.
     * 
     * @require isMapped(identifier) : "The identifier is mapped.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Identity getMappedIdentity(@Nonnull Identifier identifier) throws SQLException {
        assert isMapped(identifier) : "The identifier is mapped.";
        
        return identifiers.get(identifier);
    }
    
    
    /**
     * Maps the given identifier to a new number with the given category.
     * This method should only be called by {@link AccountOpen} outside of this class.
     * 
     * @param identifier the identifier to be mapped.
     * @param category the category of the identity to map.
     * @param reply the reply containing the category of the identity.
     * 
     * @return the newly mapped identity with the given identifier.
     * 
     * @ensure return.getCategory().equals(category) : "The category of the returned identity equals the given category.";
     */
    @NonCommitting
    public static @Nonnull Identity mapIdentity(@Nonnull Identifier identifier, @Nonnull Category category, @Nullable Reply reply) throws SQLException {
        if (isMapped(identifier)) {
            final @Nonnull Identity identity =  identifiers.get(identifier);
            if (!identity.getCategory().equals(category)) throw new SQLException("The identifier " + identifier + " should have been mapped with the category " + category + " but has already been mapped with the category " + identity.getCategory() + ".");
            return identity;
        } else {
            try (@Nonnull Statement statement = Database.createStatement()) {
                final long key = Database.executeInsert(statement, "INSERT INTO general_identity (category, address, reply) VALUES (" + category + ", " + identifier + ", " + reply + ")");
                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + identifier + ", " + key + ")");
                LOGGER.log(Level.INFORMATION, "The identity with the identifier " + identifier + " was succesfully mapped.");
                // The identity is not added to the map since the transaction might be rolled back later on.
                return createIdentity(category, key, identifier);
            }
        }
    }
    
    /**
     * Maps the given host identifier to a host identity.
     * This method should only be called by {@link Host}.
     * 
     * @param identifier the identifier of the host to map.
     * 
     * @return the host identity of the mapped identifier.
     */
    @NonCommitting
    public static @Nonnull HostIdentity mapHostIdentity(@Nonnull HostIdentifier identifier) throws SQLException {
        try {
            return mapIdentity(identifier, Category.HOST, null).toHostIdentity();
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("The host with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given identifier to a syntactic type.
     * 
     * @param identifier the non-host identifier to map.
     * 
     * @return the syntactic type of the mapped identifier.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     */
    static @Nonnull SyntacticType mapSyntacticType(@Nonnull InternalNonHostIdentifier identifier) {
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            final @Nonnull SyntacticType type = mapIdentity(identifier, Category.SYNTACTIC_TYPE, null).toSyntacticType();
            numbers.put(type.getNumber(), type);
            identifiers.put(identifier, type);
            return type;
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            try { Database.rollback(); } catch (@Nonnull SQLException exc) { throw new InitializationError("Could not rollback.", exc); }
            throw new InitializationError("The syntactic type with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given identifier to a semantic type.
     * 
     * @param identifier the non-host identifier to map.
     * 
     * @return the semantic type of the mapped identifier.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     */
    static @Nonnull SemanticType mapSemanticType(@Nonnull InternalNonHostIdentifier identifier) {
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            final @Nonnull SemanticType type = mapIdentity(identifier, Category.SEMANTIC_TYPE, null).toSemanticType();
            numbers.put(type.getNumber(), type);
            identifiers.put(identifier, type);
            return type;
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            try { Database.rollback(); } catch (@Nonnull SQLException exc) { throw new InitializationError("Could not rollback.", exc); }
            throw new InitializationError("The semantic type with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given external identifier to an external person.
     * 
     * @param identifier the identifier of the external identifier to map.
     * 
     * @return the external person of the mapped identifier.
     */
    @NonCommitting
    private static @Nonnull ExternalPerson mapExternalIdentity(@Nonnull ExternalIdentifier identifier) throws SQLException, InvalidEncodingException {
        return mapIdentity(identifier, identifier.getCategory(), null).toExternalPerson();
    }
    
    
    /**
     * Merges the given identities into the new identity.
     * (This method should only be called by {@link AccountInitialize}.)
     * 
     * @param identities the identities which are to be merged.
     * @param newIdentity the new identity of the given identities.
     */
    @NonCommitting
    public static void mergeIdentities(@Nonnull ReadonlyList<NonHostIdentity> identities, @Nonnull InternalNonHostIdentity newIdentity) throws SQLException {
        final long newNumber = newIdentity.getNumber();
        try (@Nonnull Statement statement = Database.createStatement()) {
            for (final @Nonnull NonHostIdentity identity : identities) {
                final long oldNumber = identity.getNumber();
                if (oldNumber != newNumber) {
                    updateReferences(statement, oldNumber, newNumber);
                    statement.executeUpdate("UPDATE general_identifier SET identity = " + newNumber + " WHERE identity = " + oldNumber);
                    statement.executeUpdate("DELETE FROM general_identity WHERE identity = " + oldNumber);
                    LOGGER.log(Level.INFORMATION, "The identity of " + identity.getAddress() + " was succesfully merged into " + newIdentity.getAddress() + ".");
                    unmap(identity);
                }
            }
        }
        if (identities.size() > 1) Cache.invalidateCachedAttributeValues(newIdentity);
    }
    
    /**
     * Establishes the identity of the given internal non-host identifier by checking
     * its existence and requesting its category, predecessors and successor.
     * 
     * @param identifier the identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given identifier.
     * 
     * @throws IdentityNotFoundException if no identity with the given identifier was found.
     * 
     * @require isNotMapped(identifier) : "The identifier is not mapped.";
     */
    @NonCommitting
    private static @Nonnull InternalNonHostIdentity establishInternalNonHostIdentity(@Nonnull InternalNonHostIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        assert isNotMapped(identifier) : "The identifier is not mapped.";
        
        if (Server.hasHost(identifier.getHostIdentifier())) throw new IdentityNotFoundException(identifier);
        
        // Query the identity of the given identifier.
        final @Nonnull IdentityReply reply;
        try {
            reply = new IdentityQuery(identifier).sendNotNull();
        } catch (@Nonnull PacketException exception) {
            if (exception.getError() == PacketError.IDENTIFIER) throw new IdentityNotFoundException(identifier); else throw exception;
        }
        final @Nonnull Category category = reply.getCategory();
        
        // Store all the predecessors of the given identifier into the database.
        final @Nonnull ReadonlyPredecessors predecessors = reply.getPredecessors();
        predecessors.set(identifier, reply);
        
        // Check that all the claimed and mapped predecessors have the right category, the indicated predecessors and do link back.
        final @Nonnull ReadonlyList<NonHostIdentity> identities = predecessors.getIdentities();
        for (final @Nonnull NonHostIdentity identity : identities) {
            final @Nonnull NonHostIdentifier address = identity.getAddress();
            final @Nonnull String message = "The claimed predecessor " + address + " of " + identifier;
            if (!(identity.getCategory().isExternalPerson() && category.isInternalPerson() || identity.getCategory() == category)) throw new InvalidDeclarationException(message + " has a wrong category.", identifier, reply);
            final @Nonnull Predecessor predecessor = new Predecessor(address);
            if (!predecessors.contains(predecessor)) throw new InvalidDeclarationException(message + " has other predecessors.", identifier, reply);
            if (!Successor.getReloaded(address).equals(identifier)) throw new InvalidDeclarationException(message + " does not link back.", identifier, reply);
        }
        
        final @Nonnull InternalNonHostIdentity identity;
        // Relocate the existing identity in case there is exactly one internal predecessor.
        if (identities.size() == 1 && identities.getNotNull(0).getCategory().isInternalNonHostIdentity()) {
            identity = identities.getNotNull(0).toInternalNonHostIdentity();
            try (@Nonnull Statement statement = Database.createStatement()) {
                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + identifier + ", " + identity + ")");
                statement.executeUpdate("UPDATE general_identity SET address = " + identifier + " WHERE identity = " + identity);
            }
            unmap(identity);
            LOGGER.log(Level.INFORMATION, "The identity of " + identity.getAddress() + " was succesfully relocated to " + identifier + ".");
            
        // Create a new identity and merge existing predecessors into this new identity.
        } else {
            identity = mapIdentity(identifier, category, reply).toInternalNonHostIdentity();
            if (identities.size() > 1 && !category.isInternalPerson()) throw new InvalidDeclarationException("Only internal persons may have more than one predecessor.", identifier, reply);
            mergeIdentities(identities, identity);
        }
        LOGGER.log(Level.INFORMATION, "The identity of " + identifier + " was succesfully established.");
        
        // Store the successor of the given identifier into the database if available.
        final @Nullable InternalNonHostIdentifier successor = reply.getSuccessor();
        if (successor != null) {
            Successor.set(identifier, successor, reply);
            if (!successor.getIdentity().equals(identifier.getIdentity())) throw new InvalidDeclarationException("The claimed successor " + successor + " of " + identifier + " does not link back.", identifier, reply);
            return successor.getIdentity();
        } else {
            return identity;
        }
    }
    
    /**
     * Establishes the identity of the given external identifier by mapping it.
     * 
     * @param identifier the identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given identifier.
     * 
     * @require isNotMapped(identifier) : "The identifier is not mapped.";
     */
    @NonCommitting
    private static @Nonnull Person establishExternalIdentity(@Nonnull ExternalIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        assert isNotMapped(identifier) : "The identifier is not mapped.";
        
        final @Nonnull Person person = mapExternalIdentity(identifier);
        try {
            final @Nonnull InternalNonHostIdentity identity = Successor.getReloaded(identifier).getIdentity();
            if (!identity.equals(identifier.getIdentity())) throw new InvalidDeclarationException("The claimed successor " + identity.getAddress() + " of " + identifier + " does not link back.", identifier, null);
            return identity.toPerson();
        } catch (@Nonnull PacketException exception) {
            if (exception.getError() == PacketError.EXTERNAL) return person;
            else throw exception;
        }
    }
    
    /**
     * Returns the identity of the given identifier.
     * The identity is also established if required.
     * (Only to be called from the identifier package.)
     * 
     * @param identifier the identifier of interest.
     * 
     * @return the identity of the given identifier.
     */
    @NonCommitting
    public static @Nonnull Identity getIdentity(@Nonnull Identifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        if (isMapped(identifier)) {
            return identifiers.get(identifier);
        } else {
            if (identifier instanceof HostIdentifier) {
                return Cache.establishHostIdentity((HostIdentifier) identifier);
            } else if (identifier instanceof InternalNonHostIdentifier) {
                return establishInternalNonHostIdentity((InternalNonHostIdentifier) identifier);
            } else { assert identifier instanceof ExternalIdentifier;
                return establishExternalIdentity((ExternalIdentifier) identifier);
            }
        }
    }
    
}
