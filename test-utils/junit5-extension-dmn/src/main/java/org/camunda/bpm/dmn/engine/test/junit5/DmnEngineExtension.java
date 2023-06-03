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
package org.camunda.bpm.dmn.engine.test.junit5;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit 5 Extension to create and inject a {@link DmnEngine} into the test class.
 * <p>
 * Usage with the default configuration:
 * </p>
 * <pre>
 * <code>@ExtendWith(DmnEngineExtension.class)</code>
 * public class YourTest {
 *
 *   // Provide a property where the extension can inject the DMN engine...
 *   public DmnEngine dmnEngine;
 *
 *   // ... or a test method parameter, whichever better suits your needs.
 *   <code>@Test</code>
 *   void testDecision(DmnEngine dmnEngine) {
 *   }
 *
 *   ...
 * }
 * </pre>
 * <p>
 * If you want to use a custom {@link DmnEngineConfiguration} (created in the test programmatically),
 * you can register the extension directly and use the factory method to configure it.
 * <br>
 * Usage with a custom configuration:
 * </p>
 * <pre>
 * DmnEngineConfiguration myConfiguration = createMyEngineConfiguration();
 * 
 * <code>@RegisterExtension</code>
 * DmnEngineExtension dmnEngineExtension = DmnEngineExtension.forConfiguration(myConfiguration);
 * </pre>
 */
public class DmnEngineExtension implements TestInstancePostProcessor, BeforeTestExecutionCallback, ParameterResolver {

  protected final DmnEngineConfiguration dmnEngineConfiguration;
  protected DmnEngine dmnEngine;

  public DmnEngineExtension() {
    dmnEngineConfiguration = DmnEngineConfiguration.createDefaultDmnEngineConfiguration();
  }

  public static DmnEngineExtension forConfiguration(DmnEngineConfiguration configuration) {
    return new DmnEngineExtension(
        Objects.requireNonNull(configuration, "configuration must not be null"));
  }

  protected DmnEngineExtension(DmnEngineConfiguration configuration) {
    dmnEngineConfiguration = configuration;
  }

  protected void initializeDmnEngine() {
    if (dmnEngine == null) {
      dmnEngine = dmnEngineConfiguration.buildEngine();
    }
  }

  protected void injectDmnEngine(Object instance, Field field) {
    field.setAccessible(true);
    try {
      field.set(instance, dmnEngine);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    injectIntoTestInstance(testInstance);
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) throws Exception {
    final Optional<Object> testInstance = context.getTestInstance();
    if (testInstance.isPresent()) {
      injectIntoTestInstance(testInstance.get());
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return DmnEngine.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    if (!DmnEngine.class.equals(parameterContext.getParameter().getType())) {
      return null;
    }

    initializeDmnEngine();
    return dmnEngine;
  }

  private void injectIntoTestInstance(Object testInstance) throws Exception {
    initializeDmnEngine();

    Arrays.stream(testInstance.getClass().getDeclaredFields())
        .filter(field -> DmnEngine.class.equals(field.getType()))
        .forEach(field -> injectDmnEngine(testInstance, field));
  }
}
