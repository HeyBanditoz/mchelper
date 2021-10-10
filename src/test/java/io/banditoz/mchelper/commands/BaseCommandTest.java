package io.banditoz.mchelper.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandTests;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;

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
    protected final static Database DB;
    protected final static AccountManager AM;

    static {
        if (Database.isConfigured()) {
            DB = new Database();
            AM = new AccountManager(DB);
        }
        else {
            DB = null;
            AM = null;
        }
    }

    public BaseCommandTest() {
        this.ce = mock(CommandEvent.class);
        MessageReceivedEvent mre = mock(MessageReceivedEvent.class);
        Message m = mock(Message.class);
        User u = Mocks.getMockedMember().getUser();
        this.stringCaptor = ArgumentCaptor.forClass(String.class);
        this.embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        doNothing().when(ce).sendReply(stringCaptor.capture());
        doNothing().when(ce).sendEmbedReply(embedCaptor.capture());
        doNothing().when(ce).sendPastableReply(stringCaptor.capture());
        when(mre.getMessage()).thenReturn(m);
        when(mre.getAuthor()).thenReturn(u);
        when(m.getMentionedMembers()).thenReturn(Collections.emptyList());
        when(ce.getEvent()).thenReturn(mre);
        when(ce.getEvent().getJDA()).thenReturn(mock(JDA.class));
        when(ce.getDatabase()).thenReturn(DB);
        Guild g = Mocks.getMockedGuild();
        when(ce.getGuild()).thenReturn(g);
        this.mcHelper = mock(MCHelper.class);
        when(mcHelper.getSettings()).thenReturn(CommandTests.getMockSettings());
        when(mcHelper.getDatabase()).thenReturn(DB);
        when(mcHelper.getAccountManager()).thenReturn(AM);
        Mockito.when(mcHelper.getObjectMapper()).thenReturn(new ObjectMapper());
        when(ce.getMCHelper()).thenReturn(mcHelper);
    }
}
