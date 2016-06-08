package cz.tomasdvorak.codegen.generator.utils;

import java.util.Arrays;

public enum BoxingUtils {
    BOOLEAN(Boolean.class, Boolean.TYPE),
    INTEGER(Integer.class, Integer.TYPE),
    CHARACTER(Character.class, Character.TYPE),
    BYTE(Byte.class, Byte.TYPE),
    SHORT(Short.class, Short.TYPE),
    DOUBLE(Double.class, Double.TYPE),
    LONG(Long.class, Long.TYPE),
    FLOAT(Float.class, Float.TYPE);

    private final Class<?> objectClass;
    private final Class<?> primitiveType;

    <T> BoxingUtils(Class<T> objectClass, Class<T> primitiveType) {
        this.objectClass = objectClass;
        this.primitiveType = primitiveType;
    }

    /**
     * Convert class to a primitive-type class, if this exists
     * @return primitive-type class, or the original provided
     */
    public static <T> Class<T> toPrimitiveIfAvailable(Class<T> objectClass) {
        return Arrays.stream(values()).filter(v -> v.objectClass.equals(objectClass)).map(v -> (Class<T>)v.primitiveType).findFirst().orElse(objectClass);
    }
}
