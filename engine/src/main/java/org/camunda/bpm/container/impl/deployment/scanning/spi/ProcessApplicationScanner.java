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
package org.camunda.bpm.container.impl.deployment.scanning.spi;

import java.net.URL;
import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;

/**
 * @author Daniel Meyer
 *
 */
public interface ProcessApplicationScanner {

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
  public abstract Map<String, byte[]> findResources(ClassLoader classLoader, String paResourceRootPath, URL metaFileUrl);

  /**
   *
   * @param classLoader
   *          the classloader to scan
   * @param paResourceRootPath
   *          see {@link ProcessArchiveXml#PROP_RESOURCE_ROOT_PATH}
   * @param metaFileUrl
   *          the URL to the META-INF/processes.xml file
   * @param additionalResourceSuffixes
   *          a list of additional suffixes for resources
   * @return a Map of process definitions
   */
  public abstract Map<String, byte[]> findResources(ClassLoader classLoader, String paResourceRootPath, URL metaFileUrl, String[] additionalResourceSuffixes);

}
