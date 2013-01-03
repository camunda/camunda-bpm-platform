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
package com.camunda.fox.platform.impl.util;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Daniel Meyer
 */
public class PropertyHelper {

  static Logger logger = Logger.getLogger(PropertyHelper.class.getName());

  public static <T> T getProperty(Map<String, Object> configMap, String propertyName, T defaultValue) {
    if (configMap.containsKey(propertyName)) {
        if (defaultValue instanceof Boolean) {
          return (T) Boolean.valueOf(""+configMap.get(propertyName));
        } else if (defaultValue instanceof Integer) {
          try {
            return (T) Integer.valueOf(""+configMap.get(propertyName));
          } catch (NumberFormatException nfe) {
            logger.log(
                    Level.WARNING,
                    String.format("Could not parse property %s with value %s, is the value formatted correctly?", propertyName,
                            "" + configMap.get(propertyName)));
            return defaultValue;
          }
        } else {
          return (T) configMap.get(propertyName);
        }
    } else {
      return defaultValue;
    }
  }

}
