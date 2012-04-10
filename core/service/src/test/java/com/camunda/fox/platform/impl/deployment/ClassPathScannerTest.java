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

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * @author Falko Menge
 */
@RunWith(Parameterized.class)
public class ClassPathScannerTest {

  private final String url;

  @Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][] {
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFiles/" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathWithFilesRecursive/" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPath.jar" },
            { "file:src/test/resources/com/camunda/fox/platform/impl/deployment/ClassPathScannerTest.testScanClassPathRecursive.jar" },
    });
  }
  
  
  public ClassPathScannerTest(String url) {
    this.url = url;
  }
  
  /**
   * Test method for {@link com.camunda.fox.platform.impl.deployment.ClassPathScanner#scanClassPath(java.lang.ClassLoader)}.
   * @throws MalformedURLException 
   */
  @Test
  public void testScanClassPath() throws MalformedURLException {
    List<URL> urls = new ArrayList<URL>();
    urls.add(new URL(url));
    URL[] urlz = urls.toArray(new URL[0]);
    URLClassLoader classLoader = new URLClassLoader(urlz, null);
    ClassPathScanner scanner = new ClassPathScanner();
    Set<String> scanResult = scanner.scanClassPath(classLoader);
    assertTrue("'testDeployProcessArchive.bpmn20.xml' not found", contains(scanResult, "testDeployProcessArchive.bpmn20.xml"));
    assertTrue("'testDeployProcessArchive.png' not found", contains(scanResult, "testDeployProcessArchive.png"));
    assertEquals(2, scanResult.size());
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
