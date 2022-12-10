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

package org.camunda.bpm.engine.impl.persistence.entity.util;

import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFieldsImpl;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class TypedValueFieldTest {

  private static final TypedValue INITIAL_VALUE = Variables.untypedValue(new Pojo("bar"));

  private ProcessEngine engine;
  private ProcessEngineConfigurationImpl configuration;

  @Before
  public void init() {
    this.configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
    this.configuration.setFallbackSerializerFactory(new JavaObjectSerializerFactory());
    this.engine = configuration.buildProcessEngine();
  }

  @Test
  public void shouldDetectImplicitUpdatesIfEnabled() {

    // given 'implicit update detection' is enabled 
    this.configuration.setImplicitVariableUpdateDetectionEnabled(true);

    // when variables are implicitly updated
    String result = updateImplicitlyAndReturnValue().getFoo();

    // then value is updated to 'baz'.
    assertThat(result).isEqualTo("baz");
  }

  @Test
  public void shouldIgnoreImplicitUpdatesIfDisabled() {

    // given 'implicit update detection' is disabled 
    this.configuration.setImplicitVariableUpdateDetectionEnabled(false);

    // when variables are implicitly updated
    String result = updateImplicitlyAndReturnValue().getFoo();

    // then value is NOT updated.
    assertThat(result).isEqualTo("bar");
  }

  private Pojo updateImplicitlyAndReturnValue() {
    ValueFieldsImpl valueFields = new ValueFieldsImpl();

    CommandExecutor executor = configuration.getCommandExecutorTxRequired();
    VariableSerializerFactory serializerFactory = configuration.getFallbackSerializerFactory();

    // initialize 'valueFields'
    executor.execute(ctx -> new TypedValueField(valueFields, true).setValue(INITIAL_VALUE));

    // update 'valueFields' implicitly (by calling setter)
    executor.execute(ctx -> {
      TypedValueField field = new TypedValueField(valueFields, true);

      // propagate update to 'valueFields'
      field.addImplicitUpdateListener(field::setValue);
      field.setSerializerName(serializerFactory.getSerializer(INITIAL_VALUE).getName());

      // get existing value
      TypedValue typedValue = field.getTypedValue(false);

      // update 'implicitly'
      ((Pojo) typedValue.getValue()).setFoo("baz");

      // 'valueFields' is not updated yet since CommandContext is not closed yet 
      assertThat(deserializePojo(valueFields).getFoo()).isEqualTo("bar");

      return typedValue;
    });

    // at this point 'valueFields' should reflect the updated value if implicit update detection is turned on.
    return deserializePojo(valueFields);
  }

  @After
  public void tearDown() {
    this.engine.close();
    this.configuration.close();
  }

  private static Pojo deserializePojo(ValueFieldsImpl valueFields) {
    return SerializationUtils.deserialize(valueFields.getByteArrayValue());
  }

  private static class JavaObjectSerializerFactory implements VariableSerializerFactory {

    public TypedValueSerializer<?> getSerializer(String serializerName) {
      return new JavaObjectSerializer();
    }

    public TypedValueSerializer<?> getSerializer(TypedValue value) {
      return new JavaObjectSerializer();
    }
  }

  public static class Pojo implements Serializable {
    private String foo;

    public Pojo() {
    }

    public Pojo(String foo) {
      this.foo = foo;
    }

    public String getFoo() {
      return foo;
    }

    public void setFoo(String foo) {
      this.foo = foo;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Pojo pojo = (Pojo) o;
      return Objects.equals(foo, pojo.foo);
    }

    @Override
    public int hashCode() {
      return Objects.hash(foo);
    }
  }
}
