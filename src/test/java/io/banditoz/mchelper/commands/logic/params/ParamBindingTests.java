package io.banditoz.mchelper.commands.logic.params;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.DECIMAL;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.INTEGER;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.MEMBER;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.STRING;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.enumOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.stats.Status;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;

class ParamBindingTests {
    enum Mode {
        ENCODE,
        DECODE
    }

    private static BindContext ctx() {
        return new BindContext(mock(JDA.class), null, mock(User.class));
    }

    private static Object[] bind(Handler handler, String args) throws BindException {
        return TextBinder.bind(handler.params(), args, ctx());
    }

    private static Object[] bindIn(Guild guild, Handler handler, String args) throws BindException {
        return TextBinder.bind(handler.params(), args, new BindContext(mock(JDA.class), guild, mock(User.class)));
    }

    private static Handler recording(AtomicReference<Object[]> sink) {
        return Params.of()
                .arg("name", "A name.", STRING)
                .optional("count", "How many.", INTEGER)
                .handle((ce, name, count) -> {
                    sink.set(new Object[]{name, count});
                    return Status.SUCCESS;
                });
    }

    @Test
    void invokerReceivesTypedValues() throws Exception {
        AtomicReference<Object[]> seen = new AtomicReference<>();
        Handler handler = recording(seen);

        Status status = handler.invoker().invoke(mock(ICommandEvent.class), bind(handler, "alice 3"));

        assertThat(status).isEqualTo(Status.SUCCESS);
        assertThat(seen.get()).containsExactly("alice", Optional.of(3));
    }

    @Test
    void omittedOptionalArrivesEmptyNotNull() throws Exception {
        AtomicReference<Object[]> seen = new AtomicReference<>();
        Handler handler = recording(seen);

        handler.invoker().invoke(mock(ICommandEvent.class), bind(handler, "alice"));

        assertThat(seen.get()).containsExactly("alice", Optional.empty());
    }

    @Test
    void takesNoArguments() throws Exception {
        Handler handler = Params.of().handle(ce -> Status.SUCCESS);
        assertThat(bind(handler, "")).isEmpty();
        assertThat(bind(handler, null)).isEmpty();
    }

    @Test
    void missingRequiredNamesTheParameter() {
        Handler handler = Params.of().arg("name", "A name.", STRING).handle((ce, name) -> Status.SUCCESS);
        assertThatThrownBy(() -> bind(handler, ""))
                .isInstanceOf(BindException.class)
                .hasMessageContaining("required");
        assertThat(paramNameOf(handler, "")).isEqualTo("name");
    }

    @Test
    void unparseableValueNamesTheParameter() {
        // Previously this reached the user as "Status: Calamitous: java.lang.NumberFormatException".
        Handler handler = recording(new AtomicReference<>());

        assertThat(paramNameOf(handler, "alice abc")).isEqualTo("count");
        assertThatThrownBy(() -> bind(handler, "alice abc"))
                .isInstanceOf(BindException.class)
                .hasMessageContaining("whole number");
    }

    @Test
    void restConsumesEverythingLeft() throws Exception {
        AtomicReference<Object[]> seen = new AtomicReference<>();
        Handler handler = Params.of()
                .arg("mode", "Which way.", enumOf(Mode.class))
                .rest("text", "The text.", (ce, mode, text) -> {
                    sinkSet(seen, mode, text);
                    return Status.SUCCESS;
                });

        handler.invoker().invoke(mock(ICommandEvent.class), bind(handler, "encode hello there world"));

        assertThat(seen.get()).containsExactly(Mode.ENCODE, "hello there world");
    }

    @Test
    void enumsBindCaseInsensitivelyAndReportChoices() {
        Handler handler = Params.of()
                .arg("mode", "Which way.", enumOf(Mode.class))
                .handle((ce, mode) -> Status.SUCCESS);

        assertThatThrownBy(() -> bind(handler, "sideways"))
                .isInstanceOf(BindException.class)
                .hasMessageContaining("encode");
    }

    @Test
    void quotedRunStaysOneToken() throws Exception {
        AtomicReference<Object[]> seen = new AtomicReference<>();
        Handler handler = Params.of()
                .arg("first", "First.", STRING)
                .arg("second", "Second.", STRING)
                .handle((ce, first, second) -> {
                    sinkSet(seen, first, second);
                    return Status.SUCCESS;
                });

        handler.invoker().invoke(mock(ICommandEvent.class), bind(handler, "\"hello world\" tail"));

        assertThat(seen.get()).containsExactly("hello world", "tail");
    }

    @Test
    void decimalKeepsPrecision() throws Exception {
        Handler handler = Params.of().arg("amount", "How much.", DECIMAL).handle((ce, amount) -> Status.SUCCESS);

        assertThat(bind(handler, "50.25")).containsExactly(new BigDecimal("50.25"));
        assertThat((BigDecimal) bind(handler, "0.1")[0]).isEqualByComparingTo(new BigDecimal("0.1"));
    }

    @Test
    void decimalAcceptsCurrencyFormatting() throws Exception {
        Handler handler = Params.of().arg("amount", "How much.", DECIMAL).handle((ce, amount) -> Status.SUCCESS);

        assertThat((BigDecimal) bind(handler, "$1,234.50")[0]).isEqualByComparingTo(new BigDecimal("1234.50"));
        assertThat((BigDecimal) bind(handler, "-400")[0]).isEqualByComparingTo(new BigDecimal("-400"));
        assertThat((BigDecimal) bind(handler, ".5")[0]).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    void decimalRejectsExponents() {
        Handler handler = Params.of().arg("amount", "How much.", DECIMAL).handle((ce, amount) -> Status.SUCCESS);

        for (String token : new String[]{"1E+100000000", "1e5", "1E-999999999", "NaN", "Infinity", "1.2.3", "-", "."}) {
            assertThatThrownBy(() -> bind(handler, token))
                    .as(token)
                    .isInstanceOf(BindException.class)
                    .hasMessageContaining("expected a number");
        }
    }

    @Test
    void usageIsDerivedFromTheDeclaration() {
        Handler simple = recording(new AtomicReference<>());
        assertThat(simple.params().toUsage()).isEqualTo("<name> [count]");

        Handler greedy = Params.of()
                .arg("mode", "Which way.", enumOf(Mode.class))
                .rest("text", "The text.", (ce, mode, text) -> Status.SUCCESS);
        assertThat(greedy.params().toUsage()).isEqualTo("<mode> <text...>");

        assertThat(Params.of().handle(ce -> Status.SUCCESS).params().toUsage()).isNull();
    }

    @Test
    void requiredAfterOptionalIsRejectedUpFront() {
        // Positionally unbindable, so the spec refuses to build. Happens at startup, not at invocation.
        assertThatThrownBy(() -> Params.of()
                .optional("first", "First.", STRING)
                .arg("second", "Second.", STRING)
                .handle((ce, first, second) -> Status.SUCCESS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optional parameter");
    }

    @Test
    void duplicateNamesAreRejectedUpFront() {
        assertThatThrownBy(() -> Params.of()
                .arg("name", "First.", STRING)
                .arg("name", "Second.", STRING)
                .handle((ce, a, b) -> Status.SUCCESS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void memberResolvesFromMentionOrId() throws Exception {
        Handler handler = Params.of().arg("who", "Who.", MEMBER).handle((ce, who) -> Status.SUCCESS);
        Guild guild = mock(Guild.class);
        Member member = mock(Member.class);
        when(guild.getMemberById(1234L)).thenReturn(member);

        assertThat(bindIn(guild, handler, "<@1234>")).containsExactly(member);
        assertThat(bindIn(guild, handler, "<@!1234>")).containsExactly(member);
        assertThat(bindIn(guild, handler, "1234")).containsExactly(member);
    }

    @Test
    void memberResolvesFromUniqueName() throws Exception {
        Handler handler = Params.of().arg("who", "Who.", MEMBER).handle((ce, who) -> Status.SUCCESS);
        Guild guild = mock(Guild.class);
        Member byNick = mock(Member.class);
        Member byName = mock(Member.class);
        when(guild.getMembersByEffectiveName("alice", true)).thenReturn(List.of(byNick));
        when(guild.getMembersByName("bob", true)).thenReturn(List.of(byName));

        assertThat(bindIn(guild, handler, "alice")).containsExactly(byNick);
        assertThat(bindIn(guild, handler, "bob")).containsExactly(byName);
    }

    @Test
    void memberRejectsAmbiguousOrUnknownNames() {
        Handler handler = Params.of().arg("who", "Who.", MEMBER).handle((ce, who) -> Status.SUCCESS);
        Guild guild = mock(Guild.class);
        when(guild.getMembersByEffectiveName("alice", true)).thenReturn(List.of(mock(Member.class), mock(Member.class)));

        assertThatThrownBy(() -> bindIn(guild, handler, "alice"))
                .isInstanceOf(BindException.class)
                .hasMessageContaining("more than one");
        assertThatThrownBy(() -> bindIn(guild, handler, "nobody"))
                .isInstanceOf(BindException.class)
                .hasMessageContaining("no member named");
    }

    @Test
    void memberNeedsAGuild() {
        Handler handler = Params.of().arg("who", "Who.", MEMBER).handle((ce, who) -> Status.SUCCESS);

        assertThatThrownBy(() -> bind(handler, "<@1234>"))
                .isInstanceOf(BindException.class)
                .hasMessageContaining("only be given in a guild");
    }

    @Test
    void completersAttachToTheirOwnParameter() throws Exception {
        Completer names = e -> List.of();
        Completer counts = e -> List.of();
        Completer notes = e -> List.of();
        Handler handler = Params.of()
                .arg("name", "A name.", STRING, names)
                .arg("count", "How many.", INTEGER, counts)
                .rest("note", "A note.", notes, (ce, name, count, note) -> Status.SUCCESS);

        assertThat(handler.params().params()).extracting(Arg::completer).containsExactly(names, counts, notes);
        // text commands never consult them
        assertThat(bind(handler, "alice 3 hi there")).containsExactly("alice", 3, "hi there");
    }

    @Test
    void optionalParametersTakeACompleterToo() {
        Completer counts = e -> List.of();
        Handler handler = Params.of()
                .optional("count", "How many.", INTEGER, counts)
                .handle((ce, count) -> Status.SUCCESS);

        assertThat(handler.params().params()).extracting(Arg::completer).containsExactly(counts);
    }

    @Test
    void parametersWithoutACompleterHaveNone() {
        Handler handler = Params.of().arg("name", "A name.", STRING).rest("note", "A note.", (ce, name, note) -> Status.SUCCESS);

        assertThat(handler.params().params()).extracting(Arg::completer).containsOnlyNulls();
    }

    private static void sinkSet(AtomicReference<Object[]> sink, Object... values) {
        sink.set(values);
    }

    private static String paramNameOf(Handler handler, String args) {
        try {
            bind(handler, args);
            throw new AssertionError("expected a BindException");
        } catch (BindException ex) {
            return ex.getParamName();
        }
    }
}
