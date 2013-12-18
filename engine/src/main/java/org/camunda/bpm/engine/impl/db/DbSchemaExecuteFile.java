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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Daniel Meyer
 *
 */
public class DbSchemaExecuteFile {

  /**
   * @param args
   */
  public static void main(String[] args) {

    if(args.length != 2) {
      throw new ProcessEngineException("Schema resource tool must be invoked with exactly 2 parameters: \n " +
      		"- 1st parameter is process engine configuration file, \n " +
      		"- 2nd parameter is schema resource file name");
    }

    final String configurationFileResourceName = args[0];
    final String schemaFileResourceName = args[1];

    if(configurationFileResourceName == null) {
      throw new ProcessEngineException("Process engine configuration file name cannot be null.");
    }

    if(schemaFileResourceName == null) {
      throw new ProcessEngineException("Schema resource file name cannot be null.");
    }

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
