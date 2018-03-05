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
package org.camunda.bpm.client.helper;

import org.camunda.bpm.client.LockedTask;
import org.camunda.bpm.client.impl.dto.LockedTaskDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class MockProvider {

  public static final String ENDPOINT_URL = "http://localhost:8080/engine-rest";
  public static final int MAX_TASKS = 10;

  public static final String ACTIVITY_ID = "ServiceTask_1";
  public static final String ACTIVITY_INSTANCE_ID = "ServiceTask_1:be7bb005-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final String EXECUTION_ID = "be7bb004-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final Date LOCK_EXPIRATION_TIME = createLockExpirationTime("March 5, 2018");
  public static final String PROCESS_DEFINITION_ID = "testProcess:1:20725ab5-1c95-11e8-a8b4-769e8e30ca9b";
  public static final String PROCESS_DEFINITION_KEY = "testProcess";
  public static final String PROCESS_INSTANCE_ID = "be7b88f2-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final String TOPIC_NAME = "Address Validation";
  public static final String ID = "be7bb006-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final String WORKER_ID = "neodym7db957e1-0bde-4d11-948b-9c20743e4c43";
  public static final Map<String, Object> VARIABLES = createVariables();
  public static final String ERROR_MESSAGE = "Error message";
  public static final String ERROR_DETAILS = "Error details";
  public static final boolean SUSPENSION_STATE = false;
  public static final String TENANT_ID = "tenantOne";
  public static final int RETRIES = 3;
  public static final long PRIORITY = 500;

  public static Date createLockExpirationTime(String date) {
    try {
      return new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static Map<String, Object> createVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "aVariableValue");
    return variables;
  }

  public static LockedTask createLockedTask() {
    LockedTaskDto lockedTask = new LockedTaskDto();
    lockedTask.setActivityId(ACTIVITY_ID);
    lockedTask.setActivityInstanceId(ACTIVITY_INSTANCE_ID);
    lockedTask.setExecutionId(EXECUTION_ID);
    lockedTask.setLockExpirationTime(LOCK_EXPIRATION_TIME);
    lockedTask.setProcessDefinitionId(PROCESS_DEFINITION_ID);
    lockedTask.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
    lockedTask.setProcessInstanceId(PROCESS_INSTANCE_ID);
    lockedTask.setId(ID);
    lockedTask.setWorkerId(WORKER_ID);
    lockedTask.setTopicName(TOPIC_NAME);
    lockedTask.setVariables(VARIABLES);
    lockedTask.setErrorMessage(ERROR_MESSAGE);
    lockedTask.setErrorDetails(ERROR_DETAILS);
    lockedTask.setSuspended(SUSPENSION_STATE);
    lockedTask.setTenantId(TENANT_ID);
    lockedTask.setRetries(RETRIES);
    lockedTask.setPriority(PRIORITY);
    return lockedTask;
  }

}
