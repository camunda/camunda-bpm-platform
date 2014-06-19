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

}
