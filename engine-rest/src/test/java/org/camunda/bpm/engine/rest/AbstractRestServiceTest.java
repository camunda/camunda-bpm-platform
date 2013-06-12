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
package org.camunda.bpm.engine.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.ws.rs.core.MediaType;

import org.apache.http.entity.ContentType;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceImpl;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.rest.spi.impl.MockedProcessEngineProvider;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;

public abstract class AbstractRestServiceTest {

  protected static ProcessEngine processEngine;
  protected static final String TEST_RESOURCE_ROOT_PATH = "/rest-test";
  protected static int PORT;
  
  protected static final String POST_JSON_CONTENT_TYPE = ContentType.create(MediaType.APPLICATION_JSON, "UTF-8").toString();
  
  protected static final String EMPTY_JSON_OBJECT = "{}";
  
  private static final String PROPERTIES_FILE_PATH = "/testconfig.properties";
  private static final String PORT_PROPERTY = "rest.http.port";
  
  protected static final String EXAMPLE_VARIABLE_KEY = "aVariableKey";
  protected static final String EXAMPLE_VARIABLE_VALUE = "aVariableValue";
  
  protected static final Map<String, Object> EXAMPLE_VARIABLES = new HashMap<String, Object>();
  static {
    EXAMPLE_VARIABLES.put(EXAMPLE_VARIABLE_KEY, EXAMPLE_VARIABLE_VALUE);
  }
  
  protected static final String EXAMPLE_ACTIVITY_INSTANCE_ID = "anActivityInstanceId";
  protected static final String EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID = "aParentActivityInstanceId";
  protected static final String EXAMPLE_ACTIVITY_ID = "anActivityId";
  protected static final String EXAMPLE_ACTIVITY_NAME = "anActivityName";
  protected static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcessInstanceId";
  protected static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId";
  protected static final String EXAMPLE_BUSINESS_KEY = "aBusinessKey";
  
  protected static final String CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID = "aChildActivityInstanceId";
  protected static final String CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID = "aChildParentActivityInstanceId";
  protected static final String CHILD_EXAMPLE_ACTIVITY_ID = "aChildActivityId";
  protected static final String CHILD_EXAMPLE_ACTIVITY_NAME = "aChildActivityName";
  protected static final String CHILD_EXAMPLE_PROCESS_INSTANCE_ID = "aChildProcessInstanceId";
  protected static final String CHILD_EXAMPLE_PROCESS_DEFINITION_ID = "aChildProcessDefinitionId";
  protected static final String CHILD_EXAMPLE_BUSINESS_KEY = "aChildBusinessKey";
  
  protected static final ActivityInstance EXAMPLE_ACTIVITY_INSTANCE = new ActivityInstanceImpl();
  static {
    ActivityInstanceImpl instance = (ActivityInstanceImpl) EXAMPLE_ACTIVITY_INSTANCE;
    instance.setId(EXAMPLE_ACTIVITY_INSTANCE_ID);
    instance.setParentActivityInstanceId(EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID);
    instance.setActivityId(EXAMPLE_ACTIVITY_ID);
    instance.setActivityName(EXAMPLE_ACTIVITY_NAME);
    instance.setProcessInstanceId(EXAMPLE_PROCESS_INSTANCE_ID);
    instance.setProcessDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID);
    instance.setBusinessKey(EXAMPLE_BUSINESS_KEY);
    instance.getExecutionIds().add(EXAMPLE_ACTIVITY_INSTANCE_ID);
    
    instance.setChildInstances(new ArrayList<ActivityInstance>());
    
    ActivityInstanceImpl child = new ActivityInstanceImpl();
    child.setId(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID);
    child.setParentActivityInstanceId(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID);
    child.setActivityId(CHILD_EXAMPLE_ACTIVITY_ID);
    child.setActivityName(CHILD_EXAMPLE_ACTIVITY_NAME);
    child.setProcessInstanceId(CHILD_EXAMPLE_PROCESS_INSTANCE_ID);
    child.setProcessDefinitionId(CHILD_EXAMPLE_PROCESS_DEFINITION_ID);
    child.setBusinessKey(CHILD_EXAMPLE_BUSINESS_KEY);
    child.setExecutionIds(new ArrayList<String>());
    child.getExecutionIds().add(EXAMPLE_ACTIVITY_INSTANCE_ID);

    instance.getChildInstances().add(child);
  }
  
  
  private static Properties connectionProperties = null;

  @BeforeClass
  public static void setUp() throws IOException {
    setupTestScenario();
  }
  
  protected static void setupTestScenario() throws IOException {
    setupRestAssured();
    
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader
        .load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if (iterator.hasNext()) {
      MockedProcessEngineProvider provider = (MockedProcessEngineProvider) iterator.next();
      
      // reset engine mocks before every test
      provider.resetEngines();
      
      processEngine = provider.getDefaultProcessEngine();
    }
  }
  
  protected ProcessEngine getProcessEngine(String name) {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader
        .load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if (iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine(name);
    } else {
      throw new ProcessEngineException("No provider found");
    }
  }

  private static void setupRestAssured() throws IOException {
    if (connectionProperties == null) {
      InputStream propStream = null;
      try {
        propStream = AbstractRestServiceTest.class.getResourceAsStream(PROPERTIES_FILE_PATH);
        connectionProperties = new Properties();
        connectionProperties.load(propStream);
      } finally {
        propStream.close();
      }
    }
    
    PORT = Integer.parseInt(connectionProperties.getProperty(PORT_PROPERTY));
    RestAssured.port = PORT;
  }
  

}
