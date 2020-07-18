package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ListUtils;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListUtilsTests {
    private List<String> possibilities;
    private Random rand;

    @Before public void initialize() {
        this.possibilities = new ArrayList<>();
        this.possibilities.add("foo");
        this.possibilities.add("bar");
        this.possibilities.add("baz");
        this.rand = new Random(1); // do not modify this value
    }

    @Test public void extractNumRandomlyShouldBeRandom() {
        TestCase.assertEquals("foo", ListUtils.extractNumRandomly(1, possibilities, rand));
        TestCase.assertEquals("bar", ListUtils.extractNumRandomly(1, possibilities, rand));
        TestCase.assertEquals("bar", ListUtils.extractNumRandomly(1, possibilities, rand));
        TestCase.assertEquals("foo", ListUtils.extractNumRandomly(1, possibilities, rand));
        TestCase.assertEquals("baz", ListUtils.extractNumRandomly(1, possibilities, rand));
    }

    @Test public void extractingMultipleEntriesFromListShouldBeRandom() {
        TestCase.assertEquals("foo, bar", ListUtils.extractNumRandomly(2, possibilities, rand));
        TestCase.assertEquals("bar, foo", ListUtils.extractNumRandomly(2, possibilities, rand));
        TestCase.assertEquals("baz, foo", ListUtils.extractNumRandomly(2, possibilities, rand));
        TestCase.assertEquals("baz, bar", ListUtils.extractNumRandomly(2, possibilities, rand));
        TestCase.assertEquals("bar, baz", ListUtils.extractNumRandomly(2, possibilities, rand));
    }
}
