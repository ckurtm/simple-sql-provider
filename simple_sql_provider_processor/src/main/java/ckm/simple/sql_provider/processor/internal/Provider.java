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

import javax.lang.model.element.Element;

/**
 * Created by kurt on 03 09 2015 .
 */
public final class Provider {
    public Element element;
    public String name;
    public String authority;
    public ClassName clazz;
    public ClassName configClass;
    public String database;
    public int version;

    public Provider(Element element,String name, String authority, ClassName clazz, String database, int version,ClassName configClass) {
        this.name = name;
        this.element = element;
        this.authority = authority;
        this.clazz = clazz;
        this.database = database;
        this.version = version;
        this.configClass = configClass;
    }

    @Override
    public String toString() {
        return "Provider{" +
                "name='" + name + '\'' +
                ", authority='" + authority + '\'' +
                ", clazz=" + clazz +
                ", configClass=" + configClass +
                ", database='" + database + '\'' +
                ", version=" + version +
                '}';
    }
}
