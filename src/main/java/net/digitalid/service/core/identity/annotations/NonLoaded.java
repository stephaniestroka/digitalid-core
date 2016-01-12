package net.digitalid.service.core.identity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.identity.Type;
import net.digitalid.utility.annotations.meta.TargetType;

/**
 * This annotation indicates that a {@link Type type} is {@link Type#isNotLoaded() not loaded}.
 * 
 * @see Loaded
 */
@Documented
@TargetType(Type.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface NonLoaded {}
