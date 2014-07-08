package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.spin.impl.json.tree.SpinJsonJacksonTreeNode;
import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

public abstract class JsonTreeConfigureScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name="input", value = "{\"number\": 001}")
  public void shouldConfigure() {
    SpinJsonJacksonTreeNode json1 = script.getVariable("json1");
    assertThat(json1).isNotNull();
    
    SpinJsonJacksonTreeNode json2 = script.getVariable("json2");
    assertThat(json2).isNotNull();
    
    SpinJsonJacksonTreeNode json3 = script.getVariable("json3");
    assertThat(json3).isNotNull();
  }
}
