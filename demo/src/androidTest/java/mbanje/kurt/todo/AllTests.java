package mbanje.kurt.todo;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by kurt on 2014/07/21.
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        suite.addTestSuite(TodoHelperTest.class);
        suite.addTestSuite(TodoActivityTest.class);
        return suite;
    }
}
