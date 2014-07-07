package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

public abstract class JsonTreeConfigureScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name="input", value = "{\"number\": 001}")
  public void shouldConfigure() {
    SpinJsonTreeNode json1 = script.getVariable("json1");
    assertThat(json1).isNotNull();
    
    SpinJsonTreeNode json2 = script.getVariable("json2");
    assertThat(json2).isNotNull();
    
    SpinJsonTreeNode json3 = script.getVariable("json3");
    assertThat(json3).isNotNull();
  }
}
