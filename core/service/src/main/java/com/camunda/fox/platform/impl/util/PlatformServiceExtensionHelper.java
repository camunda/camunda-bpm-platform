package com.camunda.fox.platform.impl.util;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class PlatformServiceExtensionHelper {
  
  protected static ServiceLoader<PlatformServiceExtension> cachedLoader;
  
  public static Iterator<PlatformServiceExtension> getLoadableExtensions() {
    if(cachedLoader==null) {
      cachedLoader = ServiceLoader.load(PlatformServiceExtension.class);
    }
    return cachedLoader.iterator();
  }
  
  public static void clearCachedExtensions() {
    cachedLoader = null;
  }

}
