package usa.cactuspuppy.mcmurder.utils;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class ConfigTest {
    private Config testConfig;

    @Before
    public void setup() {
        testConfig = new Config();
    }

    @After
    public void teardown() {
        testConfig = null;
    }

    @Test
    public void stringNullCheck() throws InvalidConfigurationException, IOException {
        try {
            testConfig.load((String) null);
            fail();
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void fileNullCheck() throws InvalidConfigurationException, IOException {
        try {
            testConfig.load((File) null);
            fail();
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void loadFromStringNullCheck() throws InvalidConfigurationException {
        try {
            testConfig.loadFromString(null);
            fail();
        } catch (IllegalArgumentException ignored) { }
        try {
            testConfig.loadFromString("");
            fail();
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void simpleTest() throws InvalidConfigurationException {
        String input =
        "key: value";

        testConfig.loadFromString(input);
        assertEquals("value", testConfig.get("key"));
    }

    @Test
    public void indentTest() throws InvalidConfigurationException {
        String input =
        "object:\n" +
        "  key: value02";

        testConfig.loadFromString(input);
        assertEquals("value02", testConfig.get("object.key"));
    }

    @Test
    public void commentTest01() throws InvalidConfigurationException {
        String input =
        "#============#\n" +
        "# Beep Boop\n" +
        "#============#\n" +
        "key: value";

        testConfig.loadFromString(input);
        assertEquals("value", testConfig.get("key"));
    }

    @Test
    public void commentTest02() throws InvalidConfigurationException {
        String input =
        "#============#\n" +
        "# Beep Boop\n" +
        "#============#\n" +
        "object:\n" +
        "  key: value02";

        testConfig.loadFromString(input);
        assertEquals("value02", testConfig.get("object.key"));
    }

    @Test
    public void commentTestInline() throws InvalidConfigurationException {
        String input =
        "# Starting comment\n" +
        "key: value-00  # inline comment\n" +
        "  subkey: value01  #second inline comment\n" +
        "  subkey02: value02\n" +
        "  \n" +
        "  # Explanation comment preceded by blank line\n" +
        "  explainedkey: expl_value\n" +
        "  \n" +
        "# A_comment\n" +
        "topkey: notacreepjustacookie24\n";

        testConfig.loadFromString(input);
        assertEquals("value02", testConfig.get("subkey02"));
        assertEquals("expl_value", testConfig.get("explainedkey"));
        assertEquals("notacreepjustacookie24", testConfig.get("topkey"));
    }

    @Test
    public void invalidConfigTest() {
        String input =
        "# Valid comment\n" +
        "  Invalid # line\n";
        assertThrows(InvalidConfigurationException.class, () -> testConfig.loadFromString(input));
    }
}