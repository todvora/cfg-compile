package cz.tomasdvorak.codegen.parser.utils;

import org.parboiled.support.Var;

import java.util.LinkedList;
import java.util.List;

public class ListBuilder<T> extends Var<List<T>> {
    public ListBuilder() {
        super(new LinkedList<>());
    }

    public boolean add(T value) {
        get().add(value);
        return true;
    }

    public List<T> getAndReset() {
        final LinkedList<T> result = new LinkedList<>(get());
        get().clear();
        return result;
    }
}
