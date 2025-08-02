package io.banditoz.mchelper.commands;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test(dependsOnGroups = {"BalanceCommandTests"})
public class BaltopCommandTests extends BaseCommandTest {
    private final BaltopCommand bc = new BaltopCommand(AM);

    @Test
    public void testBaltopCommand() throws Exception {
        bc.onCommand(ce);
        assertThat(embedCaptor.getValue().getAuthor().getName()).isEqualTo("Money leaderboard for QA Guild");
    }
}
