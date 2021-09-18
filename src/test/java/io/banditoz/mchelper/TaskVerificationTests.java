package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.utils.database.TaskResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

public class TaskVerificationTests {
    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void testWorkTaskResponses() throws IOException {
        StringJoiner failures = new StringJoiner("\n");
        List<TaskResponse> responses = om.readValue(getClass().getClassLoader()
                .getResource("tasks_responses.json")
                .openStream(), om.getTypeFactory().constructCollectionType(List.class, TaskResponse.class));
        hasDuplicate(responses)
                .ifPresent(taskResponse ->
                        Assertions.fail("Response \"" + taskResponse.getResponse() + "\" is a duplicate of another."));
        for (TaskResponse response : responses) {
            if (!response.getResponse().contains("%MENTION%")) {
                failures.add("Response \"" + response.getResponse() + "\" is missing the %MENTION% key.");
            }
            if (!response.getResponse().contains("%AMOUNT%")) {
                failures.add("Response \"" + response.getResponse() + "\" is missing the %AMOUNT% key.");
            }
        }
        if (!failures.toString().isEmpty()) {
            Assertions.fail(failures.toString());
        }
    }

    private Optional<TaskResponse> hasDuplicate(Iterable<TaskResponse> iterator) {
        Set<TaskResponse> set = new HashSet<>();
        Optional<TaskResponse> opt = Optional.empty();
        for (TaskResponse response : iterator) {
            if (!set.add(response)) {
                opt = Optional.of(response);
            }
        }
        return opt;
    }
}
