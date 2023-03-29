package io.banditoz.mchelper;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Mocks {
    private static final Role R;

    static {
        R = mock(Role.class);
        when(R.getName()).thenReturn("Role");
        when(R.getColor()).thenReturn(Color.RED);
        when(R.getId()).thenReturn("732863308738330636");
        when(R.getIdLong()).thenReturn(732863308738330636L);
    }

    public static Member getMockedMember() {
        Member m = mock(Member.class);
        User u = mock(User.class);

        when(u.getName()).thenReturn("Foo");
        when(u.getId()).thenReturn("163094867910590464");
        when(u.getIdLong()).thenReturn(163094867910590464L);
        when(u.getDiscriminator()).thenReturn("#0420");

        when(m.getNickname()).thenReturn("NFoo");
        when(m.getUser()).thenReturn(u);
        when(m.getId()).thenReturn("163094867910590464");
        when(m.getIdLong()).thenReturn(163094867910590464L);
        when(m.getRoles()).thenReturn(List.of(R));
        when(m.getAsMention()).thenReturn("<@!163094867910590464>");
        when(m.getEffectiveName()).thenReturn("NFoo");

        return m;
    }

    public static Member getDifferentMockedMember() {
        Member m = mock(Member.class);
        User u = mock(User.class);

        when(u.getName()).thenReturn("Bar");
        when(u.getId()).thenReturn("404837963697225729");
        when(u.getIdLong()).thenReturn(404837963697225729L);
        when(u.getDiscriminator()).thenReturn("#0069");

        when(m.getNickname()).thenReturn("NBar");
        when(m.getUser()).thenReturn(u);
        when(m.getId()).thenReturn("404837963697225729");
        when(m.getIdLong()).thenReturn(404837963697225729L);
        when(m.getRoles()).thenReturn(List.of(R));
        when(m.getAsMention()).thenReturn("<@!404837963697225729>");
        when(m.getEffectiveName()).thenReturn("NBar");

        return m;
    }

    public static Member getDiffDiffMockedMember() {
        Member m = mock(Member.class);
        User u = mock(User.class);

        when(u.getName()).thenReturn("Zat");
        when(u.getId()).thenReturn("537893458730429519");
        when(u.getIdLong()).thenReturn(537893458730429519L);
        when(u.getDiscriminator()).thenReturn("#0019");

        when(m.getNickname()).thenReturn("NZat");
        when(m.getUser()).thenReturn(u);
        when(m.getId()).thenReturn("537893458730429519");
        when(m.getIdLong()).thenReturn(537893458730429519L);
        when(m.getRoles()).thenReturn(List.of(R));
        when(m.getAsMention()).thenReturn("<@!537893458730429519>");
        when(m.getEffectiveName()).thenReturn("NZat");

        return m;
    }

    public static Guild getMockedGuild() {
        Guild g = mock(Guild.class);
        Member m1 = getMockedMember();
        Member m2 = getDifferentMockedMember();

        when(g.getId()).thenReturn("570771524697718808");
        when(g.getIdLong()).thenReturn(570771524697718808L);
        when(g.getMembers()).thenReturn(List.of(m1, m2));

        when(g.getRoles()).thenReturn(List.of(R));
        when(g.getName()).thenReturn("QA Guild");

        when(g.getMemberById(163094867910590464L)).thenReturn(m1);
        when(g.getMemberById("163094867910590464")).thenReturn(m1);

        when(g.getMemberById(404837963697225729L)).thenReturn(m2);
        when(g.getMemberById("404837963697225729")).thenReturn(m2);

        when(g.getRoleById("732863308738330636")).thenReturn(R);
        when(g.getRoleById(732863308738330636L)).thenReturn(R);

        return g;
    }
}
