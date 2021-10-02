package io.banditoz.mchelper.commands.logic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requires {
    /**
     * @return The method that needs to return non-null or not-default from {@link io.banditoz.mchelper.utils.Settings}
     * for this command to be registered.
     */
    String settingsMethod() default "";
    /**
     * @return Whether the database is required to be configured for this command to be registered.
     */
    boolean database() default false;
}
