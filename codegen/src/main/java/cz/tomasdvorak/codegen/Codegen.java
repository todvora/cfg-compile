package cz.tomasdvorak.codegen;

import cz.tomasdvorak.codegen.dto.Config;
import cz.tomasdvorak.codegen.generator.ClassesGenerator;
import cz.tomasdvorak.codegen.parser.ConfigurationParser;
import cz.tomasdvorak.codegen.parser.utils.ParsingException;
import org.apache.log4j.Logger;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Main codegen class. Serves for converting config file to java classes.
 */
public class Codegen {

    private static final Logger logger = Logger.getLogger(Codegen.class);


    private final ConfigurationParser parser;

    private Codegen() {
        this.parser = new ConfigurationParser();
    }

    /**
     * Entry point, called from the maven exec plugin (see pom.xml of myapp)
     * @param args Three string arguments - config file, target path, targetPackage
     */
    public static void main(String[] args) throws Exception {

        if(args.length != 3) {
            throw new RuntimeException("Codegen requires exactly 3 arguments: config file path, target path, target package");
        }

        final Codegen codegen = new Codegen();
        codegen.execute(args[0], args[1], args[2]);
    }

    /**
     * Read config file, parse it to the Config instance, transform to a List of Java classes and persist them
     */
    private void execute(final String configFile, final String targetPath, final String targetPackage) throws ParsingException, IOException, URISyntaxException {
        logger.info("Running Codegen with following params:");
        logger.info("Config file: " + configFile);
        logger.info("Target path: " + targetPath);
        logger.info("Target package: " + targetPackage);

        final Config config = parser.parse(configFile);
        final ClassesGenerator converter = new ClassesGenerator(targetPackage);
        final List<JavaClassSource> classes = converter.convert(config);
        classes.stream().parallel().forEach(cls -> persistClass(targetPath, cls));
    }

    private void persistClass(final String targetPath, final JavaClassSource cls) {
        final Path path = prepareClassDirectory(cls, targetPath);
        writeClass(cls, path);
    }

    private void writeClass(final JavaClassSource cls, final Path dir) {
        try {
            Files.write(dir.resolve(cls.getName() + ".java"), cls.toString().getBytes());
            logger.info("Class " + cls.getPackage() + "." + cls.getName() + " persisted");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write class class " + cls.getName() + " to directory " + dir.toAbsolutePath());
        }
    }

    /**
     * Create required directory for source code generation.
     */
    private Path prepareClassDirectory(final JavaClassSource cls, final String basePath) {
        final String packagePath = packageToPath(cls.getPackage());
        final Path dir = Paths.get(basePath, packagePath);
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create path for class " + cls.getName() + " in directory " + dir.toAbsolutePath());
        }
    }

    private String packageToPath(final String aPackage) {
        return Optional.ofNullable(aPackage).map(pkg -> pkg.replace(".", File.separator)).orElse("");
    }
}
