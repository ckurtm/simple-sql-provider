package ckm.simple.sql_provider.annotation;

/**
 * Created by kurt on 2015/09/02.
 */
public @interface SimpleSQLTable {
    String table();
    String provider();
    String queryKey() default NULL;
    String query() default NULL;
    Class<?> queryRules() default String.class;
    String NULL = "";
}
