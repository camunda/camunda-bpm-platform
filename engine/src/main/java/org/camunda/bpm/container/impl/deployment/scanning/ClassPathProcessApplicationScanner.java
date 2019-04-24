/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.container.impl.deployment.scanning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.deployment.scanning.spi.ProcessApplicationScanner;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.IoUtil;

/**
 * <p>Scans for bpmn20.xml files in the classpath of the given classloader.</p>
 *
 * <p>Scans all branches of the classpath containing a META-INF/processes.xml
 * file </p>
 *
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ClassPathProcessApplicationScanner implements ProcessApplicationScanner  {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public Map<String, byte[]> findResources(ClassLoader classLoader, String paResourceRootPath, URL metaFileUrl) {
    return findResources(classLoader, paResourceRootPath, metaFileUrl, null);
  }

  public Map<String, byte[]> findResources(ClassLoader classLoader, String paResourceRootPath, URL metaFileUrl, String[] additionalResourceSuffixes) {

    final Map<String, byte[]> resourceMap = new HashMap<String, byte[]>();

    // perform the scanning. (results are collected in 'resourceMap')
    scanPaResourceRootPath(classLoader, metaFileUrl, paResourceRootPath, additionalResourceSuffixes, resourceMap);

    return resourceMap;
  }

  public void scanPaResourceRootPath(final ClassLoader classLoader, final URL metaFileUrl, final String paResourceRootPath, Map<String, byte[]> resourceMap) {
    scanPaResourceRootPath(classLoader, metaFileUrl, paResourceRootPath, null, resourceMap);
  }

  public void scanPaResourceRootPath(final ClassLoader classLoader, final URL metaFileUrl, final String paResourceRootPath, String[] additionalResourceSuffixes, Map<String, byte[]> resourceMap) {

    if(paResourceRootPath != null && !paResourceRootPath.startsWith("pa:")) {

      //  1. CASE: paResourceRootPath specified AND it is a "classpath:" resource root

      // "classpath:directory" -> "directory"
      String strippedPath = paResourceRootPath.replace("classpath:", "");
      // "directory" -> "directory/"
      strippedPath = strippedPath.endsWith("/") ? strippedPath : strippedPath +"/";
      Enumeration<URL> resourceRoots = loadClasspathResourceRoots(classLoader, strippedPath);

      while (resourceRoots.hasMoreElements()) {
        URL resourceRoot = resourceRoots.nextElement();
        scanUrl(resourceRoot, strippedPath, false, additionalResourceSuffixes, resourceMap);
      }


    } else {

      // 2nd. CASE: no paResourceRootPath specified OR paResourceRootPath is PA-local

      String strippedPaResourceRootPath = null;
      if(paResourceRootPath != null) {
        // "pa:directory" -> "directory"
        strippedPaResourceRootPath = paResourceRootPath.replace("pa:", "");
        // "directory" -> "directory/"
        strippedPaResourceRootPath = strippedPaResourceRootPath.endsWith("/") ? strippedPaResourceRootPath : strippedPaResourceRootPath +"/";
      }

      scanUrl(metaFileUrl, strippedPaResourceRootPath, true, additionalResourceSuffixes, resourceMap);

    }
  }

  protected void scanUrl(URL url, String paResourceRootPath, boolean isPaLocal, String[] additionalResourceSuffixes, Map<String, byte[]> resourceMap) {

    String urlPath = url.toExternalForm();

    if(isPaLocal) {

      if (urlPath.startsWith("file:") || urlPath.startsWith("jar:") || urlPath.startsWith("wsjar:") || urlPath.startsWith("zip:")) {
        urlPath = url.getPath();
        int withinArchive = urlPath.indexOf('!');
        if (withinArchive != -1) {
          urlPath = urlPath.substring(0, withinArchive);
        } else {
          File file = new File(urlPath);
          urlPath = file.getParentFile().getParent();
        }
      }

    } else {
      if (urlPath.startsWith("file:") || urlPath.startsWith("jar:") || urlPath.startsWith("wsjar:") || urlPath.startsWith("zip:")) {
        urlPath = url.getPath();
        int withinArchive = urlPath.indexOf('!');
        if (withinArchive != -1) {
          urlPath = urlPath.substring(0, withinArchive);
        }
      }

    }

    try {
      urlPath = URLDecoder.decode(urlPath, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw LOG.cannotDecodePathName(e);
    }

    LOG.debugRootPath(urlPath);

    scanPath(urlPath, paResourceRootPath, isPaLocal, additionalResourceSuffixes, resourceMap);

  }

  protected void scanPath(String urlPath, String paResourceRootPath, boolean isPaLocal, String[] additionalResourceSuffixes, Map<String, byte[]> resourceMap) {
    if (urlPath.startsWith("file:")) {
      urlPath = urlPath.substring(5);
    }
    if (urlPath.indexOf('!') > 0) {
      urlPath = urlPath.substring(0, urlPath.indexOf('!'));
    }

    File file = new File(urlPath);
    if (file.isDirectory()) {
      String path = file.getPath();
      String rootPath = path.endsWith(File.separator) ? path : path+File.separator;
      handleDirectory(file, rootPath,  paResourceRootPath, paResourceRootPath, isPaLocal, additionalResourceSuffixes, resourceMap);
    }
    else {
      handleArchive(file, paResourceRootPath, additionalResourceSuffixes, resourceMap);
    }
  }

  protected void handleArchive(File file, String paResourceRootPath, String[] additionalResourceSuffixes, Map<String, byte[]> resourceMap) {
    try {
      ZipFile zipFile = new ZipFile(file);
      Enumeration< ? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();
        String modelFileName = zipEntry.getName();
        if (ProcessApplicationScanningUtil.isDeployable(modelFileName, additionalResourceSuffixes) && isBelowPath(modelFileName, paResourceRootPath)) {
          String resourceName = modelFileName;
          if (paResourceRootPath != null && paResourceRootPath.length() > 0) {
            // "directory/sub_directory/process.bpmn" -> "sub_directory/process.bpmn"
            resourceName = modelFileName.replaceFirst(paResourceRootPath, "");
          }
          addResource(zipFile.getInputStream(zipEntry), resourceMap, file.getName()+"!", resourceName);
          // find diagram(s) for process
          Enumeration< ? extends ZipEntry> entries2 = zipFile.entries();
          while (entries2.hasMoreElements()) {
            ZipEntry zipEntry2 = entries2.nextElement();
            String diagramFileName = zipEntry2.getName();
            if (ProcessApplicationScanningUtil.isDiagram(diagramFileName, modelFileName)) {
              if (paResourceRootPath != null && paResourceRootPath.length() > 0) {
                // "directory/sub_directory/process.png" -> "sub_directory/process.png"
                diagramFileName = diagramFileName.replaceFirst(paResourceRootPath, "");
              }
              addResource(zipFile.getInputStream(zipEntry), resourceMap, file.getName()+"!", diagramFileName);
            }
          }
        }
      }
      zipFile.close();
    }
    catch (IOException e) {
      throw LOG.exceptionWhileScanning(file.getAbsolutePath(), e);
    }
  }

  protected void handleDirectory(File directory, String rootPath, String localPath, String paResourceRootPath, boolean isPaLocal, String[] additionalResourceSuffixes, Map<String, byte[]> resourceMap) {
    File[] paths = directory.listFiles();

    String currentPathSegment = localPath;
    if (localPath != null && localPath.length() > 0) {
      if (localPath.indexOf('/') > 0) {
        currentPathSegment = localPath.substring(0, localPath.indexOf('/'));
        localPath = localPath.substring(localPath.indexOf('/') + 1, localPath.length());
      } else {
        localPath = null;
      }
    }

    for (File path : paths) {


      if(isPaLocal   // if it is not PA-local, we have already used the classloader to specify the root path explicitly.
              && currentPathSegment != null
              && currentPathSegment.length()>0) {

        if(path.isDirectory()) {
          // only descend into directory, if below resource root:
          if(path.getName().equals(currentPathSegment)) {
            handleDirectory(path, rootPath, localPath, paResourceRootPath, isPaLocal, additionalResourceSuffixes, resourceMap);
          }
        }

      } else { // at resource root or below -> continue scanning
        String modelFileName = path.getPath();
        if (!path.isDirectory() && ProcessApplicationScanningUtil.isDeployable(modelFileName, additionalResourceSuffixes)) {
          // (1): "...\directory\sub_directory\process.bpmn" -> "sub_directory\process.bpmn"
          // (2): "sub_directory\process.bpmn" -> "sub_directory/process.bpmn"
          addResource(path, resourceMap, paResourceRootPath, modelFileName.replace(rootPath, "").replace("\\", "/"));
          // find diagram(s) for process
          for (File file : paths) {
            String diagramFileName = file.getPath();
            if (!path.isDirectory() && ProcessApplicationScanningUtil.isDiagram(diagramFileName, modelFileName)) {
              // (1): "...\directory\sub_directory\process.png" -> "sub_directory\process.png"
              // (2): "sub_directory\process.png" -> "sub_directory/process.png"
              addResource(file, resourceMap, paResourceRootPath, diagramFileName.replace(rootPath, "").replace("\\", "/"));
            }
          }
        }
        else if (path.isDirectory()) {
          handleDirectory(path, rootPath, localPath, paResourceRootPath, isPaLocal, additionalResourceSuffixes, resourceMap);
        }
      }
    }
  }

  protected void addResource(Object source, Map<String, byte[]> resourceMap, String resourceRootPath, String resourceName) {

    String resourcePath = (resourceRootPath == null ? "" : resourceRootPath).concat(resourceName);

    LOG.debugDiscoveredResource(resourcePath);

    InputStream inputStream = null;

    try {
      if(source instanceof File) {
        try {
          inputStream = new FileInputStream((File) source);
        }
        catch (IOException e) {
          throw LOG.cannotOpenFileInputStream(((File) source).getAbsolutePath(), e);
        }
      }
      else {
        inputStream = (InputStream) source;
      }
      byte[] bytes = IoUtil.readInputStream(inputStream, resourcePath);

      resourceMap.put(resourceName, bytes);

    }
    finally {
      if(inputStream != null) {
        IoUtil.closeSilently(inputStream);
      }
    }
  }

  protected Enumeration<URL> loadClasspathResourceRoots(final ClassLoader classLoader, String strippedPaResourceRootPath) {
    Enumeration<URL> resourceRoots;
    try {
      resourceRoots = classLoader.getResources(strippedPaResourceRootPath);
    }
    catch (IOException e) {
      throw LOG.couldNotGetResource(strippedPaResourceRootPath, classLoader, e);
    }
    return resourceRoots;
  }

  protected boolean isBelowPath(String processFileName, String paResourceRootPath) {
    if(paResourceRootPath == null || paResourceRootPath.length() ==0 ) {
      return true;
    }
    else {
      return processFileName.startsWith(paResourceRootPath);
    }
  }

}
