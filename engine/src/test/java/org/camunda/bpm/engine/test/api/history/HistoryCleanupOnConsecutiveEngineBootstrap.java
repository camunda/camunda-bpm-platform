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

package org.camunda.bpm.engine.test.api.history;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Nikola Koevski
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class HistoryCleanupOnConsecutiveEngineBootstrap {

  private static final String FIRST_ENGINE_NAME = "engineWithoutHistoryCleanupBatchWindow";
  private static final String SECOND_ENGINE_NAME = "engineWithHistoryCleanupBatchWindow";

  @Test
  public void testConsecutiveEngineBootstrapHistoryCleanupJobReconfiguration() {

    ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/no-batchwindow.camunda.cfg.xml");
    processEngineConfiguration.setProcessEngineName(FIRST_ENGINE_NAME);
    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
    processEngine.close();

    processEngineConfiguration = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/batchwindow.camunda.cfg.xml");
    processEngineConfiguration.setProcessEngineName(SECOND_ENGINE_NAME);
    processEngineConfiguration.buildProcessEngine();

    assertNotNull(ProcessEngines.getProcessEngine(SECOND_ENGINE_NAME));
  }
}
