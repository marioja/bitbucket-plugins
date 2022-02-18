package ut.net.mfjassociates.bitbucket.plugins;

import org.junit.Test;
import net.mfjassociates.bitbucket.plugins.api.MyPluginComponent;
import net.mfjassociates.bitbucket.plugins.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}