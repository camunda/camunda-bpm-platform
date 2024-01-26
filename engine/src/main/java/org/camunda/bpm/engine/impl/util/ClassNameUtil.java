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
package org.camunda.bpm.engine.impl.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Tom Baeyens
 */
public abstract class ClassNameUtil {

  protected static final Map<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

  public static String getClassNameWithoutPackage(Object object) {
    return getClassNameWithoutPackage(object.getClass());
  }

  public static String getClassNameWithoutPackage(Class<?> clazz) {
    String unqualifiedClassName = cachedNames.get(clazz);
    if (unqualifiedClassName==null) {
      String fullyQualifiedClassName = clazz.getName();
      unqualifiedClassName = fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.')+1);
      cachedNames.put(clazz, unqualifiedClassName);
    }
    return unqualifiedClassName;
  }

  /**
   * Returns True if the given class name represents a lambda.
   *
   * @param className the given class name
   */
  public static boolean isLambda(String className) {
    if (isJdk21OrGreater()) {
      return className.contains("$$Lambda");
    }

    return className.contains("$$Lambda$");
  }

  /**
   * Returns true if the JDK version is greater or equal to 21.
   */
  public static boolean isJdk21OrGreater() {
    int featureVersion = Runtime.version().feature();
    return featureVersion >= 21;
  }
}
