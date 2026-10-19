package io.banditoz.mchelper.commands.logic.params;

import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.stats.Status;

public record Handler(ParamSpec params, Invoker invoker) {
    public interface Invoker {
        Status invoke(ICommandEvent ce, Object[] values) throws Exception;
    }
}
