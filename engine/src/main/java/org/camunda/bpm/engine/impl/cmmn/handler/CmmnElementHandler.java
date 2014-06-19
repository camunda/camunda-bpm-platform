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
package org.camunda.bpm.engine.impl.cmmn.handler;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.handler.ModelElementHandler;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * <p>This handler handles an instance of a {@link CmmnElement} to create
 * a new {@link CmmnActivity activity}.</p>
 *
 * @author Roman Smirnov
 *
 */
public abstract class CmmnElementHandler<T extends CmmnElement> implements ModelElementHandler<T, CmmnHandlerContext> {

  protected CmmnActivity createActivity(CmmnElement element, CmmnHandlerContext context) {
    String id = element.getId();
    CmmnActivity parent = context.getParent();

    CmmnActivity newActivity = null;

    if (parent != null) {
      newActivity = parent.createActivity(id);

    } else {
      CmmnCaseDefinition caseDefinition = context.getCaseDefinition();
      newActivity = new CmmnActivity(id, caseDefinition);
    }

    newActivity.setCmmnElement(element);

    CmmnActivityBehavior behavior = getActivityBehavior();
    newActivity.setActivityBehavior(behavior);

    return newActivity;
  }

  protected abstract void initializeActivity(T element, CmmnActivity activity, CmmnHandlerContext context);

  protected CmmnActivityBehavior getActivityBehavior() {
    return null;
  }

  public abstract CmmnActivity handleElement(T element, CmmnHandlerContext context);

}
