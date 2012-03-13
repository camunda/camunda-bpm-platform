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

package org.activiti.engine.impl.test;

import javax.naming.InitialContext;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.schema.DbSchemaOperations;
import com.camunda.fox.processarchive.executor.ProcessArchiveContextExecutor;


// overlay 
public class PluggableActivitiTestCase extends AbstractActivitiTestCase {
  
  public static final String PROCESS_ENGINE_SERVICE_LOOKUP = 
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "ProcessEngineService!com.camunda.fox.platform.api.ProcessEngineService";
  
  public static final String PROCESS_ARCHIVE_SERVICE_LOOKUP = 
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "ProcessEngineService!com.camunda.fox.platform.api.ProcessArchiveService";
  
  public static final String PROCESS_ARCHIVE_CONTEXT_EXECUTOR =
          "java:global/" +
          "activiti-testsuite/" +
          "ProcessArchiveContextExecutor!com.camunda.fox.processarchive.executor.ProcessArchiveContextExecutor";

  private static ProcessArchiveContextExecutor processArchiveContextExecutor = null;
  
  public static ProcessEngine cachedProcessEngine;
  public static ProcessArchiveService processArchiveService;
  public static DbSchemaOperations dbSchemaOperations;
  
  protected void initializeProcessEngine() {   
    initEngine();
    processEngine = cachedProcessEngine;
  }
  
  private static void initEngine() {
    if(cachedProcessEngine == null) {     
      try {
        InitialContext initialContext = new InitialContext();
        ProcessEngineService processEngineService = (ProcessEngineService) initialContext.lookup(PROCESS_ENGINE_SERVICE_LOOKUP);
        cachedProcessEngine = processEngineService.getDefaultProcessEngine();
      }catch (Exception e) {
        throw new FoxPlatformException("Could not lookup process engine: ",e);
      }
    }
  }

  public static ProcessArchiveService getProcessArchiveService() {
    if(processArchiveService == null) {
      try {
        InitialContext initialContext = new InitialContext();
        processArchiveService = (ProcessArchiveService) initialContext.lookup(PROCESS_ARCHIVE_SERVICE_LOOKUP);         
      }catch (Exception e) {
        throw new FoxPlatformException("Could not lookup process engine: ",e);
      }
    }
    return processArchiveService;
  }
  
  public static ProcessArchiveContextExecutor getProcessArchiveContextExecutor() {
    if(processArchiveContextExecutor == null) {
      try {
        InitialContext initialContext = new InitialContext();
        processArchiveContextExecutor = (ProcessArchiveContextExecutor) initialContext.lookup(PROCESS_ARCHIVE_CONTEXT_EXECUTOR);         
      }catch (Exception e) {
        throw new FoxPlatformException("Could not lookup process engine: ",e);
      }
    }
    return processArchiveContextExecutor;
  }
  
  
  public static ProcessEngine getCachedProcessEngine() {
    initEngine();
    return cachedProcessEngine;
  }
  
  
  public static DbSchemaOperations getDbSchemaOperations() {
    if(dbSchemaOperations == null) {
      dbSchemaOperations = new DbSchemaOperations();
      ProcessEngine processEngine = getCachedProcessEngine();
      ProcessEngineConfigurationImpl configuration = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration();
      dbSchemaOperations.setHistory(configuration.getHistory());
      dbSchemaOperations.setDataSourceJndiName(configuration.getDataSourceJndiName());      
    }
    return dbSchemaOperations;
  }

}
