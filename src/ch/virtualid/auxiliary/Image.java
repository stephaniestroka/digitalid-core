package ch.virtualid.auxiliary;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.DataWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

/**
 * This class models images in the Extensible Data Format (XDF).
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Image implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code image@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("image@virtualid.ch").load(DataWrapper.TYPE);
    
    
    /**
     * Stores the buffered image.
     */
    private final @Nonnull BufferedImage image;
    
    /**
     * Creates a new image.
     * 
     * @param image the buffered image.
     */
    public Image(@Nonnull BufferedImage image) {
        this.image = image;
    }
    
    /**
     * Creates a new image from the given block.
     * 
     * @param block the block containing the image.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Image(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        try {
            image = ImageIO.read(new DataWrapper(block).getDataAsInputStream());
        } catch (@Nonnull IOException exception) {
            throw new InvalidEncodingException("Failed to read an image from a block.", exception);
        }
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        try (@Nonnull ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return new DataWrapper(TYPE, output.toByteArray()).toBlock();
        } catch (@Nonnull IOException exception) {
            throw new ShouldNeverHappenError("The image could not be written to a byte array.", exception);
        }
    }
    
    
    /**
     * Returns the buffered image.
     * <p>
     * <em>Important</em>: Do not write to the returned image!
     * 
     * @return the buffered image.
     */
    @Pure
    public @Nonnull BufferedImage getImage() {
        return image;
    }
    
    
    /**
     * Returns whether this image is a square with the given length.
     * 
     * @param length the length of the image's width and height.
     * 
     * @return whether this image is a square with the given length.
     */
    @Pure
    public boolean isSquare(int length) {
        return image.getWidth() == length && image.getHeight() == length;
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Database.getConfiguration().BLOB();
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Image get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException, InvalidEncodingException {
        return new Image(Block.get(TYPE, resultSet, columnIndex));
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        toBlock().set(preparedStatement, parameterIndex);
    }
    
}
