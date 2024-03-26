package org.camunda.bpm.quarkus.engine.extension;

import org.camunda.bpm.engine.cdi.CdiExpressionManager;
import org.camunda.bpm.engine.impl.el.VariableContextElResolver;
import org.camunda.bpm.engine.impl.el.VariableScopeElResolver;
import org.camunda.bpm.engine.impl.javax.el.*;

public class QuarkusCdiExpressionManager extends CdiExpressionManager {

    @Override
    protected ELResolver createElResolver() {
        CompositeELResolver compositeElResolver = new CompositeELResolver();
        compositeElResolver.add(new VariableScopeElResolver());
        compositeElResolver.add(new VariableContextElResolver());

//        compositeElResolver.add(new CdiResolver());
        compositeElResolver.add(new QuarkusCdiELResolver());

        compositeElResolver.add(new ArrayELResolver());
        compositeElResolver.add(new ListELResolver());
        compositeElResolver.add(new MapELResolver());
        compositeElResolver.add(new BeanELResolver());

        return compositeElResolver;
    }

}
