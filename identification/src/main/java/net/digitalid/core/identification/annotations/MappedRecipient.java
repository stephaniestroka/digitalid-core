package net.digitalid.core.identification.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This annotation indicates that a method should only be invoked on a {@link Identifier#isMapped() mapped} {@link Identifier identifier}.
 * 
 * @see Mapped
 */
@Documented
@Target(ElementType.METHOD)
// TODO: Implement a value validator instead: @TargetTypes(Identifier.class)
@Retention(RetentionPolicy.CLASS)
public @interface MappedRecipient {}
