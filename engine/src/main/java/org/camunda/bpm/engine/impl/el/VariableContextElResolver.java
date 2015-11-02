/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public class VariableContextElResolver extends ELResolver {

  public static final String VAR_CTX_KEY = "variableContext";

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    VariableContext variableContext = (VariableContext) context.getContext(VariableContext.class);
    if(variableContext != null) {
      if(VAR_CTX_KEY.equals(property)) {
        context.setPropertyResolved(true);
        return variableContext;
      }
      TypedValue typedValue = variableContext.resolve((String) property);
      if(typedValue != null) {
        context.setPropertyResolved(true);
        return unpack(typedValue);
      }
    }
    return null;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    // read only
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    // always read only
    return true;
  }

  public Class< ? > getCommonPropertyType(ELContext arg0, Object arg1) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
    return null;
  }

  public Class< ? > getType(ELContext arg0, Object arg1, Object arg2) {
    return Object.class;
  }

  protected Object unpack(TypedValue typedValue) {
    if(typedValue != null) {
      return typedValue.getValue();
    }
    return null;
  }

}
