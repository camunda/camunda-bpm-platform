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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.listener.ClassDelegateCaseExecutionListener;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;

public class ClassExecutionListenerSpec extends AbstractExecutionListenerSpec {

  // could be configurable
  protected static final String CLASS_NAME = "org.camunda.bpm.test.caseexecutionlistener.ABC";

  public ClassExecutionListenerSpec(String eventName) {
    super(eventName);
  }

  protected void configureCaseExecutionListener(CmmnModelInstance modelInstance, CamundaCaseExecutionListener listener) {
    listener.setCamundaClass(CLASS_NAME);
  }

  public void verifyListener(DelegateListener<? extends BaseDelegateExecution> listener) {
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);
    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(CLASS_NAME, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(fieldSpecs.size(), fieldDeclarations.size());

    for (int i = 0; i < fieldDeclarations.size(); i++) {
      FieldDeclaration declaration = fieldDeclarations.get(i);
      FieldSpec matchingFieldSpec = fieldSpecs.get(i);
      matchingFieldSpec.verify(declaration);
    }
  }


}
