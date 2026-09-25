package io.banditoz.mchelper.mtg;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

class ScryfallCardTests {
    private final ObjectMapper om = new ObjectMapperFactory().objectMapper();

    private ScryfallCard load(String name) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/scryfall/" + name + ".json")) {
            return om.readValue(is, ScryfallCard.class);
        }
    }

    @Test
    void singleFacedCardShouldHaveOneFaceFromTopLevel() throws IOException {
        ScryfallCard card = load("lightning-bolt");

        assertThat(card.name()).isEqualTo("Lightning Bolt");
        assertThat(card.releasedAt()).isNotNull().isBefore(LocalDate.now());
        assertThat(card.scryfallSetUri()).contains("/sets/");
        assertThat(card.legalities()).containsEntry("modern", "legal");
        assertThat(card.imageUris()).isNotNull();
        assertThat(card.imageUris().png()).isNotBlank();

        assertThat(card.faces()).hasSize(1);
        ScryfallCardFace face = card.faces().getFirst();
        assertThat(face.name()).isEqualTo("Lightning Bolt");
        assertThat(face.manaCost()).isEqualTo("{R}");
        assertThat(face.typeLine()).isEqualTo("Instant");
        assertThat(face.oracleText()).contains("3 damage");
        assertThat(face.flavorText()).isNotBlank();
        assertThat(face.power()).isNull();
    }

    @Test
    void transformCardShouldHaveFacesWithOwnImages() throws IOException {
        ScryfallCard card = load("delver-of-secrets");

        assertThat(card.name()).isEqualTo("Delver of Secrets // Insectile Aberration");
        assertThat(card.imageUris()).isNull();
        assertThat(card.faces()).extracting(ScryfallCardFace::name)
                .containsExactly("Delver of Secrets", "Insectile Aberration");
        assertThat(card.faces()).allSatisfy(face -> assertThat(face.imageUris().png()).isNotBlank());
        assertThat(card.faces().get(1).power()).isEqualTo("3");
        assertThat(card.faces().get(1).toughness()).isEqualTo("2");
    }

    @Test
    void splitCardShouldHaveFacesAndTopLevelImages() throws IOException {
        ScryfallCard card = load("alive-well");

        assertThat(card.name()).isEqualTo("Alive // Well");
        assertThat(card.imageUris().png()).isNotBlank();
        assertThat(card.faces()).extracting(ScryfallCardFace::name).containsExactly("Alive", "Well");
        assertThat(card.faces()).extracting(ScryfallCardFace::manaCost).containsExactly("{3}{G}", "{W}");
        assertThat(card.faces()).allSatisfy(face -> assertThat(face.imageUris()).isNull());
    }
}
