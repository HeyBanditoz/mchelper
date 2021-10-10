package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ListUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class ListUtilsTests {
    private List<String> possibilities;
    private Random rand;

    @BeforeMethod
    public void initialize() {
        this.possibilities = new ArrayList<>();
        this.possibilities.add("foo");
        this.possibilities.add("bar");
        this.possibilities.add("baz");
        this.rand = new Random(1); // do not modify this value
    }

    @Test
    public void extractNumRandomlyShouldBeRandom() {
        assertThat(ListUtils.extractNumRandomly(1, possibilities, rand)).isEqualTo("foo");
        assertThat(ListUtils.extractNumRandomly(1, possibilities, rand)).isEqualTo("bar");
        assertThat(ListUtils.extractNumRandomly(1, possibilities, rand)).isEqualTo("bar");
        assertThat(ListUtils.extractNumRandomly(1, possibilities, rand)).isEqualTo("foo");
        assertThat(ListUtils.extractNumRandomly(1, possibilities, rand)).isEqualTo("baz");
    }

    @Test
    public void extractingMultipleEntriesFromListShouldBeRandom() {
        assertThat(ListUtils.extractNumRandomly(2, possibilities, rand)).isEqualTo("foo, bar");
        assertThat(ListUtils.extractNumRandomly(2, possibilities, rand)).isEqualTo("bar, foo");
        assertThat(ListUtils.extractNumRandomly(2, possibilities, rand)).isEqualTo("baz, foo");
        assertThat(ListUtils.extractNumRandomly(2, possibilities, rand)).isEqualTo("baz, bar");
        assertThat(ListUtils.extractNumRandomly(2, possibilities, rand)).isEqualTo("bar, baz");
    }
}
