package ckm.simple.sql_provider.annotation;

/**
 * Created by kurt on 03 09 2015 .
 */
public @interface SimpleSQLColumn {
    String value();
    boolean primary() default false;
    boolean autoincrement() default false;
}
