package ckm.simple.sql_provider.processor.generator;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import ckm.simple.sql_provider.processor.Helper;
import ckm.simple.sql_provider.processor.Messenger;
import ckm.simple.sql_provider.processor.internal.Column;
import ckm.simple.sql_provider.processor.internal.Provider;
import ckm.simple.sql_provider.processor.internal.Table;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by kurt on 03 09 2015 .
 */
public class TableGenerator {

    public static final String CLASS_PREFIX = "Table";
    public static final String FIELD_POSTFIX = "FIELD_";
    public static final String TABLE_NAME = "TABLE_NAME";

    private final Table table;
    private final Provider provider;

    public TableGenerator(Provider provider, Table table) {
        this.table = table;
        this.provider = provider;
    }


    public void generate(Messenger messenger,Filer filer) {
        JavaFile javaFile = JavaFile.builder(table.clazz.packageName(), generateClass()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messenger.error(table.element, "failed to generate class for %s: %s", table.clazz, e.getMessage());
        }
    }

    private TypeSpec generateClass() {
        ClassName content_provider = ClassName.get(provider.clazz.packageName(), table.provider);

        FieldSpec table_field = FieldSpec.builder(String.class, TABLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S",table.name)
                .build();

        FieldSpec content_uri_field = FieldSpec.builder(Helper.URI, "CONTENT_URI")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.parse($T.SCHEME + $T.AUTHORITY + \"/\" + $L)", Helper.URI, content_provider, content_provider,TABLE_NAME)
                .build();

        FieldSpec content_type_field = FieldSpec.builder(String.class, "CONTENT_TYPE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.MULTI_ROW_TYPE + $T.AUTHORITY + \".\" + $L",content_provider,content_provider,TABLE_NAME)
                .build();

        FieldSpec content_item_type_field = FieldSpec.builder(String.class, "CONTENT_ITEM_TYPE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.SINGLE_ROW_TYPE + $T.AUTHORITY + \".\" + $L",content_provider,content_provider,TABLE_NAME)
                .build();


        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("$S + ").append("$L + $S + \n");
        args.add("CREATE TABLE ");
        args.add(TABLE_NAME);
        args.add("(");

        for (int i = 0,j=table.columns.size(); i < j; i++) {
            Column col = table.columns.get(i);
            sql.append(" $L + $S ");
//
            sql.append(" + ");
            args.add(FIELD_POSTFIX + col.name.toUpperCase());

            String type =  " " + Helper.getSqlType(col.element.asType());
            if(i != j-1 && !col.primary){
                type += ",";
            }
            args.add(type);
            if(col.primary) {
                sql.append(" $S +");
                if(!col.autoincrement) {
                    args.add(" PRIMARY KEY,");
                }else{
                    args.add(" PRIMARY KEY AUTOINCREMENT,");
                }
            }

            sql.append("\n");
        }

        sql.append("$S");
        args.add(")");
        FieldSpec create_field = FieldSpec.builder(String.class, "CREATE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(sql.toString(),args.toArray())
                .build();

        TypeSpec.Builder builder = TypeSpec.classBuilder(Helper.capitalize(table.name) + CLASS_PREFIX)
                .addModifiers(PUBLIC, FINAL)
                .addField(table_field)
                .addField(content_uri_field)
                .addField(content_type_field)
                .addField(content_item_type_field);

        for(Column column:table.columns){
            builder.addField(createField(column));
        }

        builder.addField(create_field);
        builder.addMethod(getContentValues());
        builder.addMethod(getRow());
        builder.addMethod(getRows());
        return builder.build();
    }


    private FieldSpec createField(Column column){
        return FieldSpec.builder(String.class, FIELD_POSTFIX + column.name.toUpperCase())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", column.name)
                .build();
    }

    private MethodSpec getContentValues(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getContentValues")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(table.clazz, "param")
                .addParameter(boolean.class, "includePrimary")
                .returns(Helper.CONTENT_VALUES)
                .addStatement("$T values = new $T()", Helper.CONTENT_VALUES, Helper.CONTENT_VALUES);
        for (Column column : table.columns) {
            String elementType = column.element.asType().toString();
            String getterMethod = Helper.getGetter(column.element.getSimpleName().toString());
            String colname = column.element.getSimpleName().toString();

            if(column.primary){
                builder.beginControlFlow("if(includePrimary)");
            }
            if (elementType.equals(Date.class.getCanonicalName())) {
                if (column.isPrivate()) {
                    builder.addStatement("values.put($L,param.$L != null ? param.$L.getTime() : 0)", FIELD_POSTFIX + column.name.toUpperCase(), getterMethod, getterMethod);
                } else {
                    builder.addStatement("values.put($L,param.$L != null ? param.$L.getTime() : 0)", FIELD_POSTFIX + column.name.toUpperCase(), colname, colname);
                }
            } else if (elementType.equals(Helper.JODATIME.toString())){
                if (column.isPrivate()) {
                    builder.addStatement("values.put($L,param.$L != null ? param.$L.getMillis() : 0)", FIELD_POSTFIX + column.name.toUpperCase(), getterMethod, getterMethod);
                } else {
                    builder.addStatement("values.put($L,param.$L != null ? param.$L.getMillis() : 0)", FIELD_POSTFIX + column.name.toUpperCase(), colname, colname);
                }
            } else if (elementType.equals(BigDecimal.class.getCanonicalName())) {
                if(column.isPrivate()) {
                    builder.addStatement("values.put($L,param.$L != null ? param.$L.doubleValue():0)", FIELD_POSTFIX + column.name.toUpperCase(), getterMethod, getterMethod);
                }else{
                    builder.addStatement("values.put($L,param.$L != null ? param.$L.doubleValue():0)", FIELD_POSTFIX + column.name.toUpperCase(), colname, colname);
                }
            } else if (elementType.equals(boolean.class.getCanonicalName()) || elementType.equals(Boolean.class.getCanonicalName())) {
                if(column.isPrivate()) {
                    builder.addStatement("values.put($L,param.$L ? 1:0)", FIELD_POSTFIX + column.name.toUpperCase(), getterMethod);
                }else{
                    builder.addStatement("values.put($L,param.$L ? 1:0)", FIELD_POSTFIX + column.name.toUpperCase(), colname);
                }
            }else{
                if(column.isPrivate()) {
                    builder.addStatement("values.put($L, param.$L)", FIELD_POSTFIX + column.name.toUpperCase(), getterMethod);
                }else{
                    builder.addStatement("values.put($L, param.$L)", FIELD_POSTFIX + column.name.toUpperCase(), colname);
                }
            }
            if(column.primary){
                builder.endControlFlow();
            }
        }
        builder.addStatement("return values");
        return builder.build();
    }



    private MethodSpec getRow(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRow")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(Helper.CURSOR, "cursor")
                .addParameter(boolean.class, "closeCursor")
                .returns(table.clazz)
                .beginControlFlow("if(cursor.isBeforeFirst())")
                .addStatement("cursor.moveToFirst()")
                .endControlFlow()
                .addStatement("$T param = new $T()", table.clazz, table.clazz);

        for (int i = 0,j=table.columns.size(); i < j; i++) {
            Column column = table.columns.get(i);
            String elementType = column.element.asType().toString();
            String COL_FIELD = FIELD_POSTFIX + column.name.toUpperCase();
            String setterMethod = Helper.getSetter(column.element.getSimpleName().toString());
            String colname = column.element.getSimpleName().toString();

            if (elementType.equals(Date.class.getCanonicalName())) {
                builder.addStatement("long p$L = cursor.get$L(cursor.getColumnIndex($L))", i, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                if (column.isPrivate()) {
                    builder.addStatement("param.$L(new $T(p$L))", setterMethod, Date.class, i);
                } else {
                    builder.addStatement("param.$L = new $T(p$L)", colname, Date.class, i);
                }
            }else if(elementType.equals(Helper.JODATIME.toString())){
                builder.addStatement("long p$L = cursor.get$L(cursor.getColumnIndex($L))", i, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                if (column.isPrivate()) {
                    builder.addStatement("param.$L(new $T(p$L))", setterMethod, Helper.JODATIME, i);
                } else {
                    builder.addStatement("param.$L = new $T(p$L)", colname, Helper.JODATIME, i);
                }
            }else if(elementType.equals(BigDecimal.class.getCanonicalName())) {
                builder.addStatement("long p$L = cursor.get$L(cursor.getColumnIndex($L))", i, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                if(column.isPrivate()) {
                    builder.addStatement("param.$L(new $T(p$L))", setterMethod, BigDecimal.class, i);
                }else{
                    builder.addStatement("param.$L = new $T(p$L)", colname, BigDecimal.class, i);
                }
            } else if (elementType.equalsIgnoreCase(Boolean.class.getCanonicalName()) || elementType.equalsIgnoreCase(boolean.class.getCanonicalName())) {
                if(column.isPrivate()) {
                    builder.addStatement("param.$L(cursor.get$L(cursor.getColumnIndex($L)) > 0)", setterMethod, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                }else{
                    builder.addStatement("param.$L = cursor.get$L(cursor.getColumnIndex($L)) > 0", colname, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                }
            }else{
                if(column.isPrivate()) {
                    builder.addStatement("param.$L(cursor.get$L(cursor.getColumnIndex($L)))", setterMethod, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                }else{
                    builder.addStatement("param.$L = cursor.get$L(cursor.getColumnIndex($L))", colname, Helper.getSqlDataType(column.element.asType()), COL_FIELD);
                }
            }

        }

        builder.beginControlFlow("if (closeCursor)");
        builder.addStatement("cursor.close()");
        builder.endControlFlow();
        builder.addStatement("return param");
        return builder.build();
    }


    private MethodSpec getRows() {
        TypeName listOfTableType = ParameterizedTypeName.get(Helper.LIST, table.clazz);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRows")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(Helper.CURSOR, "cursor")
                .addParameter(boolean.class, "closeCursor")
                .returns(listOfTableType)
                .addStatement("$T items = new $T()", listOfTableType, Helper.ARRAYLIST)
                .addStatement("cursor.moveToPosition(-1)")
                .beginControlFlow("while (cursor.moveToNext())")
                .addStatement("$T item = getRow(cursor, false)", table.clazz)
                .addStatement("items.add(item)")
                .endControlFlow()
                .beginControlFlow("if (closeCursor)")
                .addStatement("cursor.close()")
                .endControlFlow()
                .addStatement("return items");
        return builder.build();
    }





}
