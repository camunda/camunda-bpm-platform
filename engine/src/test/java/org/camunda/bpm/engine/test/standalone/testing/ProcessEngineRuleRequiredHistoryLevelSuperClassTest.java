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

package org.camunda.bpm.engine.test.standalone.testing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * Checks if the test is ignored than the current history level is lower than
 * the required history level which is specified on the super class.
 */
public class ProcessEngineRuleRequiredHistoryLevelSuperClassTest extends ProcessEngineRuleRequiredHistoryLevelClassTest {

  @Test
  public void requiredHistoryLevelOnSuperClass() {

    assertThat(currentHistoryLevel(),
        CoreMatchers.<String>either(is(ProcessEngineConfiguration.HISTORY_AUDIT))
        .or(is(ProcessEngineConfiguration.HISTORY_FULL)));
  }

}
