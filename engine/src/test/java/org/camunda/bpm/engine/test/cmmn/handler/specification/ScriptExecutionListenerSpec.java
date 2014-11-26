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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ScriptCaseExecutionListener;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;

public class ScriptExecutionListenerSpec extends AbstractExecutionListenerSpec {

  //could be configurable
  protected static final String SCRIPT_FORMAT = "org.camunda.bpm.test.caseexecutionlistener.ABC";

  public ScriptExecutionListenerSpec(String eventName) {
    super(eventName);
  }

  protected void configureCaseExecutionListener(CmmnModelInstance modelInstance, CamundaCaseExecutionListener listener) {
    CamundaScript script = SpecUtil.createElement(modelInstance, listener, null, CamundaScript.class);
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(SCRIPT_FORMAT);
    script.setTextContent(scriptValue);
  }

  public void verifyListener(DelegateListener<? extends BaseDelegateExecution> listener) {
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(SCRIPT_FORMAT, executableScript.getLanguage());
  }

}
