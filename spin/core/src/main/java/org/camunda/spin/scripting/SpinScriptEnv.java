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
package org.camunda.spin.scripting;

import org.camunda.spin.SpinScriptException;
import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.impl.util.SpinIoUtil;

import javax.script.ScriptEngine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Spin provides a set of environment scripts for known scripting languages.
 * These scripts make it easier to use spin in a scripting language. Users can
 * execute these scripts before they execute their custom scripts.
 *
 * @author Daniel Meyer
 *
 */
public class SpinScriptEnv {

  private static final SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  private static final String ENV_PATH_TEMPLATE = "script/env/%s/spin.%s";

  /**
   * Mapping of known {@link ScriptEngine} language names and
   * file extensions of corresponding script files.
   */
  public static final Map<String,String> extensions = new HashMap<String, String>();
  static {
    extensions.put("python", "py");
    extensions.put("javascript", "js");
    extensions.put("groovy", "groovy");
    extensions.put("ruby", "rb");
  }

  /**
   * Get file extension for script language.
   *
   * @param language the language name
   * @return the file extension as string or null if the language is not in the set of languages supported by spin
   */
  public static String getExtension(String language) {
    language = language.toLowerCase();
    if("ecmascript".equals(language)) {
      language = "javascript";
    }
    return extensions.get(language);
  }

  /**
   * Get the spin scripting environment
   *
   * @param language the language name
   * @return the environment script as string or null if  the language is
   * not in the set of languages supported by spin.
   */
  public static String get(String language) {
    language = language.toLowerCase();
    if("ecmascript".equals(language)) {
      language = "javascript";
    }

    String extension = extensions.get(language);
    if(extension == null) {
      return null;

    } else {
      return loadScriptEnv(language, extension);

    }
  }

  protected static String loadScriptEnv(String language, String extension) {
    String scriptEnvPath = String.format(ENV_PATH_TEMPLATE, language, extension);
    InputStream envResource = SpinScriptException.class.getClassLoader().getResourceAsStream(scriptEnvPath);

    if(envResource == null) {
      throw LOG.noScriptEnvFoundForLanguage(language, scriptEnvPath);

    } else {
      try {
        return SpinIoUtil.inputStreamAsString(envResource);

      } finally {
        SpinIoUtil.closeSilently(envResource);

      }
    }
  }

}
