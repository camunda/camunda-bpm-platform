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

package org.camunda.bpm.engine.test.util;

import junit.framework.TestCase;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Sebastian Menski
 */
public class ReflectUtilTest extends TestCase {

  public static String invalidCharacters = " \t\n\räÄöÖüÜß`|§\\{}<>¡⅛£¤⅜⅝⅞™±°¿¸ΩŁ€®Ŧ¥↑ıØÞ¨¯`^˝Ł˙ĦŊªÐẞÆ›‹©‚‘’º×÷—¦";
  public String url = null;

  public void testUrlWithInvalidCharacters() {
    url = "file:/" + invalidCharacters;
  }

  public void testUrlWithHostname() {
    url = "http://camunda.org/test.xml";
  }

  public void testUrlWithAuthority() {
    url = "http://test:test@camunda.org/test.xml";
  }

  public void testUrlWithQuery() {
    url = "http://test:test@camunda.org/test.xml?page=2";
  }

  public void testJarUrl() {
    url = "jar:file:/home/menski/review/server/jboss-as-7.2.0.Final/modules/org/camunda/bpm/camunda-engine/main/camunda-engine-7.2.0-SNAPSHOT.jar!/ProcessApplication.xsd";
  }

  public void tearDown() throws MalformedURLException {
    URL testUrl = new URL(url);
    String uri = ReflectUtil.urlToURI(testUrl).toASCIIString();
    for (char invalidCharacter : invalidCharacters.toCharArray()) {
      assertTrue("URI <" + uri + "> should not contain the character '" + invalidCharacter + "'", uri.indexOf(invalidCharacter) == -1);
    }
  }

}
