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
package org.camunda.bpm.engine.impl.test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;

/**
 * <p>This only works if {@link ProcessEngineRule} or {@link AbstractProcessEngineTestCase} is used.
 * Furthermore, it only checks against the engines managed by these classes, e.g. it cannot prevent
 * that a test builds a custom engine against any database.
 *
 * <p>Check the constants in {@link DbSqlSessionFactory} for valid database names.
 *
 * <p>Note that this uses the process engine to check the database type. If the test
 * builds its own process engine, it may be a better idea to exclude the test via maven,
 * to avoid the overhead of unnecessarily build the engine.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequiredDatabase {

  public String[] excludes() default {};
  
  public String[] includes() default {};
}