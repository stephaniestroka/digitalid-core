package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public class EmptyWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("empty@test.digitalid.net").load(EmptyWrapper.TYPE);
        new EmptyWrapper(new EmptyWrapper(TYPE).toBlock());
    }
    
}
