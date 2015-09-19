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
    public String queryKey;
    public String query;

    public List<Column> columns = new ArrayList<>();

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", element=" + element +
                ", provider='" + provider + '\'' +
                ", clazz=" + clazz +
                ", queryKey='" + queryKey + '\'' +
                ", query='" + query + '\'' +
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

    public Column getQueryKey(){
        for(Column column:columns){
            if(column.name.equalsIgnoreCase(queryKey)){
                return column;
            }
        }
        return null;
    }
}
