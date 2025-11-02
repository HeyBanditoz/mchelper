package io.banditoz.mchelper.xonlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record XonlistResponse(@JsonProperty("server") Map<String, XonoticServer> servers,
                              XonlistInfo info) {
}
