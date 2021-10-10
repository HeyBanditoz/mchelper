package io.banditoz.mchelper.commands;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MinecraftCoordinateCommandTests extends BaseCommandTest {
    private final OverworldCommand oc;
    private final NetherCommand nc;
    private final EangleCommand ec;

    public MinecraftCoordinateCommandTests() {
        this.oc = spy(new OverworldCommand());
        this.nc = spy(new NetherCommand());
        this.ec = spy(new EangleCommand());
    }

    @Test
    public void testOverworldCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!overworld", "2384", "1202"});
        oc.onCommand(ce);
        Assertions.assertThat(stringCaptor.getValue()).isEqualTo("19072, 9616");
    }

    @Test
    public void testNetherCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!nether", "19348", "-2364"});
        nc.onCommand(ce);
        Assertions.assertThat(stringCaptor.getValue()).isEqualTo("2418, -295");
    }

    @Test
    public void testEangleCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!eangle", "23", "96", "1952", "-1956"});
        ec.onCommand(ce);
        Assertions.assertThat(stringCaptor.getValue()).isEqualTo("**Yaw:** -136.8 **Distance:** 2816.3");
    }
}
