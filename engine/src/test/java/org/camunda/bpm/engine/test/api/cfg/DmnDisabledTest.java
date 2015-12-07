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
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.junit.After;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class DmnDisabledTest {

  protected static ProcessEngineImpl processEngineImpl;

  // make sure schema is dropped
  @After
  public void cleanup() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;
  }

  @Test
  public void disabledDmn() {
    processEngineImpl = createProcessEngineImpl(false);

    // simulate manual schema creation by user
    TestHelper.createSchema(processEngineImpl.getProcessEngineConfiguration());

    // let the engine do their schema operations thing
    processEngineImpl.getProcessEngineConfiguration()
      .getCommandExecutorSchemaOperations()
      .execute(new SchemaOperationsProcessEngineBuild());
  }

  // allows to return a process engine configuration which doesn't create a schema when it's build.
  protected static class CustomStandaloneInMemProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {

    public ProcessEngine buildProcessEngine() {
      init();
      return new CreateNoSchemaProcessEngineImpl(this);
    }
  }

  protected static class CreateNoSchemaProcessEngineImpl extends ProcessEngineImpl {

    public CreateNoSchemaProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
      super(processEngineConfiguration);
    }

    protected void executeSchemaOperations() {
      // noop - do not execute create schema operations
    }
  }

  protected static ProcessEngineImpl createProcessEngineImpl(boolean dmnEnabled) {
    StandaloneInMemProcessEngineConfiguration config =
        (StandaloneInMemProcessEngineConfiguration) new CustomStandaloneInMemProcessEngineConfiguration()
               .setProcessEngineName("database-dmn-test-engine")
               .setDatabaseSchemaUpdate("false")
               .setHistory(ProcessEngineConfiguration.HISTORY_FULL)
               .setJdbcUrl("jdbc:h2:mem:DatabaseDmnTest");

    config.setDmnEnabled(dmnEnabled);

    return (ProcessEngineImpl) config.buildProcessEngine();
  }

}
