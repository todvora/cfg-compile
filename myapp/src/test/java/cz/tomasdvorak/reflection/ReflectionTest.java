package cz.tomasdvorak.reflection;

import cz.tomasdvorak.myapp.settings.SystemConstants;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionTest {

    @Test
    public void testReflection() throws Exception {
        final Map<String, Object> allConstants = getAllConstants(SystemConstants.class);

        Assert.assertEquals(3, allConstants.size());

        Assert.assertEquals(120, allConstants.get("MAX_MEMORY"));
        Assert.assertEquals(50, allConstants.get("DISK_QUOTA"));
        Assert.assertEquals("/home", allConstants.get("HOME_ROOT"));
    }

    private Map<String, Object> getAllConstants(Class<?> aClass) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                result.put(field.getName(), field.get(aClass));
            }
        }
        return result;
    }
}
