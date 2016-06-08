package cz.tomasdvorak.codegen.dto;

import java.util.List;

public class Section {
    private final String name;
    private final List<Pair> values;

    public Section(final String name, final List<Pair> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<Pair> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", values=" + values +
                '}';
    }
}
