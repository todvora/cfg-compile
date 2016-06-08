package cz.tomasdvorak.codegen.dto;

public class Pair {
    private final String key;
    private final Object value;

    public Pair(final String key, final Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + "=" + String.valueOf(value);
    }
}
