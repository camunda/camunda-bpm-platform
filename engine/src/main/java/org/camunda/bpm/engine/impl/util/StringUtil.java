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

package org.camunda.bpm.engine.impl.util;

import java.nio.charset.Charset;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Sebastian Menski
 */
public final class StringUtil {

  /**
   * Checks whether a {@link String} seams to be an expression or not
   *
   * @param text the text to check
   * @return true if the text seams to be an expression false otherwise
   */
  public static boolean isExpression(String text) {
    text = text.trim();
    return text.startsWith("${") || text.startsWith("#{");
  }

  public static String[] split(String text, String regex) {
    if (text == null) {
      return null;
    }
    else if (regex == null) {
      return new String[] { text };
    }
    else {
      String[] result = text.split(regex);
      for (int i = 0; i < result.length; i++) {
        result[i] = result[i].trim();
      }
      return result;
    }
  }

  public static boolean hasAnySuffix(String text, String[] suffixes) {
    for (String suffix : suffixes) {
      if (text.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * converts a byte array into a string using the current process engines default charset as
   * returned by {@link ProcessEngineConfigurationImpl#getDefaultCharset()}
   *
   * @param bytes the byte array
   * @param processEngine the process engine
   * @return a string representing the bytes
   */
  public static String fromBytes(byte[] bytes) {
    EnsureUtil.ensureActiveCommandContext("StringUtil.fromBytes");
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    return fromBytes(bytes, processEngineConfiguration.getProcessEngine());
  }

  /**
   * converts a byte array into a string using the provided process engine's default charset as
   * returned by {@link ProcessEngineConfigurationImpl#getDefaultCharset()}
   *
   * @param bytes the byte array
   * @param processEngine the process engine
   * @return a string representing the bytes
   */
  public static String fromBytes(byte[] bytes, ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    Charset charset = processEngineConfiguration.getDefaultCharset();
    return new String(bytes, charset);
  }

  /**
   * Gets the bytes from a string using the current process engine's default charset
   *
   * @param string the string to get the bytes form
   * @return the byte array
   */
  public static byte[] toByteArray(String string) {
    EnsureUtil.ensureActiveCommandContext("StringUtil.toByteArray");
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    return toByteArray(string, processEngineConfiguration.getProcessEngine());
  }

  /**
   * Gets the bytes from a string using the provided process engine's default charset
   *
   * @param string the string to get the bytes form
   * @param processEngine the process engine to use
   * @return the byte array
   */
  public static byte[] toByteArray(String string, ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    Charset charset = processEngineConfiguration.getDefaultCharset();
    return string.getBytes(charset);
  }

}
