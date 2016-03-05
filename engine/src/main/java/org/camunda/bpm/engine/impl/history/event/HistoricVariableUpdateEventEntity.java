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



/**
 * @author Daniel Meyer
 *
 */
public class HistoricVariableUpdateEventEntity extends HistoricDetailEventEntity {

  private static final long serialVersionUID = 1L;

  protected int revision;

  protected String variableName;
  protected String variableInstanceId;
  protected String scopeActivityInstanceId;

  protected String serializerName;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;
  protected byte[] byteValue;

  protected String byteArrayId;

  // getter / setters ////////////////////////////

  public String getSerializerName() {
    return serializerName;
  }
  public void setSerializerName(String serializerName) {
    this.serializerName = serializerName;
  }
  public String getVariableName() {
    return variableName;
  }
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
  public Long getLongValue() {
    return longValue;
  }
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }
  public Double getDoubleValue() {
    return doubleValue;
  }
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }
  public String getTextValue() {
    return textValue;
  }
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }
  public String getTextValue2() {
    return textValue2;
  }
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }
  public byte[] getByteValue() {
    return byteValue;
  }
  public void setByteValue(byte[] byteValue) {
    this.byteValue = byteValue;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  public void setByteArrayId(String id) {
    byteArrayId = id;
  }
  public String getByteArrayId() {
    return byteArrayId;
  }
  public String getVariableInstanceId() {
    return variableInstanceId;
  }
  public void setVariableInstanceId(String variableInstanceId) {
    this.variableInstanceId = variableInstanceId;
  }
  public String getScopeActivityInstanceId() {
    return scopeActivityInstanceId;
  }
  public void setScopeActivityInstanceId(String scopeActivityInstanceId) {
    this.scopeActivityInstanceId = scopeActivityInstanceId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[variableName=" + variableName
           + ", variableInstanceId=" + variableInstanceId
           + ", revision=" + revision
           + ", serializerName=" + serializerName
           + ", longValue=" + longValue
           + ", doubleValue=" + doubleValue
           + ", textValue=" + textValue
           + ", textValue2=" + textValue2
           + ", byteArrayId=" + byteArrayId
           + ", activityInstanceId=" + activityInstanceId
           + ", scopeActivityInstanceId=" + scopeActivityInstanceId
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", id=" + id
           + ", processDefinitionId=" + processInstanceId
           + ", processInstanceId=" + processInstanceId
           + ", taskId=" + taskId
           + ", timestamp=" + timestamp
           + ", tenantId=" + tenantId
           + "]";
  }

}
