package ckm.simple.demo.models;



import java.util.Date;

import ckm.simple.demo.Constants;
import ckm.simple.sql_provider.annotation.SimpleSQLColumn;
import ckm.simple.sql_provider.annotation.SimpleSQLTable;

/**
 * Created by simple on 2015/08/31.
 */
@SimpleSQLTable(table = "parent",provider = Constants.PROVIDER_CLASS)
public class Parent {

    @SimpleSQLColumn(value = "pid",primary = true)
    public int parentId;

    @SimpleSQLColumn("name")
    public String name;

    @SimpleSQLColumn("surname")
    public String surname;

    @SimpleSQLColumn("dob")
    public Date dob;

    public Parent() {}

    public Parent(int parentId, String name, String surname) {
        this.name = name;
        this.surname = surname;
        this.parentId = parentId;
    }
}
