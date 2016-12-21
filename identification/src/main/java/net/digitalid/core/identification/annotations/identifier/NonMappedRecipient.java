package net.digitalid.core.identification.annotations.identifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This annotation indicates that a method should only be invoked on a non-mapped {@link Identifier identifier}.
 * 
 * TODO: Identifier#isMapped() no longer exists.
 * 
 * @see MappedRecipient
 */
@Documented
@Target(ElementType.METHOD)
// TODO: Implement a method validator instead: @TargetTypes(Identifier.class)
@Retention(RetentionPolicy.CLASS)
public @interface NonMappedRecipient {}
