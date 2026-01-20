package io.banditoz.mchelper.commands.logic.slash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Parameter for a slash command for Java method parameters. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    /** Default is the parameter name, if empty string (not overridden.) */
    String name() default "";
    /** Default is the parameter name, if empty string (not overridden.)*/
    String desc() default "";
}
