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

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;


public class DeploymentHelper {
  
  public static final String CAMUNDA_EJB_CLIENT = "org.camunda.bpm.javaee:camunda-ejb-client";
  public static final String CAMUNDA_ENGINE_CDI = "org.camunda.bpm:camunda-engine-cdi";
  public static final String CAMUNDA_ENGINE_SPRING = "org.camunda.bpm:camunda-engine-spring";
  
  private static JavaArchive CACHED_CLIENT_ASSET;
  private static JavaArchive CACHED_ENGINE_CDI_ASSET;
  private static JavaArchive[] CACHED_WELD_ASSETS;
  private static JavaArchive[] CACHED_SPRING_ASSETS;

  public static JavaArchive getEjbClient() {
    if(CACHED_CLIENT_ASSET != null) {
      return CACHED_CLIENT_ASSET;
    } else {
      
      JavaArchive[] resolvedArchives = Maven.resolver()
          .offline()
          .loadPomFromFile("pom.xml")
          .resolve(CAMUNDA_EJB_CLIENT)
          .withTransitivity()
          .as(JavaArchive.class);
      
      if(resolvedArchives.length != 1) {
        throw new RuntimeException("could not resolve "+CAMUNDA_EJB_CLIENT);
      } else {    
        CACHED_CLIENT_ASSET = resolvedArchives[0];
        return CACHED_CLIENT_ASSET;
      }
    }
    
  }
  
  public static JavaArchive getEngineCdi() {
    if(CACHED_ENGINE_CDI_ASSET != null) {
      return CACHED_ENGINE_CDI_ASSET;
    } else {

      JavaArchive[] resolvedArchives = Maven.resolver()
          .offline()
          .loadPomFromFile("pom.xml")
          .resolve(CAMUNDA_ENGINE_CDI)
          .withTransitivity()
          .as(JavaArchive.class);
      
      if(resolvedArchives.length != 1) {
        throw new RuntimeException("could not resolve "+CAMUNDA_ENGINE_CDI);
      } else {    
        CACHED_ENGINE_CDI_ASSET = resolvedArchives[0];
        return CACHED_ENGINE_CDI_ASSET;
      }
    }    
  }
  
  public static JavaArchive[] getWeld() {
    if(CACHED_WELD_ASSETS != null) {
      return CACHED_WELD_ASSETS;
    } else {

      JavaArchive[] resolvedArchives = Maven.resolver()
          .offline()
          .loadPomFromFile("pom.xml")
          .resolve(CAMUNDA_ENGINE_CDI, "org.jboss.weld.servlet:weld-servlet")
          .withTransitivity()
          .as(JavaArchive.class);
      
      if(resolvedArchives.length == 0) {
        throw new RuntimeException("could not resolve org.jboss.weld.servlet:weld-servlet");
      } else {    
        CACHED_WELD_ASSETS = resolvedArchives;
        return CACHED_WELD_ASSETS;
      }
    }
    
  }
  
  public static JavaArchive[] getEngineSpring() {
    if(CACHED_SPRING_ASSETS != null) {
      return CACHED_SPRING_ASSETS;
    } else {

      JavaArchive[] resolvedArchives = Maven.resolver()
          .offline()
          .loadPomFromFile("pom.xml")
          .addDependencies(
              MavenDependencies.createDependency("org.camunda.bpm:camunda-engine-spring", ScopeType.COMPILE, false,
                  MavenDependencies.createExclusion("org.camunda.bpm:camunda-engine")),
                  MavenDependencies.createDependency("org.springframework:spring-web", ScopeType.COMPILE, false))
          .resolve()
          .withTransitivity()
          .as(JavaArchive.class);
      
      if(resolvedArchives.length == 0) {
        throw new RuntimeException("could not resolve org.camunda.bpm:camunda-engine-spring");
      } else {    
        CACHED_SPRING_ASSETS = resolvedArchives;
        return CACHED_SPRING_ASSETS;
      }
    }
    
  }
   
}
