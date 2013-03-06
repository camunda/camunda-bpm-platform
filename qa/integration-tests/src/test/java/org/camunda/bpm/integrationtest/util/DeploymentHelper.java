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
package org.camunda.bpm.integrationtest.util;

import java.util.Collection;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;


public class DeploymentHelper {
  
  private static MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class).goOffline().loadMetadataFromPom("pom.xml");
  
  private static JavaArchive CACHED_CLIENT_ASSET;
  private static Collection<JavaArchive> CACHED_WELD_ASSETS;

  public static JavaArchive getFoxPlatformClient() {
    if(CACHED_CLIENT_ASSET != null) {
      return CACHED_CLIENT_ASSET;
    } else {
      Collection<JavaArchive> resolvedArchives = resolver.artifact("com.camunda.fox.platform:fox-platform-client").resolveAs(JavaArchive.class);
      
      if(resolvedArchives.size() != 1) {
        throw new RuntimeException("could not resolve com.camunda.fox.platform:fox-platform-client");
      } else {    
        CACHED_CLIENT_ASSET = resolvedArchives.iterator().next();
        return CACHED_CLIENT_ASSET;
      }
    }
    
  }
  
  public static Collection<JavaArchive> getWeld() {
    if(CACHED_WELD_ASSETS != null) {
      return CACHED_WELD_ASSETS;
    } else {      
      Collection<JavaArchive> resolvedArchives = resolver.artifact("org.jboss.weld.servlet:weld-servlet").resolveAs(JavaArchive.class);
      
      if(resolvedArchives.size()==0) {
        throw new RuntimeException("could not resolve org.jboss.weld.servlet:weld-servlet");
      } else {    
        CACHED_WELD_ASSETS = resolvedArchives;
        return CACHED_WELD_ASSETS;
      }
    }
    
  }
   
}
