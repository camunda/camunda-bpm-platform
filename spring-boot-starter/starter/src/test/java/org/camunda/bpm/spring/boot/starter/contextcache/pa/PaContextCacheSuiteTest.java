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
package org.camunda.bpm.spring.boot.starter.contextcache.pa;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This JUnit Suite combines {@link PaContextCacheTest1}, {@link PaContextCacheTest2}
 * and {@link PaContextCacheTest3} so that ApplicationContext caching is tested. All three
 * tests store a <code>hashCode()</code> of their ApplicationContext in a static <code>Map</code>
 * for comparison. The tests replicate the following scenario:
 * <ol>
 *   <li>
 *     {@link PaContextCacheTest1} creates an ApplicationContext
 *     with a unique ProcessEngine name, ProcessApplication and database. It asserts that
 *     the ProcessEngine, and its name, are not the "default" one.
 *   </li>
 *   <li>
 *     {@link PaContextCacheTest2} creates a second ApplicationContext
 *     with a unique ProcessEngine name, a second ProcessApplication and
 *     a second database. It asserts that the ProcessEngine, and its
 *     name, are not the "default" one, and that the current ApplicationContext
 *     is different than the one created by {@link PaContextCacheTest1}.
 *   </li>
 *   <li>
 *     {@link PaContextCacheTest3} tests if the first ApplicationContext
 *     (from {@link PaContextCacheTest1}) is reused. It expects to share the
 *     same ProcessEngine, ProcessApplication and database.
 *   </li>
 *   <li>
 *     {@link PaContextCacheTest4} tests if the second ApplicationContext
 *     (from {@link PaContextCacheTest2}) is reused. It expects to share the
 *     same (unique-named) ProcessEngine, ProcessApplication and database.
 *   </li>
 *   <li>
 *     {@link PaContextCacheTest5} creates a third ApplicationContext
 *     with a unique ProcessEngine and ProcessApplication name and database.
 *     It asserts that the ProcessEngine, and its name, are not the "default"
 *     one, and that the current ApplicationContext is different than the one created
 *     by {@link PaContextCacheTest1} and {@link PaContextCacheTest2}.
 *   </li>
 * </ol>
 *
 * @author Nikola Koevski
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  PaContextCacheTest1.class,
  PaContextCacheTest2.class,
  PaContextCacheTest3.class,
  PaContextCacheTest4.class,
  PaContextCacheTest5.class
})
public class PaContextCacheSuiteTest {
}
