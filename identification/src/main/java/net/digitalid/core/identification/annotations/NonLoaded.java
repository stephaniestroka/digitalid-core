package net.digitalid.core.identification.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identity.Type;

/**
 * This annotation indicates that a {@link Type type} is {@link Type#isNotLoaded() not loaded}.
 * 
 * @see Loaded
 */
@Documented
// TODO: Implement a value validator instead: @TargetTypes(Type.class)
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.CLASS)
public @interface NonLoaded {}
