package io.banditoz.mchelper.mtg;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallBulkData(String id, String type, OffsetDateTime updatedAt, String uri, String jsonlDownloadUri,
                               long compressedSize) {
}
