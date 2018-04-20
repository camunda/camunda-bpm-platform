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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.variable.impl.TypedValueField;

/**
 * @author Tassilo Weidner
 */
public class MockProvider {

  public static final MockProvider MOCK_PROVIDER = new MockProvider();

  public static final String BASE_URL = "http://localhost:8080/engine-rest";
  public static final int MAX_TASKS = 10;

  // external task
  public static final String ACTIVITY_ID = "ServiceTask_1";
  public static final String ACTIVITY_INSTANCE_ID = "ServiceTask_1:be7bb005-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final String EXECUTION_ID = "be7bb004-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final Date LOCK_EXPIRATION_TIME = createDate("March 5, 2018");
  public static final String PROCESS_DEFINITION_ID = "testProcess:1:20725ab5-1c95-11e8-a8b4-769e8e30ca9b";
  public static final String PROCESS_DEFINITION_KEY = "testProcess";
  public static final String PROCESS_INSTANCE_ID = "be7b88f2-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final String TOPIC_NAME = "Address Validation";
  public static final String ID = "be7bb006-1cb7-11e8-a8b4-769e8e30ca9b";
  public static final String WORKER_ID = "neodym7db957e1-0bde-4d11-948b-9c20743e4c43";

  public static final String ERROR_MESSAGE = "Error message";
  public static final String ERROR_DETAILS = "Error details";
  public static final boolean SUSPENSION_STATE = false;
  public static final String TENANT_ID = "tenantOne";
  public static final int RETRIES = 3;
  public static final long PRIORITY = 500;

  // handle failure
  public static final long RETRY_TIMEOUT = 600000;

  // handle bpmn error
  public static final String ERROR_CODE = "bpmn-error";

  // extend lock
  public static final long NEW_DURATION = 123456;

  // variables
  public static final String BOOLEAN_VARIABLE_TYPE = "Boolean";
  public static final String BOOLEAN_VARIABLE_NAME = "booleanVariable";
  public static final boolean BOOLEAN_VARIABLE_VALUE = true;

  public static final String SHORT_VARIABLE_TYPE = "Short";
  public static final String SHORT_VARIABLE_NAME = "shortVariable";
  public static final short SHORT_VARIABLE_VALUE = (short) 47;

  public static final String INTEGER_VARIABLE_TYPE = "Integer";
  public static final String INTEGER_VARIABLE_NAME = "integerVariable";
  public static final int INTEGER_VARIABLE_VALUE = (int) 4711;

  public static final String LONG_VARIABLE_TYPE = "Long";
  public static final String LONG_VARIABLE_NAME = "longVariable";
  public static final long LONG_VARIABLE_VALUE = (long) 123L;

  public static final String STRING_VARIABLE_TYPE = "String";
  public static final String STRING_VARIABLE_NAME = "stringVariable";
  public static final String STRING_VARIABLE_VALUE = "Lorem ipsum dolor sit amet";

  public static final String DOUBLE_VARIABLE_TYPE = "Double";
  public static final String DOUBLE_VARIABLE_NAME = "doubleVariable";
  public static final double DOUBLE_VARIABLE_VALUE = (double) 3.1415926535897932384626433;

  public static final String DATE_VARIABLE_TYPE = "Date";
  public static final String DATE_VARIABLE_NAME = "dateVariable";
  public static final Date DATE_VARIABLE_VALUE = createDate("March 5, 2018");
  public static final String DATE_VARIABLE_VALUE_SERIALIZED = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(DATE_VARIABLE_VALUE);

  public static final String BYTES_VARIABLE_TYPE = "Bytes";
  public static final String BYTES_VARIABLE_NAME = "bytesVariable";
  public static final byte[] BYTES_VARIABLE_VALUE = STRING_VARIABLE_VALUE.getBytes();
  public static final String BYTES_VARIABLE_VALUE_SERIALIZED = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQ=";

  public static final String NULL_VARIABLE_TYPE = "Null";
  public static final String NULL_VARIABLE_NAME = "nullVariable";

  public static final Map<String, TypedValueField> VARIABLES = createVariables();
  public static final String BUSINESS_KEY = "aBusinessKey";

  public static Date createDate(String date) {
    try {
      return new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static TypedValueField createBooleanVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(BOOLEAN_VARIABLE_TYPE);
    typedValueDto.setValue(BOOLEAN_VARIABLE_VALUE);

    return typedValueDto;
  }

  public static TypedValueField createShortVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(SHORT_VARIABLE_TYPE);
    typedValueDto.setValue(SHORT_VARIABLE_VALUE);

    return typedValueDto;
  }

  public static TypedValueField createIntegerVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(INTEGER_VARIABLE_TYPE);
    typedValueDto.setValue(INTEGER_VARIABLE_VALUE);

    return typedValueDto;
  }

  public static TypedValueField createLongVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(LONG_VARIABLE_TYPE);
    typedValueDto.setValue(LONG_VARIABLE_VALUE);

    return typedValueDto;
  }

  public static TypedValueField createStringVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(STRING_VARIABLE_TYPE);
    typedValueDto.setValue(STRING_VARIABLE_VALUE);

    return typedValueDto;
  }

  public static TypedValueField createDoubleVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(DOUBLE_VARIABLE_TYPE);
    typedValueDto.setValue(DOUBLE_VARIABLE_VALUE);

    return typedValueDto;
  }

  public static TypedValueField createDateVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(DATE_VARIABLE_TYPE);
    typedValueDto.setValue(DATE_VARIABLE_VALUE_SERIALIZED);

    return typedValueDto;
  }

  public static TypedValueField createBytesVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(BYTES_VARIABLE_TYPE);
    typedValueDto.setValue(BYTES_VARIABLE_VALUE_SERIALIZED);

    return typedValueDto;
  }

  public static TypedValueField createNullVariable() {
    TypedValueField typedValueDto = new TypedValueField();
    typedValueDto.setType(NULL_VARIABLE_TYPE);
    typedValueDto.setValue(null);

    return typedValueDto;
  }

  public static Map<String, TypedValueField> createVariables() {
    Map<String, TypedValueField> variables = new HashMap<>();
    variables.put(BOOLEAN_VARIABLE_NAME, createBooleanVariable());
    variables.put(SHORT_VARIABLE_NAME, createShortVariable());
    variables.put(LONG_VARIABLE_NAME, createLongVariable());
    variables.put(INTEGER_VARIABLE_NAME, createIntegerVariable());
    variables.put(DOUBLE_VARIABLE_NAME, createDoubleVariable());
    variables.put(DATE_VARIABLE_NAME, createDateVariable());
    variables.put(STRING_VARIABLE_NAME, createStringVariable());
    variables.put(NULL_VARIABLE_NAME, createNullVariable());
    variables.put(BYTES_VARIABLE_NAME, createBytesVariable());

    return variables;
  }

  public static ExternalTask createExternalTask() {
    ExternalTaskImpl externalTask = new ExternalTaskImpl();
    externalTask.setActivityId(ACTIVITY_ID);
    externalTask.setActivityInstanceId(ACTIVITY_INSTANCE_ID);
    externalTask.setExecutionId(EXECUTION_ID);
    externalTask.setLockExpirationTime(LOCK_EXPIRATION_TIME);
    externalTask.setProcessDefinitionId(PROCESS_DEFINITION_ID);
    externalTask.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
    externalTask.setProcessInstanceId(PROCESS_INSTANCE_ID);
    externalTask.setId(ID);
    externalTask.setWorkerId(WORKER_ID);
    externalTask.setTopicName(TOPIC_NAME);
    externalTask.setVariables(VARIABLES);
    externalTask.setErrorMessage(ERROR_MESSAGE);
    externalTask.setErrorDetails(ERROR_DETAILS);
    externalTask.setSuspended(SUSPENSION_STATE);
    externalTask.setTenantId(TENANT_ID);
    externalTask.setRetries(RETRIES);
    externalTask.setPriority(PRIORITY);
    externalTask.setBusinessKey(BUSINESS_KEY);
    return externalTask;
  }

  public static ExternalTask createExternalTaskWithoutVariables() {
    ExternalTaskImpl externalTask = new ExternalTaskImpl();
    externalTask.setActivityId(ACTIVITY_ID);
    externalTask.setActivityInstanceId(ACTIVITY_INSTANCE_ID);
    externalTask.setExecutionId(EXECUTION_ID);
    externalTask.setLockExpirationTime(LOCK_EXPIRATION_TIME);
    externalTask.setProcessDefinitionId(PROCESS_DEFINITION_ID);
    externalTask.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
    externalTask.setProcessInstanceId(PROCESS_INSTANCE_ID);
    externalTask.setId(ID);
    externalTask.setWorkerId(WORKER_ID);
    externalTask.setTopicName(TOPIC_NAME);
    externalTask.setVariables(new HashMap<>());
    externalTask.setErrorMessage(ERROR_MESSAGE);
    externalTask.setErrorDetails(ERROR_DETAILS);
    externalTask.setSuspended(SUSPENSION_STATE);
    externalTask.setTenantId(TENANT_ID);
    externalTask.setRetries(RETRIES);
    externalTask.setPriority(PRIORITY);
    return externalTask;
  }

}
