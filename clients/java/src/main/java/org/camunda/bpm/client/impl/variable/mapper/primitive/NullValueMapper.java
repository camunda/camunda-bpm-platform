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
package org.camunda.bpm.client.impl.variable.mapper.primitive;

import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.impl.value.NullValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class NullValueMapper extends AbstractPrimitiveValueMapper<NullValueImpl> implements ValueMapper<NullValueImpl> {

  public NullValueMapper() {
    super(ValueType.NULL);
  }

  public boolean isAssignable(TypedValue typedValue) {
    return typedValue == null || typedValue.getValue() == null;
  }

  public NullValueImpl deserializeTypedValue(TypedValueDto typedValueDto) {
    Map<String, Object> valueInfo = typedValueDto.getValueInfo();
    if (valueInfo != null) {

      Object isTransient = valueInfo.get("transient");
      if (isTransient != null && isTransient instanceof Boolean) {
        if ((boolean) isTransient) {
          return NullValueImpl.INSTANCE_TRANSIENT;
        }
      }
    }

    return NullValueImpl.INSTANCE;
  }

  public NullValueImpl convertToTypedValue(UntypedValueImpl untypedValue) {
    return NullValueImpl.INSTANCE;
  }

}
