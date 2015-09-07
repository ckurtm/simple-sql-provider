package ckm.simple.sql_provider.processor;


import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import ckm.simple.sql_provider.annotation.SimpleSQLColumn;
import ckm.simple.sql_provider.annotation.SimpleSQLConfig;
import ckm.simple.sql_provider.annotation.SimpleSQLTable;
import ckm.simple.sql_provider.processor.generator.DatabaseGenerator;
import ckm.simple.sql_provider.processor.generator.ProviderGenerator;
import ckm.simple.sql_provider.processor.generator.TableGenerator;
import ckm.simple.sql_provider.processor.internal.Column;
import ckm.simple.sql_provider.processor.internal.Provider;
import ckm.simple.sql_provider.processor.internal.Table;

/**
 * Created by kurt on 03 09 2015 .
 */
public class SimpleSQLProviderProcesor extends AbstractProcessor {
    private final Messenger messenger = new Messenger();
    private Helper helper;
    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messenger.init(env);
        helper = new Helper();
        filer = env.getFiler();
        elementUtils = env.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(SimpleSQLConfig.class.getCanonicalName());
        annotations.add(SimpleSQLColumn.class.getCanonicalName());
        annotations.add(SimpleSQLTable.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        Map<String,Provider> providers = new HashMap<>();
        Map<String,List<Table>> tables = new HashMap<>();

        //parse the annotated classes
        parseProviders(environment, providers);
        parseTables(environment, tables);

        //generate the code
         for(String providername:providers.keySet()){
             Provider provider = providers.get(providername);
             List<Table> providerTables = tables.get(providername);

             ProviderGenerator providerGenerator = new ProviderGenerator(provider,providerTables);
             providerGenerator.generate(messenger,filer);
             for(Table table:providerTables){
                 TableGenerator tableGenerator = new TableGenerator(provider,table);
                 tableGenerator.generate(messenger,filer);
             }
             DatabaseGenerator databaseGenerator = new DatabaseGenerator(provider,providerTables);
             databaseGenerator.generate(messenger,filer);
         }


        return false;
    }

    private void parseProviders(RoundEnvironment environment, Map<String,Provider> providers){
        for (Element element : environment.getElementsAnnotatedWith(SimpleSQLConfig.class)) {
//            messenger.warn(element, "----PARSE_PROVIDERS() %s", element);
            if(!helper.isValidConfigClass(element.asType())){
                messenger.error(element, "@SimpleSQLConfig can only be used on implementations of ProviderConfig class");
            }else {
                Provider provider = getProvider(element);
                if(!providers.keySet().contains(provider.name)) {
                    providers.put(provider.name, provider);
//                    messenger.warn(element, "%s", provider);
                }else{
                    messenger.error(element, "more than 1 class annotated with @SimpleSQLConfig has been declared to define a provider with the name <%s>",provider.name);
                }
            }
        }
    }


    private void parseTables(RoundEnvironment environment,Map<String,List<Table>> tables){
        for (Element element : environment.getElementsAnnotatedWith(SimpleSQLTable.class)) {
//            messenger.warn(element, "----PARSE_TABLES() %s", element);
            TypeElement typeElement = (TypeElement) element;
            final SimpleSQLTable tableAnnotation = element.getAnnotation(SimpleSQLTable.class);
            if(helper.isInterface(element.asType()) || !Helper.isPublic(typeElement)){
                messenger.error(element, "%s can only be applied public classes %s is not a valid class", tableAnnotation,element.getSimpleName());
            }
            Table table = new Table();
            table.element = element;
            table.name = tableAnnotation.table();
            table.provider = tableAnnotation.provider();
            String packageName = helper.getPackageName(elementUtils, typeElement);
            table.clazz = ClassName.get(packageName,typeElement.getSimpleName().toString());
            boolean hasValidConstructor = false;
            for(Element classElement:element.getEnclosedElements()) {
                if (classElement.getKind() == ElementKind.FIELD) {
                    final SimpleSQLColumn columnAnnotation = classElement.getAnnotation(SimpleSQLColumn.class);
                    if (columnAnnotation != null) {
                        Column column = new Column(columnAnnotation.primary(),columnAnnotation.autoincrement(),columnAnnotation.value(),classElement);
                        if (!table.columns.contains(column)) {
                            if(helper.isValidColumType(classElement.asType())){
                                table.columns.add(column);
                            }else{
                                messenger.error(classElement, "return type of %s is not currently supported for table columns", classElement.asType());
                            }
                        } else {
                            messenger.error(classElement, "duplicate column name <%s> in %s [column name is not case sensitive]", columnAnnotation.value(), table.clazz);
                        }
                    }
                } else if (classElement.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement el = (ExecutableElement) classElement;
                    if (el.getParameters().size() == 0 && el.getReturnType().getKind() == TypeKind.VOID && el.getModifiers().contains(Modifier.PUBLIC)) {
                        hasValidConstructor = true;
                    }
                }
            }
            if(!hasValidConstructor){
                messenger.error(element, " %s has no default empty constructor",table.clazz);
            }
            List<Table> providerTables = tables.get(table.provider);
            if (providerTables == null) {
                providerTables = new ArrayList<>();
            }
            providerTables.add(table);
            tables.put(table.provider, providerTables);
            //messenger.warn(element, "%s", table);
        }
    }


    private Provider getProvider(Element element) {
        final SimpleSQLConfig config = element.getAnnotation(SimpleSQLConfig.class);
        TypeElement typeElement = (TypeElement) element;
        String packagename = helper.getPackageName(elementUtils, typeElement);
        ClassName providerClass = ClassName.get(packagename,config.name());
        ClassName configClass = ClassName.get(packagename,typeElement.getSimpleName().toString());
        return new Provider(element,config.name(),config.authority(),providerClass,config.database(),config.version(),configClass);
    }

}
