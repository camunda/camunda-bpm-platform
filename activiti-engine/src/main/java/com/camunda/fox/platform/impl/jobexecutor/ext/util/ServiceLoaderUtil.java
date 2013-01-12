package com.camunda.fox.platform.impl.jobexecutor.ext.util;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.camunda.fox.platform.FoxPlatformException;

// TODO: Extract to fox-platform-ext-config-commons
/**
 * @author roman.smirnov
 */
public class ServiceLoaderUtil {
  
  public static <T> T loadService(Class<T> type, Class<? extends T> defaultImplementation) {
    ServiceLoader<T> loader = ServiceLoader.load(type);
    Iterator<T> iterator = loader.iterator();
    if(iterator.hasNext()) {
      return iterator.next();
    } else {
      try {
        return defaultImplementation.newInstance();
      } catch (Exception e) {
        throw new FoxPlatformException("Could not create intance of '"+defaultImplementation+"'", e);
      }
    }
    
  }

}
