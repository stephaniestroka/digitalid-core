package net.digitalid.core.compression;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class (de-)compresses the wrapped object for conversion.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Compression<T> extends RootClass {
    
    @Pure
    public abstract @Nonnull T getObject();
    
}