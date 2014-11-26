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
package org.camunda.bpm.engine.test.cmmn.handler.specification;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;

public abstract class AbstractExecutionListenerSpec {

  public static final String ANY_EVENT = "any";

  protected String eventNameToRegisterOn;
  protected Set<String> expectedRegisteredEvents;

  protected List<FieldSpec> fieldSpecs;

  public AbstractExecutionListenerSpec(String eventName) {
    this.eventNameToRegisterOn = eventName;
    this.expectedRegisteredEvents = new HashSet<String>();
    this.expectedRegisteredEvents.add(eventName);

    this.fieldSpecs = new ArrayList<FieldSpec>();
  }

  public void addListenerToElement(CmmnModelInstance modelInstance, CmmnModelElementInstance modelElement) {
    ExtensionElements extensionElements = SpecUtil.createElement(modelInstance, modelElement, null, ExtensionElements.class);
    CamundaCaseExecutionListener caseExecutionListener = SpecUtil.createElement(modelInstance, extensionElements, null, CamundaCaseExecutionListener.class);

    if (!ANY_EVENT.equals(eventNameToRegisterOn)) {
      caseExecutionListener.setCamundaEvent(eventNameToRegisterOn);
    }

    configureCaseExecutionListener(modelInstance, caseExecutionListener);

    for (FieldSpec fieldSpec : fieldSpecs) {
      fieldSpec.addFieldToListenerElement(modelInstance, caseExecutionListener);
    }
  }

  protected abstract void configureCaseExecutionListener(CmmnModelInstance modelInstance, CamundaCaseExecutionListener listener);

  public void verify(CmmnActivity activity) {

    assertEquals(expectedRegisteredEvents.size(), activity.getListeners().size());

    for (String expectedRegisteredEvent : expectedRegisteredEvents) {
      List<DelegateListener<? extends BaseDelegateExecution>> listeners = activity.getListeners(expectedRegisteredEvent);
      assertEquals(1, listeners.size());
      verifyListener(listeners.get(0));
    }
  }

  protected abstract void verifyListener(DelegateListener<? extends BaseDelegateExecution> listener);

  public AbstractExecutionListenerSpec expectRegistrationFor(List<String> events) {
    expectedRegisteredEvents = new HashSet<String>(events);
    return this;
  }

  public AbstractExecutionListenerSpec withFieldExpression(String fieldName, String expression) {
    fieldSpecs.add(new FieldSpec(fieldName, expression, null, null, null));
    return this;
  }

  public AbstractExecutionListenerSpec withFieldChildExpression(String fieldName, String expression) {
    fieldSpecs.add(new FieldSpec(fieldName, null, expression, null, null));
    return this;
  }

  public AbstractExecutionListenerSpec withFieldStringValue(String fieldName, String value) {
    fieldSpecs.add(new FieldSpec(fieldName, null, null, value, null));
    return this;
  }

  public AbstractExecutionListenerSpec withFieldChildStringValue(String fieldName, String value) {
    fieldSpecs.add(new FieldSpec(fieldName, null, null, null, value));
    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("{type=");
    sb.append(this.getClass().getSimpleName());
    sb.append(", event=");
    sb.append(eventNameToRegisterOn);
    sb.append("}");

    return sb.toString();
  }
}
