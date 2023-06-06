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
package org.camunda.bpm.qa.upgrade.scenarios7190.variables;

import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class JPATestVariableSerializer extends AbstractTypedValueSerializer<ObjectValue> {

  public static final String NAME = "jpa";

  public JPATestVariableSerializer() {
    super(ValueType.OBJECT);
  }

  public String getName() {
    return NAME;
  }

  // implementation is left empty for test purposes
  protected boolean canWriteValue(TypedValue value) {
    return false;
  }

  protected boolean isDeserializedObjectValue(TypedValue value) {
    return false;
  }

  public ObjectValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return null;
  }

  public void writeValue(ObjectValue objectValue, ValueFields valueFields) {
  }

  public ObjectValue readValue(ValueFields valueFields, boolean deserializeObjectValue, boolean asTransientValue) {
    return null;
  }

}
