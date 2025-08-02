package io.banditoz.mchelper.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import io.banditoz.mchelper.database.dao.CoordsDaoImpl;
import org.testng.annotations.Test;

@Test(dependsOnGroups = {"DatabaseInitializationTests"})
public class CoordCommandTests extends BaseCommandTest {
    private final CoordCommand cc = spy(new CoordCommand(new CoordsDaoImpl(DB)));

    @Test
    public void testCoordinateAdd() throws Exception {
        setArgs("add TestBase 100 -100");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("100, -100 saved.");
    }

    @Test(dependsOnMethods = {"testCoordinateAdd"})
    public void testCoordinateGetOne() throws Exception {
        setArgs("show TestBase");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("100, -100");
    }

    @Test(dependsOnMethods = {"testCoordinateAdd"})
    public void testCoordinateGetAll() throws Exception {
        setArgs("list");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("TestBase: 100, -100");
    }

    @Test(dependsOnMethods = {"testCoordinateGetAll"})
    public void testCoordinateDelete() throws Exception {
        setArgs("remove test");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Deleted.");
    }
}
