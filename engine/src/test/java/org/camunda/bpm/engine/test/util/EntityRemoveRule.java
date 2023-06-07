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

package org.camunda.bpm.engine.test.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 4 Rule that performs resource cleanup for methods that require post-method execution cleanup.
 * Currently, the rule supports only clean up of {@link Task}s but the rule can be extended for other resources that
 * might pollute sequential execution of other test methods.
 */
public class EntityRemoveRule extends TestWatcher {

  private static final Logger LOG = LoggerFactory.getLogger(EntityRemoveRule.class);

  protected Removable removable;

  private EntityRemoveRule() {
    this.removable = Removable.of((ProcessEngine) null);
  }

  private EntityRemoveRule(ProcessEngineTestRule engineTestRule) {
    this.removable = Removable.of(engineTestRule);
  }

  public static EntityRemoveRule of(ProcessEngineTestRule rule) {
    return new EntityRemoveRule(rule);
  }

  public static EntityRemoveRule ofLazyRule(Supplier<ProcessEngineTestRule> ruleSupplier) {
    return new LazyEntityRemoveRuleProxy(ruleSupplier);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    RemoveAfter removeAfterAnnotation = getAnnotation(description, RemoveAfter.class);
    boolean methodHasRemoveAfterAnnotation = (removeAfterAnnotation != null);

    try {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          base.evaluate();
          executePostEvaluate(removeAfterAnnotation, methodHasRemoveAfterAnnotation);
        }
      };
    } finally {
      LOG.debug("deleteTasks: {}", methodHasRemoveAfterAnnotation);
    }
  }

  protected void executePostEvaluate(RemoveAfter removeAfterAnnotation, boolean methodHasRemoveAfterAnnotation) {

    if (!methodHasRemoveAfterAnnotation) {
      return;
    }

    executePreRemoval();
    executeRemoval(removeAfterAnnotation);
  }

  /**
   * Hook method to supp
   */
  protected void executePreRemoval() {
  }

  /**
   * Hook method for executing removal.
   *
   * @param removeAfterAnnotation the remove after annotation parameter of the executing method.
   */
  protected void executeRemoval(RemoveAfter removeAfterAnnotation) {

    if (hasZeroArguments(removeAfterAnnotation)) {
      removable.removeAll();
      return;
    }

    removable.remove(removeAfterAnnotation.value());
  }

  private boolean hasZeroArguments(RemoveAfter annotation) {
    return annotation.value() == null || annotation.value().length == 0;
  }

  private <T extends Annotation> T getAnnotation(Description description, Class<T> annotation) {
    String methodName = description.getMethodName();

    try {
      Class<?> testClass = description.getTestClass();
      String methodWithoutParamsName = methodName.split("\\[")[0];

      Method method = testClass.getMethod(methodWithoutParamsName);
      return method.getAnnotation(annotation);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(
          "Failed to fetch annotation | annotationName: " + annotation.getName() + ", methodName: " + methodName, e);
    }
  }

  /* Proxy that enables EntityRemoveRule to support lazy initialization by initializing the rule using a supplier &
   *  after the execution of the method, before removal. */
  private static class LazyEntityRemoveRuleProxy extends EntityRemoveRule {

    private Supplier<ProcessEngineTestRule> supplier;

    public LazyEntityRemoveRuleProxy(Supplier<ProcessEngineTestRule> supplier) {
      super();
      this.supplier = supplier;
    }

    @Override
    protected void executePreRemoval() {
      removable = Removable.of(supplier.get());
    }
  }

}