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
package org.camunda.bpm.spring.boot.starter.test.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.rules.TestRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Runner that ensures closing process engines after test run.
 */
public class ProcessEngineRuleRunner extends BlockJUnit4ClassRunner {

  private final Collection<ProcessEngineRule> processEngineRules = new ArrayList<>();

  public ProcessEngineRuleRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected List<TestRule> getTestRules(Object target) {
    List<TestRule> testRules = super.getTestRules(target);

    testRules.stream()
      .filter(t -> t instanceof ProcessEngineRule)
      .map(t -> (ProcessEngineRule)t)
      .forEach(processEngineRules::add);

    return testRules;
  }

  @Override
  public void run(RunNotifier notifier) {
    super.run(notifier);
    for (ProcessEngineRule processEngineRule : processEngineRules) {
      try {
        processEngineRule.getProcessEngine().close();
      } catch (Exception e) {
        // close quietly
      }
    }
  }

}
