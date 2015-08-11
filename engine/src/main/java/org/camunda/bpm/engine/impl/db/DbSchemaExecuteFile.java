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
package org.camunda.bpm.engine.impl.db;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Daniel Meyer
 *
 */
public class DbSchemaExecuteFile {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  /**
   * @param args
   */
  public static void main(String[] args) {

    if(args.length != 2) {
      throw LOG.invokeSchemaResourceToolException(args.length);
    }

    final String configurationFileResourceName = args[0];
    final String schemaFileResourceName = args[1];

    ensureNotNull("Process engine configuration file name cannot be null", "configurationFileResourceName", configurationFileResourceName);
    ensureNotNull("Schema resource file name cannot be null", "schemaFileResourceName", schemaFileResourceName);

    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(configurationFileResourceName);
    ProcessEngine processEngine = configuration.buildProcessEngine();

    configuration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {

        commandContext.getDbSqlSession()
          .executeSchemaResource(schemaFileResourceName);

        return null;
      }

    });

    processEngine.close();

  }

}
