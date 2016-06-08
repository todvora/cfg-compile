package cz.tomasdvorak.codegen.generator;

import cz.tomasdvorak.codegen.dto.Pair;
import cz.tomasdvorak.codegen.dto.Config;
import cz.tomasdvorak.codegen.dto.Section;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassesGeneratorTest {

    private ClassesGenerator converter;

    @Before
    public void setUp() throws Exception {
        converter = new ClassesGenerator("cz.tomasdvorak.codegen");
    }

    @Test
    public void convert() throws Exception {

        final Config config = new Config(Arrays.asList(
                new Section("UserConfig", Collections.singletonList(new Pair("MAX_DISK_SPACE", 1024))),
                new Section("SystemConfig", Arrays.asList(new Pair("RATIO", 3.5), new Pair("PATH", "/tmp")))
        ));

        final List<JavaClassSource> classes = converter.convert(config);
        Assert.assertEquals(2, classes.size());

        Assert.assertEquals("package cz.tomasdvorak.codegen;\n" +
                "public final class UserConfig {\n" +
                "\n" +
                "\tpublic static final int MAX_DISK_SPACE = 1024;\n" +
                "}", classes.get(0).toString());

        Assert.assertEquals("package cz.tomasdvorak.codegen;\n" +
                "public final class SystemConfig {\n" +
                "\n" +
                "\tpublic static final double RATIO = 3.5;\n" +
                "\tpublic static final String PATH = \"/tmp\";\n" +
                "}", classes.get(1).toString());
    }


}