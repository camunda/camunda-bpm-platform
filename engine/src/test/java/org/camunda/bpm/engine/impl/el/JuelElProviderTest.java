package org.camunda.bpm.engine.impl.el;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author MPlukas
 *
 */
public class JuelElProviderTest {

    @Test
    public void testVariableContextResolverSkipsNestedProperty() {
      ExpressionManager expressionManager = new ExpressionManager();
      ValueExpression expr = expressionManager.createValueExpression("${a.b}");

      Map<String, Object> mapVar = new HashMap<>(1);
      mapVar.put("b", "B_FROM_MAP");
      ELContext context = expressionManager.createElContext(
              Variables.createVariables().putValue("a", mapVar).putValue("b", "B_FROM_CONTEXT").asVariableContext());
      Object val = expr.getValue(context);

      assertThat(val).isEqualTo("B_FROM_MAP");
    }

    @Test
    public void testVariableContextResolverSkipsListIndexProperty() {
      ExpressionManager expressionManager = new ExpressionManager();
      ValueExpression expr = expressionManager.createValueExpression("${a[0]}");

      List<String> listVar = new ArrayList<>(1);
      listVar.add("0_FROM_LIST");
      ELContext context = expressionManager.createElContext(
              Variables.createVariables().putValue("a", listVar).asVariableContext());
      Object val = expr.getValue(context);

      assertThat(val).isEqualTo("0_FROM_LIST");
    }

}
