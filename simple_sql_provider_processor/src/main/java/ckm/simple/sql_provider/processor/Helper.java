/*
 * Copyright (c) 2015 Kurt Mbanje
 *
 *   Apache License (Version 2.0)
 *
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package ckm.simple.sql_provider.processor;

import com.squareup.javapoet.ClassName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import ckm.simple.sql_provider.annotation.ProviderConfig;

import static javax.lang.model.element.ElementKind.INTERFACE;


/**
 * Created by kurt on 05 07 2015 .
 */
public class Helper {
    public static final String SIMPLE_PROVIDER_INTERFACE = ProviderConfig.class.getCanonicalName();

    public static final ClassName URI = ClassName.get("android.net", "Uri");
    public static final ClassName LOG = ClassName.get("android.util", "Log");
    public static final ClassName CONTENT_PROVIDER = ClassName.get("android.content", "ContentProvider");
    public static final ClassName CONTENT_VALUES = ClassName.get("android.content", "ContentValues");
    public static final ClassName CONTENT_URIS = ClassName.get("android.content", "ContentUris");

    public static final ClassName CURSOR = ClassName.get("android.database", "Cursor");
    public static final ClassName LIST = ClassName.get("java.util", "List");
    public static final ClassName ARRAYLIST = ClassName.get("java.util", "ArrayList");
    public static final ClassName CONTEXT = ClassName.get("android.content", "Context");
    public static final ClassName RESOURCES = ClassName.get("android.content.res", "Resources");
    public static final ClassName JODATIME = ClassName.get("org.joda.time","DateTime");


    public static boolean isPublic(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(Modifier.PUBLIC);
    }

    public boolean isInterface(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }
        return ((DeclaredType) typeMirror).asElement().getKind() == INTERFACE;
    }

    public boolean isValidColumType(TypeMirror element){
        return element.getKind().isPrimitive() || element.toString().equals(Number.class.getCanonicalName())
                || element.toString().equals(String.class.getCanonicalName())
                || element.toString().equals(Integer.class.getCanonicalName())
                || element.toString().equals(Long.class.getCanonicalName())
                || element.toString().equals(Short.class.getCanonicalName())
                || element.toString().equals(Float.class.getCanonicalName())
                || element.toString().equals(Double.class.getCanonicalName())
                || element.toString().equals(Date.class.getCanonicalName())
                || element.toString().equals(BigDecimal.class.getCanonicalName())
                || element.toString().equals(JODATIME.toString())
                ;
    }

    public static String getSqlType(TypeMirror element){
        if(element.toString().equals(String.class.getCanonicalName())) {
            return "TEXT";
        }else if(element.toString().equals(Integer.class.getCanonicalName())
                || element.toString().equals(int.class.getCanonicalName())

                || element.toString().equals(Short.class.getCanonicalName())
                || element.toString().equals(short.class.getCanonicalName())

                || element.toString().equals(Long.class.getCanonicalName())
                || element.toString().equals(long.class.getCanonicalName())){
            return "INTEGER";
        }else if(element.toString().equals(Double.class.getCanonicalName())
                || element.toString().equals(double.class.getCanonicalName())

                || element.toString().equals(Float.class.getCanonicalName())
                || element.toString().equals(float.class.getCanonicalName())

                || element.toString().equals(BigDecimal.class.getCanonicalName())
                ){
            return "REAL";
        }else if(element.toString().equals(Boolean.class.getCanonicalName())
                || element.toString().equals(boolean.class.getCanonicalName())){
            return "INTEGER";
        }else if(element.toString().equals(Date.class.getCanonicalName())
                || element.toString().equals(JODATIME.toString())
                ){
            return "INTEGER";
        }
        return null;
    }

    public static String getSqlDataType(TypeMirror element){
        if(element.toString().equals(String.class.getCanonicalName())) {
            return "String";
        }else if(element.toString().equals(Integer.class.getCanonicalName())
                || element.toString().equals(int.class.getCanonicalName())
                || element.toString().equals(Short.class.getCanonicalName())
                || element.toString().equals(short.class.getCanonicalName())) {
            return "Int";
        }else if(element.toString().equals(Long.class.getCanonicalName())
                || element.toString().equals(long.class.getCanonicalName())){
            return "Long";
        }else if(element.toString().equals(Double.class.getCanonicalName())
                || element.toString().equals(double.class.getCanonicalName())){
            return "Double";
        }else if(element.toString().equals(Float.class.getCanonicalName())
                || element.toString().equals(float.class.getCanonicalName())){
            return "Float";
        }else if(element.toString().equals(BigDecimal.class.getCanonicalName())){
            return "Long";
        }else if(element.toString().equals(Boolean.class.getCanonicalName())
                || element.toString().equals(boolean.class.getCanonicalName())){
            return "Int";
        } else if (element.toString().equals(Date.class.getCanonicalName())
                || element.toString().equals(JODATIME.toString())
                ) {
            return "Long";
        } else if (element.toString().equals(Boolean.class.getCanonicalName())
                || element.toString().equals(boolean.class.getCanonicalName())) {
            return "Int";
        }else{
            return "Long";
        }
    }

    public boolean isValidConfigClass(TypeMirror element){
        return isSubtypeOfType(element,SIMPLE_PROVIDER_INTERFACE);
    }

    public boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (otherType.equals(typeMirror.toString())) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }


    public String getPackageName(Elements elementUtils,TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }


    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }


    public static String getGetter(String variable){
        StringBuilder builder = new StringBuilder("get");
        if(variable.length() > 1) {
            final char first = variable.charAt(0);
            final char second = variable.charAt(1);
            if (!Character.isUpperCase(variable.charAt(0)) &&
                    first == 'm' && Character.isUpperCase(second)) {
                   builder.append(capitalize(variable.substring(1,variable.length())));
            }else{
                builder.append(capitalize(variable.substring(0,variable.length())));
            }
        }
        return builder.append("()").toString();
    }

    public static String getSetter(String variable){
        StringBuilder builder = new StringBuilder("set");
        if(variable.length() > 1) {
            final char first = variable.charAt(0);
            final char second = variable.charAt(1);
            if (!Character.isUpperCase(variable.charAt(0)) &&
                    first == 'm' && Character.isUpperCase(second)) {
                builder.append(capitalize(variable.substring(1,variable.length())));
            }else{
                builder.append(capitalize(variable.substring(0,variable.length())));
            }
        }
        return builder.toString();
    }

}
