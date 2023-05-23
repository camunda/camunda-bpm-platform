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

/**
 * Class modeling a property of an object having getters / setters. Allows for value retrieval using reflection.
 */
public class ObjectProperty {

  private final Object object;
  private final String propertyName;

  private ObjectProperty(Object Object, String propertyName) {
    this.object = Object;
    this.propertyName = propertyName;
  }

  /**
   * Static factory method to create an {@link ObjectProperty} from the name of the a setter method.
   * @param object the object that contains the setter method of the corresponding property
   * @param setterMethodName the name of the setter method
   * @return the ObjectProperty
   */
  public static ObjectProperty ofSetterMethod(Object object, String setterMethodName) {
    String[] tokens = setterMethodName.split("set");

    if (tokens.length < 2) {
      throw new IllegalArgumentException("The given method name : " + setterMethodName + " is not a setter method");
    }

    String propertyName = tokens[1];
    return new ObjectProperty(object, propertyName);
  }

  public String getPropertyName() {
    return this.propertyName;
  }

  public Object getValue() {
    String getterName = "get" + propertyName;
    return MethodInvocation.of(object, getterName).invoke();
  }
}
