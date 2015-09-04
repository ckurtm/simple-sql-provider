package ckm.simple.sql_provider.processor.generator;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.Filer;

import ckm.simple.sql_provider.UpgradeScript;
import ckm.simple.sql_provider.annotation.ProviderConfig;
import ckm.simple.sql_provider.processor.Helper;
import ckm.simple.sql_provider.processor.Messenger;
import ckm.simple.sql_provider.processor.internal.Provider;
import ckm.simple.sql_provider.processor.internal.Table;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by kurt on 03 09 2015 .
 */
public class DatabaseGenerator {

    public static final ClassName SQLITE_OPEN_HELPER = ClassName.get("android.database.sqlite", "SQLiteOpenHelper");
    public static final ClassName SQLITE_DATABASE = ClassName.get("android.database.sqlite", "SQLiteDatabase");

    public static final ClassName BUFFERED_READER = ClassName.get("java.io", "BufferedReader");
    public static final ClassName IO_EXCEPTION = ClassName.get("java.io", "IOException");
    public static final ClassName INPUT_STREAM = ClassName.get("java.io", "InputStream");
    public static final ClassName INPUT_STREAM_READER = ClassName.get("java.io", "InputStreamReader");

    private final Provider provider;
    private final List<Table> tables;

    public static final String CLASS_PREFIX = "DatabaseHelper";

    public DatabaseGenerator(Provider provider, List<Table> tables) {
        this.provider = provider;
        this.tables = tables;
    }


    public void generate(Messenger messenger,Filer filer) {
        JavaFile javaFile = JavaFile.builder(provider.clazz.packageName(), generateClass()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messenger.error(provider.element, "failed to generate class for %s: %s", provider.clazz, e.getMessage());
        }
    }


    private TypeSpec generateClass()  {

        FieldSpec tag = FieldSpec.builder(String.class, "TAG")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("$S", provider.name + CLASS_PREFIX)
                .build();

        FieldSpec databaseName = FieldSpec.builder(String.class, "database_name")
                .addModifiers(PRIVATE)
//                .initializer("$S",provider.database)
                .build();

        FieldSpec databaseVersion = FieldSpec.builder(int.class, "database_version")
                .addModifiers(PRIVATE)
//                .initializer("$L", provider.version)
                .build();

        FieldSpec providerConfig = FieldSpec.builder(ProviderConfig.class, "config")
                .addModifiers(PRIVATE)
                .build();

        FieldSpec context = FieldSpec.builder(Helper.CONTEXT, "context")
                .addModifiers(PRIVATE)
                .build();


        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(ProviderConfig.class, "config")
                .addParameter(Helper.CONTEXT, "context")
                .addParameter(String.class, "database_name")
                .addParameter(int.class, "database_version")
                .addStatement("super(context,database_name,null,database_version)")
                .addStatement("this.config = config")
                .addStatement("this.context = context")
                .addStatement("this.database_name = database_name")
                .addStatement("this.database_version = database_version")
                .build();


        TypeSpec.Builder builder = TypeSpec.classBuilder(provider.clazz.simpleName()+CLASS_PREFIX)
                .superclass(SQLITE_OPEN_HELPER)
                .addModifiers(PUBLIC, FINAL)
                .addField(tag)
                .addField(databaseName)
                .addField(databaseVersion)
                .addField(providerConfig)
                .addField(context)
                .addMethod(constructor)
                .addMethod(getOnCreate())
                .addMethod(getOnUpgrade())
                .addMethod(getReadAndExecuteSQLScript())
                .addMethod(getExecuteSQLScript())
                .addMethod(getDatabaseName())
                .addMethod(getDatabaseVersion());

        return builder.build();
    }


    private MethodSpec getDatabaseName(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getDatabaseName")
                .addModifiers(PUBLIC)
                .addStatement("return database_name")
                .returns(String.class);
        return builder.build();
    }



    private MethodSpec getDatabaseVersion(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getDatabaseVersion")
                .addModifiers(PUBLIC)
                .addStatement("return database_version")
                .returns(int.class);
        return builder.build();
    }

    private MethodSpec getOnCreate(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(SQLITE_DATABASE, "db")
                .returns(void.class);
        for(Table table:tables){
            ClassName tableClass = ClassName.get(table.clazz.packageName(),Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement("db.execSQL($T.CREATE)",tableClass);
        }
        return builder.build();
    }


    private MethodSpec getOnUpgrade(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onUpgrade")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(SQLITE_DATABASE, "db")
                .addParameter(int.class, "oldVersion")
                .addParameter(int.class, "newVersion")
                .addStatement("$T.d(TAG,\"Upgrading database from version \" + oldVersion + \" to \" + newVersion)", Helper.LOG)
                .beginControlFlow("if (config != null)")
                .addStatement("$T[] scripts = config.getUpdateScripts()", UpgradeScript.class)
                .beginControlFlow("for($T script:scripts)",UpgradeScript.class)
                .beginControlFlow("if (oldVersion < script.oldVersion)")
                .addStatement("readAndExecuteSQLScript(db, context, script.sqlScriptResource)")
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .returns(void.class);
        return builder.build();
    }



    private MethodSpec getReadAndExecuteSQLScript(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("readAndExecuteSQLScript")
                .addModifiers(PRIVATE)
                .addParameter(SQLITE_DATABASE, "db")
                .addParameter(Helper.CONTEXT, "ctx")
                .addParameter(int.class, "sqlScriptResId")
                .beginControlFlow("if (sqlScriptResId == 0)")
                .addStatement("throw new $T(\"No SQL script found for specified resource.\")", IllegalArgumentException.class)
                .endControlFlow()
                .addStatement("$T.d(TAG, \"Script found. Executing...\")", Helper.LOG)
                .addStatement("$T res = ctx.getResources()", Helper.RESOURCES)
                .addStatement("$T reader = null", BUFFERED_READER)
                .beginControlFlow("try")
                .addStatement("$T is = res.openRawResource(sqlScriptResId)", INPUT_STREAM)
                .addStatement("$T isr = new $T(is)", INPUT_STREAM_READER, INPUT_STREAM_READER)
                .addStatement("reader = new $T(isr)", BUFFERED_READER)
                .addStatement("executeSQLScript(db, reader)")
                .endControlFlow()
                .beginControlFlow("catch ($T e)", IO_EXCEPTION)
                .addStatement("$T.e(TAG, \"problem running update script\", e)", Helper.LOG)
                .endControlFlow()
                .beginControlFlow("finally")
                .beginControlFlow("if (reader != null) ")
                .beginControlFlow("try")
                .addStatement("reader.close()")
                .endControlFlow()
                .beginControlFlow("catch ($T e) ", IO_EXCEPTION)
                .addStatement("$T.e(TAG, \"problem closing the update script\", e)", Helper.LOG)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .returns(void.class);
        return builder.build();
    }

    private MethodSpec getExecuteSQLScript(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("executeSQLScript")
                .addModifiers(PRIVATE)
                .addException(IO_EXCEPTION)
                .addParameter(SQLITE_DATABASE, "db")
                .addParameter(BUFFERED_READER, "reader")
                .addStatement("$T line", String.class)
                .addStatement("$T statement = new $T()", StringBuilder.class, StringBuilder.class)
                .beginControlFlow("while ((line = reader.readLine()) != null)")
                .addStatement("statement.append(line)")
                .addStatement("statement.append(\"\\n\")")
                .beginControlFlow(" if (line.endsWith(\";\"))")
                .addStatement("db.execSQL(statement.toString())")
                .addStatement(" statement = new $T()", StringBuilder.class)
                .endControlFlow()
                .endControlFlow()
                .returns(void.class);
        return builder.build();
    }

}
