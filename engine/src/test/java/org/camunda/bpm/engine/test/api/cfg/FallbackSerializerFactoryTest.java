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

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class FallbackSerializerFactoryTest {

  protected ProcessEngine processEngine;
  protected String deployment;

  @After
  public void tearDown() {

    if (processEngine != null) {
      if (deployment != null) {
        processEngine.getRepositoryService().deleteDeployment(deployment, true);
      }

      processEngine.close();
    }
  }

  @Test
  public void testFallbackSerializer() {
    // given
    // that the process engine is configured with a fallback serializer factory
     ProcessEngineConfigurationImpl engineConfiguration = new StandaloneInMemProcessEngineConfiguration()
       .setJdbcUrl("jdbc:h2:mem:camunda-forceclose")
       .setProcessEngineName("engine-forceclose");

     engineConfiguration.setFallbackSerializerFactory(new ExampleSerializerFactory());

     processEngine = engineConfiguration.buildProcessEngine();
     deployOneTaskProcess(processEngine);

     // when setting a variable that no regular serializer can handle
     ObjectValue objectValue = Variables.objectValue("foo").serializationDataFormat(ExampleSerializer.FORMAT).create();

     ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess",
         Variables.createVariables().putValueTyped("var", objectValue));

     ObjectValue fetchedValue = processEngine.getRuntimeService().getVariableTyped(pi.getId(), "var", true);

     // then the fallback serializer is used
     Assert.assertNotNull(fetchedValue);
     Assert.assertEquals(ExampleSerializer.FORMAT, fetchedValue.getSerializationDataFormat());
     Assert.assertEquals("foo", fetchedValue.getValue());
  }

  @Test
  public void testFallbackSerializerDoesNotOverrideRegularSerializer() {
    // given
    // that the process engine is configured with a serializer for a certain format
    // and a fallback serializer factory for the same format
     ProcessEngineConfigurationImpl engineConfiguration = new StandaloneInMemProcessEngineConfiguration()
       .setJdbcUrl("jdbc:h2:mem:camunda-forceclose")
       .setProcessEngineName("engine-forceclose");

     engineConfiguration.setCustomPreVariableSerializers(Arrays.<TypedValueSerializer>asList(new ExampleConstantSerializer()));
     engineConfiguration.setFallbackSerializerFactory(new ExampleSerializerFactory());

     processEngine = engineConfiguration.buildProcessEngine();
     deployOneTaskProcess(processEngine);

     // when setting a variable that no regular serializer can handle
     ObjectValue objectValue = Variables.objectValue("foo").serializationDataFormat(ExampleSerializer.FORMAT).create();

     ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess",
         Variables.createVariables().putValueTyped("var", objectValue));

     ObjectValue fetchedValue = processEngine.getRuntimeService().getVariableTyped(pi.getId(), "var", true);

     // then the fallback serializer is used
     Assert.assertNotNull(fetchedValue);
     Assert.assertEquals(ExampleSerializer.FORMAT, fetchedValue.getSerializationDataFormat());
     Assert.assertEquals(ExampleConstantSerializer.DESERIALIZED_VALUE, fetchedValue.getValue());
  }

  public static class ExampleSerializerFactory implements VariableSerializerFactory {

    public TypedValueSerializer<?> getSerializer(String serializerName) {
      return new ExampleSerializer();
    }

    public TypedValueSerializer<?> getSerializer(TypedValue value) {
      return new ExampleSerializer();
    }

  }

  public static class ExampleSerializer extends JavaObjectSerializer {

    public static final String FORMAT = "example";

    public ExampleSerializer() {
      super();
      this.serializationDataFormat = FORMAT;
    }

    public String getName() {
      return FORMAT;
    }

  }

  public static class ExampleConstantSerializer extends JavaObjectSerializer {

    public static final String DESERIALIZED_VALUE = "bar";

    public ExampleConstantSerializer() {
      super();
      this.serializationDataFormat = ExampleSerializer.FORMAT;
    }

    public String getName() {
      return ExampleSerializer.FORMAT;
    }

    protected Object deserializeFromByteArray(byte[] bytes, String objectTypeName) throws Exception {
      // deserialize everything to a constant string
      return DESERIALIZED_VALUE;
    }

  }

  protected void deployOneTaskProcess(ProcessEngine engine) {
    deployment = engine.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .deploy()
        .getId();
  }
}
