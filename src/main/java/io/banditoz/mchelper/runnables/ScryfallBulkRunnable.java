package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.mtg.ScryfallBulkService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class ScryfallBulkRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ScryfallBulkRunnable.class);
    private final ScryfallBulkService scryfallBulkService;

    @Inject
    public ScryfallBulkRunnable(ScryfallBulkService scryfallBulkService) {
        this.scryfallBulkService = scryfallBulkService;
    }

    @Override
    public void run() {
        try {
            scryfallBulkService.loadBulk();
        } catch (Exception e) {
            log.error("Could not import Scryfall bulk data.", e);
        }
    }
}
