package io.banditoz.mchelper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtils {
    public static String runProcess(Process p) throws InterruptedException, IOException {
        p.waitFor(); // hacky for right now, but this is dangerous! make sure your bash commands won't hang
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder("```");

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        reader.close();
        output.append("```");

        if (output.toString().compareTo("``````") == 0) { // bash gave us empty output, clarify this
            output = new StringBuilder("<no output>");
        }
        return output.toString();
    }
}
