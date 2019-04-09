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
package org.camunda.bpm.engine.rest.helper;

import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableTypeHelper {

  /**
   * The REST API is expected to return the variable's value type name in capitalized form.
   */
  public static String toExpectedValueTypeName(ValueType type) {
    String typeName = type.getName();

    String expectedName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);

    return expectedName;
  }
}
