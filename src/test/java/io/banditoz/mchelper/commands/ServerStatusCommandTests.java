package io.banditoz.mchelper.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServerStatusCommandTests extends BaseCommandTest {
    private final ServerStatusCommand ssc;

    public ServerStatusCommandTests() throws Exception {
        this.ssc = spy(new ServerStatusCommand());
        doNothing().when(ce).sendEmbedThumbnailReply(embedCaptor.capture(), any(), any());
        ObjectMapper om = new ObjectMapper();
        Mockito.when(mcHelper.getObjectMapper()).thenReturn(om);
        when(ce.getMCHelper()).thenReturn(mcHelper);
    }

    @Test
    public void testMinecraftServerStatusCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!status", "mc.hypixel.net"}); // this should always be up, hopefully...
        ssc.onCommand(ce);
        assertThat(embedCaptor.getValue().getColor()).isEqualTo(Color.GREEN);
    }

    @Test
    public void testMinecraftServerStatusCommandBadServer() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!status", "127.0.0.1"}); // shouldn't work
        ssc.onCommand(ce);
        assertThat(embedCaptor.getValue().getColor()).isEqualTo(Color.RED);
    }
}
