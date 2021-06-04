/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.standalone.scripting;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.SourceExecutableScript;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractScriptEnvironmentTest extends PluggableProcessEngineTest {

  protected final String processPath = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

  protected ScriptEnvResolver resolver;
  protected ScriptFactory scriptFactory;
  protected EmbeddedProcessApplication processApplication;

  @Before
  public void setUp() {
    scriptFactory = processEngineConfiguration.getScriptFactory();
    resolver = getResolver();
    processEngineConfiguration.getEnvScriptResolvers().add(resolver);
    processApplication = new EmbeddedProcessApplication();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.getEnvScriptResolvers().remove(resolver);
  }

  protected abstract ScriptEnvResolver getResolver();
  protected abstract String getScript();

  protected void executeScript(final ProcessApplicationInterface processApplication, String language) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          return Context.executeWithinProcessApplication(new Callable<Void>() {

            public Void call() throws Exception {
              ScriptingEngines scriptingEngines = processEngineConfiguration.getScriptingEngines();
              ScriptEngine scriptEngine = scriptingEngines.getScriptEngineForLanguage(language);

              SourceExecutableScript script = createScript(language, getScript());

              ScriptingEnvironment scriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();
              Bindings bindings = scriptingEngines.createBindings(scriptEngine, null);
              scriptingEnvironment.execute(script, null, bindings, scriptEngine);

              return null;
            }
          }, processApplication.getReference());
        }
      });
  }

  protected SourceExecutableScript createScript(String language, String source) {
    return (SourceExecutableScript) scriptFactory.createScriptFromSource(language, source);
  }

}
