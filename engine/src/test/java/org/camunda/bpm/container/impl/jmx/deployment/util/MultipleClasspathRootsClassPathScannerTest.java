/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.container.impl.jmx.deployment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.container.impl.deployment.scanning.ClassPathProcessApplicationScanner;
import org.junit.Test;


/**
 * @author Falko Menge
 * @author Daniel Meyer
 */
public class MultipleClasspathRootsClassPathScannerTest {

  @Test
  public void testScanClassPath_multipleRoots() throws MalformedURLException {

    // define a classloader with multiple roots.
    URLClassLoader classLoader = new URLClassLoader(
      new URL[]{
        new URL("file:src/test/resources/org/camunda/bpm/container/impl/jmx/deployment/util/ClassPathScannerTest.testScanClassPathWithFiles/"),
        new URL("file:src/test/resources/org/camunda/bpm/container/impl/jmx/deployment/util/ClassPathScannerTest.testScanClassPathWithFilesRecursive/"),
        new URL("file:src/test/resources/org/camunda/bpm/container/impl/jmx/deployment/util/ClassPathScannerTest.testScanClassPathRecursiveTwoDirectories.jar")
      });

    ClassPathProcessApplicationScanner scanner = new ClassPathProcessApplicationScanner();

    Map<String, byte[]> scanResult = new HashMap<String, byte[]>();

    scanner.scanPaResourceRootPath(classLoader, null, "classpath:directory/",scanResult);

    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size()); // only finds two files since the resource name of the processes (and diagrams) is the same

    scanResult.clear();
    scanner.scanPaResourceRootPath(classLoader, null, "directory/", scanResult);

    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size()); // only finds two files since the resource name of the processes (and diagrams) is the same

    scanResult.clear();
    scanner.scanPaResourceRootPath(classLoader, new URL("file:src/test/resources/org/camunda/bpm/container/impl/jmx/deployment/util/ClassPathScannerTest.testScanClassPathWithFilesRecursive/META-INF/processes.xml"), "pa:directory/", scanResult);

    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size()); // only finds two files since a PA-local resource root path is provided

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
