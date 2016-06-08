package cz.tomasdvorak.users;

import org.junit.Assert;
import org.junit.Test;

public class UserUtilsTest {

    @Test
    public void getHomeDirectory() throws Exception {
        Assert.assertEquals("/home/tomas", UserUtils.getHomeDirectory("Tomas"));
    }

    @Test
    public void getDiskQuota() throws Exception {
        Assert.assertEquals(175.0, UserUtils.getDiskQuota(), 0.0001);
    }

    @Test
    public void getMemoryLimit() throws Exception {
        Assert.assertEquals(420.0, UserUtils.getMaxMemory(), 0.0001);
    }
}