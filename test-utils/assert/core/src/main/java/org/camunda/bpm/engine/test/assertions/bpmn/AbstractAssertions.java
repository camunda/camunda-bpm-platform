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
package org.camunda.bpm.engine.test.assertions.bpmn;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;

public abstract class AbstractAssertions {

  static ThreadLocal<ProcessEngine> processEngine = new ThreadLocal<>();

  /**
   * Retrieve the processEngine bound to the current testing thread
   * via calling init(ProcessEngine processEngine). In case no such
   * processEngine is bound yet, init(processEngine) is called with
   * a default process engine.
   *
   * @return  processEngine bound to the current testing thread
   * @throws  IllegalStateException in case a processEngine has not
   *          been initialised yet and cannot be initialised with a
   *          default engine.
   */
  public static ProcessEngine processEngine() {
    ProcessEngine processEngine = AbstractAssertions.processEngine.get();
    if (processEngine != null)
      return processEngine;
    Map<String, ProcessEngine> processEngines = ProcessEngines.getProcessEngines();
    if (processEngines.size() == 1) {
      processEngine = processEngines.values().iterator().next();
      init(processEngine);
      return processEngine;
    }
    String message = processEngines.size() == 0 ? "No ProcessEngine found to be " +
      "registered with " + ProcessEngines.class.getSimpleName() + "!"
      : String.format(processEngines.size() + " ProcessEngines initialized. Call %s.init" +
      "(ProcessEngine processEngine) first!", BpmnAwareTests.class.getSimpleName());
    throw new IllegalStateException(message);
  }

  /**
   * Bind an instance of ProcessEngine to the current testing calls done
   * in your test method.
   *
   * @param   processEngine ProcessEngine which should be bound to the
   *          current testing thread.
   */
  public static void init(final ProcessEngine processEngine) {
    AbstractAssertions.processEngine.set(processEngine);
    AbstractProcessAssert.resetLastAsserts();
  }

  /**
   * Resets operations done via calling init(ProcessEngine processEngine)
   * to its clean state - just as before calling init() for the first time.
   */
  public static void reset() {
    AbstractAssertions.processEngine.remove();
    AbstractProcessAssert.resetLastAsserts();
  }

}
