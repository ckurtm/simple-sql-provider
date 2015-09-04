package ckm.simple.sql_provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kurt on 2015/09/02.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface SimpleSQLConfig {
    String name();
    String authority();
    String database();
    int version();
}
