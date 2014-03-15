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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.camunda.bpm.container.impl.jmx.deployment.scanning.spi.ProcessApplicationScanner;
import org.camunda.bpm.engine.ProcessEngineException;
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

  private static Logger log = Logger.getLogger(ClassPathProcessApplicationScanner.class.getName());
  
  public Map<String, byte[]> findResources(ClassLoader classLoader, String paResourceRootPath, URL metaFileUrl) {    
    
    final Map<String, byte[]> resourceMap = new HashMap<String, byte[]>();
    
    // perform the scanning. (results are collected in 'resourceMap')
    scanPaResourceRootPath(classLoader, metaFileUrl, paResourceRootPath, resourceMap);
    
    return resourceMap;
  }

  public void scanPaResourceRootPath(final ClassLoader classLoader, final URL metaFileUrl, final String paResourceRootPath, Map<String, byte[]> resourceMap) {
    
    if(paResourceRootPath != null && !paResourceRootPath.startsWith("pa:")) { 
      
      //  1. CASE: paResourceRootPath specified AND it is a "classpath:" resource root
      
      String strippedPath = paResourceRootPath.replace("classpath:", "");
      Enumeration<URL> resourceRoots = loadClasspathResourceRoots(classLoader, strippedPath);
      
      while (resourceRoots.hasMoreElements()) {
        URL resourceRoot = (URL) resourceRoots.nextElement();
        scanUrl(resourceRoot, strippedPath, false, resourceMap);      
      }

      
    } else {
      
      // 2nd. CASE: no paResourceRootPath specified OR paResourceRootPath is PA-local
      
      String strippedPaResourceRootPath = null;
      if(paResourceRootPath != null) {
        strippedPaResourceRootPath = paResourceRootPath.replace("pa:", "");        
        strippedPaResourceRootPath = strippedPaResourceRootPath.endsWith("/") ? strippedPaResourceRootPath : strippedPaResourceRootPath +"/";
      }
      
      scanUrl(metaFileUrl, strippedPaResourceRootPath, true, resourceMap);
      
    }
  }

  protected void scanUrl(URL url, String paResourceRootPath, boolean isPaLocal, Map<String, byte[]> resourceMap) {

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
    } catch (UnsupportedEncodingException e) {
      throw new ProcessEngineException("Could not decode pathname using utf-8 decoder.", e);
    }

    log.log(Level.FINEST, "Rootpath is {0}", urlPath);

    scanPath(urlPath, paResourceRootPath, isPaLocal, resourceMap);
    
  }

  protected void scanPath(String urlPath, String paResourceRootPath, boolean isPaLocal, Map<String, byte[]> resourceMap) {
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
      handleDirectory(file, rootPath,  paResourceRootPath, paResourceRootPath, isPaLocal, resourceMap);
    } else {
      handleArchive(file, paResourceRootPath, resourceMap);
    }
  }

  protected void handleArchive(File file, String paResourceRootPath, Map<String, byte[]> resourceMap) {    
    try {
      ZipFile zipFile = new ZipFile(file);
      Enumeration< ? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = (ZipEntry) entries.nextElement();
        String processFileName = zipEntry.getName();        
        if (ProcessApplicationScanningUtil.isDeployable(processFileName) && isBelowPath(processFileName, paResourceRootPath)) {          
          addResource(zipFile.getInputStream(zipEntry), resourceMap, file.getName()+"!", processFileName);
          // find diagram(s) for process
          Enumeration< ? extends ZipEntry> entries2 = zipFile.entries();
          while (entries2.hasMoreElements()) {
            ZipEntry zipEntry2 = (ZipEntry) entries2.nextElement();
            String diagramFileName = zipEntry2.getName();
            if (ProcessApplicationScanningUtil.isDiagramForProcess(diagramFileName, processFileName)) {
              addResource(zipFile.getInputStream(zipEntry), resourceMap, file.getName()+"!", diagramFileName);
            }
          }
        }
      }
      zipFile.close();
    } catch (IOException e) {
      throw new ProcessEngineException("IOException while scanning archive '"+file+"'.", e);
    }
  }

  protected void handleDirectory(File directory, String rootPath, String localPath, String paResourceRootPath, boolean isPaLocal, Map<String, byte[]> resourceMap) {
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
            handleDirectory(path, rootPath, localPath, paResourceRootPath, isPaLocal, resourceMap);
          }
        }
        
      } else { // at resource root or below -> continue scanning 
        String processFileName = path.getPath();
        if (!path.isDirectory() && ProcessApplicationScanningUtil.isDeployable(processFileName)) {
          addResource(path, resourceMap, paResourceRootPath, processFileName.replace(rootPath, ""));
          // find diagram(s) for process
          for (File file : paths) {
            String diagramFileName = file.getPath();
            if (!path.isDirectory() && ProcessApplicationScanningUtil.isDiagramForProcess(diagramFileName, processFileName)) {
              addResource(file, resourceMap, paResourceRootPath, diagramFileName.replace(rootPath, ""));
            }
          }
        } else if (path.isDirectory()) {
          handleDirectory(path, rootPath, localPath, paResourceRootPath, isPaLocal, resourceMap);
        }
      }
    }
  }
  
  protected void addResource(Object source, Map<String, byte[]> resourceMap, String resourceRootPath, String resourceName) {
    
    String resourcePath = (resourceRootPath == null ? "" : resourceRootPath).concat(resourceName);
    
    log.log(Level.FINEST, "discovered process resource {0}", resourcePath);
        
    InputStream inputStream = null;
    
    try {
      if(source instanceof File) {
        try {
          inputStream = new FileInputStream((File) source);
        } catch (IOException e) {
          throw new ProcessEngineException("Could not open file for reading "+source + ". "+e.getMessage(), e);
        }
      } else {
        inputStream = (InputStream) source;
      }
      byte[] bytes = IoUtil.readInputStream(inputStream, resourcePath);
      
      resourceMap.put(resourcePath, bytes);
      
    } finally {
      if(inputStream != null) {
        IoUtil.closeSilently(inputStream);
      }
    }
  }

  protected Enumeration<URL> loadClasspathResourceRoots(final ClassLoader classLoader, String strippedPaResourceRootPath) {
    Enumeration<URL> resourceRoots;
    try {
      resourceRoots = classLoader.getResources(strippedPaResourceRootPath);
    } catch (IOException e) {
      throw new ProcessEngineException("Could not load resources at '"+strippedPaResourceRootPath+"' using classloaded '"+classLoader+"'", e);
    }
    return resourceRoots;
  }
  
  protected boolean isBelowPath(String processFileName, String paResourceRootPath) {
    if(paResourceRootPath == null || paResourceRootPath.length() ==0 ) {
      return true;
    } else {
      return processFileName.startsWith(paResourceRootPath);
    }    
  }

}
