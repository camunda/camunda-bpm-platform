package com.camunda.fox.platform.impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class PlatformServiceExtensionHelper {
  
  protected static List<PlatformServiceExtension> cachedProviders;
  
  public static List<PlatformServiceExtension> getLoadableExtensions() {
    if(cachedProviders == null) {
      cachedProviders = new ArrayList<PlatformServiceExtension>();
      ServiceLoader<PlatformServiceExtension> cachedLoader = ServiceLoader.load(PlatformServiceExtension.class);
      Iterator<PlatformServiceExtension> iterator = cachedLoader.iterator();
      while (iterator.hasNext()) {
        PlatformServiceExtension platformServiceExtension = (PlatformServiceExtension) iterator.next();
        cachedProviders.add(platformServiceExtension);        
      }
      
      Collections.sort(cachedProviders, new Comparator<PlatformServiceExtension>() {
        public int compare(PlatformServiceExtension o1, PlatformServiceExtension o2) {       
          return ((Integer)o1.getPrecedence()).compareTo(o2.getPrecedence());
        }      
      });
      
    }
    return cachedProviders;
  }
  
  public static void clearCachedExtensions() {
    cachedProviders = null;
  }

}
