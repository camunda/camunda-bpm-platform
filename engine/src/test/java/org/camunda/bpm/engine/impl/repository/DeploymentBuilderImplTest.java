package org.camunda.bpm.engine.impl.repository;


import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.*;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

public class DeploymentBuilderImplTest extends PluggableProcessEngineTestCase {
    private String key = UUID.randomUUID().toString();
    private String bpmnResource = key + ".bpmn";
    private String cmmnResource = key + ".cmmn";
    private String dmnResource = key + ".dmn";
    private Deployment deployment;

    @Test
    public void testDeployDmnModelInstance() throws Exception {
        try {
            DmnModelInstance modelInstance = Dmn.createEmptyModel();
            Definitions definitions = modelInstance.newInstance(Definitions.class);
            definitions.setId(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
            definitions.setName(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
            definitions.setNamespace(DmnModelConstants.CAMUNDA_NS);
            modelInstance.setDefinitions(definitions);

            Decision decision = modelInstance.newInstance(Decision.class);
            decision.setId("Decision-1");
            decision.setName(key);
            modelInstance.getDefinitions().addChildElement(decision);

            DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
            decisionTable.setId(DmnModelConstants.DMN_ELEMENT_DECISION_TABLE);
            decisionTable.setHitPolicy(HitPolicy.FIRST);
            decision.addChildElement(decisionTable);

            Input input = modelInstance.newInstance(Input.class);
            input.setId("Input-1");
            input.setLabel("Input");
            decisionTable.addChildElement(input);

            InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
            inputExpression.setId("InputExpression-1");
            Text inputExpressionText = modelInstance.newInstance(Text.class);
            inputExpressionText.setTextContent("input");
            inputExpression.setText(inputExpressionText);
            inputExpression.setTypeRef("string");
            input.setInputExpression(inputExpression);

            Output output = modelInstance.newInstance(Output.class);
            output.setName("output");
            output.setLabel("Output");
            output.setTypeRef("string");
            decisionTable.addChildElement(output);

            deployment = repositoryService.createDeployment().addModelInstance(dmnResource, modelInstance).deploy();

            assertTrue(repositoryService.createDecisionDefinitionQuery()
                    .decisionDefinitionResourceName(dmnResource).singleResult() != null);
        } finally {
            repositoryService.deleteDeployment(deployment.getId());
        }
    }

    @Test
    public void testDeployBpmnModelInstance() throws Exception {
        try {
            final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(key).startEvent().userTask().endEvent().done();

            deployment = repositoryService.createDeployment().addModelInstance(bpmnResource, modelInstance).deploy();

            assertTrue(repositoryService.createProcessDefinitionQuery().processDefinitionResourceName(bpmnResource).singleResult() != null);
        } finally {
            repositoryService.deleteDeployment(deployment.getId());
        }
    }

    @Test
    @Ignore("fails due to invalid cmmn model")
    public void testDeployCmmnModelInstance() throws Exception {
        try {
            final CmmnModelInstance modelInstance = Cmmn.createEmptyModel();
            org.camunda.bpm.model.cmmn.instance.Definitions definitions = modelInstance.newInstance(org.camunda.bpm.model.cmmn.instance.Definitions.class);
            definitions.setTargetNamespace("http://camunda.org/examples");
            modelInstance.setDefinitions(definitions);


            Case caseElement = modelInstance.newInstance(Case.class);
            caseElement.setId("a-case");
            definitions.addChildElement(caseElement);

            deployment = repositoryService.createDeployment().addModelInstance(cmmnResource, modelInstance).deploy();

            assertTrue(repositoryService.createCaseDefinitionQuery().caseDefinitionResourceName(cmmnResource).singleResult() != null);
        } finally {
            repositoryService.deleteDeployment(deployment.getId());
        }
    }
}