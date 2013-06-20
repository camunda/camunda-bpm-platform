define([ 'angular',
         'jquery', 
         'cockpit/directives/processDiagram',
         'cockpit/directives/activityInstances',
         'cockpit/resources/processDefinitionResource',
         'angular-resource',
         'cockpit/filters/shortenNumber' ], function(angular, $) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    describe('activity instances directive', function() {
      var element;

      function createElement(content) {
        return $(content).appendTo(document.body);
      }

      afterEach(function() {
        $(document.body).html('');
        dealoc(element);
      });

      beforeEach(function () {
        angular.module('testmodule', [ 'cockpit.directives', 
                                       'cockpit.resources.process.definition', 
                                       'ngResource',
                                       'cockpit.filters.shorten.number' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));
      
      beforeEach(inject(function($httpBackend) {
        // backend definition common for all tests
        $httpBackend
          .whenGET('engine://process-definition/FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726/xml')
          .respond({
            id: 'FailingProcess:1:2bc4b300-d72c-11e2-be8e-f0def1557726',
            bpmn20Xml: '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\" id=\"_QzotMMIpEeKp3rR3eqX9hQ\" targetNamespace=\"http://activiti.org/bpmn\">\r\n  <bpmn2:process id=\"FailingProcess\" name=\"FailingProcess\" isExecutable=\"true\">\r\n    <bpmn2:serviceTask id=\"ServiceTask_1\" activiti:class=\"org.camunda.bpm.pa.service.FailingDelegate\" activiti:async=\"true\" name=\"Service Task\">\r\n      <bpmn2:incoming>SequenceFlow_3</bpmn2:incoming>\r\n      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>\r\n    </bpmn2:serviceTask>\r\n    <bpmn2:endEvent id=\"EndEvent_1\" name=\"End Event\">\r\n      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>\r\n    </bpmn2:endEvent>\r\n    <bpmn2:sequenceFlow id=\"SequenceFlow_2\" sourceRef=\"ServiceTask_1\" targetRef=\"EndEvent_1\"/>\r\n    <bpmn2:startEvent id=\"StartEvent_1\" name=\"Start Event\">\r\n      <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>\r\n    </bpmn2:startEvent>\r\n    <bpmn2:sequenceFlow id=\"SequenceFlow_1\" sourceRef=\"StartEvent_1\" targetRef=\"ServiceTask_2\"/>\r\n    <bpmn2:serviceTask id=\"ServiceTask_2\" activiti:expression=\"${true}\" name=\"Service Task\">\r\n      <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>\r\n      <bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing>\r\n    </bpmn2:serviceTask>\r\n    <bpmn2:sequenceFlow id=\"SequenceFlow_3\" name=\"\" sourceRef=\"ServiceTask_2\" targetRef=\"ServiceTask_1\"/>\r\n  </bpmn2:process>\r\n  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\" name=\"Test\">\r\n    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"FailingProcess\">\r\n      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_16\" bpmnElement=\"StartEvent_1\">\r\n        <dc:Bounds height=\"36.0\" width=\"36.0\" x=\"156.0\" y=\"259.0\"/>\r\n        <bpmndi:BPMNLabel>\r\n          <dc:Bounds height=\"22.0\" width=\"70.0\" x=\"139.0\" y=\"300.0\"/>\r\n        </bpmndi:BPMNLabel>\r\n      </bpmndi:BPMNShape>\r\n      <bpmndi:BPMNShape id=\"_BPMNShape_ServiceTask_3\" bpmnElement=\"ServiceTask_1\">\r\n        <dc:Bounds height=\"80.0\" width=\"100.0\" x=\"382.0\" y=\"237.0\"/>\r\n      </bpmndi:BPMNShape>\r\n      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_1\" bpmnElement=\"SequenceFlow_1\" sourceElement=\"_BPMNShape_StartEvent_16\" targetElement=\"_BPMNShape_ServiceTask_4\">\r\n        <di:waypoint xsi:type=\"dc:Point\" x=\"192.0\" y=\"277.0\"/>\r\n        <di:waypoint xsi:type=\"dc:Point\" x=\"251.0\" y=\"277.0\"/>\r\n        <bpmndi:BPMNLabel>\r\n          <dc:Bounds height=\"0.0\" width=\"0.0\" x=\"217.0\" y=\"277.0\"/>\r\n        </bpmndi:BPMNLabel>\r\n      </bpmndi:BPMNEdge>\r\n      <bpmndi:BPMNShape id=\"_BPMNShape_EndEvent_18\" bpmnElement=\"EndEvent_1\">\r\n        <dc:Bounds height=\"36.0\" width=\"36.0\" x=\"532.0\" y=\"259.0\"/>\r\n      </bpmndi:BPMNShape>\r\n      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_2\" bpmnElement=\"SequenceFlow_2\" sourceElement=\"_BPMNShape_ServiceTask_3\" targetElement=\"_BPMNShape_EndEvent_18\">\r\n        <di:waypoint xsi:type=\"dc:Point\" x=\"482.0\" y=\"277.0\"/>\r\n        <di:waypoint xsi:type=\"dc:Point\" x=\"532.0\" y=\"277.0\"/>\r\n      </bpmndi:BPMNEdge>\r\n      <bpmndi:BPMNShape id=\"_BPMNShape_ServiceTask_4\" bpmnElement=\"ServiceTask_2\">\r\n        <dc:Bounds height=\"80.0\" width=\"100.0\" x=\"251.0\" y=\"237.0\"/>\r\n      </bpmndi:BPMNShape>\r\n      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_3\" bpmnElement=\"SequenceFlow_3\" sourceElement=\"_BPMNShape_ServiceTask_4\" targetElement=\"_BPMNShape_ServiceTask_3\">\r\n        <di:waypoint xsi:type=\"dc:Point\" x=\"351.0\" y=\"277.0\"/>\r\n        <di:waypoint xsi:type=\"dc:Point\" x=\"382.0\" y=\"277.0\"/>\r\n        <bpmndi:BPMNLabel>\r\n          <dc:Bounds height=\"6.0\" width=\"6.0\" x=\"378.0\" y=\"277.0\"/>\r\n        </bpmndi:BPMNLabel>\r\n      </bpmndi:BPMNEdge>\r\n    </bpmndi:BPMNPlane>\r\n  </bpmndi:BPMNDiagram>\r\n</bpmn2:definitions>'
          });
        
        $httpBackend
        .whenGET('engine://process-definition/OrderProcess:1:2bc48bef-d72c-11e2-be8e-f0def1557726/xml')
        .respond({
          id: 'OrderProcess:1:2bc48bef-d72c-11e2-be8e-f0def1557726',
          bpmn20Xml: '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\" id=\"_VZ_GsMH8EeKp3rR3eqX9hQ\" targetNamespace=\"http://activiti.org/bpmn\">\n  <bpmn2:collaboration id=\"_Collaboration_2\">\n    <bpmn2:participant id=\"_Participant_2\" name=\"Order Goods\" processRef=\"OrderProcess\"/>\n  </bpmn2:collaboration>\n  <bpmn2:process id=\"OrderProcess\" isExecutable=\"true\">\n    <bpmn2:startEvent id=\"StartEvent_1\" name=\"\">\n      <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>\n    </bpmn2:startEvent>\n    <bpmn2:userTask id=\"UserTask_1\" name=\"Identify needs\">\n      <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>\n      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>\n    </bpmn2:userTask>\n    <bpmn2:sequenceFlow id=\"SequenceFlow_1\" sourceRef=\"StartEvent_1\" targetRef=\"UserTask_1\"/>\n    <bpmn2:userTask id=\"UserTask_2\" name=\"Order the needs\">\n      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>\n      <bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing>\n    </bpmn2:userTask>\n    <bpmn2:sequenceFlow id=\"SequenceFlow_2\" sourceRef=\"UserTask_1\" targetRef=\"UserTask_2\"/>\n    <bpmn2:endEvent id=\"EndEvent_1\" name=\"End Event\">\n      <bpmn2:incoming>SequenceFlow_3</bpmn2:incoming>\n    </bpmn2:endEvent>\n    <bpmn2:sequenceFlow id=\"SequenceFlow_3\" sourceRef=\"UserTask_2\" targetRef=\"EndEvent_1\"/>\n  </bpmn2:process>\n  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"_Collaboration_2\">\n      <bpmndi:BPMNShape id=\"_BPMNShape_Participant_2\" bpmnElement=\"_Participant_2\" isHorizontal=\"true\">\n        <dc:Bounds height=\"215.0\" width=\"540.0\" x=\"192.0\" y=\"144.0\"/>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_15\" bpmnElement=\"StartEvent_1\">\n        <dc:Bounds height=\"36.0\" width=\"36.0\" x=\"238.0\" y=\"234.0\"/>\n        <bpmndi:BPMNLabel>\n          <dc:Bounds height=\"6.0\" width=\"6.0\" x=\"253.0\" y=\"275.0\"/>\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"_BPMNShape_UserTask_2\" bpmnElement=\"UserTask_1\">\n        <dc:Bounds height=\"80.0\" width=\"100.0\" x=\"324.0\" y=\"212.0\"/>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_1\" bpmnElement=\"SequenceFlow_1\" sourceElement=\"_BPMNShape_StartEvent_15\" targetElement=\"_BPMNShape_UserTask_2\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"274.0\" y=\"252.0\"/>\n        <di:waypoint xsi:type=\"dc:Point\" x=\"324.0\" y=\"252.0\"/>\n        <bpmndi:BPMNLabel>\n          <dc:Bounds height=\"0.0\" width=\"0.0\" x=\"299.0\" y=\"252.0\"/>\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"_BPMNShape_UserTask_3\" bpmnElement=\"UserTask_2\">\n        <dc:Bounds height=\"80.0\" width=\"100.0\" x=\"474.0\" y=\"212.0\"/>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_2\" bpmnElement=\"SequenceFlow_2\" sourceElement=\"_BPMNShape_UserTask_2\" targetElement=\"_BPMNShape_UserTask_3\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"424.0\" y=\"252.0\"/>\n        <di:waypoint xsi:type=\"dc:Point\" x=\"474.0\" y=\"252.0\"/>\n        <bpmndi:BPMNLabel>\n          <dc:Bounds height=\"0.0\" width=\"0.0\" x=\"449.0\" y=\"252.0\"/>\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"_BPMNShape_EndEvent_17\" bpmnElement=\"EndEvent_1\">\n        <dc:Bounds height=\"36.0\" width=\"36.0\" x=\"624.0\" y=\"234.0\"/>\n        <bpmndi:BPMNLabel>\n          <dc:Bounds height=\"22.0\" width=\"65.0\" x=\"610.0\" y=\"275.0\"/>\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_3\" bpmnElement=\"SequenceFlow_3\" sourceElement=\"_BPMNShape_UserTask_3\" targetElement=\"_BPMNShape_EndEvent_17\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"574.0\" y=\"252.0\"/>\n        <di:waypoint xsi:type=\"dc:Point\" x=\"624.0\" y=\"252.0\"/>\n        <bpmndi:BPMNLabel>\n          <dc:Bounds height=\"0.0\" width=\"0.0\" x=\"599.0\" y=\"252.0\"/>\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n    </bpmndi:BPMNPlane>\n  </bpmndi:BPMNDiagram>\n</bpmn2:definitions>'

        });

      }));
      
      it('should show number of current instances on activity (transitions)', inject(function($rootScope, $compile, $httpBackend) {
        
        // given process diagram
        $rootScope.processDefinitionId = 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726';
        $rootScope.tree = {
                            id: '2c090edb-d72c-11e2-be8e-f0def1557726',
                            parentActivityInstanceId: null,
                            activityId: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726',
                            processInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
                            processDefinitionId: 'FailingProcess:1:2bc4b300-d72c-11e2-be8e-f0def1557726',
                            childActivityInstances: [],
                            childTransitionInstances:
                              [{ 
                                id: '2c090edb-d72c-11e2-be8e-f0def1557726',
                                parentActivityInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
                                processInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
                                processDefinitionId: 'FailingProcess:1:2bc4b300-d72c-11e2-be8e-f0def1557726',
                                targetActivityId: 'ServiceTask_1',
                                executionId: '2c090edb-d72c-11e2-be8e-f0def1557726'
                              }],
                            executionIds: ['2c090edb-d72c-11e2-be8e-f0def1557726']
                          };
        
        element = createElement('<process-diagram process-definition-id="processDefinitionId" activity-instances=tree />');
        element = $compile(element)($rootScope);
        
        $rootScope.$digest();
        
        $httpBackend.flush();
        
        // then
        expect(element.html()).toContain('<div id="ServiceTask_1" class="bpmnElement" style="position: absolute; left: 382px; top: 237px; width: 100px; height: 80px;"><div class="badgePosition"><p class="badge">1</p></div></div>');
        expect(element.text()).toBe('Service TaskEnd EventStart EventService Task1');
      }));
      
      it('should show number of current instances on activity (activities)', inject(function($rootScope, $compile, $httpBackend) {
        
        // given process diagram
        $rootScope.processDefinitionId = 'OrderProcess:1:2bc48bef-d72c-11e2-be8e-f0def1557726';
        $rootScope.tree = {
                            id: '2bceeb68-d72c-11e2-be8e-f0def1557726',
                            parentActivityInstanceId: null,
                            activityId: 'OrderProcess:1:2bc48bef-d72c-11e2-be8e-f0def1557726',
                            processInstanceId: '2bceeb68-d72c-11e2-be8e-f0def1557726',
                            processDefinitionId: 'OrderProcess:1:2bc48bef-d72c-11e2-be8e-f0def1557726',
                            childActivityInstances: [{
                              id: 'UserTask_1:2bceeb6c-d72c-11e2-be8e-f0def1557726',
                              parentActivityInstanceId: '2bceeb68-d72c-11e2-be8e-f0def1557726',
                              activityId: 'UserTask_1',
                              processInstanceId: '2bceeb68-d72c-11e2-be8e-f0def1557726',
                              processDefinitionId: 'OrderProcess:1:2bc48bef-d72c-11e2-be8e-f0def1557726',
                              childActivityInstances: [],
                              childTransitionInstances: [],
                              executionIds: ['2bceeb68-d72c-11e2-be8e-f0def1557726']
                            }],
                            childTransitionInstances: [],
                            executionIds: ['2bceeb68-d72c-11e2-be8e-f0def1557726']
                          };
        
        element = createElement('<process-diagram process-definition-id="processDefinitionId" activity-instances=tree />');
        element = $compile(element)($rootScope);
        
        $rootScope.$digest();
        
        $httpBackend.flush();
        
        // then
        expect(element.html()).toContain('<div id="UserTask_1" class="bpmnElement" style="position: absolute; left: 324px; top: 212px; width: 100px; height: 80px;"><div class="badgePosition"><p class="badge">1</p></div></div>');
        expect(element.text()).toBe('Order GoodsIdentify needsOrder the needsEnd Event1');
      }));
      
    });
  });
});