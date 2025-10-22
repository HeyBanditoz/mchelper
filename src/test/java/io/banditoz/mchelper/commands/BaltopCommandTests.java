package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.money.AccountManager;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class BaltopCommandTests extends BaseCommandTest {
    @Inject
    BaltopCommand bc;

    @Inject
    AccountManager am;

    @BeforeEach
    void clear() {
        truncate("accounts", "transactions");
    }

    @Test
    void testBaltopCommand() throws Exception {
        // create account
        am.queryBalance(ce.getUser().getIdLong(), true);
        bc.onCommand(ce);
        MessageEmbed embed = embedCaptor.getValue();
        assertThat(embed.getAuthor().getName()).isEqualTo("Money leaderboard for QA Guild");
        assertThat(embed.getDescription()).contains("""
                ```
                Rank  Name
                1.    NFoo                 $1,000
                __________________________________
                Total                      $1,000
                ```""");
    }
}
