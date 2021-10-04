package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;

import static io.banditoz.mchelper.commands.logic.CommandUtils.commandArgs;
import static io.banditoz.mchelper.commands.logic.CommandUtils.generateCommandArgsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandParserTests {
    @Test
    public void testCommandParser() {
        String[] test = new String[]{"this", "is", "a", "test"};
        assertArrayEquals(test, commandArgs("**<test>** this is a test")); // the name is wrapped in <> from bots that bridge chat services
        assertArrayEquals(test, commandArgs("this is a test"));
        assertArrayEquals(test, commandArgs("**<test>** this\n is a test"));
        assertArrayEquals(test, commandArgs("this\n is a test"));

        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        Message m = mock(Message.class);
        when(event.getMessage()).thenReturn(m);
        when(m.getContentDisplay()).thenReturn("!this   is    argument test 12,3\nhello\n\n\nworld");
        assertEquals("is argument test 12,3 hello world", generateCommandArgsString(event, false));
    }
}
