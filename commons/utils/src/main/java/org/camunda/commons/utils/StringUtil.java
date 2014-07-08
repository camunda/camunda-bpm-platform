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

package org.camunda.commons.utils;

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
    if (text == null) {
      return false;
    }
    text = text.trim();
    return text.startsWith("${") || text.startsWith("#{");
  }

  /**
   * Splits a {@link String} by an expression.
   *
   * @param text the text to split
   * @param regex the regex to split by
   * @return the parts of the text or null if text was null
   */
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

  /**
   * Joins a list of Strings to a single one.
   *
   * @param delimiter the delimiter between the joined parts
   * @param parts the parts to join
   * @return the joined String or null if parts was null
   */
  public static String join(String delimiter, String... parts) {
    if (parts == null) {
      return null;
    }

    if (delimiter == null) {
      delimiter = "";
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (int i=0; i < parts.length; i++) {
      if (i > 0) {
        stringBuilder.append(delimiter);
      }
      stringBuilder.append(parts[i]);
    }
    return stringBuilder.toString();
  }

}
