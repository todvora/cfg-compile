package cz.tomasdvorak.codegen.generator;

import cz.tomasdvorak.codegen.dto.Pair;
import cz.tomasdvorak.codegen.dto.Config;
import cz.tomasdvorak.codegen.dto.Section;
import cz.tomasdvorak.codegen.generator.utils.BoxingUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jboss.forge.roaster._shade.org.eclipse.jdt.internal.compiler.parser.Parser.name;

public class ClassesGenerator {

    /**
     * All generated classes will be located in one package
     */
    private final String targetPackage;

    public ClassesGenerator(final String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public List<JavaClassSource> convert(Config config) {
       return config.getSections().stream().map(this::sectionToClass).collect(Collectors.toList());
    }

    /**
     * Convert configuration section to a java class, which contains all key=value pairs as constants
     */
    private JavaClassSource sectionToClass(final Section section) {
        final JavaClassSource javaClassSource = Roaster.create(JavaClassSource.class);
        javaClassSource.setPackage(targetPackage);
        javaClassSource.setName(section.getName());
        javaClassSource.setFinal(true);

        section.getValues().forEach(assignment -> {
            final FieldSource<JavaClassSource> field = javaClassSource.addField()
                    .setType(getPrimitiveType(assignment))
                    .setName(assignment.getKey())
                    .setPublic()
                    .setStatic(true)
                    .setFinal(true);
            if(assignment.getValue() instanceof String) {
                field.setStringInitializer(String.valueOf(assignment.getValue()));
            } else {
                field.setLiteralInitializer(String.valueOf(assignment.getValue()));
            }
        });

        return javaClassSource;
    }

    private Class<?> getPrimitiveType(final Pair assignment) {
        return BoxingUtils.toPrimitiveIfAvailable(assignment.getValue().getClass());
    }

}
