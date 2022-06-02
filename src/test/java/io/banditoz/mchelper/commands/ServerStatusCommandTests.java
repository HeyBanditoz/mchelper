package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServerStatusCommandTests extends BaseCommandTest {
    private final ServerStatusCommand ssc;

    public ServerStatusCommandTests() throws Exception {
        this.ssc = spy(new ServerStatusCommand());
        doNothing().when(ce).sendEmbedThumbnailReply(embedCaptor.capture(), any(), any());
        when(ce.getMCHelper()).thenReturn(mcHelper);
    }

    @Test
    public void testMinecraftServerStatusCommand() throws Exception {
        setArgs("mc.hypixel.net");
        ssc.onCommand(ce);
        assertThat(embedCaptor.getValue().getColor()).isEqualTo(Color.GREEN);
    }

    @Test
    public void testMinecraftServerStatusCommandBadServer() throws Exception {
        setArgs("127.0.0.1");
        ssc.onCommand(ce);
        assertThat(embedCaptor.getValue().getColor()).isEqualTo(Color.RED);
    }
}
