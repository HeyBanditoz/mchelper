package io.banditoz.mchelper.utils.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResponse that = (TaskResponse) o;
        return task == that.task && Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, response);
    }
}
