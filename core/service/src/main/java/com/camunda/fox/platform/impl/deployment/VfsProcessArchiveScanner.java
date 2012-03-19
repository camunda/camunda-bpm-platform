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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.util.IoUtil;
import org.jboss.modules.Module;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import com.camunda.fox.platform.impl.deployment.spi.ProcessArchiveScanner;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * <p>A {@link ProcessArchiveScanner} which uses Jboss Modules and Jboss VFS for
 * scanning the process archive for processes.</p>
 * 
 * <p>This implementation should be used on Jboss AS 7</p>
 * 
 * @author Daniel Meyer
 */
public class VfsProcessArchiveScanner implements ProcessArchiveScanner {

  private static Logger log = Logger.getLogger(VfsProcessArchiveScanner.class.getName());
  
  @Override
  public Map<String, byte[]> findResources(ProcessArchive processArchive) {
    
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    
    Module module = Module.forClassLoader(processArchive.getClassLoader(), true);
    Enumeration<URL> markerFileLocations = module.getExportedResources(MARKER_FILE_LOCATION);
    while (markerFileLocations.hasMoreElements()) { 
      URL url = markerFileLocations.nextElement();
      try {
        VirtualFile virtualFile = VFS.getChild(url.toURI());
        VirtualFile processArchiveRoot = virtualFile.getParent().getParent();
        
        scanRoot(processArchiveRoot, resources);
        
      } catch (URISyntaxException e) {
        log.log(Level.WARNING, "Could not load VFS File for resource: "+url, e);
      }
     
    }
    return resources;
  }

  protected void scanRoot(VirtualFile processArchiveRoot, Map<String, byte[]> resources) {
    try {
      List<VirtualFile> deployableResources = processArchiveRoot.getChildrenRecursively(new VirtualFileFilter() {
        public boolean accepts(VirtualFile file) {
          return file.getName().endsWith(BPMN_20_RESOURCE_SUFFIX);
        }
      });
      for (VirtualFile virtualFile : deployableResources) {
        String resourceName = virtualFile.getPathNameRelativeTo(processArchiveRoot);
        InputStream inputStream = virtualFile.openStream();
        byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
        IoUtil.closeSilently(inputStream);
        resources.put(resourceName,bytes);        
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not scan VFS root: "+processArchiveRoot, e);
    }
  }

}
