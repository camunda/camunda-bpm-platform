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

package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.CommandContextFunctionMapper;
import org.camunda.bpm.engine.impl.el.DateTimeFunctionMapper;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 */
public class CustomExpressionManagerTest {

  protected ProcessEngine engine;

  @Test
  public void testBuiltinFunctionMapperRegistration() {
    // given a process engine configuration with a custom function mapper
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    CustomExpressionManager customExpressionManager = new CustomExpressionManager();
    Assert.assertTrue(customExpressionManager.getFunctionMappers().isEmpty());
    config.setExpressionManager(customExpressionManager);

    // when the engine is initialized
    engine = config.buildProcessEngine();

    // then two default function mappers should be registered
    Assert.assertSame(customExpressionManager, config.getExpressionManager());
    Assert.assertEquals(2, customExpressionManager.getFunctionMappers().size());

    boolean commandContextMapperFound = false;
    boolean dateTimeMapperFound = false;

    for (FunctionMapper functionMapper : customExpressionManager.getFunctionMappers()) {
      if (functionMapper instanceof CommandContextFunctionMapper) {
        commandContextMapperFound = true;
      }

      if (functionMapper instanceof DateTimeFunctionMapper) {
        dateTimeMapperFound = true;
      }
    }

    Assert.assertTrue(commandContextMapperFound && dateTimeMapperFound);
  }

  @After
  public void tearDown() {
    if (engine != null) {
      engine.close();
      engine = null;
    }
  }
}
