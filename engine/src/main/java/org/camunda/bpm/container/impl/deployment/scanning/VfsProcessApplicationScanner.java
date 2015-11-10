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
package org.camunda.bpm.container.impl.deployment.scanning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.deployment.scanning.spi.ProcessApplicationScanner;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * <p>A {@link ProcessArchiveScanner} which uses Jboss VFS for
 * scanning the process archive for processes.</p>
 *
 * <p>This implementation should be used on Jboss AS 7</p>
 *
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class VfsProcessApplicationScanner implements ProcessApplicationScanner {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public Map<String, byte[]> findResources(ClassLoader classLoader, String resourceRootPath, URL processesXml) {
    return findResources(classLoader, resourceRootPath, processesXml, null);
  }

  public Map<String, byte[]> findResources(ClassLoader classLoader, String resourceRootPath, URL processesXml, String[] additionalResourceSuffixes) {

    // the map in which we collect the resources
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();

    if(resourceRootPath != null && !resourceRootPath.startsWith("pa:")) {

        //  1. CASE: paResourceRootPath specified AND it is a "classpath:" resource root

        String strippedPath = resourceRootPath.replace("classpath:", "");
        Enumeration<URL> resourceRoots = loadClasspathResourceRoots(classLoader, strippedPath);

        if(!resourceRoots.hasMoreElements()) {
          LOG.cannotFindResourcesForPath(resourceRootPath, classLoader);
        }

        while (resourceRoots.hasMoreElements()) {
          URL resourceRoot = resourceRoots.nextElement();
          VirtualFile virtualRoot = getVirtualFileForUrl(resourceRoot);
          scanRoot(virtualRoot, additionalResourceSuffixes, resources);
        }

    } else {

      // 2nd. CASE: no paResourceRootPath specified OR paResourceRootPath is PA-local
      if (processesXml != null) {

        VirtualFile virtualFile = getVirtualFileForUrl(processesXml);
        // use the parent resource of the META-INF folder
        VirtualFile resourceRoot = virtualFile.getParent().getParent();

        if (resourceRootPath != null) { // pa-local path provided
          String strippedPath = resourceRootPath.replace("pa:", "");
          resourceRoot = resourceRoot.getChild(strippedPath);
        }

        // perform the scanning
        scanRoot(resourceRoot, additionalResourceSuffixes, resources);

      }
    }

    return resources;

  }

  protected VirtualFile getVirtualFileForUrl(final URL url) {
    try {
      return  VFS.getChild(url.toURI());
    }
    catch (URISyntaxException e) {
      throw LOG.exceptionWhileGettingVirtualFolder(url, e);
    }
  }

  protected void scanRoot(VirtualFile processArchiveRoot, final String[] additionalResourceSuffixes, Map<String, byte[]> resources) {
    try {
      List<VirtualFile> processes = processArchiveRoot.getChildrenRecursively(new VirtualFileFilter() {
        public boolean accepts(VirtualFile file) {
          return file.isFile() && ProcessApplicationScanningUtil.isDeployable(file.getName(), additionalResourceSuffixes);
        }
      });
      for (final VirtualFile process : processes) {
        addResource(process, processArchiveRoot, resources);
        // find diagram(s) for process
        List<VirtualFile> diagrams = process.getParent().getChildren(new VirtualFileFilter() {
          public boolean accepts(VirtualFile file) {
            return ProcessApplicationScanningUtil.isDiagram(file.getName(), process.getName());
          }
        });
        for (VirtualFile diagram : diagrams) {
          addResource(diagram, processArchiveRoot, resources);
        }
      }
    }
    catch (IOException e) {
      LOG.cannotScanVfsRoot(processArchiveRoot, e);
    }
  }

  private void addResource(VirtualFile virtualFile, VirtualFile processArchiveRoot, Map<String, byte[]> resources) {
    String resourceName = virtualFile.getPathNameRelativeTo(processArchiveRoot);
    try {
      InputStream inputStream = virtualFile.openStream();
      byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
      IoUtil.closeSilently(inputStream);
      resources.put(resourceName, bytes);
    }
    catch (IOException e) {
      LOG.cannotReadInputStreamForFile(resourceName, processArchiveRoot, e);
    }
  }

  protected Enumeration<URL> loadClasspathResourceRoots(final ClassLoader classLoader, String strippedPaResourceRootPath) {
    try {
      return classLoader.getResources(strippedPaResourceRootPath);
    }
    catch (IOException e) {
      throw LOG.exceptionWhileLoadingCpRoots(strippedPaResourceRootPath, classLoader, e);
    }
  }

}
