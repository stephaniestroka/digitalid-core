package ch.virtualid.identifier;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * This class models internal identifiers.
 * 
 * @see HostIdentifier
 * @see InternalNonHostIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class InternalIdentifier extends IdentifierClass implements Immutable {
    
    /**
     * The pattern that valid internal identifiers have to match.
     */
    private static final Pattern PATTERN = Pattern.compile("(?:(?:[a-z0-9]+(?:[._-][a-z0-9]+)*)?@)?[a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z][a-z]+");
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return IdentifierClass.isConforming(string) && PATTERN.matcher(string).matches() && string.length() - string.indexOf("@") < 40;
    }
    
    /**
     * Returns whether the given string is a valid internal identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid internal identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains("@") ? InternalNonHostIdentifier.isValid(string) : HostIdentifier.isValid(string);
    }
    
    /**
     * Returns a new internal identifier with the given string.
     * 
     * @param string the string of the new internal identifier.
     * 
     * @return a new internal identifier with the given string.
     * 
     * @require isValid(string) : "The string is a valid internal identifier.";
     */
    @Pure
    public static @Nonnull InternalIdentifier create(@Nonnull String string) {
        assert isValid(string) : "The string is a valid internal identifier.";
        
        return string.contains("@") ? new InternalNonHostIdentifier(string) : new HostIdentifier(string);
    }
    
    
    /**
     * Creates an internal identifier with the given string.
     * 
     * @param string the string of the internal identifier.
     * 
     * @require isValid(string) : "The string is a valid internal identifier.";
     */
    InternalIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid internal identifier.";
    }
    
    
    @Pure
    @Override
    @DoesNotCommit
    public abstract @Nonnull InternalIdentity getMappedIdentity() throws SQLException;
    
    @Pure
    @Override
    @DoesNotCommit
    public abstract @Nonnull InternalIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Returns whether an identity with this internal identifier exists.
     * 
     * @return whether an identity with this internal identifier exists.
     */
    @Pure
    @DoesNotCommit
    public final boolean exists() throws SQLException, IOException, PacketException, ExternalException {
        try {
            Mapper.getIdentity(this);
            return true;
        } catch (@Nonnull IdentityNotFoundException exception) {
            return false;
        }
    }
    
    /**
     * Returns whether an identity with this internal identifier does not exist.
     * 
     * @return whether an identity with this internal identifier does not exist.
     */
    @Pure
    @DoesNotCommit
    public final boolean doesNotExist() throws SQLException, IOException, PacketException, ExternalException {
        return !exists();
    }
    
    
    /**
     * Returns the host part of this internal identifier.
     * 
     * @return the host part of this internal identifier.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getHostIdentifier();
    
}
