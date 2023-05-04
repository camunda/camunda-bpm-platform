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

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class modeling a method invocation using reflection.
 */
public class MethodInvocation {

  private final Object object;
  private final Method method;
  private final Object[] args;

  private MethodInvocation(Object object, Method method, Object[] args) {
    this.object = object;
    this.method = method;
    this.args = args;
  }

  /**
   * Static factory method using a method name and arguments.
   *
   * @param object     The object on which to invoke the method
   * @param methodName Simple method name of the mtehod to invoke on the object
   * @param args       the arguments passed to the method
   * @return the method invocation object
   */
  public static MethodInvocation of(Object object, String methodName, Object[] args) {
    Method method = getMethodByName(object, methodName, args);

    return new MethodInvocation(object, method, args);
  }

  /**
   * Static factory method for creating a method.
   *
   * @param object     The object on which to invoke a method.
   * @param methodName simple method name of the method to invoke on the object.
   * @return the method invocation object
   */
  public static MethodInvocation of(Object object, String methodName) {
    Method method = getMethodByName(object, methodName, null);

    return new MethodInvocation(object, method, null);
  }

  /**
   * Invokes the method & returns the value.
   *
   * @return the value
   * @throws MethodInvocationException in case failure to retrieve value
   */
  public Object invoke() {
    try {
      if (args != null) {
        return method.invoke(object, args);
      }

      return method.invoke(object);
    } catch (Exception e) {
      throw new MethodInvocationException(object, method.getName(), args);
    }
  }

  private static Method getMethodByName(Object object, String methodName, Object[] args) {
    return Arrays.stream(object.getClass().getMethods())
        .filter(method -> methodName.equals(method.getName()))
        .findFirst()
        .orElseThrow(() -> new MethodInvocationException(object, methodName, args));
  }

  static class MethodInvocationException extends RuntimeException {

    private final Object object;
    private final String methodName;
    private final Object[] args;

    public MethodInvocationException(Object object, String methodName, Object[] args) {
      this.object = object;
      this.methodName = methodName;
      this.args = args;
    }

    @Override
    public String toString() {
      return "Failed to invoke method: " + methodName + ", object: " + object + ", with args: " + args;
    }
  }
}
