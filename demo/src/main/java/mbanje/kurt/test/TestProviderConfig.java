package mbanje.kurt.test;


import ckm.simple.sql_provider.UpgradeScript;
import ckm.simple.sql_provider.annotation.ProviderConfig;
import ckm.simple.sql_provider.annotation.SimpleSQLConfig;
import mbanje.kurt.App;

/**
 * Created by kurt on 2015/09/02.
 */
@SimpleSQLConfig(
        name = App.TEST_PROVIDER,
        authority = "just.some.test_provider.authority",
        database = "test.db",
        version = 1)
public class TestProviderConfig implements ProviderConfig {
    @Override
    public UpgradeScript[] getUpdateScripts() {
        return new UpgradeScript[0];
    }
}
