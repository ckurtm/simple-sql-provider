# SimpleSQLProvider
 The Fastest Way to create a sql based ContentProvider in Android using annotations (No reflection)

 [ ![Download](https://api.bintray.com/packages/ckurtm/maven/SimpleSQLProvider/images/download.svg) ](https://bintray.com/ckurtm/maven/SimpleSQLProvider/_latestVersion)
 
 
 
# HOW TO ADD TO YOUR PROJECT

 Gradle:
 
 add the apt plugin as a dependency to your root build gradle as below:
 
   ```groovy
   buildscript {
       repositories {
           jcenter()
       }
       dependencies {
           classpath 'com.android.tools.build:gradle:1.3.0'
           classpath 'com.neenbedankt.gradle.plugins:android-apt:1.6'
       }
   }
   ```
 
 apply the apt plugin to your main project's build.gradle
 
  ```groovy
  apply plugin: 'com.neenbedankt.android-apt'
  ```
  
 add the required dependencies as below
 
 ```groovy
 dependencies {
         compile 'com.squareup:javapoet:1.2.0'
         compile 'ckm.simple:simple_sql_provider_annotation:1.0.6'
         compile 'ckm.simple:simple_sql_provider_processor:1.0.6'
 }
 ```

# QUICK START
  1. Create ProviderConfig class that defines your content providers details e.g.
  
  ```java
   @SimpleSQLConfig(
           name = "TestProvider",
           authority = "just.some.test_provider.authority",
           database = "test.db",
           version = 1)
   public class TestProviderConfig implements ProviderConfig {
       @Override
       public UpgradeScript[] getUpdateScripts() {
           return new UpgradeScript[0];
       }
   }
   ```
   
   This class file says 
    - Create a ContentProvider called TestProvider
    - The authority for this Provider is "just.some.test_provider.authority"
    - The Provider uses a database file named "test.db"
    - The Current database version is 1
    - provider UpdateScripts as a defined by the UpdateScripts class
     
  2. Annotate your Pojo file that defines a table in the Database as below.
  
  @SimpleSQLTable(table = "test", provider = App.TEST_PROVIDER)
  public class Test {
  
    ```java
    @SimpleSQLTable(table = "test", provider = "TestProvider")
    public class Test {

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
     ```

  3. The rebuild your project
  
  4. You will now have access to files generated for you to access the Table prefixed with "Table" using the usual Android ContentProvider methods, e.g. TestTable for the above class.
     The generated files have convinience functions for you to add values to the table, e.g. getContentValues() with an instance of the Test class to insert into db e.g.
     
     ```java
     Test testInstance = new Test(...);
     getContentResolver().insert(TestTable.CONTENT_URI,TestTable.getContentValues(testInstance,false));   
     ```
     
     To get data from the database use:
     
     ```java
        Cursor cursor = getContentResolver().query(TestTable.CONTENT_URI,null,null,null,null);
        //one row
        Test testRow = TestTable.getRow(cursor,true);
        //multiple rows
        List<Test> testRows = TestTable.getRows(cursor,false);   
     ```
     
     
    5. add your provider as usual to your AndroidManifest.xml file with a matching authority as defined in ProviderConfig class
     
     ```xml
         <provider
            android:authorities="just.some.test_provider.authority"
            android:name="example.kurt.test.TestProvider"/>   
     ```
  