package ckm.simple.sql_provider.annotation;

/**
 * Created by kurt on 2015/09/02.
 */
public @interface SimpleSQLView {
    String name();
    String query();
    String provider();
}
