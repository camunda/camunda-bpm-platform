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
package org.camunda.bpm.engine.rest.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class EqualsListTest {

  protected List<String> list1;
  protected List<String> list2;

  @Before
  public void setUp() {
    list1 = new ArrayList<String>();
    list2 = new ArrayList<String>();
  }

  @Test
  public void testListsSame() {
    assertTrue(new EqualsList(list1).matches(list1));
  }

  @Test
  public void testListsEqual() {
    list1.add("aString");
    list2.add("aString");

    assertTrue(new EqualsList(list1).matches(list2));
    assertTrue(new EqualsList(list2).matches(list1));
  }

  @Test
  public void testListsNotEqual() {
    list1.add("aString");

    assertFalse(new EqualsList(list1).matches(list2));
    assertFalse(new EqualsList(list2).matches(list1));
  }

  @Test
  public void testListsNull() {
    assertFalse(new EqualsList(null).matches(list1));
    assertFalse(new EqualsList(list1).matches(null));
    assertTrue(new EqualsList(null).matches(null));
  }

}
