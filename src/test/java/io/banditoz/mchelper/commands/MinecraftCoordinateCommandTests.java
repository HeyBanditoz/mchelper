package io.banditoz.mchelper.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("19072, 9616", stringCaptor.getValue());
    }

    @Test
    public void testNetherCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!nether", "19348", "-2364"});
        nc.onCommand(ce);
        assertEquals("2418, -295", stringCaptor.getValue());
    }

    @Test
    public void testEangleCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!eangle", "23", "96", "1952", "-1956"});
        ec.onCommand(ce);
        assertEquals("**Yaw:** -136.8 **Distance:** 2816.3", stringCaptor.getValue());
    }
}
