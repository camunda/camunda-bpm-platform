/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.task;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.variable.impl.DefaultValueMappers;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.ValueMappers;
import org.camunda.bpm.client.variable.impl.VariableValue;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.junit.Test;

/**
 * @author Tobias Metzke
 *
 */
public class ExternalTaskImplTest {

  @Test
  public void shouldDisplayAttributesEmptyInToString() {
    // no attributes set, only priority initialized as 0
    ExternalTaskImpl task = new ExternalTaskImpl();
    assertEquals("ExternalTaskImpl [activityId=null, "
        + "activityInstanceId=null, "
        + "businessKey=null, "
        + "errorDetails=null, "
        + "errorMessage=null, "
        + "executionId=null, "
        + "id=null, "
        + "lockExpirationTime=null, "
        + "createTime=null, "
        + "priority=0, "
        + "processDefinitionId=null, "
        + "processDefinitionKey=null, "
        + "processDefinitionVersionTag=null, "
        + "processInstanceId=null, "
        + "receivedVariableMap=null, "
        + "retries=null, "
        + "tenantId=null, "
        + "topicName=null, "
        + "variables=null, "
        + "workerId=null]", task.toString());
  }

  @Test
  public void shouldDisplayAttributesFilledInToString() {
    // with basic attributes set, attributes should be displayed and separated by comma
    ExternalTaskImpl task = new ExternalTaskImpl();
    
    Date date = new Date(0L);// January 1,1970, 00:00:00 GMT
    task.setActivityId("ai");
    task.setActivityInstanceId("aii");
    task.setBusinessKey("bk");
    task.setErrorDetails("ed");
    task.setErrorMessage("em");
    task.setExecutionId("ei");
    task.setId("i");
    task.setLockExpirationTime(date);
    task.setCreateTime(date);
    task.setPriority(3L);
    task.setProcessDefinitionId("pdi");
    task.setProcessDefinitionKey("pdk");
    task.setProcessDefinitionVersionTag("versionTag");
    task.setProcessInstanceId("pii");
    task.setRetries(34);
    task.setTenantId("ti");
    task.setTopicName("tn");
    task.setWorkerId("wi");
    
    assertEquals("ExternalTaskImpl [activityId=ai, "
        + "activityInstanceId=aii, "
        + "businessKey=bk, "
        + "errorDetails=ed, "
        + "errorMessage=em, "
        + "executionId=ei, "
        + "id=i, "
        + "lockExpirationTime=" + DateFormat.getDateTimeInstance().format(date) + ", "
        + "createTime=" + DateFormat.getDateTimeInstance().format(date) + ", "
        + "priority=3, "
        + "processDefinitionId=pdi, "
        + "processDefinitionKey=pdk, "
        + "processDefinitionVersionTag=versionTag, "
        + "processInstanceId=pii, "
        + "receivedVariableMap=null, "
        + "retries=34, "
        + "tenantId=ti, "
        + "topicName=tn, "
        + "variables=null, "
        + "workerId=wi]", 
        task.toString());
  }
  
  @SuppressWarnings("rawtypes")
  @Test
  public void shouldDisplayAttributesIncludingMapsInToString() {
    // variables map entries should be displayed as well
    ExternalTaskImpl task = new ExternalTaskImpl();

    Date date = new Date(0L);// January 1,1970, 00:00:00 GMT
    task.setActivityId("ai");
    task.setActivityInstanceId("aii");
    task.setBusinessKey("bk");
    task.setErrorDetails("ed");
    task.setErrorMessage("em");
    task.setExecutionId("ei");
    task.setId("i");
    task.setLockExpirationTime(date);
    task.setCreateTime(date);
    task.setPriority(3L);
    task.setProcessDefinitionId("pdi");
    task.setProcessDefinitionKey("pdk");
    task.setProcessDefinitionVersionTag("versionTag");
    task.setProcessInstanceId("pii");
    task.setRetries(34);
    task.setTenantId("ti");
    task.setTopicName("tn");
    task.setWorkerId("wi");

    Map<String, VariableValue> receivedVariables = new LinkedHashMap<>();
    receivedVariables.put("rv1", generateVariableValue(task.getExecutionId(), "variable1", ValueType.STRING.getName(), "value1", 42, "vi2"));
    receivedVariables.put("rv2", generateVariableValue(task.getExecutionId(), "variable2", ValueType.INTEGER.getName(), 99, 42, "vi2", 87L));
    task.setReceivedVariableMap(receivedVariables);

    Map<String, TypedValueField> variables = new LinkedHashMap<>();
    variables.put("v1", generateTypedValueField(ValueType.STRING.getName(), "value2", 43, "vi3"));
    variables.put("v2", generateTypedValueField(ValueType.INTEGER.getName(), 999, 43, "vi3", 88L));
    task.setVariables(variables);

    assertEquals("ExternalTaskImpl [activityId=ai, "
        + "activityInstanceId=aii, "
        + "businessKey=bk, "
        + "errorDetails=ed, "
        + "errorMessage=em, "
        + "executionId=ei, "
        + "id=i, "
        + "lockExpirationTime=" + DateFormat.getDateTimeInstance().format(date) + ", "
        + "createTime=" + DateFormat.getDateTimeInstance().format(date) + ", "
        + "priority=3, "
        + "processDefinitionId=pdi, "
        + "processDefinitionKey=pdk, "
        + "processDefinitionVersionTag=versionTag, "
        + "processInstanceId=pii, "
        + "receivedVariableMap={"
          + "rv1=VariableValue [cachedValue=null, executionId=ei, variableName=variable1, typedValueField="
            + "TypedValueField [type=string, value=value1, valueInfo={vi1=42, vi2=vi2}]], "
          + "rv2=VariableValue [cachedValue=null, executionId=ei, variableName=variable2, typedValueField="
            + "TypedValueField [type=integer, value=99, valueInfo={vi1=42, vi2=vi2, vi3=87}]]"
        + "}, "
        + "retries=34, "
        + "tenantId=ti, "
        + "topicName=tn, "
        + "variables={"
          + "v1=TypedValueField [type=string, value=value2, valueInfo={vi1=43, vi2=vi3}], "
          + "v2=TypedValueField [type=integer, value=999, valueInfo={vi1=43, vi2=vi3, vi3=88}]"
        + "}, "
        + "workerId=wi]", task.toString());
  }
  
  // helper methods and constants
  
  @SuppressWarnings("rawtypes")
  private static final ValueMappers DEFAULT_MAPPERS = new DefaultValueMappers(Variables.SerializationDataFormats.JSON.getName());
  
  @SuppressWarnings("rawtypes")
  private static VariableValue generateVariableValue(String executionId, String variableName,
      final String typeI, final Object valueI, Object... valueInfos) {
    TypedValueField typedValueField = generateTypedValueField(typeI, valueI, valueInfos);
    return new VariableValue(executionId, variableName, typedValueField, DEFAULT_MAPPERS);
  }

  private static TypedValueField generateTypedValueField(final String typeI, final Object valueI, Object... valueInfos) {
    Map<String,Object> valueInfoI = new LinkedHashMap<>();
    for (int i = 0; i < valueInfos.length; i++) {
      valueInfoI.put("vi" + (i + 1), valueInfos[i]);
    }
    TypedValueField typedValueField = new TypedValueField() {{setType(typeI); setValue(valueI); setValueInfo(valueInfoI); }};
    return typedValueField;
  }
}
