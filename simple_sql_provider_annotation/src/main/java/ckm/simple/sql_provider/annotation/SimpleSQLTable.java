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

package ckm.simple.sql_provider.annotation;

/**
 * Created by kurt on 2015/09/02.
 */
public @interface SimpleSQLTable {
    String table();
    String provider();
    String queryKey() default NULL;
    String query() default NULL;
    Class<?> queryRules() default String.class;
    String NULL = "";
}
