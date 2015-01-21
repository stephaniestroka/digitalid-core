package ch.virtualid.attribute;

import ch.virtualid.cache.Cache;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.AttributeNotFoundException;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.setup.IdentitySetup;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Attribute attribute} with its {@link Action actions}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AttributeTest extends IdentitySetup {
    
    private static final @Nonnull String NAME = "Test Person";
        
    @Test
    public void _01_testValueReplace() throws SQLException, IOException, PacketException, ExternalException {
        print("_01_testValueReplace");
        final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeType.NAME);
        attribute.setValue(new UncertifiedAttributeValue(new StringWrapper(AttributeType.NAME, NAME)));
        attribute.reset(); // Not necessary but I want to test the database state.
        final @Nullable AttributeValue attributeValue = attribute.getValue();
        Assert.assertNotNull(attributeValue);
        Assert.assertEquals(NAME, new StringWrapper(attributeValue.getContent()).getString());
    }
    
    @Test(expected = AttributeNotFoundException.class)
    public void _02_testNonPublicAccess() throws SQLException, IOException, PacketException, ExternalException {
        print("_02_testNonPublicAccess");
        Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeType.NAME, false);
    }
    
    @Test
    public void _03_testVisibilityReplace() throws SQLException, IOException, PacketException, ExternalException {
        print("_03_testVisibilityReplace");
        final @Nonnull PassiveExpression passiveExpression = new PassiveExpression(getRole(), "everybody");
        final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeType.NAME);
        attribute.setVisibility(passiveExpression);
        attribute.reset(); // Not necessary but I want to test the database state.
        Assert.assertEquals(passiveExpression, attribute.getVisibility());
    }
    
    @Test
    public void _04_testPublicAccess() throws SQLException, IOException, PacketException, ExternalException {
        print("_04_testPublicAccess");
        final @Nonnull Block content = Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeType.NAME, false);
        Assert.assertEquals(NAME, new StringWrapper(content).getString());
        Database.commit();
    }
    
}
