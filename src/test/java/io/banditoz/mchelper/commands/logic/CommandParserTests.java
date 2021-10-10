package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.testng.annotations.Test;

import static io.banditoz.mchelper.commands.logic.CommandUtils.commandArgs;
import static io.banditoz.mchelper.commands.logic.CommandUtils.generateCommandArgsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandParserTests {
    @Test
    public void testCommandParser() {
        String[] test = new String[]{"this", "is", "a", "test"};
        assertThat(commandArgs("**<test>** this is a test")).isEqualTo(test);
        assertThat(commandArgs("this is a test")).isEqualTo(test);
        assertThat(commandArgs("**<test>** this\n is a test")).isEqualTo(test);
        assertThat(commandArgs("this\n is a test")).isEqualTo(test);

        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        Message m = mock(Message.class);
        when(event.getMessage()).thenReturn(m);
        when(m.getContentDisplay()).thenReturn("!this   is    argument test 12,3\nhello\n\n\nworld");
        assertThat(generateCommandArgsString(event, false)).isEqualTo("is argument test 12,3 hello world");
    }
}
