package io.banditoz.mchelper.xonlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy.class)
public record XonlistInfo(int totalServers, int lastUpdate, int lastUpdate_epoch /* guh this name */, int totalBots,
                          int totalPlayers, int activeServers) {
}
