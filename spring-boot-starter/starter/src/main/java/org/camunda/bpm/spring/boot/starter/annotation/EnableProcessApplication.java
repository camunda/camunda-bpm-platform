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
package org.camunda.bpm.spring.boot.starter.annotation;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that disables the {@link SpringProcessEngineConfiguration}
 * auto-deploy feature and instead uses the required META-INF/processes.xml
 * as an indicator for resource scanning. This also allows all processes.xml
 * configuration features described here:
 * <a href="https://docs.camunda.org/manual/latest/user-guide/process-applications/the-processes-xml-deployment-descriptor/">The processes.xml Deployment Descriptor</a>
 * 
 * <p>To use it, just add the annotation to your Spring Boot application class:</p>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableProcessApplication("myProcessApplicationName")
 * public class MyApplication {
 * 
 * ...
 * 
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SpringBootProcessApplication.class)
@Documented
@Inherited
public @interface EnableProcessApplication {

  String value() default "";
}
