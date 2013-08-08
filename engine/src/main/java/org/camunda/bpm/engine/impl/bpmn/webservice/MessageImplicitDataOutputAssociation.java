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
package org.camunda.bpm.engine.impl.bpmn.webservice;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.data.AbstractDataAssociation;
import org.camunda.bpm.engine.impl.bpmn.data.FieldBaseStructureInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * An implicit data output association between a source and a target.
 * source is a property in the message and target is a variable in the current execution context
 * 
 * @author Esteban Robles Luna
 */
public class MessageImplicitDataOutputAssociation extends AbstractDataAssociation {

  public MessageImplicitDataOutputAssociation(String targetRef, Expression sourceExpression) {
    super(sourceExpression, targetRef);
  }

  public MessageImplicitDataOutputAssociation(String targetRef, String sourceRef) {
    super(sourceRef, targetRef);
  }

  public MessageImplicitDataOutputAssociation(String variables) {
    super(variables);
  }

  @Override
  public void evaluate(ActivityExecution execution) {
    MessageInstance message = (MessageInstance) execution.getVariable(WebServiceActivityBehavior.CURRENT_MESSAGE);
    if (message.getStructureInstance() instanceof FieldBaseStructureInstance) {
      FieldBaseStructureInstance structure = (FieldBaseStructureInstance) message.getStructureInstance();
      execution.setVariable(this.getTarget(), structure.getFieldValue(this.getSource()));
    }
  }
}
