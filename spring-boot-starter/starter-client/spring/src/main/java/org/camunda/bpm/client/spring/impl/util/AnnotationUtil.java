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
package org.camunda.bpm.client.spring.impl.util;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.NoSuchElementException;

public class AnnotationUtil {

  public static <T extends Annotation> T get(Class<T> annotationClass,
                                             AnnotatedTypeMetadata metadata) {
    MergedAnnotations annotations = metadata.getAnnotations();
    MergedAnnotation<T> mergedAnnotation = annotations.get(annotationClass);

    try {
      return mergedAnnotation.synthesize();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

}
