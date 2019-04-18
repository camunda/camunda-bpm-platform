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
package org.camunda.bpm.container.impl.jboss.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Utility methods to manipulate the Current Thread Context Classloader
 * 
 * @author Daniel Meyer
 */
public class Tccl {

  public static interface Operation<T> {
    T run();
  }

  public static <T> T runUnderClassloader(final Operation<T> operation, final ClassLoader classLoader) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      return AccessController.doPrivileged(new PrivilegedAction<T>() {
        public T run() {
          try {
            return runWithTccl(operation, classLoader);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      });     
    } else {
      return runWithTccl(operation, classLoader);
    }
  }

  private static <T> T runWithTccl(Operation<T> operation, ClassLoader classLoader) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      return operation.run();
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

}