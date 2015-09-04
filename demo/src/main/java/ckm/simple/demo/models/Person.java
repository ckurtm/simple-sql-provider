package ckm.simple.demo.models;


import ckm.simple.demo.Constants;
import ckm.simple.sql_provider.annotation.SimpleSQLColumn;
import ckm.simple.sql_provider.annotation.SimpleSQLTable;

/**
 * Created by simple on 01 09 2015 .
 */
@SimpleSQLTable(table = "person",provider = Constants.PROVIDER_CLASS)
public class Person {
    @SimpleSQLColumn(value = "id",primary = true)
    public int id;

    @SimpleSQLColumn("name")
    public String name;

    @SimpleSQLColumn("surname")
    public String surname;

    @SimpleSQLColumn("parentId")
    public int parentId;

    public Person() {}

    public Person(int id, String name, String surname, int parentId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.parentId = parentId;
    }
}
