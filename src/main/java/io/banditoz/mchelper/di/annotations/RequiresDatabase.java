package io.banditoz.mchelper.di.annotations;

import io.avaje.inject.RequiresProperty;

/**
 * Marks this {@link jakarta.inject.Singleton} requires the database to be configured (just looks for DB property)
 * for the Singleton to be created and available for use.
 */
@RequiresProperty(value = "DB")
public @interface RequiresDatabase {
}
