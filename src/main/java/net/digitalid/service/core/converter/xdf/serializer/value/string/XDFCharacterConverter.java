package net.digitalid.service.core.converter.xdf.serializer.value.string;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.string.String01Wrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFCharacterConverter extends XDFConverter<Character> {
    
    @Override
    protected @Nonnull Character recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return String01Wrapper.decode(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        assert (object instanceof Character) : "The object is an instance of a character type.";

        final @Nonnull Character character = (Character) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, String01Wrapper.XDF_TYPE);
        return String01Wrapper.encode(semanticType, character);
    }
}
