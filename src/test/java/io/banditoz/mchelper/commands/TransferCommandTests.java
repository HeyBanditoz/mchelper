package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.money.MoneyException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Test(dependsOnGroups = {"BalanceCommandTests"})
public class TransferCommandTests extends BaseCommandTest {
    private final TransferCommand tc = new TransferCommand();
    private final Member member = Mocks.getMockedMember();
    private final User otherUser = Mocks.getDifferentMockedMember().getUser();

    public TransferCommandTests() {
        when(ce.getEvent().getMessage().getMentionedMembers()).thenReturn(List.of(member));
        when(ce.getRawCommandArgs()).thenReturn(new String[]{"!transfer", "", "400"});
    }

    @Test
    public void testTransferCommand() throws Exception {
        when(ce.getEvent().getAuthor()).thenReturn(otherUser);
        when(ce.getEvent().getMessage().getMentionedMembers()).thenReturn(List.of(member));
        tc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Transfer of $400 to <@!163094867910590464> complete. You have $600 left.");
    }

    @Test(dependsOnMethods = {"testTransferCommand"})
    public void cannotTransferNegativeAmount() {
        when(ce.getRawCommandArgs()).thenReturn(new String[]{"!transfer", "", "-400"});
        assertThatThrownBy(() -> tc.onCommand(ce)).isInstanceOf(MoneyException.class); // can't transfer negative amount
    }

    @Test(dependsOnMethods = {"testTransferCommand"})
    public void testCannotTransferToSelf() {
        User u = member.getUser();
        when(ce.getEvent().getAuthor()).thenReturn(u);
        when(ce.getEvent().getMessage().getMentionedMembers()).thenReturn(List.of(member));
        assertThatThrownBy(() -> tc.onCommand(ce)).isInstanceOf(MoneyException.class); // can't transfer to self
    }
}
