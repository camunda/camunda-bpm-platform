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
package org.camunda.templateengines;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * JSR 223 compatible wrapper for the Velocity template engine.
 *
 * @author Sebastian Menski
 */
public class VelocityScriptEngine extends AbstractScriptEngine {

  protected ScriptEngineFactory scriptEngineFactory;
  protected VelocityEngine velocityEngine;

  public VelocityScriptEngine() {
    this(null);
  }

  public VelocityScriptEngine(ScriptEngineFactory scriptEngineFactory) {
    this.scriptEngineFactory = scriptEngineFactory;
  }

  public Object eval(String script, ScriptContext context) throws ScriptException {
    return eval(new StringReader(script), context);
  }

  public Object eval(Reader script, ScriptContext context) throws ScriptException {
    initVelocityEngine();
    String filename = getFilename(context);
    Writer writer = new StringWriter();
    Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

    try {
      velocityEngine.evaluate(new VelocityContext(bindings), writer, filename, script);
      writer.flush();
    } catch (Exception e) {
      throw new ScriptException(e);
    }

    return writer.toString();
  }

  public Bindings createBindings() {
    return new SimpleBindings();
  }

  public ScriptEngineFactory getFactory() {
    if (scriptEngineFactory == null) {
      synchronized (this) {
        if (scriptEngineFactory == null) {
          scriptEngineFactory = new VelocityScriptEngineFactory();
        }
      }
    }
    return scriptEngineFactory;
  }

  public void initVelocityEngine() {
    if (velocityEngine == null) {
      synchronized (this) {
        if (velocityEngine == null) {
          velocityEngine = new VelocityEngine();
          velocityEngine.init();
        }
      }
    }
  }

  protected String getFilename(ScriptContext context) {
    String filename = (String) context.getAttribute(ScriptEngine.FILENAME);
    if (filename != null) {
      return filename;
    }
    else {
      return "unknown";
    }
  }

}
