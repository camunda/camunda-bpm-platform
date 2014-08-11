/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.variables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.runtime.SerializedVariableValue;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.DummySerializable;
import org.camunda.spin.DataFormats;
import org.junit.Assert;
import org.junit.Test;

public class VariableSerializedValueTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/variables/oneTaskProcess.bpmn20.xml";

  protected static final String JSON_FORMAT_NAME = DataFormats.jsonTreeFormat().getName();

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedBooleanValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("booleanVariable", true));

    SerializedVariableValue serializedValue = getSerializedVariableValue("booleanVariable");
    assertEquals(true, serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedByteArrayValue() {
    byte[] byteArray = new byte[] {42, 47};
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("byteArrayVariable", byteArray));

    SerializedVariableValue serializedValue = getSerializedVariableValue("byteArrayVariable");
    Assert.assertArrayEquals(byteArray, (byte[]) serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedSerializableValue() {
    DummySerializable serializable = new DummySerializable();
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", serializable));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    Assert.assertArrayEquals(toExpectedBytes(serializable), (byte[]) serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedDateValue() {
    long aLongValue = 12345678L;
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", new Date(aLongValue)));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    assertEquals(aLongValue, serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedDoubleValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", 4.2d));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    assertEquals(4.2d, serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedIntegerValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", 42));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    assertEquals(42, serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedShortValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", (short) 42));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    assertEquals((short) 42, serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedLongValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", 123123123123L));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    assertEquals(123123123123L, serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }

  @Test
  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedStringValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap("serializableVariable", "a String Value"));

    SerializedVariableValue serializedValue = getSerializedVariableValue("serializableVariable");
    assertEquals("a String Value", serializedValue.getValue());
    assertEquals(0, serializedValue.getConfig().size());
  }


  protected SerializedVariableValue getSerializedVariableValue(String name) {
    return runtimeService.createVariableInstanceQuery().variableName(name).singleResult().getSerializedValue();
  }

  protected byte[] toExpectedBytes(Object o) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream ois = null;
    try {
      ois = new ObjectOutputStream(baos);
      ois.writeObject(o);
    } catch (IOException e) {
      throw new ProcessEngineException(e);
    } finally {
      IoUtil.closeSilently(ois);
    }
    return baos.toByteArray();
  }

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(key, value);
    return map;
  }
}
