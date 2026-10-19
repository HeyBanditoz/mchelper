package io.banditoz.mchelper.commands.logic.slash;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.BOOLEAN;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.STRING;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.enumOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.params.Completer;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.junit.jupiter.api.Test;

class SlashCommandTests {
    enum Mode {
        ENCODE,
        DECODE
    }

    private static BoundCommand declaring(Supplier<Handler> declaration) {
        Handler handler = declaration.get();
        BoundCommand command = mock(BoundCommand.class);
        when(command.commandName()).thenReturn("test");
        when(command.getHelp()).thenReturn(new Help("test", false).withDescription("A test command."));
        when(command.getRequiredPermissions()).thenReturn(EnumSet.noneOf(Permission.class));
        when(command.paramSpec()).thenReturn(handler.params());
        return command;
    }

    @Test
    void onlyParametersWithACompleterAreAutocompleted() throws Exception {
        SlashCommand command = new SlashCommand(declaring(() -> Params.of()
                .arg("card", "A card.", STRING, e -> List.of())
                .optional("print", "A printing.", STRING)
                .handle((ce, card, print) -> Status.SUCCESS)));

        assertThat(command.getDataForCommand().getOptions())
                .extracting(OptionData::isAutoComplete)
                .containsExactly(true, false);
    }

    @Test
    void completerIsFoundByOptionName() throws Exception {
        Completer cards = e -> List.of();
        Completer prints = e -> List.of();
        SlashCommand command = new SlashCommand(declaring(() -> Params.of()
                .arg("card", "A card.", STRING, cards)
                .optional("print", "A printing.", STRING, prints)
                .optional("plain", "Not autocompleted.", STRING)
                .handle((ce, card, print, plain) -> Status.SUCCESS)));

        assertThat(command.completerFor("card")).isSameAs(cards);
        assertThat(command.completerFor("print")).isSameAs(prints);
        assertThat(command.completerFor("plain")).isNull();
        assertThat(command.completerFor("nonexistent")).isNull();
    }

    @Test
    void choicesCannotAlsoBeAutocompleted() {
        assertThatThrownBy(() -> new SlashCommand(declaring(() -> Params.of()
                .arg("mode", "A mode.", enumOf(Mode.class), e -> List.of())
                .handle((ce, mode) -> Status.SUCCESS))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test autocompletes parameter mode");
    }

    @Test
    void onlyTextAndNumbersCanBeAutocompleted() {
        assertThatThrownBy(() -> new SlashCommand(declaring(() -> Params.of()
                .arg("flag", "A flag.", BOOLEAN, e -> List.of())
                .handle((ce, flag) -> Status.SUCCESS))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test autocompletes parameter flag");
    }
}
