package cz.tomasdvorak.codegen.generator.utils;

import org.junit.Assert;
import org.junit.Test;

public class BoxingUtilsTest {
    @Test
    public void toPrimitiveIfAvailable() throws Exception {
        Assert.assertEquals(String.class, BoxingUtils.toPrimitiveIfAvailable(String.class));
        Assert.assertEquals(int.class, BoxingUtils.toPrimitiveIfAvailable(Integer.class));
        Assert.assertEquals(double.class, BoxingUtils.toPrimitiveIfAvailable(Double.class));
    }

}