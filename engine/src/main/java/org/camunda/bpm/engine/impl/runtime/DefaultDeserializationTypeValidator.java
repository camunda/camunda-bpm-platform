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
package org.camunda.bpm.engine.impl.runtime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.runtime.WhitelistingDeserializationTypeValidator;

/**
 * Validate a type against a list of allowed packages and classes. Allows a basic
 * set of packages and classes without known security issues based on Jackson
 * Databind's <a href=
 * "https://github.com/FasterXML/jackson-databind/blob/master/src/main/java/com/fasterxml/jackson/databind/jsontype/impl/SubTypeValidator.java">SubTypeValidator</a>.
 */
public class DefaultDeserializationTypeValidator implements WhitelistingDeserializationTypeValidator {

  protected static final Collection<String> ALLOWED_PACKAGES = Arrays.asList("java.lang");
  protected static final Collection<String> ALLOWED_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      "java.util.ArrayList", "java.util.Arrays$ArrayList", "java.util.HashMap", "java.util.HashSet",
      "java.util.LinkedHashMap", "java.util.LinkedHashSet", "java.util.LinkedList",
      "java.util.Properties", "java.util.TreeMap", "java.util.TreeSet")));

  protected Set<String> allowedClasses = new HashSet<>(ALLOWED_CLASSES);
  protected Set<String> allowedPackages = new HashSet<>(ALLOWED_PACKAGES);

  @Override
  public void setAllowedClasses(String deserializationAllowedClasses) {
    extractElements(deserializationAllowedClasses, allowedClasses);
  }

  @Override
  public void setAllowedPackages(String deserializationAllowedPackages) {
    extractElements(deserializationAllowedPackages, allowedPackages);
  }

  @Override
  public boolean validate(String className) {
    if (className == null || className.trim().isEmpty()) {
      return true;
    }
    return isPackageAllowed(className) || isClassNameAllowed(className);
  }

  protected boolean isPackageAllowed(String className) {
    if (!isPackageAllowed(className, ALLOWED_PACKAGES)) {
      return isPackageAllowed(className, allowedPackages);
    }
    return true;
  }

  protected boolean isPackageAllowed(String className, Collection<String> allowedPackages) {
    for (String allowedPackage : allowedPackages) {
      if (!allowedPackage.isEmpty() && className.startsWith(allowedPackage)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isClassNameAllowed(String className) {
    if (!ALLOWED_CLASSES.contains(className)) {
      return allowedClasses.contains(className);
    }
    return true;
  }

  protected void extractElements(String allowedElements, Set<String> set) {
    if (!set.isEmpty()) {
      set.clear();
    }
    if (allowedElements == null) {
      return;
    }
    String allowedElementsSanitized = allowedElements.replaceAll("\\s", "");
    if (allowedElementsSanitized.isEmpty()) {
      return;
    }
    String[] classes = allowedElementsSanitized.split(",");
    for (String className : classes) {
      set.add(className.trim());
    }
  }
}
