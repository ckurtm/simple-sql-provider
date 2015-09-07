package ckm.simple.sql_provider.processor.generator;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.Filer;

import ckm.simple.sql_provider.annotation.SimpleSQLTable;
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
public class ProviderGenerator {
    private final Provider provider;
    private final List<Table> tables;
    private final String ID_PREFIX = "_ID";

    private ClassName URI_MATCHER = ClassName.get("android.content","UriMatcher");
    private ClassName SQLITE_QUERY_BUILDER = ClassName.get("android.database.sqlite", "SQLiteQueryBuilder");
    private ClassName SQLITE_DATABASE = ClassName.get("android.database.sqlite", "SQLiteDatabase");
    private ClassName SQLITE_EXCEPTION = ClassName.get("android.database", "SQLException");
    private ClassName TEXT_UTILS = ClassName.get("android.text", "TextUtils");

    private ClassName DATABASE_HELPER_CLASS;

    public ProviderGenerator(Provider provider, List<Table> providerTables) {
        this.provider = provider;
        this.tables = providerTables;
        DATABASE_HELPER_CLASS = ClassName.get(provider.clazz.packageName(), Helper.capitalize(provider.name) + DatabaseGenerator.CLASS_PREFIX);
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

        FieldSpec authority = FieldSpec.builder(String.class, "AUTHORITY")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("$S", provider.authority)
                .build();

        FieldSpec scheme = FieldSpec.builder(String.class, "SCHEME")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("$S", "content://")
                .build();


        FieldSpec single_row = FieldSpec.builder(String.class, "SINGLE_ROW_TYPE")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("$S","vnd.android.cursor.item/")
                .build();


        FieldSpec multi_row = FieldSpec.builder(String.class, "MULTI_ROW_TYPE")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("$S", "vnd.android.cursor.dir/")
                .build();


        FieldSpec matcher = FieldSpec.builder(URI_MATCHER, "matcher")
                .addModifiers(PRIVATE, STATIC, FINAL)
                .build();

        FieldSpec databaseHelper = FieldSpec.builder(DATABASE_HELPER_CLASS, "databaseHelper")
                .addModifiers(PRIVATE)
                .build();



        CodeBlock.Builder staticBlock  = CodeBlock.builder();
        staticBlock.addStatement("matcher = new $T($T.NO_MATCH)", URI_MATCHER, URI_MATCHER);

        for(Table table:tables){
            String contractTable = Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX;
            staticBlock.addStatement("matcher.addURI(AUTHORITY, $T.$L, $L)", ClassName.get(table.clazz.packageName(), contractTable),TableGenerator.TABLE_NAME, table.name.toUpperCase());
            staticBlock.addStatement("matcher.addURI(AUTHORITY, $T.$L + \"/*\", $L_ID)", ClassName.get(table.clazz.packageName(), contractTable),TableGenerator.TABLE_NAME, table.name.toUpperCase());
        }


        TypeSpec.Builder builder = TypeSpec.classBuilder(provider.clazz.simpleName())
                .superclass(Helper.CONTENT_PROVIDER)
                .addModifiers(PUBLIC, FINAL)
                .addField(authority)
                .addField(scheme)
                .addField(single_row)
                .addField(multi_row)
                .addField(matcher)
                .addField(databaseHelper)
                .addStaticBlock(staticBlock.build());


        for (int i = 0,j=tables.size(); i < j; i++) {
            Table table= tables.get(i);
            builder.addField(createMultiRowField(table, (i*2) + 1));
            builder.addField(createSingleRowField(table,(i*2) + 2));
        }

        builder.addMethod(getOnCreate())
                .addMethod(getQuery())
                .addMethod(getType())
                .addMethod(getInsert())
                .addMethod(getDelete())
                .addMethod(getUpdate())
                .addMethod(getNotifyUri())
                .addMethod(getDatabaseHelper())
                .addMethod(getReset1())
                .addMethod(getReset2())
                .addMethod(getContentProvider());
        return builder.build();

    }


    private FieldSpec createSingleRowField(Table table,int index){
        return FieldSpec.builder(int.class,table.name.toUpperCase() + ID_PREFIX)
                .addModifiers(PRIVATE, STATIC, FINAL)
                .initializer("$L",index)
                .build();
    }

    private FieldSpec createMultiRowField(Table table,int index){
        return FieldSpec.builder(int.class, table.name.toUpperCase())
                .addModifiers(PRIVATE, STATIC, FINAL)
                .initializer("$L", index)
                .build();
    }

    private MethodSpec getOnCreate(){
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .returns(boolean.class)
                .addModifiers(PUBLIC)
                .addStatement("databaseHelper = new $T(new $T(), getContext(),$S,$L)", DATABASE_HELPER_CLASS, provider.configClass, provider.database, provider.version)
                .addStatement("return true")
                .build();
    }

    private MethodSpec getQuery(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("query")
                .addAnnotation(Override.class)
                .returns(Helper.CURSOR)
                .addModifiers(PUBLIC)
                .addParameter(Helper.URI, "uri")
                .addParameter(String[].class, "projection")
                .addParameter(String.class, "selection")
                .addParameter(String[].class, "selectionArgs")
                .addParameter(String.class, "sortOrder")
                .addStatement("$T qb = new $T()", SQLITE_QUERY_BUILDER, SQLITE_QUERY_BUILDER)
                .beginControlFlow("switch (matcher.match(uri)) ");

        for (int i = 0,j=tables.size(); i < j; i++) {
            Table table = tables.get(i);
            ClassName tableClass = ClassName.get(table.clazz.packageName(),Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement(" case $L:", table.name.toUpperCase());
            if(table.query.equalsIgnoreCase(SimpleSQLTable.NULL)) {
                builder.addStatement("   qb.setTables($T.$L)", tableClass, TableGenerator.TABLE_NAME);
            }else{
                builder.addStatement("   qb.setTables($S)", table.query);
            }
            builder.addStatement("break")
                    .addStatement(" case $L_ID:", table.name.toUpperCase());
            if(table.query.equalsIgnoreCase(SimpleSQLTable.NULL)) {
                builder.addStatement("   qb.setTables($T.$L)", tableClass, TableGenerator.TABLE_NAME);
                if(table.hasPrimary()) {
                    builder.addStatement("   qb.appendWhere($T.$L + \"=\" + uri.getLastPathSegment())", tableClass, TableGenerator.FIELD_POSTFIX+table.getPrimary().name.toUpperCase());
                }
            }else{
                builder.addStatement("   qb.setTables($S)", table.query);
            }


            builder.addStatement("break");
        }

        builder.addStatement("default: \n throw new $T(\"Unknown URI \" + uri)", IllegalArgumentException.class)
                .endControlFlow()
                .addStatement("$T db = databaseHelper.getReadableDatabase()",SQLITE_DATABASE)
                .addStatement("$T cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)",Helper.CURSOR)
                .beginControlFlow("if(getContext() != null && getContext().getContentResolver() != null) ")
                .addStatement("cursor.setNotificationUri(getContext().getContentResolver(), uri)")
                .endControlFlow();
        builder.addStatement("return cursor");
        return builder.build();
    }

    private MethodSpec getType(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getType")
                .addAnnotation(Override.class)
                .returns(String.class)
                .addModifiers(PUBLIC)
                .addParameter(Helper.URI, "uri")
                .beginControlFlow("switch (matcher.match(uri)) ");
        for (int i = 0,j=tables.size(); i < j; i++) {
            Table table = tables.get(i);
            ClassName tableClass = ClassName.get(table.clazz.packageName(),Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement(" case $L:", table.name.toUpperCase())
                    .addStatement("   return $T.CONTENT_TYPE", tableClass)
                    .addStatement(" case $L_ID:", table.name.toUpperCase())
                    .addStatement("   return $T.CONTENT_ITEM_TYPE", tableClass);
        }
        builder.addStatement("default:\n throw new $T(\"Unknown URI \" + uri)", IllegalArgumentException.class)
                .endControlFlow();
        return builder.build();
    }


    private MethodSpec getInsert(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insert")
                .addAnnotation(Override.class)
                .returns(Helper.URI)
                .addModifiers(PUBLIC)
                .addParameter(Helper.URI, "uri")
                .addParameter(Helper.CONTENT_VALUES, "values");
        builder.addStatement("$T matchFound = false", boolean.class);
        for (int index = 0,max=tables.size(); index< max; index++) {
            Table table = tables.get(index);
            builder.beginControlFlow("if (matcher.match(uri) == $L)", table.name.toUpperCase())
                    .addStatement("matchFound=true")
                    .endControlFlow();
        }
        builder.beginControlFlow("if(!matchFound)")
                .addStatement("throw new $T(\"Unknown URI \" + uri)",IllegalArgumentException.class)
                .endControlFlow();
        builder.addStatement("$T table", String.class);
        builder.beginControlFlow("switch (matcher.match(uri)) ");
        for (int index = 0,max=tables.size(); index< max; index++) {
            Table table = tables.get(index);
            ClassName tableClass = ClassName.get(table.clazz.packageName(),Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement(" case $L:", table.name.toUpperCase())
                    .addStatement("   table = $T.$L", tableClass, TableGenerator.TABLE_NAME)
                    .addStatement("break");
        }
        builder.addStatement("default:\n throw new $T(\"Unknown URI \" + uri)", IllegalArgumentException.class);
        builder.endControlFlow();
        builder.addStatement("$T db = databaseHelper.getWritableDatabase()",SQLITE_DATABASE)
                .addStatement("long rowId = db.insert(table, null, values)")
                .beginControlFlow("if (rowId > 0) ")
                .addStatement("$T noteUri = $T.withAppendedId(uri, rowId)",Helper.URI,Helper.CONTENT_URIS)
                .addStatement("notifyUri(noteUri)")
                .addStatement("return noteUri")
                .endControlFlow()
                .addStatement("throw new $T(\"Failed to insert row into \" + uri)", SQLITE_EXCEPTION);
        return  builder.build();

    }


    private MethodSpec getDelete(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete")
                .addAnnotation(Override.class)
                .returns(int.class)
                .addModifiers(PUBLIC)
                .addParameter(Helper.URI, "uri")
                .addParameter(String.class, "where")
                .addParameter(String[].class, "whereArgs");

        builder.addStatement("$T db = databaseHelper.getWritableDatabase()", SQLITE_DATABASE)
                .addStatement("$T finalWhere = null", String.class)
                .addStatement("$T count =0", int.class)
                .beginControlFlow("switch (matcher.match(uri)) ");


        for (int i = 0, j = tables.size(); i < j; i++) {
            Table table = tables.get(i);
            ClassName tableClass = ClassName.get(table.clazz.packageName(), Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement(" case $L:", table.name.toUpperCase())
                    .addStatement("   count = db.delete($T.$L,where,whereArgs)", tableClass, TableGenerator.TABLE_NAME)
                    .addStatement("break")
                    .addStatement(" case $L_ID:", table.name.toUpperCase());
            if (table.hasPrimary() || !table.queryKey.equals("")) {
                if(table.queryKey.equals("")) {
                    builder.addStatement("   finalWhere = $T.$L + \" = \" + uri.getLastPathSegment()", tableClass, TableGenerator.FIELD_POSTFIX + table.getPrimary().name.toUpperCase());
                }else{
                    builder.addStatement("   finalWhere = $T.$L + \" = \" + uri.getLastPathSegment()", tableClass, TableGenerator.FIELD_POSTFIX + table.getQueryKey().name.toUpperCase());
                }
                builder.beginControlFlow("if (where != null && !$T.isEmpty(finalWhere)) ", TEXT_UTILS)
                        .addStatement("finalWhere = finalWhere + \" AND \" + where")
                        .endControlFlow()
                        .addStatement(" count = db.delete($T.$L,finalWhere,whereArgs)", tableClass, TableGenerator.TABLE_NAME);
            }
            builder.addStatement("break");
        }


        builder.addStatement("default:\n" +
                "  throw new $T(\"Unknown URI \" + uri)", IllegalArgumentException.class)
                .endControlFlow()
                .addStatement("notifyUri(uri)")
                .addStatement("return count");
        return builder.build();
    }

    private MethodSpec getUpdate(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update")
                .addAnnotation(Override.class)
                .returns(int.class)
                .addModifiers(PUBLIC)
                .addParameter(Helper.URI, "uri")
                .addParameter(Helper.CONTENT_VALUES, "values")
                .addParameter(String.class, "where")
                .addParameter(String[].class, "whereArgs");

        builder.addStatement("$T db = databaseHelper.getWritableDatabase()", SQLITE_DATABASE)
                .addStatement("$T finalWhere = null", String.class)
                .addStatement("$T count = 0", int.class)
                .beginControlFlow("switch (matcher.match(uri)) ");


        for (int i = 0, j = tables.size(); i < j; i++) {
            Table table = tables.get(i);
            ClassName tableClass = ClassName.get(table.clazz.packageName(), Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement(" case $L:", table.name.toUpperCase())
                    .addStatement("   count = db.update($T.$L,values,where,whereArgs)", tableClass, TableGenerator.TABLE_NAME)
                    .addStatement("break")
                    .addStatement(" case $L_ID:", table.name.toUpperCase());
            if (table.hasPrimary() || !table.queryKey.equals("")) {
                if(table.queryKey.equals("")) {
                    builder.addStatement("   finalWhere = $T.$L + \" = \" + uri.getLastPathSegment()", tableClass, TableGenerator.FIELD_POSTFIX + table.getPrimary().name.toUpperCase());
                }else{
                    builder.addStatement("   finalWhere = $T.$L + \" = \" + uri.getLastPathSegment()", tableClass, TableGenerator.FIELD_POSTFIX + table.getQueryKey().name.toUpperCase());
                }
                builder.beginControlFlow("if (where != null && !$T.isEmpty(finalWhere)) ", TEXT_UTILS)
                        .addStatement("finalWhere = finalWhere + \" AND \" + where")
                        .endControlFlow()
                        .addStatement(" count = db.update($T.$L,values,finalWhere,whereArgs)", tableClass, TableGenerator.TABLE_NAME);
            }
            builder.addStatement("break");
        }


        builder.addStatement("default:\n" +
                "  throw new $T(\"Unknown URI \" + uri)", IllegalArgumentException.class)
                .endControlFlow()
                .addStatement("notifyUri(uri)")
                .addStatement("return count");
        return builder.build();
    }



    private MethodSpec getNotifyUri(){
        return MethodSpec.methodBuilder("notifyUri")
                .addModifiers(PRIVATE)
                .addParameter(Helper.URI, "uri")
                .returns(void.class)
                .beginControlFlow("if (getContext() != null) ")
                .beginControlFlow(" if (getContext().getContentResolver() != null) ")
                .addStatement("getContext().getContentResolver().notifyChange(uri, null)")
                .endControlFlow()
                .endControlFlow()
                .build();
    }


    private MethodSpec getReset1(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("reset")
                .addModifiers(PUBLIC)
                .addParameter(String.class, "database_name")
                .addParameter(int.class, "database_version")
                .addStatement("databaseHelper = new $T(new $T(), getContext(),database_name,database_version)", DATABASE_HELPER_CLASS, provider.configClass);
        for (int i = 0, j = tables.size(); i < j; i++) {
            Table table = tables.get(i);
            ClassName tableClass = ClassName.get(table.clazz.packageName(), Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement("notifyUri($T.CONTENT_URI)", tableClass);
        }
        builder.returns(void.class);
        return  builder.build();
    }


    private MethodSpec getReset2(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("reset")
                .addModifiers(PUBLIC)
                .addParameter(String.class, "database_name")
                .addStatement("databaseHelper = new $T(new $T(), getContext(),database_name,databaseHelper.getDatabaseVersion())", DATABASE_HELPER_CLASS, provider.configClass);
        for (int i = 0, j = tables.size(); i < j; i++) {
            Table table = tables.get(i);
            ClassName tableClass = ClassName.get(table.clazz.packageName(), Helper.capitalize(table.name) + TableGenerator.CLASS_PREFIX);
            builder.addStatement("notifyUri($T.CONTENT_URI)", tableClass);
        }
        builder.returns(void.class);
        return  builder.build();
    }

    private MethodSpec getDatabaseHelper(){
        return MethodSpec.methodBuilder("getDatabaseHelper")
                .returns(DATABASE_HELPER_CLASS)
                .addModifiers(PUBLIC)
                .addStatement("return databaseHelper")
                .build();
    }


    private MethodSpec getContentProvider() {
        ClassName PROVIDER_CLASS = ClassName.get(provider.clazz.packageName(), Helper.capitalize(provider.name));
        ClassName PROVIDER_CLIENT = ClassName.get("android.content", "ContentProviderClient");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getContentProvider")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(Helper.CONTEXT, "context")
                .returns(PROVIDER_CLASS)
                .addStatement("$T client = context.getContentResolver().acquireContentProviderClient($T.AUTHORITY)", PROVIDER_CLIENT, PROVIDER_CLASS)
                .beginControlFlow("if(client != null)")
                .addStatement("$T contentProvider = client.getLocalContentProvider()", Helper.CONTENT_PROVIDER)
                .addStatement("client.release()")
                .addStatement("return ($T) contentProvider",PROVIDER_CLASS)
                .endControlFlow()
                .addStatement("return null");
        return builder.build();
    }
}
