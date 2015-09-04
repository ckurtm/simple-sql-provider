package ckm.simple.sql_provider.processor.internal;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;

/**
 * Created by kurt on 03 09 2015 .
 */
public final class Provider {
    public Element element;
    public String name;
    public String authority;
    public ClassName clazz;
    public ClassName configClass;
    public String database;
    public int version;

    public Provider(Element element,String name, String authority, ClassName clazz, String database, int version,ClassName configClass) {
        this.name = name;
        this.element = element;
        this.authority = authority;
        this.clazz = clazz;
        this.database = database;
        this.version = version;
        this.configClass = configClass;
    }

    @Override
    public String toString() {
        return "Provider{" +
                "name='" + name + '\'' +
                ", authority='" + authority + '\'' +
                ", clazz=" + clazz +
                ", configClass=" + configClass +
                ", database='" + database + '\'' +
                ", version=" + version +
                '}';
    }
}
