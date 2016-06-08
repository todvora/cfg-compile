package cz.tomasdvorak.codegen.parser;

import cz.tomasdvorak.codegen.dto.Config;
import cz.tomasdvorak.codegen.parser.utils.ParsingException;
import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ConfigurationParser {

    private final ConfigurationGrammar parser;

    public ConfigurationParser() {
        this.parser = Parboiled.createParser(ConfigurationGrammar.class);
    }

    public Config parse(String configFilePath) throws IOException, ParsingException {
        final String fileContent = new String(Files.readAllBytes(Paths.get(configFilePath)));
        final ParsingResult<Object> result = new ReportingParseRunner<>(parser.Configuration()).run(fileContent);

        if (result.hasErrors()) {
            final String errors = result.parseErrors.stream().map(ErrorUtils::printParseError).collect(Collectors.joining(", "));
            throw new ParsingException("Parsing encountered errors! " + errors);
        }

        if(!result.matched) {
            throw new ParsingException("Input cannot be parsed!");
        }

        if(result.resultValue == null) {
            throw new ParsingException("No value returned from parser!");
        }

        if(!(result.resultValue instanceof Config)) {
            throw new ParsingException("Returned value is not Config but " + result.getClass().getName());
        } else {
            return (Config) result.resultValue;
        }
    }
}
