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
package org.camunda.bpm.qa.rolling.update;

import java.util.Arrays;
import java.util.Collection;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * The abstract rolling update test case, which should be used as base class from all
 * rolling update test cases. Defines a parameterized test case, which executes
 * all tests for each engine version. The engine version will be used as tag for
 * the upgrade test rule.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public abstract class AbstractRollingUpdateTestCase {

  @Parameters(name = "{0} engine")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
          {RollingUpdateConstants.OLD_ENGINE_TAG}, {RollingUpdateConstants.NEW_ENGINE_TAG}
           });
    }

  @Parameter
  public String tag;

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Before
  public void init() {
    rule.setTag(tag);
  }
}
