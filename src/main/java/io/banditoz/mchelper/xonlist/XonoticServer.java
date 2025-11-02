package io.banditoz.mchelper.xonlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * API implementing some of the response data from a Xonotic list exposing data as JSON.
 * <a href="https://xonotic.lifeisabug.com">xonotic.lifeisabug.com</a>
 * @apiNote Vestigial; does not include most response data. See their JSON if you want more.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy.class)
public record XonoticServer(String realName, String address, String geo, int maxPlayers, int numPlayers) {
}
