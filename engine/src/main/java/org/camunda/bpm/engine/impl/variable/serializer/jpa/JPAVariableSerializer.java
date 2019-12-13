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
package org.camunda.bpm.engine.impl.variable.serializer.jpa;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * Variable type capable of storing reference to JPA-entities. Only JPA-Entities which
 * are configured by annotations are supported. Use of compound primary keys is not supported.
 *
 * @author Frederik Heremans
 * @author Daniel Meyer
 */
public class JPAVariableSerializer extends AbstractTypedValueSerializer<ObjectValue> {

  public static final String NAME = "jpa";

  private JPAEntityMappings mappings;

  public JPAVariableSerializer() {
    super(ValueType.OBJECT);
    mappings = new JPAEntityMappings();
  }

  public String getName() {
    return NAME;
  }

  protected boolean canWriteValue(TypedValue value) {
    if (isDeserializedObjectValue(value) || value instanceof UntypedValueImpl) {
      return value.getValue() == null || mappings.isJPAEntity(value.getValue());
    }
    else {
      return false;
    }
  }

  protected boolean isDeserializedObjectValue(TypedValue value) {
    return value instanceof ObjectValue && ((ObjectValue) value).isDeserialized();
  }

  public ObjectValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.objectValue(untypedValue.getValue(), untypedValue.isTransient()).create();
  }

  public void writeValue(ObjectValue objectValue, ValueFields valueFields) {
    EntityManagerSession entityManagerSession = Context
      .getCommandContext()
      .getSession(EntityManagerSession.class);
    if (entityManagerSession == null) {
      throw new ProcessEngineException("Cannot set JPA variable: " + EntityManagerSession.class + " not configured");
    } else {
      // Before we set the value we must flush all pending changes from the entitymanager
      // If we don't do this, in some cases the primary key will not yet be set in the object
      // which will cause exceptions down the road.
      entityManagerSession.flush();
    }

    Object value = objectValue.getValue();
    if(value != null) {
      String className = mappings.getJPAClassString(value);
      String idString = mappings.getJPAIdString(value);
      valueFields.setTextValue(className);
      valueFields.setTextValue2(idString);
    } else {
      valueFields.setTextValue(null);
      valueFields.setTextValue2(null);
    }
  }

  public ObjectValue readValue(ValueFields valueFields, boolean deserializeObjectValue, boolean asTransientValue) {
    if(valueFields.getTextValue() != null && valueFields.getTextValue2() != null) {
      Object jpaEntity = mappings.getJPAEntity(valueFields.getTextValue(), valueFields.getTextValue2());
      return Variables.objectValue(jpaEntity).setTransient(asTransientValue).create();
    }
    return Variables.objectValue(null).setTransient(asTransientValue).create();
  }

}
