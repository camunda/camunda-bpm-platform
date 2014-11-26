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
package org.camunda.bpm.container.impl.jmx.deployment.util;

import org.camunda.bpm.container.impl.deployment.scanning.ProcessApplicationScanningUtil;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * @author Clint Manning
 */
public class VfsProcessApplicationScannerTest {

  @Test
  public void testScanProcessArchivePathForResources() throws MalformedURLException {

    // given: scanning the relative test resource root
    URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:")});
    String processRootPath = "classpath:org/camunda/bpm/container/impl/jmx/deployment/process/";
    Map<String, byte[]> scanResult = ProcessApplicationScanningUtil.findResources(classLoader, processRootPath, null);

    // expect: finds only the BPMN process file and not treats the 'bpmn' folder
    assertEquals(1, scanResult.size());
    String processFileName = "VfsProcessScannerTest.bpmn20.xml";
    assertTrue("'" + processFileName + "'not found", contains(scanResult, processFileName));
    assertFalse("'bpmn' folder in resource path found", contains(scanResult, "processResource.txt"));
  }

  @Test
  public void testScanProcessArchivePathForCmmnResources() throws MalformedURLException {

    // given: scanning the relative test resource root
    URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:")});
    String processRootPath = "classpath:org/camunda/bpm/container/impl/jmx/deployment/case/";
    Map<String, byte[]> scanResult = ProcessApplicationScanningUtil.findResources(classLoader, processRootPath, null);

    // expect: finds only the CMMN process file and not treats the 'cmmn' folder
    assertEquals(1, scanResult.size());
    String processFileName = "VfsProcessScannerTest.cmmn";
    assertTrue("'" + processFileName + "' not found", contains(scanResult, processFileName));
    assertFalse("'cmmn' in resource path found", contains(scanResult, "caseResource.txt"));
  }

  @Test
  public void testScanProcessArchivePathWithAdditionalResourceSuffixes() throws MalformedURLException {
    URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:")});
    String processRootPath = "classpath:org/camunda/bpm/container/impl/jmx/deployment/script/";
    String[] additionalResourceSuffixes = new String[] { "py", "groovy", "rb" };
    Map<String, byte[]> scanResult = ProcessApplicationScanningUtil.findResources(classLoader, processRootPath, null, additionalResourceSuffixes);

    assertEquals(4, scanResult.size());
    String processFileName = "VfsProcessScannerTest.bpmn20.xml";
    assertTrue("'" + processFileName + "' not found", contains(scanResult, processFileName));
    assertTrue("'hello.py' in resource path found", contains(scanResult, "hello.py"));
    assertTrue("'hello.rb' in resource path found", contains(scanResult, "hello.rb"));
    assertTrue("'hello.groovy' in resource path found", contains(scanResult, "hello.groovy"));
  }

  private boolean contains(Map<String, byte[]> scanResult, String suffix) {
    for (String string : scanResult.keySet()) {
      if (string.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }
}
