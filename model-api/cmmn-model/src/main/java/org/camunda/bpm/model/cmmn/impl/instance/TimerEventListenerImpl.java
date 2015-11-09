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
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_TIMER_EVENT_LISTENER;

import org.camunda.bpm.model.cmmn.instance.EventListener;
import org.camunda.bpm.model.cmmn.instance.StartTrigger;
import org.camunda.bpm.model.cmmn.instance.TimerEventListener;
import org.camunda.bpm.model.cmmn.instance.TimerExpression;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class TimerEventListenerImpl extends EventListenerImpl implements TimerEventListener {

  protected static ChildElement<TimerExpression> timerExpressionChild;
  protected static ChildElement<StartTrigger> timerStartChild;

  public TimerEventListenerImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public TimerExpression getTimerExpression() {
    return timerExpressionChild.getChild(this);
  }

  public void setTimerExpression(TimerExpression timerExpression) {
    timerExpressionChild.setChild(this, timerExpression);
  }

  public StartTrigger getTimerStart() {
    return timerStartChild.getChild(this);
  }

  public void setTimerStart(StartTrigger timerStart) {
    timerStartChild.setChild(this, timerStart);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(TimerEventListener.class, CMMN_ELEMENT_TIMER_EVENT_LISTENER)
        .namespaceUri(CMMN11_NS)
        .extendsType(EventListener.class)
        .instanceProvider(new ModelTypeInstanceProvider<TimerEventListener>() {
          public TimerEventListener newInstance(ModelTypeInstanceContext instanceContext) {
            return new TimerEventListenerImpl(instanceContext);
          }
        });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    timerExpressionChild = sequenceBuilder.element(TimerExpression.class)
        .build();

    timerStartChild = sequenceBuilder.element(StartTrigger.class)
        .build();

    typeBuilder.build();
  }

}
