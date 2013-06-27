define([ 'angular', 'cockpit/services/transform',
                    'cockpit/services/activityInstance',
                    'cockpit/filters/shorten' ], function(angular) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('services', function() {

    describe('ActivityInstance', function() {

      beforeEach(function () {
        angular.module('testmodule', [ 'cockpit.services', 'cockpit.filters.shorten' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

     beforeEach(inject(function($rootScope) {
        // given a complex activity instance ;-)
        $rootScope.activityInstances = {
          id : 'instance_1',
          parentActivityInstanceId : null,
          activityId : 'Process_1',
          processInstanceId : 'instance_1',
          processDefinitionId : 'Process_1',
          childActivityInstances : [ {
            id : 'instance_2',
            parentActivityInstanceId : 'instance_1',
            activityId : 'SubProcess_1',
            processInstanceId : 'instance_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [ {
              id : 'instance_3',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_1',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_3' ]
            }, {
              id : 'instance_4',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_2',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_4' ]
            } ],
            childTransitionInstances : [ {
              executionId: 'execution_1',
              id: 'transition_instance_1',
              parentActivityInstanceId: 'instance_2',
              processDefinitionId: 'Process_1',
              processInstanceId: 'instance_1',
              targetActivityId: 'ServiceTask_2'
            }],
            executionIds : [ 'instance_2' ]
          }, {
            id : 'instance_5',
            parentActivityInstanceId : 'instance_1',
            activityId : 'SubProcess_1',
            processInstanceId : 'instance_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [ {
              id : 'instance_6',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_1',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_6' ]
            }, {
              id : 'instance_7',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_2',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_1' ]
            } ],
            childTransitionInstances : [ {
              executionId: 'execution_2',
              id: 'transition_instance_2',
              parentActivityInstanceId: 'instance_5',
              processDefinitionId: 'Process_1',
              processInstanceId: 'instance_1',
              targetActivityId: 'ServiceTask_2'
            }],
            executionIds : [ 'instance_7' ]
          }, {
            id : 'instance_8',
            parentActivityInstanceId : 'instance_1',
            activityId : 'ServiceTask_1',
            processInstanceId : 'Process_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [],
            childTransitionInstances : [],
            executionIds : [ 'instance_8' ]
          }, {
            id : 'instance_9',
            parentActivityInstanceId : 'instance_1',
            activityId : 'SubProcess_2',
            processInstanceId : 'instance_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [ {
              id : 'instance_10',
              parentActivityInstanceId : 'instance_9',
              activityId : 'NestedSubProcess_1',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [ {
                id : 'instance_11',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_11' ]
              }, {
                id : 'instance_12',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_12' ]
              }, {
                id : 'instance_13',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_13' ]
              }, {
                id : 'instance_14',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_14' ]
              }, {
                id : 'instance_15',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_15' ]
              } ],
              childTransitionInstances : [],
              executionIds : [ 'instance_10' ]
            } ],
            childTransitionInstances : [],
            executionIds : [ 'instance_9' ]
          } ],
          childTransitionInstances : [],
          executionIds : [ 'instance_1' ]
        };
      }));

      it('should aggregate activity instances', inject(function($rootScope, ActivityInstance) {
        // when
        var result = ActivityInstance.aggregateActivityInstances($rootScope.activityInstances);

        // then
        expect(result.NestedSubProcess_1).toBeDefined();
        expect(result.NestedSubProcess_1.length).toBe(1);

        expect(result.ServiceTask_1).toBeDefined();
        expect(result.ServiceTask_1.length).toBe(1);

        expect(result.ServiceTask_2).toBeDefined();
        expect(result.ServiceTask_2.length).toBe(2);

        expect(result.SubProcess_1).toBeDefined();
        expect(result.SubProcess_1.length).toBe(2);

        expect(result.SubProcess_2).toBeDefined();
        expect(result.SubProcess_2.length).toBe(1);

        expect(result.UserTask_1).toBeDefined();
        expect(result.UserTask_1.length).toBe(2);

        expect(result.UserTask_2).toBeDefined();
        expect(result.UserTask_2.length).toBe(2);

        expect(result.UserTask_3).toBeDefined();
        expect(result.UserTask_3.length).toBe(5);
        
      }));

      it('should create a tree', inject(function($rootScope, ActivityInstance, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?>' +
                  '<bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="__pzRIN71EeKfS5UE4t-U3A">' +
                    '<bpmn2:process id="Process_1" isExecutable="false">' +
                      '<bpmn2:subProcess id="SubProcess_1" name="Parallel Multi Instance SubProcess">' +
                        '<bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>' +
                        '<bpmn2:outgoing>SequenceFlow_21</bpmn2:outgoing>' +
                        '<bpmn2:multiInstanceLoopCharacteristics>' +
                          '<bpmn2:loopCardinality xsi:type="bpmn2:tFormalExpression">2</bpmn2:loopCardinality>' +
                        '</bpmn2:multiInstanceLoopCharacteristics>' +
                        '<bpmn2:startEvent id="StartEvent_2" name="Start Event">' +
                          '<bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing>' +
                        '</bpmn2:startEvent>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_3" name="" sourceRef="StartEvent_2" targetRef="ParallelGateway_2"/>' +
                        '<bpmn2:parallelGateway id="ParallelGateway_2" name="Parallel Gateway">' +
                          '<bpmn2:incoming>SequenceFlow_3</bpmn2:incoming>' +
                          '<bpmn2:outgoing>SequenceFlow_4</bpmn2:outgoing>' +
                          '<bpmn2:outgoing>SequenceFlow_5</bpmn2:outgoing>' +
                          '<bpmn2:outgoing>SequenceFlow_6</bpmn2:outgoing>' +
                        '</bpmn2:parallelGateway>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_4" sourceRef="ParallelGateway_2" targetRef="UserTask_1"/>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_5" sourceRef="ParallelGateway_2" targetRef="UserTask_2"/>' +
                        '<bpmn2:userTask id="UserTask_2" name="Second User Task">' +
                          '<bpmn2:incoming>SequenceFlow_5</bpmn2:incoming>' +
                          '<bpmn2:outgoing>SequenceFlow_8</bpmn2:outgoing>' +
                        '</bpmn2:userTask>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_6" sourceRef="ParallelGateway_2" targetRef="ServiceTask_2"/>' +
                        '<bpmn2:serviceTask id="ServiceTask_2">' +
                          '<bpmn2:incoming>SequenceFlow_6</bpmn2:incoming>' +
                          '<bpmn2:outgoing>SequenceFlow_7</bpmn2:outgoing>' +
                        '</bpmn2:serviceTask>' +
                        '<bpmn2:userTask id="UserTask_1" name="First User Task">' +
                          '<bpmn2:incoming>SequenceFlow_4</bpmn2:incoming>' +
                          '<bpmn2:outgoing>SequenceFlow_9</bpmn2:outgoing>' +
                        '</bpmn2:userTask>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_7" sourceRef="ServiceTask_2" targetRef="ParallelGateway_3"/>' +
                        '<bpmn2:parallelGateway id="ParallelGateway_3" name="Parallel Gateway">' +
                          '<bpmn2:incoming>SequenceFlow_7</bpmn2:incoming>' +
                          '<bpmn2:incoming>SequenceFlow_8</bpmn2:incoming>' +
                          '<bpmn2:incoming>SequenceFlow_9</bpmn2:incoming>' +
                          '<bpmn2:outgoing>SequenceFlow_10</bpmn2:outgoing>' +
                        '</bpmn2:parallelGateway>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_8" name="" sourceRef="UserTask_2" targetRef="ParallelGateway_3"/>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_9" name="" sourceRef="UserTask_1" targetRef="ParallelGateway_3"/>' +
                        '<bpmn2:endEvent id="EndEvent_1" name="End Event">' +
                          '<bpmn2:incoming>SequenceFlow_10</bpmn2:incoming>' +
                        '</bpmn2:endEvent>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_10" sourceRef="ParallelGateway_3" targetRef="EndEvent_1"/>' +
                      '</bpmn2:subProcess>' +
                      '<bpmn2:subProcess id="SubProcess_2" name="Sub Process with nested Sub Process">' +
                        '<bpmn2:incoming>SequenceFlow_11</bpmn2:incoming>' +
                        '<bpmn2:outgoing>SequenceFlow_19</bpmn2:outgoing>' +
                        '<bpmn2:startEvent id="StartEvent_3" name="Start Event">' +
                          '<bpmn2:outgoing>SequenceFlow_12</bpmn2:outgoing>' +
                        '</bpmn2:startEvent>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_12" sourceRef="StartEvent_3" targetRef="NestedSubProcess_1"/>' +
                        '<bpmn2:subProcess id="NestedSubProcess_1" name="Nested SubProcess">' +
                          '<bpmn2:incoming>SequenceFlow_12</bpmn2:incoming>' +
                          '<bpmn2:outgoing>SequenceFlow_17</bpmn2:outgoing>' +
                          '<bpmn2:startEvent id="StartEvent_4" name="Start Event">' +
                            '<bpmn2:outgoing>SequenceFlow_14</bpmn2:outgoing>' +
                          '</bpmn2:startEvent>' +
                          '<bpmn2:userTask id="UserTask_3" name="Third User Task">' +
                            '<bpmn2:incoming>SequenceFlow_14</bpmn2:incoming>' +
                            '<bpmn2:outgoing>SequenceFlow_15</bpmn2:outgoing>' +
                            '<bpmn2:multiInstanceLoopCharacteristics>' +
                              '<bpmn2:loopCardinality xsi:type="bpmn2:tFormalExpression">5</bpmn2:loopCardinality>' +
                            '</bpmn2:multiInstanceLoopCharacteristics>' +
                          '</bpmn2:userTask>' +
                          '<bpmn2:sequenceFlow id="SequenceFlow_14" sourceRef="StartEvent_4" targetRef="UserTask_3"/>' +
                          '<bpmn2:endEvent id="EndEvent_3" name="End Event">' +
                            '<bpmn2:incoming>SequenceFlow_15</bpmn2:incoming>' +
                          '</bpmn2:endEvent>' +
                          '<bpmn2:sequenceFlow id="SequenceFlow_15" sourceRef="UserTask_3" targetRef="EndEvent_3"/>' +
                        '</bpmn2:subProcess>' +
                        '<bpmn2:sequenceFlow id="SequenceFlow_17" sourceRef="NestedSubProcess_1" targetRef="EndEvent_5"/>' +
                        '<bpmn2:endEvent id="EndEvent_5" name="End Event">' +
                          '<bpmn2:incoming>SequenceFlow_17</bpmn2:incoming>' +
                        '</bpmn2:endEvent>' +
                      '</bpmn2:subProcess>' +
                      '<bpmn2:startEvent id="StartEvent_1" name="Start Event">' +
                        '<bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>' +
                      '</bpmn2:startEvent>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_1" name="" sourceRef="StartEvent_1" targetRef="ParallelGateway_1"/>' +
                      '<bpmn2:parallelGateway id="ParallelGateway_1" name="Parallel Gateway">' +
                        '<bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>' +
                        '<bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>' +
                        '<bpmn2:outgoing>SequenceFlow_11</bpmn2:outgoing>' +
                        '<bpmn2:outgoing>SequenceFlow_18</bpmn2:outgoing>' +
                      '</bpmn2:parallelGateway>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ParallelGateway_1" targetRef="SubProcess_1"/>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_11" sourceRef="ParallelGateway_1" targetRef="SubProcess_2"/>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_18" sourceRef="ParallelGateway_1" targetRef="ServiceTask_1"/>' +
                      '<bpmn2:serviceTask id="ServiceTask_1" name="My Service Task">' +
                        '<bpmn2:incoming>SequenceFlow_18</bpmn2:incoming>' +
                        '<bpmn2:outgoing>SequenceFlow_20</bpmn2:outgoing>' +
                      '</bpmn2:serviceTask>' +
                      '<bpmn2:parallelGateway id="ParallelGateway_4" name="Parallel Gateway">' +
                        '<bpmn2:incoming>SequenceFlow_19</bpmn2:incoming>' +
                        '<bpmn2:incoming>SequenceFlow_20</bpmn2:incoming>' +
                        '<bpmn2:incoming>SequenceFlow_21</bpmn2:incoming>' +
                        '<bpmn2:outgoing>SequenceFlow_22</bpmn2:outgoing>' +
                      '</bpmn2:parallelGateway>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_19" sourceRef="SubProcess_2" targetRef="ParallelGateway_4"/>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_20" name="" sourceRef="ServiceTask_1" targetRef="ParallelGateway_4"/>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_21" name="" sourceRef="SubProcess_1" targetRef="ParallelGateway_4"/>' +
                      '<bpmn2:endEvent id="EndEvent_6" name="End Event">' +
                        '<bpmn2:incoming>SequenceFlow_22</bpmn2:incoming>' +
                      '</bpmn2:endEvent>' +
                      '<bpmn2:sequenceFlow id="SequenceFlow_22" sourceRef="ParallelGateway_4" targetRef="EndEvent_6"/>' +
                    '</bpmn2:process>' +
                  '</bpmn2:definitions>';
        var semantic = Transform.transformBpmn20Xml(xml);
        var resultMap = {};
        var processDefinition = {key: 'Process_1'};

        // when
        var tree = ActivityInstance.createActivityInstanceTree(processDefinition, semantic, $rootScope.activityInstances, resultMap);

        // then
        expect(tree).toBeDefined();
        expect(resultMap).toBeDefined();

        // root node Process_1
        var process1Node = resultMap.Process_1;     
        expect(process1Node).toBeDefined();
        expect(process1Node.length).toBe(1);

        expect(process1Node[0].id).toBe('instance_1');
        expect(process1Node[0].activityId).toBe('Process_1');
        expect(process1Node[0].label).toBe('process (Process_...)');
        expect(process1Node[0].children.length).toBe(4);

        // children of Process_1 node
        var subProcess1Nodes = resultMap.SubProcess_1;
        expect(subProcess1Nodes).toBeDefined();
        expect(subProcess1Nodes.length).toBe(2);

        expect(subProcess1Nodes[0].id).toBe('instance_2');
        expect(subProcess1Nodes[0].activityId).toBe('SubProcess_1');
        expect(subProcess1Nodes[0].label).toBe('Parallel Multi Instance SubProcess');
        expect(subProcess1Nodes[0].children.length).toBe(3);

        expect(subProcess1Nodes[1].id).toBe('instance_5');
        expect(subProcess1Nodes[1].activityId).toBe('SubProcess_1');
        expect(subProcess1Nodes[1].label).toBe('Parallel Multi Instance SubProcess');
        expect(subProcess1Nodes[1].children.length).toBe(3);

        expect(process1Node[0].children).toContain(subProcess1Nodes[0]);
        expect(process1Node[0].children).toContain(subProcess1Nodes[1]);

        var subProcess2Nodes = resultMap.SubProcess_2;
        expect(subProcess2Nodes).toBeDefined();
        expect(subProcess2Nodes.length).toBe(1);

        expect(subProcess2Nodes[0].id).toBe('instance_9');
        expect(subProcess2Nodes[0].activityId).toBe('SubProcess_2');
        expect(subProcess2Nodes[0].label).toBe('Sub Process with nested Sub Process');
        expect(subProcess2Nodes[0].children.length).toBe(1);

        expect(process1Node[0].children).toContain(subProcess2Nodes[0]);

        var serviceTask1nodes = resultMap.ServiceTask_1
        expect(serviceTask1nodes).toBeDefined();
        expect(serviceTask1nodes.length).toBe(1);        

        expect(serviceTask1nodes[0].id).toBe('instance_8');
        expect(serviceTask1nodes[0].activityId).toBe('ServiceTask_1');
        expect(serviceTask1nodes[0].label).toBe('My Service Task');
        expect(serviceTask1nodes[0].children.length).toBe(0);

        expect(process1Node[0].children).toContain(serviceTask1nodes[0]);

        // children of SubProcess_1 node
        var userTask1Nodes = resultMap.UserTask_1;
        expect(userTask1Nodes).toBeDefined();
        expect(userTask1Nodes.length).toBe(2);

        expect(userTask1Nodes[0].id).toBe('instance_3');
        expect(userTask1Nodes[0].activityId).toBe('UserTask_1');
        expect(userTask1Nodes[0].label).toBe('First User Task');
        expect(userTask1Nodes[0].children.length).toBe(0);

        expect(subProcess1Nodes[0].children).toContain(userTask1Nodes[0]);

        expect(userTask1Nodes[1].id).toBe('instance_6');
        expect(userTask1Nodes[1].activityId).toBe('UserTask_1');
        expect(userTask1Nodes[1].label).toBe('First User Task');
        expect(userTask1Nodes[1].children.length).toBe(0);
        
        expect(subProcess1Nodes[1].children).toContain(userTask1Nodes[1]);

        var userTask2Nodes = resultMap.UserTask_2;
        expect(userTask2Nodes).toBeDefined();
        expect(userTask2Nodes.length).toBe(2);

        expect(userTask2Nodes[0].id).toBe('instance_4');
        expect(userTask2Nodes[0].activityId).toBe('UserTask_2');
        expect(userTask2Nodes[0].label).toBe('Second User Task');
        expect(userTask2Nodes[0].children.length).toBe(0);

        expect(subProcess1Nodes[0].children).toContain(userTask2Nodes[0]);

        expect(userTask2Nodes[1].id).toBe('instance_7');
        expect(userTask2Nodes[1].activityId).toBe('UserTask_2');
        expect(userTask2Nodes[1].label).toBe('Second User Task');
        expect(userTask2Nodes[1].children.length).toBe(0);
        
        expect(subProcess1Nodes[1].children).toContain(userTask2Nodes[1]);

        var serviceTask2nodes = resultMap.ServiceTask_2;
        expect(serviceTask2nodes).toBeDefined();
        expect(serviceTask2nodes.length).toBe(2);

        expect(serviceTask2nodes[0].id).toBe('transition_instance_1');
        expect(serviceTask2nodes[0].activityId).toBe('ServiceTask_2');
        expect(serviceTask2nodes[0].label).toBe('serviceTask (ServiceT...)');
        expect(serviceTask2nodes[0].children.length).toBe(0);

        expect(subProcess1Nodes[0].children).toContain(serviceTask2nodes[0]);

        expect(serviceTask2nodes[1].id).toBe('transition_instance_2');
        expect(serviceTask2nodes[1].activityId).toBe('ServiceTask_2');
        expect(serviceTask2nodes[1].label).toBe('serviceTask (ServiceT...)');
        expect(serviceTask2nodes[1].children.length).toBe(0);
        
        expect(subProcess1Nodes[1].children).toContain(serviceTask2nodes[1]);

        // children of SubProcess_2 node
        var nestedSubProcess1Nodes = resultMap.NestedSubProcess_1;
        expect(nestedSubProcess1Nodes).toBeDefined();
        expect(nestedSubProcess1Nodes.length).toBe(1);

        expect(nestedSubProcess1Nodes[0].id).toBe('instance_10');
        expect(nestedSubProcess1Nodes[0].activityId).toBe('NestedSubProcess_1');
        expect(nestedSubProcess1Nodes[0].label).toBe('Nested SubProcess');
        expect(nestedSubProcess1Nodes[0].children.length).toBe(5);

        expect(subProcess2Nodes[0].children).toContain(nestedSubProcess1Nodes[0]);

        // children of NestedSubProcess_1
        var userTask3Nodes = resultMap.UserTask_3;
        expect(userTask3Nodes).toBeDefined();
        expect(userTask3Nodes.length).toBe(5);

        expect(userTask3Nodes[0].id).toBe('instance_11');
        expect(userTask3Nodes[0].activityId).toBe('UserTask_3');
        expect(userTask3Nodes[0].label).toBe('Third User Task');
        expect(userTask3Nodes[0].children.length).toBe(0);
        
        expect(nestedSubProcess1Nodes[0].children).toContain(userTask3Nodes[0]);

        expect(userTask3Nodes[1].id).toBe('instance_12');
        expect(userTask3Nodes[1].activityId).toBe('UserTask_3');
        expect(userTask3Nodes[1].label).toBe('Third User Task');
        expect(userTask3Nodes[1].children.length).toBe(0);
        
        expect(nestedSubProcess1Nodes[0].children).toContain(userTask3Nodes[1]);

        expect(userTask3Nodes[2].id).toBe('instance_13');
        expect(userTask3Nodes[2].activityId).toBe('UserTask_3');
        expect(userTask3Nodes[2].label).toBe('Third User Task');
        expect(userTask3Nodes[2].children.length).toBe(0);
        
        expect(nestedSubProcess1Nodes[0].children).toContain(userTask3Nodes[2]);

        expect(userTask3Nodes[3].id).toBe('instance_14');
        expect(userTask3Nodes[3].activityId).toBe('UserTask_3');
        expect(userTask3Nodes[3].label).toBe('Third User Task');
        expect(userTask3Nodes[3].children.length).toBe(0);
        
        expect(nestedSubProcess1Nodes[0].children).toContain(userTask3Nodes[3]);

        expect(userTask3Nodes[4].id).toBe('instance_15');
        expect(userTask3Nodes[4].activityId).toBe('UserTask_3');
        expect(userTask3Nodes[4].label).toBe('Third User Task');
        expect(userTask3Nodes[4].children.length).toBe(0);
        
        expect(nestedSubProcess1Nodes[0].children).toContain(userTask3Nodes[4]);

      }));

    });
  });
});