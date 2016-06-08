package cz.tomasdvorak.codegen.parser;

import cz.tomasdvorak.codegen.dto.Pair;
import cz.tomasdvorak.codegen.dto.Config;
import cz.tomasdvorak.codegen.dto.Section;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfParserTest {

    private ConfigurationParser confParser;

    @Before
    public void setUp() throws Exception {
        confParser = new ConfigurationParser();
    }

    @Test
    public void parse() throws Exception {
        final Config config = confParser.parse(getClass().getResource("/test-config.cfg").getFile());

        Assert.assertEquals(2, config.getSections().size());

        Assert.assertEquals(120, (int)readValue(config, "SystemConstants", "MAX_MEMORY")); // integer
        Assert.assertEquals("/foo/bar", readValue(config, "SystemConstants", "PATH")); // string

        Assert.assertEquals(3.5, readValue(config, "UserConstants", "RATIO"), Double.MIN_VALUE); // double
        Assert.assertEquals(50, (int)readValue(config, "UserConstants", "DISK_QUOTA")); // integer
        Assert.assertEquals(true, readValue(config, "UserConstants", "ENABLED")); // boolean
    }

    private <T> T readValue(Config config, String sectionName, String keyName) {
        final Section section = config.getSections().stream()
                .filter(s -> s.getName().equals(sectionName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Section " + sectionName + " cannot be found"));

        return section.getValues().stream()
                .filter(a -> a.getKey().equals(keyName))
                .map(Pair::getValue)
                .map(obj -> (T)obj)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Section " + sectionName + " does not contain a key " + keyName));
    }
}