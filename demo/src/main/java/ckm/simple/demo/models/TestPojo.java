package ckm.simple.demo.models;



import java.math.BigDecimal;
import java.util.Date;

import ckm.simple.demo.Constants;
import ckm.simple.sql_provider.annotation.SimpleSQLColumn;
import ckm.simple.sql_provider.annotation.SimpleSQLTable;

/**
 * Created by kurt on 2015/09/04.
 */
@SimpleSQLTable(table = "testPojo", provider = Constants.PROVIDER_CLASS)
public class TestPojo {

    @SimpleSQLColumn("col_str")
    public String myString;

    @SimpleSQLColumn(value = "col_int", primary = true)
    public int anInt;

    @SimpleSQLColumn("col_integer")
    public int myinteger;

    @SimpleSQLColumn("col_short")
    public int myshort;

    @SimpleSQLColumn("col_short2")
    public int myShort;

    @SimpleSQLColumn("col_long")
    public long mylong;

    @SimpleSQLColumn("col_long2")
    public int myLong;

    @SimpleSQLColumn("col_double")
    public long mydouble;

    @SimpleSQLColumn("col_double2")
    public int myDouble;

    @SimpleSQLColumn("col_float")
    public long myfloat;

    @SimpleSQLColumn("col_float2")
    public int myFloat;

    @SimpleSQLColumn("col_bigdecimal")
    public BigDecimal bigD;

    @SimpleSQLColumn("col_bool")
    public boolean mybool;

    @SimpleSQLColumn("col_bool2")
    public boolean myBool;

    @SimpleSQLColumn("col_date")
    public Date mydateCol;
}
