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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.script.ScriptEngine;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.SourceExecutableScript;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;

/**
 * @author Roman Smirnov
 *
 */
public class EnvScriptCachingTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String SCRIPT_LANGUAGE = "groovy";
  protected static final String SCRIPT = "println 'hello world'";
  protected static final String ENV_SCRIPT = "println 'hello world from env script'";
  protected static final ScriptEnvResolver RESOLVER;

  static {
    RESOLVER = new ScriptEnvResolver() {
      public String[] resolve(String language) {
        return new String[] { ENV_SCRIPT };
      }
    };
  }

  protected ScriptFactory scriptFactory;

  public void setUp() throws Exception {
    super.setUp();
    scriptFactory = processEngineConfiguration.getScriptFactory();
    processEngineConfiguration.getEnvScriptResolvers().add(RESOLVER);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    processEngineConfiguration.getEnvScriptResolvers().remove(RESOLVER);
  }

  public void testEnabledPaEnvScriptCaching() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(PROCESS_PATH)
        .deploy();

    // when
    executeScript(processApplication);

    // then
    Map<String, List<ExecutableScript>> environmentScripts = processApplication.getEnvironmentScripts();
    assertNotNull(environmentScripts);

    List<ExecutableScript> groovyEnvScripts = environmentScripts.get(SCRIPT_LANGUAGE);

    assertNotNull(groovyEnvScripts);
    assertFalse(groovyEnvScripts.isEmpty());
    assertEquals(processEngineConfiguration.getEnvScriptResolvers().size(), groovyEnvScripts.size());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testDisabledPaEnvScriptCaching() {
    // given
    processEngineConfiguration.setEnableFetchScriptEngineFromProcessApplication(false);

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(PROCESS_PATH)
        .deploy();

    // when
    executeScript(processApplication);

    // then
    Map<String, List<ExecutableScript>> environmentScripts = processApplication.getEnvironmentScripts();
    assertNotNull(environmentScripts);
    assertNull(environmentScripts.get(SCRIPT_LANGUAGE));

    repositoryService.deleteDeployment(deployment.getId(), true);

    processEngineConfiguration.setEnableFetchScriptEngineFromProcessApplication(true);
  }

  protected SourceExecutableScript createScript(String language, String source) {
    return (SourceExecutableScript) scriptFactory.createScriptFromSource(language, source);
  }

  protected void executeScript(final ProcessApplicationInterface processApplication) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          return Context.executeWithinProcessApplication(new Callable<Void>() {

            public Void call() throws Exception {
              ScriptingEngines scriptingEngines = processEngineConfiguration.getScriptingEngines();
              ScriptEngine scriptEngine = scriptingEngines.getScriptEngineForLanguage(SCRIPT_LANGUAGE);

              SourceExecutableScript script = createScript(SCRIPT_LANGUAGE, SCRIPT);

              ScriptingEnvironment scriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();
              scriptingEnvironment.execute(script, null, null, scriptEngine);

              return null;
            }
          }, processApplication.getReference());
        }
      });
  }

}
