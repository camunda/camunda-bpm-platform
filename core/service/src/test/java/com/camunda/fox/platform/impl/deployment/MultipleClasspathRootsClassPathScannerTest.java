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

package com.camunda.fox.platform.impl.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

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
        new URL("file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFiles/"),
        new URL("file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFilesRecursive/"),
        new URL("file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathRecursiveTwoDirectories.jar")                    
      });
    
    ClassPathScanner scanner = new ClassPathScanner();
    Set<String> scanResult = scanner.scanPaResourceRootPath(classLoader, null, "classpath:directory/");
    
    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size());
    
    scanResult = scanner.scanPaResourceRootPath(classLoader, null, "directory/");
    
    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size());
    
    scanResult = scanner.scanPaResourceRootPath(classLoader, new URL("file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFilesRecursive/META-INF/processes.xml"), "pa:directory/");

    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size()); // only finds two processes since a PA-local resource root path is provided

  }
  
  private boolean contains(Set<String> scanResult, String suffix) {
    for (String string : scanResult) {
      if (string.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }

}
