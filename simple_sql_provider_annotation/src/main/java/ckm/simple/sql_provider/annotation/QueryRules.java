package ckm.simple.sql_provider.annotation;

/**
 * Created by kurt on 2015/09/07.
 */
public interface QueryRules {
    QueryRule[] getRules();
    public static  Object EMPTY = null;
}
