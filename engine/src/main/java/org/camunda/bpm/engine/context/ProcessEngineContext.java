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
package org.camunda.bpm.engine.context;

import org.camunda.bpm.engine.impl.context.ProcessEngineContextImpl;

import java.util.concurrent.Callable;

/**
 * <p>A utility to declare a new Process Engine Context in order
 * for database operations to be separated in a new transaction.</p>
 *
 * Example on declaring a new Process Engine Context:
 *
 * <pre>
 *  try {
 *    ProcessEngineContext.requiresNew();
 *    runtimeService.startProcessInstanceByKey("EXAMPLE_INSTANCE");
 *  } finally {
 *    ProcessEngineContext.clear();
 *  }
 * </pre>
 */
public class ProcessEngineContext {

  /**
   * Declares to the Process Engine that a new, separate context,
   * bound to the current thread, needs to be created for all subsequent
   * Process Engine database operations.
   *
   * The method should always be used in a try-finally block
   * to ensure that {@link #clear()} is called under any circumstances.
   */
  public static void requiresNew() {
    ProcessEngineContextImpl.set(true);
  }

  /**
   * Declares to the Process Engine that the new Context created
   * by the {@link #requiresNew()} method can be closed.
   */
  public static void clear() {
    ProcessEngineContextImpl.clear();
  }

  /**
   * <p>Takes a callable and executes all engine API invocations
   * within that callable in a new Process Engine Context</p>
   *
   * An alternative to calling:
   *
   * <code>
   *   try {
   *     requiresNew();
   *     callable.call();
   *   } finally {
   *     clear();
   *   }
   * </code>
   *
   * @param callable the callable to execute
   * @return what is defined by the callable passed to the method
   * @throws Exception
   */
  public static <T> T withNewProcessEngineContext(Callable<T> callable) throws Exception {
    try {
      requiresNew();
      return callable.call();
    } finally {
      clear();
    }
  }
}
