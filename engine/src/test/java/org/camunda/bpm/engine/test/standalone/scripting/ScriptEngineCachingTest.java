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
package org.camunda.bpm.engine.test.standalone.scripting;

import java.util.concurrent.Callable;

import javax.script.ScriptEngine;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;

/**
 * @author Roman Smirnov
 *
 */
public class ScriptEngineCachingTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String SCRIPT_LANGUAGE = "groovy";

  public void testGlobalCachingOfScriptEngine() {
    // when
    ScriptEngine engine = getScriptEngine(SCRIPT_LANGUAGE);

    // then
    assertNotNull(engine);
    assertEquals(engine, getScriptEngine(SCRIPT_LANGUAGE));
  }

  public void testGlobalDisableCachingOfScriptEngine() {
    // then
    processEngineConfiguration.setEnableScriptEngineCaching(false);
    getScriptingEngines().setEnableScriptEngineCaching(false);

    // when
    ScriptEngine engine = getScriptEngine(SCRIPT_LANGUAGE);

    // then
    assertNotNull(engine);
    assertFalse(engine.equals(getScriptEngine(SCRIPT_LANGUAGE)));

    processEngineConfiguration.setEnableScriptEngineCaching(true);
    getScriptingEngines().setEnableScriptEngineCaching(true);
  }

  public void testCachingOfScriptEngineInProcessApplication() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // when
    ScriptEngine engine = processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, true);

    // then
    assertNotNull(engine);
    assertEquals(engine, processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, true));
  }

  public void testDisableCachingOfScriptEngineInProcessApplication() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // when
    ScriptEngine engine = processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, false);

    // then
    assertNotNull(engine);
    assertFalse(engine.equals(processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, false)));
  }

  public void testFetchScriptEngineFromPaEnableCaching() {
    // then
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(PROCESS_PATH)
        .deploy();

    // when
    ScriptEngine engine = getScriptEngineFromPa(SCRIPT_LANGUAGE, processApplication);

    // then
    assertNotNull(engine);
    assertEquals(engine, getScriptEngineFromPa(SCRIPT_LANGUAGE, processApplication));

    // cached in pa
    assertEquals(engine, processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, true));

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testFetchScriptEngineFromPaDisableCaching() {
    // then
    processEngineConfiguration.setEnableScriptEngineCaching(false);
    getScriptingEngines().setEnableScriptEngineCaching(false);

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(PROCESS_PATH)
        .deploy();

    // when
    ScriptEngine engine = getScriptEngineFromPa(SCRIPT_LANGUAGE, processApplication);

    // then
    assertNotNull(engine);
    assertFalse(engine.equals(getScriptEngineFromPa(SCRIPT_LANGUAGE, processApplication)));

    // not cached in pa
    assertFalse(engine.equals(processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, false)));

    repositoryService.deleteDeployment(deployment.getId(), true);

    processEngineConfiguration.setEnableScriptEngineCaching(true);
    getScriptingEngines().setEnableScriptEngineCaching(true);
  }

  public void testDisableFetchScriptEngineFromProcessApplication() {
    // when
    processEngineConfiguration.setEnableFetchScriptEngineFromProcessApplication(false);

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(PROCESS_PATH)
        .deploy();

    // when
    ScriptEngine engine = getScriptEngineFromPa(SCRIPT_LANGUAGE, processApplication);

    // then
    assertNotNull(engine);
    assertEquals(engine, getScriptEngineFromPa(SCRIPT_LANGUAGE, processApplication));

    // not cached in pa
    assertFalse(engine.equals(processApplication.getScriptEngineForName(SCRIPT_LANGUAGE, true)));

    repositoryService.deleteDeployment(deployment.getId(), true);

    processEngineConfiguration.setEnableFetchScriptEngineFromProcessApplication(true);
  }

  protected ScriptingEngines getScriptingEngines() {
    return processEngineConfiguration.getScriptingEngines();
  }

  protected ScriptEngine getScriptEngine(final String name) {
    final ScriptingEngines scriptingEngines = getScriptingEngines();
    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<ScriptEngine>() {
        public ScriptEngine execute(CommandContext commandContext) {
          return scriptingEngines.getScriptEngineForLanguage(name);
        }
      });
  }

  protected ScriptEngine getScriptEngineFromPa(final String name, final ProcessApplicationInterface processApplication) {
    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<ScriptEngine>() {
        public ScriptEngine execute(CommandContext commandContext) {
          return Context.executeWithinProcessApplication(new Callable<ScriptEngine>() {

            public ScriptEngine call() throws Exception {
              return getScriptEngine(name);
            }
          }, processApplication.getReference());
        }
      });
  }

}
