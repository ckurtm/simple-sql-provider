package example.kurt.todo;


import ckm.simple.sql_provider.UpgradeScript;
import ckm.simple.sql_provider.annotation.ProviderConfig;
import ckm.simple.sql_provider.annotation.SimpleSQLConfig;
import example.kurt.App;

/**
 * Created by kurt on 2015/09/02.
 */
@SimpleSQLConfig(
        name = App.TODO_PROVIDER,
        authority = "example.kurt.todo_provider.authority",
        database = "base",
        version = 1)
public class TodoProviderConfig implements ProviderConfig {
    @Override
    public UpgradeScript[] getUpdateScripts() {
        return new UpgradeScript[0];
    }
}
