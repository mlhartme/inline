package net.oneandone.inline.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SplitTest {
    @Test
    public void empty() {
        check("");
        check(" ");
    }

    @Test
    public void one() {
        check("1", "1");
        check(" 1", "1");
        check("1  ", "1");
    }

    @Test
    public void two() {
        check("1 2", "1", "2");
        check(" 1  2" , "1", "2");
        check("1 2 ", "1", "2");
    }
    private void check(String str, String ... expected) {
        assertEquals(Arrays.asList(expected), Split.split(str));
    }
}
