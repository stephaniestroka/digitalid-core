package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.cryptography.SymmetricKey;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.ServerSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link EncryptionWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class EncryptionWrapperTest extends ServerSetup {
    
    @Test
    public void testWrapping() throws Exception {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("encryption@test.digitalid.net").load(EncryptionWrapper.TYPE, STRING);
        
        final @Nonnull Block[] blocks = new Block[] {null, new StringWrapper(STRING, "This is a secret message.").toBlock()};
        final @Nullable SymmetricKey[] symmetricKeys = new SymmetricKey[] {null, new SymmetricKey()};
        
        for (int i = 1; i < 3; i++) {
//            System.out.println("\nRound " + i + ":\n");
            for (final @Nullable Block block : blocks) {
                for (final @Nullable SymmetricKey symmetricKey : symmetricKeys) {
//                    System.out.println("Block: " + block + "; Symmetric Key: " + symmetricKey);
                    
                    // From client to host:
                    @Nonnull Block cipherBlock = new EncryptionWrapper(TYPE, block, getRecipient(), symmetricKey).toBlock();
//                    System.out.println("–> From client to host: " + cipherBlock);
                    @Nonnull EncryptionWrapper encryption = new EncryptionWrapper(cipherBlock, null);
                    Assert.assertEquals(block, encryption.getElement());
                    Assert.assertEquals(getRecipient(), encryption.getRecipient());
                    Assert.assertEquals(symmetricKey, encryption.getSymmetricKey());
                    
                    // From host to client:
                    cipherBlock = new EncryptionWrapper(TYPE, block, null, symmetricKey).toBlock();
//                    System.out.println("–> From host to client:" + cipherBlock);
                    encryption = new EncryptionWrapper(cipherBlock, symmetricKey);
                    Assert.assertEquals(block, encryption.getElement());
                    Assert.assertEquals(null, encryption.getRecipient());
                    Assert.assertEquals(symmetricKey, encryption.getSymmetricKey());
                }
            }
        }
    }
    
}
