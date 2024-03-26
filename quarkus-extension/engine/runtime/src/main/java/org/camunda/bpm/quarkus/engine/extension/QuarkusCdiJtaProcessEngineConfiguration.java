package org.camunda.bpm.quarkus.engine.extension;

import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;


public class QuarkusCdiJtaProcessEngineConfiguration extends JtaProcessEngineConfiguration {
    @Override
    protected void initExpressionManager() {
//        expressionManager = new CdiExpressionManager();
        expressionManager = new QuarkusCdiExpressionManager();
        super.initExpressionManager();
    }
}
