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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.container.impl.jmx.deployment.scanning.ProcessApplicationScanningUtil;


/**
 * @author Clint Manning
 */
public class VfsProcessApplicationScannerTest {

   @Test
   public void testScanProcessArchivePathForResources() throws MalformedURLException 
   {	  
	   // root of the class path
	   URLClassLoader classLoader = new URLClassLoader(
	      new URL[]{
	        new URL("file:"),	     
	      });   
	   
	   // Resource root of the process archive relative to the root of the class path 
	   String processArchiveRootPath = "classpath:org/camunda/bpm/container/impl/jmx/deployment/process/";
	   String processArchiveName = "VfsProcessScannerTest.bpmn20.xml";
	   
	   Map<String, byte[]> scanResult = ProcessApplicationScanningUtil.findResources(classLoader,processArchiveRootPath, null);
	   
	  assertTrue("'" + processArchiveName + "' found", contains(scanResult, processArchiveName));
	  // check if a folder name is not treated like a process archive
	  assertTrue("'bpmn' folder in resource path not found", !contains(scanResult, "bpmn"));
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
