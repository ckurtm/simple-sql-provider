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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;


public final class Messenger {

  private javax.annotation.processing.Messager messager;

  public void init(ProcessingEnvironment processingEnvironment) {
    messager = processingEnvironment.getMessager();
  }

  public void note(Element e, String msg, Object... args) {
    checkInitialized();
    messager.printMessage(NOTE, String.format(msg, args), e);
  }

  public void warn(Element e, String msg, Object... args) {
    checkInitialized();
    messager.printMessage(WARNING, String.format(msg, args), e);
  }

  public void error(Element e, String msg, Object... args) {
    checkInitialized();
    messager.printMessage(ERROR, String.format(msg, args), e);
  }

  private void checkInitialized() {
    if (messager == null) {
      throw new IllegalStateException("Messager not ready. Have you called init()?");
    }
  }
}
