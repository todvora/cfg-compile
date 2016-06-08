package cz.tomasdvorak.codegen.dto;

import java.util.List;

public class Config {
    private final List<Section> sections;

    public Config(final List<Section> sections) {
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }

    @Override
    public String toString() {
        return sections.toString();
    }
}
