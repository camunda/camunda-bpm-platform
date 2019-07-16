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
 * <p>When a Process Engine API call is performed, the engine
 * will create a Process Engine Context. The context caches database
 * entities, so that multiple operations on the same entity do not
 * result in multiple database queries. This also means that the changes
 * to these entities are accumulated and are flushed to the database
 * as soon as the Process Engine API call returns (however, the current
 * transaction might be committed at a later time).</p>
 *
 * <p>If a Process Engine API call is nested into another call, the
 * default behaviour is to reuse the existing Process Engine Context.
 * This means that the nested call will have access to the same cached
 * entities and the changes made to them.</p>
 *
 * <p>When the nested call is to be executed in a new transaction, a new Process
 * Engine Context needs to be created for its execution. In this case, the
 * nested call will use a new cache for the database entities, independent of
 * the previous (outer) call cache. This means that, the changes in the cache of
 * one call are invisible to the other call and vice versa. When the nested call
 * returns, the changes are flushed to the database independently of the Process
 * Engine Context of the outer call.</p>
 *
 * <p>The <code>ProcessEngineContext</code> is a utility class to declare to
 * the Process Engine that a new Process Engine Context needs to be created
 * in order for the database operations in a nested Process Engine API call
 * to be separated in a new transaction.</p>
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
   * Process Engine database operations. The method should always be used
   * in a try-finally block to ensure that {@link #clear()} is called
   * under any circumstances.
   *
   * Please see the {@link ProcessEngineContext} class documentation for
   * a more detailed description on the purpose of this method.
   */
  public static void requiresNew() {
    ProcessEngineContextImpl.set(true);
  }

  /**
   * Declares to the Process Engine that the new Context created
   * by the {@link #requiresNew()} method can be closed. Please
   * see the {@link ProcessEngineContext} class documentation for
   * a more detailed description on the purpose of this method.
   */
  public static void clear() {
    ProcessEngineContextImpl.clear();
  }

  /**
   * <p>Takes a callable and executes all engine API invocations
   * within that callable in a new Process Engine Context. Please
   * see the {@link ProcessEngineContext} class documentation for
   * a more detailed description on the purpose of this method.</p>
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
