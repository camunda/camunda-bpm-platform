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

package org.camunda.bpm.engine.test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a test method or class to specify the required history level.
 * If the current history level of the process engine is lower than the
 * specified one then the test method is skipped.
 *
 * <p>Usage:</p>
 *
 * <pre>
 * package org.example;
 *
 * ...
 *
 * public class ExampleTest {
 *
 *   &#64;RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
 *   public void testWithHistory() {
 *
 *     // test something with the history service (e.g. variables)
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequiredHistoryLevel {

  /**
   * The required history level.
   */
  public String value();

}
