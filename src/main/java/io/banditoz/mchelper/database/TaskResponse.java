package io.banditoz.mchelper.database;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;

public record TaskResponse(
        @JsonProperty("task")
        Task task,
        @JsonProperty("response")
        String response,
        @JsonProperty(value = "rare", defaultValue = "false")
        boolean rare
) {
    public boolean notRare() {
        return !rare;
    }

    public String getResponse(long id, BigDecimal amount, Task t) {
        if (t == Task.WORK) {
            return response.replace("%MENTION%", "<@" + id + ">")
                    .replace("%AMOUNT%", "$" + AccountManager.format(amount));
        }
        return null;
    }
}
