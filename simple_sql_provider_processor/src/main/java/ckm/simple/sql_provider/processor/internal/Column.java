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


import javax.lang.model.element.Element;

import ckm.simple.sql_provider.processor.Helper;

/**
 * Created by kurt on 03 09 2015 .
 */
public final class Column {
    public String name;
    public Element element;
    public boolean primary;
    public boolean autoincrement;

    public Column(boolean primary,boolean autoincrement,String name, Element element) {
        this.name = name;
        this.primary = primary;
        this.element = element;
        this.autoincrement = autoincrement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Column)) return false;
        Column column = (Column) o;
        return name.equalsIgnoreCase(column.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Column {" +
                "name='" + name + '\'' +
                ", element=" + element.asType() +
                ", field=" + element.getSimpleName() +
                ", primary=" + primary +
                ", autoincrement=" + autoincrement +
                '}';
    }

    public boolean isPrivate(){
            return !Helper.isPublic(element);
    }
}
