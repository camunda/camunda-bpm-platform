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
package org.camunda.bpm.engine.test.standalone.initialization;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.PvmTestCase;

/**
 * @author Tom Baeyens
 * @author Stefan Hentschel
 * @author Roman Smirnov
 */
public class ProcessEngineInitializationTest extends PvmTestCase {

  public void testNoTables() {
    try {
      ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/standalone/initialization/notables.camunda.cfg.xml")
        .buildProcessEngine();
      fail("expected exception");
    } catch (Exception e) {
      // OK
      assertTextPresent("ENGINE-03057 There are no Camunda tables in the database. Hint: Set <property name=\"databaseSchemaUpdate\" to value=\"true\" or value=\"create-drop\" (use create-drop for testing only!) in bean processEngineConfiguration in camunda.cfg.xml for automatic schema creation", e.getMessage());
    }
  }

  public void testDefaultRetries() {
    ProcessEngineConfiguration configuration = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/standalone/initialization/defaultretries.camunda.cfg.xml");

    assertEquals(JobEntity.DEFAULT_RETRIES, configuration.getDefaultNumberOfRetries());
  }

  public void testCustomDefaultRetries() {
    ProcessEngineConfiguration configuration = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/standalone/initialization/customdefaultretries.camunda.cfg.xml");

    assertEquals(5, configuration.getDefaultNumberOfRetries());
  }

}
