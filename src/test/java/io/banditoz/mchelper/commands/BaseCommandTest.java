package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.MCHelperTestImpl;
import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandTests;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;

import java.util.Collections;
import java.util.List;

import static io.banditoz.mchelper.Whitebox.setInternalState;
import static org.mockito.Mockito.*;

/**
 * The base class that all command-based tests should extend. If you want to add functionality to a test, if it can be
 * shared across multiple tests, you should add it here.
 */
public abstract class BaseCommandTest {
    /** The mocked {@link CommandEvent}. */
    protected final CommandEvent ce;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendReply(String)} */
    protected final ArgumentCaptor<String> stringCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendEmbedReply(MessageEmbed)} */
    protected final ArgumentCaptor<MessageEmbed> embedCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendEmbedPaginatedReply(List)} */
    protected final ArgumentCaptor<List<MessageEmbed>> embedsCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link MessageChannel#sendMessage(Message)} */
    protected final ArgumentCaptor<Message> messageCaptor;
    /** The {@link MCHelper} instance. */
    protected final MCHelper mcHelper;
    protected final MessageReceivedEvent mre;
    protected final Message m;
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
        mre = mock(MessageReceivedEvent.class);
        m = mock(Message.class);
        User u = Mocks.getMockedMember().getUser();
        Member me = Mocks.getMockedMember();
        JDA j = mock(JDA.class);
        this.stringCaptor = ArgumentCaptor.forClass(String.class);
        this.embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        this.embedsCaptor = ArgumentCaptor.forClass(List.class);
        this.messageCaptor = ArgumentCaptor.forClass(Message.class);
        doNothing().when(ce).sendReply(stringCaptor.capture());
        doNothing().when(ce).sendEmbedReply(embedCaptor.capture());
        doNothing().when(ce).sendEmbedPaginatedReply(embedsCaptor.capture());
        doNothing().when(ce).sendPastableReply(stringCaptor.capture());
        when(ce.getMentionedUsers()).thenReturn(Collections.emptyList());
        when(ce.getMentionedMembers()).thenReturn(Collections.emptyList());
        when(j.getRegisteredListeners()).thenReturn(Collections.emptyList());
        when(mre.getMessage()).thenReturn(m);
        when(mre.getAuthor()).thenReturn(u);
        when(mre.getMember()).thenReturn(me);
        MessageChannelUnion mc = mock(MessageChannelUnion.class);
        when(mre.getChannel()).thenReturn(mc);
        MessageAction ma = mock(MessageAction.class);
        when(mc.sendMessage(stringCaptor.capture())).thenReturn(ma);
        when(mc.sendMessage(messageCaptor.capture())).thenReturn(ma);
        when(m.addReaction(any())).thenReturn(mock(RestAction.class));
        when(m.clearReactions()).thenReturn(mock(RestAction.class));
        when(ce.getEvent()).thenReturn(mre);
        when(ce.getEvent().getJDA()).thenReturn(j);
        when(ce.getDatabase()).thenReturn(DB);
        Guild g = Mocks.getMockedGuild();
        when(ce.getGuild()).thenReturn(g);
        this.mcHelper = spy(MCHelperTestImpl.class);
        when(mcHelper.getSettings()).thenReturn(CommandTests.getMockSettings());
        when(mcHelper.getDatabase()).thenReturn(DB);
        when(mcHelper.getAccountManager()).thenReturn(AM);
        when(mcHelper.getJDA()).thenReturn(j);
        when(ce.getMCHelper()).thenReturn(mcHelper);
    }

    /**
     * Sets the command arguments for this mocked {@link CommandEvent}.<br>
     * <i>Note!</i> Do not include the command name! (i.e. !command) It will be inserted at the beginning.<br>
     * If you need to call this method more than once in a single test, call {@link BaseCommandTest#clearArgs()}.
     *
     * @param s The arguments to use.
     */
    protected void setArgs(String s) {
        s = "!commandundertest" + (s.isEmpty() ? "" : " ") + s;
        setInternalState(ce, "EVENT", mre);
        when(m.getContentRaw()).thenReturn(s);
        when(m.getContentDisplay()).thenReturn(s);
        when(ce.getCommandArgsString()).thenCallRealMethod();
        when(ce.getCommandArgs()).thenCallRealMethod();
        when(ce.getRawCommandArgs()).thenCallRealMethod();
        when(ce.getRawCommandArgsString()).thenCallRealMethod();
        when(ce.getCommandArgsWithoutName()).thenCallRealMethod();
    }

    /**
     * Clears and resets the command arguments for this test method.
     * <i>Note!</i> This is called after the end of each method.
     */
    @BeforeMethod
    protected void clearArgs() {
        when(m.getContentRaw()).thenReturn("!commandundertest");
        when(m.getContentDisplay()).thenReturn("!commandundertest");
        setInternalState(ce, "COMMAND_ARGS_STRING", null);
        setInternalState(ce, "RAW_COMMAND_ARGS_STRING", null);
        setInternalState(ce, "COMMAND_ARGS", null);
        setInternalState(ce, "RAW_ARGS", null);
    }
}
