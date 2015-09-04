package ckm.simple.sql_provider.processor.internal;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by kurt on 03 09 2015 .
 */
public final class Table {
    public String name;
    public Element element;
    public String provider;
    public ClassName clazz;
    public List<Column> columns = new ArrayList<>();

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", provider='" + provider + '\'' +
                ", clazz=" + clazz +
                ", columns=" + columns +
                '}';
    }

    public boolean hasPrimary() {
        for(Column column:columns){
            if(column.primary){
                return true;
            }
        }
        return false;
    }


    public Column getPrimary(){
        for(Column column:columns){
            if(column.primary){
                return column;
            }
        }
        return null;
    }
}
