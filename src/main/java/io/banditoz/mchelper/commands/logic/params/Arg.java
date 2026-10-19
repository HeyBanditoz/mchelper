package io.banditoz.mchelper.commands.logic.params;

import javax.annotation.Nullable;

public record Arg<T>(String name, String desc, ParamType<T> type, boolean required, boolean rest,
                     @Nullable Completer completer) {
    public Arg {
        name = name.toLowerCase();
    }
}
