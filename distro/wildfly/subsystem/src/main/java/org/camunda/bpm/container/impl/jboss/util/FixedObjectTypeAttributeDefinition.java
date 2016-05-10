package org.camunda.bpm.container.impl.jboss.util;

import org.jboss.as.controller.AbstractAttributeDefinitionBuilder;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.MapAttributeDefinition;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.ObjectTypeValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Fix value type validation for ObjectTypeAttributeDefinition containing a map as value type.
 * Works without this hack in WF-10, not in WF-8.
 *
 * @author Christian Lipphardt
 */
public class FixedObjectTypeAttributeDefinition extends ObjectTypeAttributeDefinition {

  public FixedObjectTypeAttributeDefinition(AbstractAttributeDefinitionBuilder<?, ? extends ObjectTypeAttributeDefinition> builder, String suffix, AttributeDefinition[] valueTypes) {
    super(builder, suffix, valueTypes);
  }

  @Override
  protected void addValueTypeDescription(ModelNode node, String prefix, ResourceBundle bundle, ResourceDescriptionResolver resolver, Locale locale) {
    super.addValueTypeDescription(node, prefix, bundle, resolver, locale);

    try {
      Field valueTypesField = ObjectTypeAttributeDefinition.class.getDeclaredField("valueTypes");
      valueTypesField.setAccessible(true);
      Object value = valueTypesField.get(this);
      if (value == null) {
        return;
      } else if (AttributeDefinition[].class.isAssignableFrom(value.getClass())){
        for (AttributeDefinition valueType : (AttributeDefinition[])value) {
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
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static final class Builder extends AbstractAttributeDefinitionBuilder<Builder, FixedObjectTypeAttributeDefinition> {
    private String suffix;
    private final AttributeDefinition[] valueTypes;

    public Builder(final String name, final AttributeDefinition... valueTypes) {
      super(name, ModelType.OBJECT, true);
      this.valueTypes = valueTypes;

    }

    public static Builder of(final String name, final AttributeDefinition... valueTypes) {
      return new Builder(name, valueTypes);
    }

    public static Builder of(final String name, final AttributeDefinition[] valueTypes, final AttributeDefinition[] moreValueTypes) {
      ArrayList<AttributeDefinition> list = new ArrayList<AttributeDefinition>(Arrays.asList(valueTypes));
      list.addAll(Arrays.asList(moreValueTypes));
      AttributeDefinition[] allValueTypes = new AttributeDefinition[list.size()];
      list.toArray(allValueTypes);

      return new Builder(name, allValueTypes);
    }

    public FixedObjectTypeAttributeDefinition build() {
      if (validator == null) { validator = new ObjectTypeValidator(allowNull, valueTypes); }
//      attributeMarshaller = new Object
      return new FixedObjectTypeAttributeDefinition(this, suffix, valueTypes);
    }

    public Builder setSuffix(final String suffix) {
      this.suffix = suffix;
      return this;
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
