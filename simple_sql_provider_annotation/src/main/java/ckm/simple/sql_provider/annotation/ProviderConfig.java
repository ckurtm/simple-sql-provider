package ckm.simple.sql_provider.annotation;


import ckm.simple.sql_provider.UpgradeScript;

/**
 * Created by kurt on 2015/09/02.
 */
public interface ProviderConfig {
    UpgradeScript[] getUpdateScripts();
}
