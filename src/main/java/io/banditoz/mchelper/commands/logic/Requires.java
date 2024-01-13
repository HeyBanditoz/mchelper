package io.banditoz.mchelper.commands.logic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requires {
    /**
     * @return The config key that needs to be non-null for this command to be registered.
     */
    String config() default "";
    /**
     * @return Whether the database is required to be configured for this command to be registered.
     */
    boolean database() default false;
}
