package io.banditoz.mchelper.serverstatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusResponse(JsonNode description, Players players, Version version, String favicon, int time) {
    // these should be actual objects, but we don't need all the crap that comes with them, look at https://wiki.vg/Chat
    public String getDescriptionAsString() {
        StringBuilder descStringBuilder = new StringBuilder();
        if (description.has("extra")) {
            for (JsonNode node : description.withArray("extra")) {
                if (node.has("text")) {
                    descStringBuilder.append(node.get("text").asText());
                }
            }
        }
        if (description.has("text")) {
            descStringBuilder.append(description.get("text").asText());
        }
        else { // description is just a plain JSON string, so append it plainly
            descStringBuilder.append(description.textValue());
        }
        return descStringBuilder.toString();
    }

    public ByteArrayOutputStream getFaviconAsImage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (this.favicon == null) {
            InputStream packBytes = getClass().getClassLoader().getResource("pack.png").openStream();
            baos.write(packBytes.readAllBytes());
            packBytes.close();
        }
        else {
            baos.write(Base64.getDecoder().decode(this.favicon.substring(this.favicon.indexOf(",") + 1).replace("\n", "")));
        }
        return baos;
    }
}
