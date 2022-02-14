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
package org.camunda.bpm.client.variable;

import org.camunda.bpm.client.variable.impl.type.JsonTypeImpl;
import org.camunda.bpm.client.variable.impl.type.XmlTypeImpl;
import org.camunda.bpm.client.variable.impl.value.JsonValueImpl;
import org.camunda.bpm.client.variable.impl.value.XmlValueImpl;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.client.variable.value.XmlValue;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

public class ClientValues extends Variables {

  public static final PrimitiveValueType JSON = new JsonTypeImpl();

  public static final PrimitiveValueType XML = new XmlTypeImpl();

  public static JsonValue jsonValue(String jsonValue) {
    return jsonValue(jsonValue, false);
  }

  public static JsonValue jsonValue(String jsonValue, boolean isTransient) {
    return new JsonValueImpl(jsonValue, isTransient);
  }

  public static XmlValue xmlValue(String xmlValue) {
    return xmlValue(xmlValue, false);
  }

  public static XmlValue xmlValue(String xmlValue, boolean isTransient) {
    return new XmlValueImpl(xmlValue, isTransient);
  }

}
