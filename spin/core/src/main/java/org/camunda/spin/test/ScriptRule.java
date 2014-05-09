/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.spin.test;

import org.camunda.spin.SpinFileNotFoundException;
import org.camunda.spin.SpinScriptException;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.camunda.spin.test.SpinTestLogger.LOG;

/**
 * A jUnit4 {@link TestRule} to load and execute a script. To
 * execute a {@link org.camunda.spin.test.ScriptEngine} {@link ClassRule}
 * is used to obtain a {@link ScriptEngine}.
 *
 * @author Sebastian Menski
 */
public class ScriptRule implements TestRule {

  /**
   * Mapping of known {@link ScriptEngine} language names and
   * file extensions of corresponding script files.
   */
  public static final Map<String,String> extensions = new HashMap<String, String>();
  static {
    extensions.put("python", "py");
    extensions.put("ECMAScript", "js");
    extensions.put("Groovy", "groovy");
    extensions.put("ruby", "rb");
  }

  private static final Map<String, String> testEnvironment = new HashMap<String, String>();
  static {
    testEnvironment.put("python", "import org.camunda.spin.Spin.S as S");
    testEnvironment.put("ECMAScript", "var S = org.camunda.spin.Spin.S");
    testEnvironment.put("Groovy", "S = org.camunda.spin.Spin.&S");
    testEnvironment.put("ruby", "def S(*args)\norg.camunda.spin.Spin.S(args)\nend");
  }

  private String script;
  private String scriptPath;
  private ScriptEngine scriptEngine;

  /**
   * The variables of the script accessed during script execution.
   */
  public final Map<String, Object> variables = new HashMap<String, Object>();

  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      public void evaluate() throws Throwable {
        loadScript(description);
        base.evaluate();
      }
    };
  }

  /**
   * Load a script and the script variables defined. Also execute the
   * script if {@link Script#execute()} is {@link true}.
   *
   * @param description the description of the test method
   */
  private void loadScript(Description description) {
    scriptEngine = getScriptEngine(description);
    if (scriptEngine == null) {
      return;
    }

    script = getScript(description);
    collectScriptVariables(description);
    boolean execute = isExecuteScript(description);
    if (execute) {
      executeScript();
    }
  }

  /**
   * Returns the {@link ScriptEngine} of the {@link ScriptEngineRule} of the
   * test class.
   *
   * @param description the description of the test method
   * @return the script engine found or null
   */
  private ScriptEngine getScriptEngine(Description description) {
    try {
      ScriptEngineRule scriptEngineRule = (ScriptEngineRule) description.getTestClass().getField("scriptEngine").get(null);
      return scriptEngineRule.getScriptEngine();
    } catch (NoSuchFieldException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  /**
   * Return the script as {@link String} based on the {@literal @}{@link Script} annotation
   * of the test method.
   *
   * @param description the description of the test method
   * @return the script as string or null if no script was found
   */
  private String getScript(Description description) {
    Script scriptAnnotation = description.getAnnotation(Script.class);
    if (scriptAnnotation == null) {
      return null;
    }
    String scriptBasename = getScriptBasename(scriptAnnotation, description);
    scriptPath = getScriptPath(scriptBasename, description);
    return getScriptAsString(scriptPath, description);
  }

  /**
   * Collect all {@literal @}{@link ScriptVariable} annotations of the test method
   * and save the variables in the {@link #variables} field.
   *
   * @param description the description of the test method
   */
  private void collectScriptVariables(Description description) {
    ScriptVariable scriptVariable = description.getAnnotation(ScriptVariable.class);
    collectScriptVariable(scriptVariable, description);

    Script script = description.getAnnotation(Script.class);
    if (script != null) {
      for (ScriptVariable variable : script.variables()) {
        collectScriptVariable(variable, description);
      }
    }
  }

  /**
   * Extract the variable of a single {@literal @}{@link ScriptVariable} annotation.
   *
   * @param scriptVariable the annotation
   * @param description the description of the test method
   */
  private void collectScriptVariable(ScriptVariable scriptVariable, Description description) {
    if (scriptVariable == null) {
      return;
    }

    String name = scriptVariable.name();
    String value = scriptVariable.value();
    String filename = scriptVariable.file();
    if (!value.isEmpty()) {
      variables.put(name, value);
      LOG.scriptVariableFound(name, "string", value);
    }
    else if (!filename.isEmpty()) {
      File file = getFile(filename, description);
      InputStream fileAsStream = getFileAsStream(file);
      variables.put(name, fileAsStream);
      LOG.scriptVariableFound(name, "input stream", filename);
    }
    else {
      throw LOG.scriptVariableIncomplete(name);
    }
  }

  /**
   * Determines if the script should be executed before the call of the
   * java test method.
   *
   * @param description the description of the test method
   * @return true if the script should be executed in front or false otherwise
   */
  private boolean isExecuteScript(Description description) {
    Script annotation = description.getAnnotation(Script.class);
    return annotation != null && annotation.execute();
  }

  /**
   * Executes the script with the set variables.
   *
   * @throws SpinScriptException if an error occurs during the script execution
   */
  private void executeScript() {
    if (scriptEngine != null) {
      try {
        String environment = testEnvironment.get(scriptEngine.getFactory().getLanguageName());

        SimpleBindings bindings = new SimpleBindings(variables);
        LOG.executeScriptWithScriptEngine(scriptPath, scriptEngine.getFactory().getEngineName());
        scriptEngine.eval(environment, bindings);
        scriptEngine.eval(script, bindings);
      } catch (ScriptException e) {
        throw LOG.scriptExecutionError(scriptPath, e);
      }
    }
  }

  /**
   * Execute the script and add the given variables to the script variables.
   *
   * @param scriptVariables the variables to set additionally
   * @return the variables map after execution of the script
   * @throws SpinScriptException if an error occurs during the script execution
   */
  public Map<String, Object> execute(Map<String, Object> scriptVariables) {
    variables.putAll(scriptVariables);
    executeScript();
    return variables;
  }

  /**
   * Determines the base filename of the script.
   *
   * @param scriptAnnotation the script annotation of the test method
   * @param description the description of the test method
   * @return the base filename of the script
   */
  private String getScriptBasename(Script scriptAnnotation, Description description) {
    String scriptBasename = scriptAnnotation.value();
    if (scriptBasename.isEmpty()) {
      scriptBasename = scriptAnnotation.name();
    }
    if (scriptBasename.isEmpty()) {
      scriptBasename = description.getTestClass().getSimpleName() + "." + description.getMethodName();
    }
    return scriptBasename;
  }

  /**
   * Returns the directory path of the package.
   *
   * @param description the description of the test method
   * @return the directory for the package
   */
  private String getPackageDirectoryPath(Description description) {
    String packageName = description.getTestClass().getPackage().getName();
    return replaceDotsWithPathSeparators(packageName) + File.separator;
  }

  /**
   * Replace all dots in the path with the {@link File#separator} character.
   *
   * @param path the path to process
   * @return the processed path
   */
  private String replaceDotsWithPathSeparators(String path) {
    return path.replaceAll("\\.", File.separator);
  }

  /**
   * Returns the full path of the script based on package and basename.
   *
   * @param scriptBasename the basename of the script file
   * @param description the description of the test method
   * @return the full path
   */
  private String getScriptPath(String scriptBasename, Description description) {
    return getPackageDirectoryPath(description) +  scriptBasename + getScriptExtension();
  }

  /**
   * Returns the script file extension based on the {@link ScriptEngine} language.
   *
   * @return the file extension or empty string if none was found
   */
  private String getScriptExtension() {
    String languageName = scriptEngine.getFactory().getLanguageName();
    String extension = extensions.get(languageName);
    if (extension == null) {
      LOG.noScriptExtensionFoundForScriptLanguage(languageName);
      return "";
    }
    else {
      return "." + extension;
    }
  }

  /**
   * Returns the script as {@link String}.
   *
   * @param scriptPath the path of the script file
   * @param description the description of the test method
   * @return the script as string
   * @throws SpinFileNotFoundException if the script was cannot be loaded
   */
  private String getScriptAsString(String scriptPath, Description description) {
    File file = getFile(scriptPath, description);
    InputStream inputStream = null;
    try {
      inputStream = getFileAsStream(file);
      byte[] buffer = new byte[(int) file.length()];
      inputStream.read(buffer);
      LOG.scriptLoaded(scriptPath);
      return new String(buffer);
    } catch (IOException e) {
      throw LOG.fileNotFoundException(scriptPath, e);
    }
    finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Returns the input stream of a file.
   *
   * @param file the {@link File} to load
   * @return the file content as input stream
   * @throws SpinFileNotFoundException if the file cannot be loaded
   */
  private InputStream getFileAsStream(File file) {
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      throw LOG.fileNotFoundException(file.getAbsolutePath());
    }
  }

  /**
   * Returns the {@link File} for a filename.
   *
   * @param filename the filename to load
   * @param description the description of the test method
   * @return the file object
   * @throws SpinFileNotFoundException if the file cannot be loaded
   */
  private File getFile(String filename, Description description) {
    ClassLoader classLoader = description.getTestClass().getClassLoader();
    URL fileUrl = classLoader.getResource(filename);
    if (fileUrl == null) {
      throw LOG.fileNotFoundException(filename);
    }

    try {
      return new File(fileUrl.toURI());
    } catch (URISyntaxException e) {
      throw LOG.fileNotFoundException(filename, e);
    }
  }

}
