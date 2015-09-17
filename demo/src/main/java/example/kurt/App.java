package example.kurt;

import android.app.Application;


/**
 * Created by kurt on 2015/07/01.
 */
public class App extends Application {

    public static final String TEST_PROVIDER = "TestProvider";
    public static final String TODO_PROVIDER = "TodoProvider";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
