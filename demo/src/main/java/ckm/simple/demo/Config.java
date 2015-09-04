package ckm.simple.demo;


import ckm.simple.sql_provider.UpgradeScript;
import ckm.simple.sql_provider.annotation.ProviderConfig;
import ckm.simple.sql_provider.annotation.SimpleSQLConfig;

/**
 * Created by kurt on 2015/09/02.
 */
@SimpleSQLConfig(
        name = Constants.PROVIDER_CLASS,
        authority = "ckm.simple.demo.example.provider",
        database = "test.db",
        version = 1)
public class Config implements ProviderConfig {
    @Override
    public UpgradeScript[] getUpdateScripts() {
        return new UpgradeScript[0];
    }
}
