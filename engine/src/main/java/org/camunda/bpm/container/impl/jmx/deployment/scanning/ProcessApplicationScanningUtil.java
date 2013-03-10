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
package org.camunda.bpm.container.impl.jmx.deployment.scanning;

import java.net.URL;
import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.container.impl.jmx.deployment.scanning.spi.ProcessApplicationScanner;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;

public class ProcessApplicationScanningUtil {
  
  /**
   * 
   * @param classLoader
   *          the classloader to scan
   * @param paResourceRootPath
   *          see {@link ProcessArchiveXml#PROP_RESOURCE_ROOT_PATH}
   * @param metaFileUrl
   *          the URL to the META-INF/processes.xml file
   * @return a Map of process definitions
   */
  public static Map<String, byte[]> findResources(ClassLoader classLoader, String paResourceRootPath, URL metaFileUrl) { 
    ProcessApplicationScanner scanner = null;
    
    try {
      // check if we must use JBoss VFS
      classLoader.loadClass("org.jboss.vfs.VFS"); 
      scanner = new VfsProcessApplicationScanner();
      
    } catch (Throwable t) {
      scanner = new ClassPathProcessApplicationScanner();
    }
    
    return scanner.findResources(classLoader, paResourceRootPath, metaFileUrl);
    
  }
  
  public static boolean isDeployable(String filename) {
    for (String bpmnResourceSuffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if (filename.endsWith(bpmnResourceSuffix)) {
        return true;
      }
    }
    return false; 
  }
  
  public static boolean isDiagramForProcess(String diagramFileName, String processFileName) {
    for (String bpmnResourceSuffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if (processFileName.endsWith(bpmnResourceSuffix)) {
        String processFilePrefix = processFileName.substring(0, processFileName.length() - bpmnResourceSuffix.length());
        if (diagramFileName.startsWith(processFilePrefix)) {
          for (String diagramResourceSuffix : BpmnDeployer.DIAGRAM_SUFFIXES) {
            if (diagramFileName.endsWith(diagramResourceSuffix)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
}