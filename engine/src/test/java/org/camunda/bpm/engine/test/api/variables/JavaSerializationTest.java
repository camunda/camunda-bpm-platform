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

package org.camunda.bpm.engine.test.api.variables;

import static org.camunda.bpm.engine.test.util.TypedValueAssert.assertObjectValueDeserialized;
import static org.camunda.bpm.engine.test.util.TypedValueAssert.assertObjectValueDeserializedNull;
import static org.camunda.bpm.engine.test.util.TypedValueAssert.assertObjectValueSerializedJava;
import static org.camunda.bpm.engine.test.util.TypedValueAssert.assertObjectValueSerializedNull;
import static org.camunda.bpm.engine.test.util.TypedValueAssert.assertUntypedNullValue;
import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.camunda.bpm.engine.variable.Variables.serializedObjectValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class JavaSerializationTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/variables/oneTaskProcess.bpmn20.xml";

  protected static final String JAVA_DATA_FORMAT = Variables.SerializationDataFormats.JAVA.getName();

  protected String originalSerializationFormat;

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializationAsJava() throws Exception {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    JavaSerializable javaSerializable = new JavaSerializable("foo");
    runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(javaSerializable).serializationDataFormat(JAVA_DATA_FORMAT).create());

    // validate untyped value
    JavaSerializable value = (JavaSerializable) runtimeService.getVariable(instance.getId(), "simpleBean");
    assertEquals(javaSerializable, value);

    // validate typed value
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertObjectValueDeserialized(typedValue, javaSerializable);
    assertObjectValueSerializedJava(typedValue, javaSerializable);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectSerialized() throws Exception {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    JavaSerializable javaSerializable = new JavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), processEngine);

    runtimeService.setVariable(instance.getId(), "simpleBean",
        serializedObjectValue(serializedObject)
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .objectTypeName(JavaSerializable.class.getName())
        .create());

    // validate untyped value
    JavaSerializable value = (JavaSerializable) runtimeService.getVariable(instance.getId(), "simpleBean");
    assertEquals(javaSerializable, value);

    // validate typed value
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertObjectValueDeserialized(typedValue, javaSerializable);
    assertObjectValueSerializedJava(typedValue, javaSerializable);
  }

  @Deployment
  public void testJavaObjectDeserializedInFirstCommand() throws Exception {

    // this test makes sure that if a serialized value is set, it can be deserialized in the same command in which it is set.

    // given
    // a serialized Java Object
    JavaSerializable javaSerializable = new JavaSerializable("foo");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), processEngine);

    // if
    // I start a process instance in which a Java Delegate reads the value in its deserialized form
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.createVariables()
      .putValue("varName", serializedObjectValue(serializedObject)
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .objectTypeName(JavaSerializable.class.getName())
        .create()));

    // then
    // it does not fail
  }

  @Deployment
  public void testJavaObjectNotDeserializedIfNotRequested() throws Exception {

    // this test makes sure that if a serialized value is set, it is not automatically deserialized if deserialization is not requested

    // given
    // a serialized Java Object
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    byte[] serializedObjectBytes = baos.toByteArray();
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(serializedObjectBytes), processEngine);

    // which cannot be deserialized
    try {
      ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedObjectBytes));
      objectInputStream.readObject();
      fail("Exception expected");
    } catch(RuntimeException e) {
      assertTextPresent("Exception while deserializing object", e.getMessage());
    }

    // if
    // I start a process instance in which a Java Delegate reads the value in its serialized form
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.createVariables()
      .putValue("varName", serializedObjectValue(serializedObject)
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .objectTypeName(JavaSerializable.class.getName())
        .create()));

    // then
    // it does not fail
  }


  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectNullDeserialized() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // set null value as "deserialized" object
    runtimeService.setVariable(instance.getId(), "nullObject",
        objectValue(null)
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .create());

    // get null value via untyped api
    assertNull(runtimeService.getVariable(instance.getId(), "nullObject"));

    // get null via typed api
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject");
    assertObjectValueDeserializedNull(typedValue);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectNullSerialized() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // set null value as "serialized" object
    runtimeService.setVariable(instance.getId(), "nullObject",
        serializedObjectValue()
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .create()); // Note: no object type name provided

    // get null value via untyped api
    assertNull(runtimeService.getVariable(instance.getId(), "nullObject"));

    // get null via typed api
    ObjectValue deserializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject");
    assertObjectValueDeserializedNull(deserializedTypedValue);

    ObjectValue serializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject", false);
    assertObjectValueSerializedNull(serializedTypedValue);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectNullSerializedObjectTypeName() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String typeName = "some.type.Name";

    // set null value as "serialized" object
    runtimeService.setVariable(instance.getId(), "nullObject",
        serializedObjectValue()
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .objectTypeName(typeName) // This time an objectTypeName is provided
        .create());

    // get null value via untyped api
    assertNull(runtimeService.getVariable(instance.getId(), "nullObject"));

    // get null via typed api
    ObjectValue deserializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject");
    assertNotNull(deserializedTypedValue);
    assertTrue(deserializedTypedValue.isDeserialized());
    assertEquals(JAVA_DATA_FORMAT, deserializedTypedValue.getSerializationDataFormat());
    assertNull(deserializedTypedValue.getValue());
    assertNull(deserializedTypedValue.getValueSerialized());
    assertNull(deserializedTypedValue.getObjectType());
    assertEquals(typeName, deserializedTypedValue.getObjectTypeName());

    ObjectValue serializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject", false);
    assertNotNull(serializedTypedValue);
    assertFalse(serializedTypedValue.isDeserialized());
    assertEquals(JAVA_DATA_FORMAT, serializedTypedValue.getSerializationDataFormat());
    assertNull(serializedTypedValue.getValueSerialized());
    assertEquals(typeName, serializedTypedValue.getObjectTypeName());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetUntypedNullForExistingVariable() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // initially the variable has a value
    JavaSerializable javaSerializable = new JavaSerializable("foo");

    runtimeService.setVariable(instance.getId(), "varName",
        objectValue(javaSerializable)
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .create());

    // get value via untyped api
    assertEquals(javaSerializable, runtimeService.getVariable(instance.getId(), "varName"));

    // set the variable to null via untyped Api
    runtimeService.setVariable(instance.getId(), "varName", null);

    // variable is now untyped null
    TypedValue nullValue = runtimeService.getVariableTyped(instance.getId(), "varName");
    assertUntypedNullValue(nullValue);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetTypedNullForExistingVariable() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // initially the variable has a value
    JavaSerializable javaSerializable = new JavaSerializable("foo");

    runtimeService.setVariable(instance.getId(), "varName",
        objectValue(javaSerializable)
        .serializationDataFormat(JAVA_DATA_FORMAT)
        .create());

    // get value via untyped api
    assertEquals(javaSerializable, runtimeService.getVariable(instance.getId(), "varName"));

    // set the variable to null via typed Api
    runtimeService.setVariable(instance.getId(), "varName", objectValue(null));

    // variable is still of type object
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "varName");
    assertObjectValueDeserializedNull(typedValue);
  }

}
