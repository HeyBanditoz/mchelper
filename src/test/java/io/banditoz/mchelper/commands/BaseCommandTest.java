package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandTests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * The base class that all command-based tests should extend. If you want to add functionality to a command, if it can
 * be shared across multiple commands, you should add it here.
 */
public abstract class BaseCommandTest {
    /** The mocked {@link CommandEvent}. */
    protected final CommandEvent ce;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendReply(String)} */
    protected final ArgumentCaptor<String> stringCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendEmbedReply(MessageEmbed)} */
    protected final ArgumentCaptor<MessageEmbed> embedCaptor;
    /** The {@link MCHelper} instance. */
    protected final MCHelper mcHelper;

    public BaseCommandTest() {
        this.ce = mock(CommandEvent.class);
        this.stringCaptor = ArgumentCaptor.forClass(String.class);
        this.embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        doNothing().when(ce).sendReply(stringCaptor.capture());
        doNothing().when(ce).sendEmbedReply(embedCaptor.capture());
        doNothing().when(ce).sendPastableReply(stringCaptor.capture());
        when(ce.getEvent()).thenReturn(mock(MessageReceivedEvent.class));
        when(ce.getEvent().getJDA()).thenReturn(mock(JDA.class));
        Guild g = Mocks.getMockedGuild();
        when(ce.getGuild()).thenReturn(g);
        this.mcHelper = mock(MCHelper.class);
        Mockito.when(mcHelper.getSettings()).thenReturn(CommandTests.getMockSettings());
    }
}
