<<<<<<< HEAD
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
=======
>>>>>>> bf1847a0643ae882b03606507f3a99c94fa433eb
package com.camunda.fox.platform.impl.util;

import java.util.Map;

/**
 * 
 * @author Daniel Meyer
 */
public class PropertyHelper {
  
  public static <T> T getProperty(Map<String, Object> configMap, String propertyName, T defaultValue) {
    if(configMap.containsKey(propertyName)) {
      return (T) configMap.get(propertyName);
    } else {
      return defaultValue;
    }
  }

}
