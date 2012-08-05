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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * @author Falko Menge
 * @author Daniel Meyer
 */
@RunWith(Parameterized.class)
public class ClassPathScannerTest {

  private final String url;
  private static ClassPathScanner scanner;

  @Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][] {
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFiles/" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFilesRecursive/" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFilesRecursiveTwoDirectories/" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPath.jar" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathRecursive.jar" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathRecursiveTwoDirectories.jar" },
    });
  }
  
  
  public ClassPathScannerTest(String url) {
    this.url = url;
  }
  
  @BeforeClass
  public static void setup() {
    scanner = new ClassPathScanner();
  }
  
  /**
   * Test method for {@link com.camunda.fox.platform.impl.deployment.ClassPathScanner#scanClassPath(java.lang.ClassLoader)}.
   * @throws MalformedURLException 
   */
  @Test
  public void testScanClassPath() throws MalformedURLException {
    
    URLClassLoader classLoader = getClassloader();
    
    Set<String> scanResult = scanner.scanPaResourceRootPath(classLoader, new URL(url+"/META-INF/processes.xml"), null);

    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    if(url.contains("TwoDirectories")) {
      assertEquals(4, scanResult.size());
    } else {
      assertEquals(2, scanResult.size());
    }
  }
  
  @Test
  public void testScanClassPathWithNonExistingRootPath_relativeToPa() throws MalformedURLException {

    URLClassLoader classLoader = getClassloader();
    
    Set<String> scanResult = scanner.scanPaResourceRootPath(classLoader, new URL(url+"/META-INF/processes.xml"), "pa:nonexisting");

    assertFalse("'testDeployProcessArchive.bpmn20.xml' found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertFalse("'testDeployProcessArchive.png' found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(0, scanResult.size());
  }
  
  @Test
  public void testScanClassPathWithNonExistingRootPath_nonRelativeToPa() throws MalformedURLException {
    
    URLClassLoader classLoader = getClassloader();
    
    Set<String> scanResult = scanner.scanPaResourceRootPath(classLoader, null, "nonexisting");
    
    assertFalse("'testDeployProcessArchive.bpmn20.xml' found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertFalse("'testDeployProcessArchive.png' found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(0, scanResult.size());
  }

  @Test
  public void testScanClassPathWithExistingRootPath_relativeToPa() throws MalformedURLException {
    

    URLClassLoader classLoader = getClassloader();
    
    Set<String> scanResult = scanner.scanPaResourceRootPath(classLoader, new URL(url+"/META-INF/processes.xml"), "pa:directory/");

    if(url.contains("Recursive")) {
      assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
      assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
      assertEquals(2, scanResult.size());      
    } else {
      assertFalse("'testDeployProcessArchive.bpmn20.xml' found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
      assertFalse("'testDeployProcessArchive.png' found", contains(scanResult, "testDeployProcessArchive.png"));
      assertEquals(0, scanResult.size());
    }
  }
  
  @Test
  public void testScanClassPathWithExistingRootPath_nonRelativeToPa() throws MalformedURLException {
    
    URLClassLoader classLoader = getClassloader();
    
    Set<String> scanResult = scanner.scanPaResourceRootPath(classLoader, null, "directory/");
        
    if(url.contains("Recursive")) {
      assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
      assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
      assertEquals(2, scanResult.size());      
    } else {
      assertFalse("'testDeployProcessArchive.bpmn20.xml' found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
      assertFalse("'testDeployProcessArchive.png' found", contains(scanResult, "testDeployProcessArchive.png"));
      assertEquals(0, scanResult.size());
    }
  }
  

  private URLClassLoader getClassloader() throws MalformedURLException {
    return new URLClassLoader(new URL[]{new URL(url)});
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
