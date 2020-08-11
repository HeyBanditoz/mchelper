package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ListUtilsTests {
    private List<String> possibilities;
    private Random rand;

    @BeforeEach
    public void initialize() {
        this.possibilities = new ArrayList<>();
        this.possibilities.add("foo");
        this.possibilities.add("bar");
        this.possibilities.add("baz");
        this.rand = new Random(1); // do not modify this value
    }

    @Test
    public void extractNumRandomlyShouldBeRandom() {
        assertEquals("foo", ListUtils.extractNumRandomly(1, possibilities, rand));
        assertEquals("bar", ListUtils.extractNumRandomly(1, possibilities, rand));
        assertEquals("bar", ListUtils.extractNumRandomly(1, possibilities, rand));
        assertEquals("foo", ListUtils.extractNumRandomly(1, possibilities, rand));
        assertEquals("baz", ListUtils.extractNumRandomly(1, possibilities, rand));
    }

    @Test
    public void extractingMultipleEntriesFromListShouldBeRandom() {
        assertEquals("foo, bar", ListUtils.extractNumRandomly(2, possibilities, rand));
        assertEquals("bar, foo", ListUtils.extractNumRandomly(2, possibilities, rand));
        assertEquals("baz, foo", ListUtils.extractNumRandomly(2, possibilities, rand));
        assertEquals("baz, bar", ListUtils.extractNumRandomly(2, possibilities, rand));
        assertEquals("bar, baz", ListUtils.extractNumRandomly(2, possibilities, rand));
    }
}
