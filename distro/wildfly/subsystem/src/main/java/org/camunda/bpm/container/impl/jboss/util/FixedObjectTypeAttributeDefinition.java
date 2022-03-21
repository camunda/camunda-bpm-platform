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

import org.jboss.as.controller.AbstractAttributeDefinitionBuilder;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.MapAttributeDefinition;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.ObjectTypeValidator;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Fix value type validation for ObjectTypeAttributeDefinition containing a map as value type.
 * Works without this hack in WF-10, not in WF-8.
 *
 * @author Christian Lipphardt
 */
public class FixedObjectTypeAttributeDefinition extends ObjectTypeAttributeDefinition {

  private static final Logger LOG = Logger.getLogger(FixedObjectTypeAttributeDefinition.class.getName());

  public FixedObjectTypeAttributeDefinition(AbstractAttributeDefinitionBuilder<?, ? extends ObjectTypeAttributeDefinition> builder,
                                            String suffix,
                                            AttributeDefinition[] valueTypes) {
    super(builder, suffix, valueTypes);
  }

  @Override
  protected void addValueTypeDescription(ModelNode node,
                                         String prefix,
                                         ResourceBundle bundle,
                                         ResourceDescriptionResolver resolver,
                                         Locale locale) {
    super.addValueTypeDescription(node, prefix, bundle, false, resolver, locale);

    try {
      Field valueTypesField = ObjectTypeAttributeDefinition.class.getDeclaredField("valueTypes");
      valueTypesField.setAccessible(true);
      Object value = valueTypesField.get(this);
      if (value != null) {
        if (AttributeDefinition[].class.isAssignableFrom(value.getClass())) {
          for (AttributeDefinition valueType : (AttributeDefinition[]) value) {
            final ModelNode childType = node.get(ModelDescriptionConstants.VALUE_TYPE, valueType.getName());

            if (valueType instanceof MapAttributeDefinition) {
              if (!childType.hasDefined(ModelDescriptionConstants.VALUE_TYPE)) {
                childType.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
              }
              if (!childType.hasDefined(ModelDescriptionConstants.EXPRESSIONS_ALLOWED)) {
                childType.get(ModelDescriptionConstants.EXPRESSIONS_ALLOWED).set(new ModelNode(false));
              }
            }
          }
        }
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      LOG.warning("Could not access 'valueTypes', the attribute is added nonetheless");
    }
  }

  public static final class Builder
      extends AbstractAttributeDefinitionBuilder<Builder, FixedObjectTypeAttributeDefinition> {
    private String suffix;
    private final AttributeDefinition[] valueTypes;

    public Builder(final String name, final AttributeDefinition... valueTypes) {
      super(name, ModelType.OBJECT, true);
      this.valueTypes = valueTypes;

    }

    public static Builder of(final String name, final AttributeDefinition... valueTypes) {
      return new Builder(name, valueTypes);
    }

    public static Builder of(final String name,
                             final AttributeDefinition[] valueTypes,
                             final AttributeDefinition[] moreValueTypes) {
      ArrayList<AttributeDefinition> list = new ArrayList<>(Arrays.asList(valueTypes));
      list.addAll(Arrays.asList(moreValueTypes));
      AttributeDefinition[] allValueTypes = new AttributeDefinition[list.size()];
      list.toArray(allValueTypes);

      return new Builder(name, allValueTypes);
    }

    public FixedObjectTypeAttributeDefinition build() {
      ParameterValidator validator = getValidator();
      if (validator == null) {
        ObjectTypeValidator objectTypeValidator = new ObjectTypeValidator(isAllowNull(), valueTypes);
        setValidator(objectTypeValidator);
      }
      return new FixedObjectTypeAttributeDefinition(this, suffix, valueTypes);
    }

    /*
   --------------------------
   added for binary compatibility for running compatibilty tests
    */
    @Override
    public Builder setAllowNull(boolean allowNull) {
      return super.setAllowNull(allowNull);
    }
  }

}
