/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.javascript.json.tree;

import org.camunda.spin.impl.test.ScriptEngine;
import org.camunda.spin.json.tree.JsonTreeEditListPropertyScriptTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Stefan Hentschel
 *
 */
@ScriptEngine("javascript")
public class JsonTreeEditListPropertyJavascriptTest extends JsonTreeEditListPropertyScriptTest {

  /**
   * Ignored since javascript implementation changed
   * see: https://app.camunda.com/jira/browse/CAM-3612
   */
  @Test
  @Ignore
  public void shouldFailInsertAtWithWrongObject() throws Throwable {
  }

  /**
   * Ignored since javascript implementation changed
   * see: https://app.camunda.com/jira/browse/CAM-3612
   */
  @Test
  @Ignore
  public void shouldFailInsertWrongObjectAfterSearchObject() throws Throwable {
  }

  /**
   * Ignored since javascript implementation changed
   * see: https://app.camunda.com/jira/browse/CAM-3612
   */
  @Test
  @Ignore
  public void shouldFailAppendWrongNode() throws Throwable {
  }

  /**
   * Ignored since javascript implementation changed
   * see: https://app.camunda.com/jira/browse/CAM-3612
   */
  @Test
  @Ignore
  public void shouldFailInsertWrongObjectBeforeSearchObject() throws Throwable {
  }
}
