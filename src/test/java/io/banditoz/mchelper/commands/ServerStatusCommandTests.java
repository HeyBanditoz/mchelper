package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@InjectTest
class ServerStatusCommandTests extends BaseCommandTest {
    @Inject
    ServerStatusCommand ssc;

    @BeforeEach
    void beforeAll() throws Exception {
        doNothing().when(ce).sendEmbedThumbnailReply(embedCaptor.capture(), any(), any());
    }

    @Test
    @Tag("external-integration")
    void testMinecraftServerStatusCommand() throws Exception {
        setArgs("mc.hypixel.net");
        ssc.onCommand(ce);
        assertThat(embedCaptor.getValue().getColor()).isEqualTo(Color.GREEN);
    }

    @Test
    void testMinecraftServerStatusCommandBadServer() throws Exception {
        setArgs("127.0.0.1");
        ssc.onCommand(ce);
        assertThat(embedCaptor.getValue().getColor()).isEqualTo(Color.RED);
    }
}
