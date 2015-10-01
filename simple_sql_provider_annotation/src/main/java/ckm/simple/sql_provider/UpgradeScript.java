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

package ckm.simple.sql_provider;

/**
 * Created by kurt on 2015/09/02.
 */
public class UpgradeScript {
    public int newVersion;
    public int sqlScriptResource;

    public UpgradeScript() {
    }

    public UpgradeScript(int newVersion, int sqlScriptResource) {
        this.newVersion = newVersion;
        this.sqlScriptResource = sqlScriptResource;
    }
}
