/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.rolling.upgrade;

import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class RollingUpdateRule extends UpgradeTestRule {

  public RollingUpdateRule() {
    super("camunda.cfg.xml");
  }

  public RollingUpdateRule(String configurationResource) {
    super(configurationResource);
  }

  @Override
  public void starting(Description description) {
    super.starting(description);
  }

  protected String[] versions;
  protected boolean isReadVersion = false;

  @Override
  public Statement apply(Statement statement, Description description) {

    Statement result = statement;
    if (!isReadVersion) {
      Class<?> testClass = description.getTestClass();
      EngineVersions version = testClass.getAnnotation(EngineVersions.class);
      if (version != null) {
        versions = version.value();
      }
      isReadVersion = true;
    }

    if (versions != null) {
      result = new TaggedStatement(statement, description, versions);
    }
    return result;
  }

  private class TaggedStatement extends Statement {

    private final Statement statement;
    private final String tags[];
    private final Description description;

    public TaggedStatement(Statement statement, Description description, String[] tags) {
      this.statement = statement;
      this.tags = tags;
      this.description = description;
    }

    @Override
    public void evaluate() throws Throwable {
      for (String tag : tags) {
        RollingUpdateRule.this.setTag(tag);
        RollingUpdateRule.super.apply(statement, description).evaluate();
      }
    }
  }

}
