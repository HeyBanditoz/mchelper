package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.utils.database.TaskResponse;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TaskVerificationTests {
    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void testWorkTaskResponses() throws IOException {
        StringJoiner failures = new StringJoiner("\n");
        List<TaskResponse> responses = om.readValue(getClass().getClassLoader()
                .getResource("tasks_responses.json")
                .openStream(), om.getTypeFactory().constructCollectionType(List.class, TaskResponse.class));
        assertThat(responses).doesNotHaveDuplicates();
        for (TaskResponse response : responses) {
            if (!response.response().contains("%MENTION%")) {
                failures.add("Response \"" + response.response() + "\" is missing the %MENTION% key.");
            }
            if (!response.response().contains("%AMOUNT%")) {
                failures.add("Response \"" + response.response() + "\" is missing the %AMOUNT% key.");
            }
        }
        if (!failures.toString().isEmpty()) {
            fail(failures.toString());
        }
    }
}
