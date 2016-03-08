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

package org.camunda.bpm.engine.impl.history.event;

import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.util.ByteArrayField;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class HistoricDecisionInputInstanceEntity extends HistoryEvent implements HistoricDecisionInputInstance, ValueFields {

  private static final long serialVersionUID = 1L;

  protected String decisionInstanceId;

  protected String clauseId;
  protected String clauseName;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected String tenantId;

  protected ByteArrayField byteArrayField = new ByteArrayField(this);
  protected TypedValueField typedValueField = new TypedValueField(this, false);

  @Override
  public String getDecisionInstanceId() {
    return decisionInstanceId;
  }

  public void setDecisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
  }

  @Override
  public String getClauseId() {
    return clauseId;
  }

  public void setClauseId(String clauseId) {
    this.clauseId = clauseId;
  }

  @Override
  public String getClauseName() {
    return clauseName;
  }

  public void setClauseName(String clauseName) {
    this.clauseName = clauseName;
  }

  @Override
  public String getTypeName() {
    return typedValueField.getTypeName();
  }

  public void setTypeName(String typeName) {
    typedValueField.setSerializerName(typeName);
  }

  @Override
  public Object getValue() {
    return typedValueField.getValue();
  }

  @Override
  public TypedValue getTypedValue() {
    return typedValueField.getTypedValue();
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    return typedValueField.getTypedValue(deserializeValue);
  }

  @Override
  public String getErrorMessage() {
    return typedValueField.getErrorMessage();
  }

  @Override
  public String getName() {
    // used for save a byte value
    return clauseId;
  }

  @Override
  public String getTextValue() {
    return textValue;
  }

  @Override
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  @Override
  public String getTextValue2() {
    return textValue2;
  }

  @Override
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  @Override
  public Long getLongValue() {
    return longValue;
  }

  @Override
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  @Override
  public Double getDoubleValue() {
    return doubleValue;
  }

  @Override
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public String getByteArrayValueId() {
    return byteArrayField.getByteArrayId();
  }

  public void setByteArrayValueId(String byteArrayId) {
    byteArrayField.setByteArrayId(byteArrayId);
  }

  @Override
  public byte[] getByteArrayValue() {
    return byteArrayField.getByteArrayValue();
  }

  @Override
  public void setByteArrayValue(byte[] bytes) {
    byteArrayField.setByteArrayValue(bytes);
  }

  public void setValue(TypedValue typedValue) {
    typedValueField.setValue(typedValue);
  }

  public String getSerializerName() {
    return typedValueField.getSerializerName();
  }

  public void setSerializerName(String serializerName) {
    typedValueField.setSerializerName(serializerName);
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public void delete() {
    byteArrayField.deleteByteArrayValue();

    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

}
