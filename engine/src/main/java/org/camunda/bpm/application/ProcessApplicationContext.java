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
package org.camunda.bpm.application;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.impl.ProcessApplicationContextImpl;
import org.camunda.bpm.application.impl.ProcessApplicationIdentifier;

/**
 * <p>A utility to declare the process application in which subsequent engine API calls
 * are executed. Process application context is important for the engine
 * to access custom classes as well as process-application-level entities like
 * script engines or Spin data formats.
 *
 * <p>By default, the process engine only guarantees to switch into the context
 * of the process application when it executes custom code (e.g. a JavaDelegate).
 * This utility allows to declare a process application into which the process engine
 * then switches as soon as it begins executing a command.
 *
 * Example using a variable that is serialized with a Camunda Spin data format:
 *
 * <pre>
 *  try {
 *    ProcessApplicationContext.setCurrentProcessApplication("someProcessApplication");
 *    runtimeService.setVariable(
 *      "processInstanceId",
 *      "variableName",
 *      Variables.objectValue(anObject).serializationDataFormat(SerializationDataFormats.JSON).create());
 *  } finally {
 *    ProcessApplicationContext.clear();
 *  }
 * </pre>
 *
 * <p>Declaring the process application context allows the engine to access the Spin JSON data format
 * as configured in that process application to serialize the object value. Without declaring the context,
 * the global json data format is used.
 *
 * <p>Declaring the context process application affects only engine API invocations. It DOES NOT affect
 * the context class loader for subsequent code.
 *
 * @author Thorben Lindhauer
 */
public class ProcessApplicationContext {

  /**
   * Declares the context process application for all subsequent engine API invocations
   * until {@link #clear()} is called. The context is bound to the current thread.
   * This method should always be used in a try-finally block to ensure that {@link #clear()}
   * is called under any circumstances.
   *
   * @param processApplicationName the name of the process application to switch into
   */
  public static void setCurrentProcessApplication(String processApplicationName) {
    ProcessApplicationContextImpl.set(new ProcessApplicationIdentifier(processApplicationName));
  }

  /**
   * Declares the context process application for all subsequent engine API invocations
   * until {@link #clear()} is called. The context is bound to the current thread.
   * This method should always be used in a try-finally block to ensure that {@link #clear()}
   * is called under any circumstances.
   *
   * @param reference a reference to the process application to switch into
   */
  public static void setCurrentProcessApplication(ProcessApplicationReference reference) {
    ProcessApplicationContextImpl.set(new ProcessApplicationIdentifier(reference));
  }

  /**
   * Declares the context process application for all subsequent engine API invocations
   * until {@link #clear()} is called. The context is bound to the current thread.
   * This method should always be used in a try-finally block to ensure that {@link #clear()}
   * is called under any circumstances.
   *
   * @param processApplication the process application to switch into
   */
  public static void setCurrentProcessApplication(ProcessApplicationInterface processApplication) {
    ProcessApplicationContextImpl.set(new ProcessApplicationIdentifier(processApplication));
  }

  /**
   * Clears the currently declared context process application.
   */
  public static void clear() {
    ProcessApplicationContextImpl.clear();
  }

  /**
   * <p>Takes a callable and executes all engine API invocations within that callable in the context
   * of the given process application
   *
   * <p>Equivalent to
   * <pre>
   *   try {
   *     ProcessApplicationContext.setCurrentProcessApplication("someProcessApplication");
   *     callable.call();
   *   } finally {
   *     ProcessApplicationContext.clear();
   *   }
   * </pre>
   *
   * @param callable the callable to execute
   * @param name the name of the process application to switch into
   */
  public static <T> T withProcessApplicationContext(Callable<T> callable, String processApplicationName) throws Exception {
    try {
      setCurrentProcessApplication(processApplicationName);
      return callable.call();
    }
    finally {
      clear();
    }
  }

  /**
   * <p>Takes a callable and executes all engine API invocations within that callable in the context
   * of the given process application
   *
   * <p>Equivalent to
   * <pre>
   *   try {
   *     ProcessApplicationContext.setCurrentProcessApplication("someProcessApplication");
   *     callable.call();
   *   } finally {
   *     ProcessApplicationContext.clear();
   *   }
   * </pre>
   *
   * @param callable the callable to execute
   * @param reference a reference of the process application to switch into
   */
  public static <T> T withProcessApplicationContext(Callable<T> callable, ProcessApplicationReference reference) throws Exception {
    try {
      setCurrentProcessApplication(reference);
      return callable.call();
    }
    finally {
      clear();
    }
  }

  /**
   * <p>Takes a callable and executes all engine API invocations within that callable in the context
   * of the given process application
   *
   * <p>Equivalent to
   * <pre>
   *   try {
   *     ProcessApplicationContext.setCurrentProcessApplication("someProcessApplication");
   *     callable.call();
   *   } finally {
   *     ProcessApplicationContext.clear();
   *   }
   * </pre>
   *
   * @param callable the callable to execute
   * @param processApplication the process application to switch into
   */
  public static <T> T withProcessApplicationContext(Callable<T> callable, ProcessApplicationInterface processApplication) throws Exception {
    try {
      setCurrentProcessApplication(processApplication);
      return callable.call();
    }
    finally {
      clear();
    }
  }
}
