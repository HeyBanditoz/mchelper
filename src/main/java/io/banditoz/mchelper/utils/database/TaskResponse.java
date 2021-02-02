package io.banditoz.mchelper.utils.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;

import java.math.BigDecimal;

public class TaskResponse {
    @JsonProperty("task")
    private Task task;
    @JsonProperty("response")
    private String response;

    public Task getTask() {
        return task;
    }

    public String getResponse() {
        return response;
    }

    public String getResponse(long id, BigDecimal amount, Task t) {
        if (t == Task.WORK) {
            return response.replace("%MENTION%", "<@" + id + ">")
                    .replace("%AMOUNT%", "$" + AccountManager.format(amount));
        }
        return null;
    }
}
