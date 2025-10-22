package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.BaseTest;
import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static io.banditoz.mchelper.utils.Whitebox.setInternalState;
import static org.mockito.Mockito.*;

/**
 * The base class that all command-based tests should extend. If you want to add functionality to a test, if it can be
 * shared across multiple tests, you should add it here.
 */
public abstract class BaseCommandTest extends BaseTest {
    /** The mocked {@link CommandEvent}. */
    protected CommandEvent ce;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendReply(String)} */
    protected ArgumentCaptor<String> stringCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendEmbedReply(MessageEmbed)} */
    protected ArgumentCaptor<MessageEmbed> embedCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link CommandEvent#sendEmbedPaginatedReply(List)} */
    protected ArgumentCaptor<List<MessageEmbed>> embedsCaptor;
    /** The {@link ArgumentCaptor} for capturing {@link MessageChannel#sendMessage(Message)} */
    protected ArgumentCaptor<MessageCreateData> messageCaptor;
    protected MessageReceivedEvent mre;
    protected Message m;

    public BaseCommandTest() {
        resetMocks();
    }

    protected void resetMocks() {
        this.ce = mock(CommandEvent.class);
        mre = mock(MessageReceivedEvent.class);
        m = mock(Message.class);
        User u = Mocks.getMockedMember().getUser();
        Member me = Mocks.getMockedMember();
        JDA j = mock(JDA.class);
        this.stringCaptor = ArgumentCaptor.forClass(String.class);
        this.embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        this.embedsCaptor = ArgumentCaptor.forClass(List.class);
        this.messageCaptor = ArgumentCaptor.forClass(MessageCreateData.class);
        doNothing().when(ce).sendReply(stringCaptor.capture());
        doNothing().when(ce).sendEmbedReply(embedCaptor.capture());
        doNothing().when(ce).sendEmbedPaginatedReply(embedsCaptor.capture());
        doNothing().when(ce).sendEmbedPaginatedReplyWithPageNumber(embedsCaptor.capture());
        doNothing().when(ce).sendPastableReply(stringCaptor.capture());
        Guild g = Mocks.getMockedGuild();
        when(me.getGuild()).thenReturn(g);
        when(ce.getGuild()).thenReturn(g);
        when(ce.getMentionedUsers()).thenReturn(Collections.emptyList());
        when(ce.getMentionedMembers()).thenReturn(Collections.emptyList());
        when(j.getRegisteredListeners()).thenReturn(Collections.emptyList());
        when(j.getGuildById(570771524697718808L)).thenReturn(g);
        SelfUser selfUser = mock(SelfUser.class);
        when(selfUser.getIdLong()).thenReturn(9999L);
        when(selfUser.getId()).thenReturn("9999");
        when(j.getSelfUser()).thenReturn(selfUser);
        when(mre.getMessage()).thenReturn(m);
        when(mre.getAuthor()).thenReturn(u);
        when(mre.getMember()).thenReturn(me);
        MessageChannelUnion mc = mock(MessageChannelUnion.class);
        when(mre.getChannel()).thenReturn(mc);
        TextChannel tc = mock(TextChannel.class);
        when(tc.getGuild()).thenReturn(g);
        when(mc.asTextChannel()).thenReturn(tc);
        MessageCreateAction mca = mock(MessageCreateAction.class);
        when(mca.mentionRepliedUser(anyBoolean())).thenReturn(mca);
        when(mc.sendMessage(stringCaptor.capture())).thenReturn(mca);
        when(mc.sendMessage(messageCaptor.capture())).thenReturn(mca);
        when(m.addReaction(any())).thenReturn(mock(RestAction.class));
        when(m.clearReactions()).thenReturn(mock(RestAction.class));
        when(ce.getEvent()).thenReturn(mre);
        when(ce.getEvent().getJDA()).thenReturn(j);
        when(ce.getUser()).thenReturn(u);
    }

    /**
     * Sets the command arguments for this mocked {@link CommandEvent}.<br>
     * <i>Note!</i> Do not include the command name! (i.e. !command) It will be inserted at the beginning.<br>
     * If you need to call this method more than once in a single test, call {@link BaseCommandTest#resetMocks()}.
     *
     * @param s The arguments to use.
     */
    protected void setArgs(String s) {
        s = "!commandundertest" + (s.isEmpty() ? "" : " ") + s;
        setRawArgs(s);
    }

    protected void setRawArgs(String s) {
        setInternalState(ce, "EVENT", mre);
        when(m.getContentRaw()).thenReturn(s);
        when(m.getContentDisplay()).thenReturn(s);
        when(ce.getCommandArgsString()).thenCallRealMethod();
        when(ce.getCommandArgs()).thenCallRealMethod();
        when(ce.getRawCommandArgs()).thenCallRealMethod();
        when(ce.getRawCommandArgsString()).thenCallRealMethod();
        when(ce.getCommandArgsWithoutName()).thenCallRealMethod();
    }
}
