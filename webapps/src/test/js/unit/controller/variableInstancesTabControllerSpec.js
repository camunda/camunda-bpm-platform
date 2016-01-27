/* global define: false, describe: false, xdescribe: false, beforeEach: false, module: false, inject: false, it: false, expect: false */
define([
  'angular',
  'plugin/base/app/views/processInstance/variableInstancesTab',
  'cockpit/resources/localExecutionVariableResource',
  'cockpit-plugin',
  //'cockpit-plugin/service',
  'angular-resource',
  'camunda-common/services/uri',
  // 'camunda-common/services/requestStatus',
  'camunda-common/directives/requestAware'
], function(angular) {
  'use strict';
  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('controllers', function() {

    xdescribe('variable instances controller', function() {

      var instanceIdToInstanceMap = {
                                      instance_1: {
                                                    id : 'instance_1',
                                                    parentActivityInstanceId : null,
                                                    activityId : 'FailingProcess',
                                                    processInstanceId : 'instance_1',
                                                    processDefinitionId : 'aProcessDefinitionId',
                                                    childActivityInstances : [ ],
                                                    childTransitionInstances : [ ],
                                                    executionIds : [ 'instance_1' ]
                                                  },
                                      instance_2: {
                                                    id : 'instance_2',
                                                    parentActivityInstanceId : 'instance_1',
                                                    activityId : 'ServiceTask_1',
                                                    processInstanceId : 'instance_1',
                                                    processDefinitionId : 'aProcessDefinitionId',
                                                    childActivityInstances : [ ],
                                                    childTransitionInstances : [ ],
                                                    executionIds : [ 'instance_2' ]
                                                  },
                                      instance_3: {
                                                    id : 'instance_3',
                                                    parentActivityInstanceId : 'instance_1',
                                                    activityId : 'UserTask_1',
                                                    processInstanceId : 'instance_1',
                                                    processDefinitionId : 'aProcessDefinitionId',
                                                    childActivityInstances : [ ],
                                                    childTransitionInstances : [ ],
                                                    executionIds : [ 'instance_3' ]
                                                  }
                                    };

      beforeEach(function () {
        angular.module('testmodule', [ 'cockpit.plugin',
                                       'cockpit.resources',
                                       'cockpit.plugin.base.views',
                                       'camunda.common.services',
                                       'camunda.common.services.uri',
                                       'ngResource' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      beforeEach(inject(function($rootScope) {
        $rootScope.processInstanceId = 'aProcessInstanceId';
        $rootScope.selection = {};
        $rootScope.processInstance = {};
      }));

      beforeEach(inject(function($httpBackend) {
        // backend definition common for all tests
        $httpBackend
          .when('POST', 'engine://engine/:engine/variable-instance/count',
            {
              processInstanceIdIn : ['aProcessInstanceId'],
              activityInstanceIdIn :  []
            })
          .respond(
            {
              count: 5
            });

        $httpBackend
          .when('POST', 'engine://engine/:engine/variable-instance/?firstResult=0&maxResults=50',
            {
              processInstanceIdIn : ['aProcessInstanceId'],
              activityInstanceIdIn :  []
            })
          .respond(
            [
              {
                name: 'value3',
                type: 'String',
                value: 'c',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              },
              {
                name: 'value1',
                type: 'String',
                value: 'a',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'value2',
                type: 'String',
                value: 'b',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'nrOfInstances',
                type: 'Integer',
                value: 5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'nrOfCompletedInstances',
                type: 'Integer',
                value: 0,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_3',
                taskId: null,
                activityInstanceId: 'instance_3'
              }
            ]);

        $httpBackend
          .when('POST', 'engine://engine/:engine/variable-instance/count',
            {
              processInstanceIdIn : ['aProcessInstanceId'],
              activityInstanceIdIn :  [ 'instance_2' ]
            })
          .respond(
            {
              count: 3
            });

        $httpBackend
          .when('POST', 'engine://engine/:engine/variable-instance/?firstResult=0&maxResults=50',
            {
              processInstanceIdIn : ['aProcessInstanceId'],
              activityInstanceIdIn :  [ 'instance_2' ]
            })
          .respond(
            [
              {
                name: 'value1',
                type: 'String',
                value: 'a',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'value2',
                type: 'String',
                value: 'b',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'nrOfInstances',
                type: 'Integer',
                value: 5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              }
            ]);

        $httpBackend
          .when('POST', 'engine://engine/:engine/variable-instance/count',
            {
              processInstanceIdIn : ['aProcessInstanceId'],
              activityInstanceIdIn :  [ 'instance_1', 'instance_2' ]
            })
          .respond(
            {
              count: 4
            });

        $httpBackend
          .when('POST', 'engine://engine/:engine/variable-instance/?firstResult=0&maxResults=50',
            {
              processInstanceIdIn : ['aProcessInstanceId'],
              activityInstanceIdIn :  [ 'instance_1', 'instance_2' ]
            })
          .respond(
            [
              {
                name: 'value3',
                type: 'String',
                value: 'c',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              },
              {
                name: 'value1',
                type: 'String',
                value: 'a',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'value2',
                type: 'String',
                value: 'b',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              },
              {
                name: 'nrOfInstances',
                type: 'Integer',
                value: 5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_2',
                taskId: null,
                activityInstanceId: 'instance_2'
              }
            ]);

      }));


      it('should initially load all variable instances', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        // when
        $httpBackend.flush();

        // then
        expect($rootScope.variables).toBeDefined();
        expect($rootScope.variables.length).toBe(5);

        for (var i = 0; i < $rootScope.variables.length; i++) {
          var variable = $rootScope.variables[i];
          expect(variable.id).toBe(i);
        }

        expect($rootScope.pages.total).toBe(1);

      }));

      it('should load all variable instances, when processInstance.instanceIdToInstanceMap has been set', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        // when
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        $rootScope.$digest();
        $httpBackend.flush();

        // then
        expect($rootScope.variables).toBeDefined();
        expect($rootScope.variables.length).toBe(5);

        for (var i = 0; i < $rootScope.variables.length; i++) {
          var variable = $rootScope.variables[i];
          expect(variable.id).toBe(i);
          expect($rootScope.getCopy(variable.id)).toBeDefined();
          expect(angular.equals(variable, $rootScope.getCopy(variable.id))).toBe(true);
        }

        expect($rootScope.pages.total).toBe(1);

      }));

      it('should load variable instances for specific activity instance', inject(function($rootScope, $controller, $httpBackend) {

        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when
        $rootScope.selection.treeDiagramMapping = {activityInstances: [$rootScope.processInstance.instanceIdToInstanceMap.instance_2]};
        $rootScope.$digest();
        $httpBackend.flush();

        // then
        expect($rootScope.variables).toBeDefined();
        expect($rootScope.variables.length).toBe(3);

        for (var i = 0; i < $rootScope.variables.length; i++) {
          var variable = $rootScope.variables[i];
          expect(variable.id).toBe(i);
          expect($rootScope.getCopy(variable.id)).toBeDefined();
          expect(angular.equals(variable, $rootScope.getCopy(variable.id))).toBe(true);
        }

        expect($rootScope.pages.total).toBe(1);

      }));

      it('should load variable instances for two activity instances', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when

        $rootScope.selection.treeDiagramMapping = {activityInstances: [
          $rootScope.processInstance.instanceIdToInstanceMap.instance_1,
          $rootScope.processInstance.instanceIdToInstanceMap.instance_2
        ]};

        $rootScope.$digest();
        $httpBackend.flush();

        // then
        expect($rootScope.variables).toBeDefined();
        expect($rootScope.variables.length).toBe(4);

        for (var i = 0; i < $rootScope.variables.length; i++) {
          var variable = $rootScope.variables[i];
          expect(variable.id).toBe(i);
          expect($rootScope.getCopy(variable.id)).toBeDefined();
          expect(angular.equals(variable, $rootScope.getCopy(variable.id))).toBe(true);
        }

        expect($rootScope.pages.total).toBe(1);
      }));


      it('should return true for a String variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'String',
                value: 'c',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'string',
                value: 'c',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isString(variable1);
        var result2 = $rootScope.isString(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Integer variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Integer',
                value: 1,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'integer',
                value: 1,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isInteger(variable1);
        var result2 = $rootScope.isInteger(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));


      it('should return true for a Short variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Short',
                value: 1,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'short',
                value: 1,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isShort(variable1);
        var result2 = $rootScope.isShort(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Long variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Long',
                value: 1,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'long',
                value: 1,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isLong(variable1);
        var result2 = $rootScope.isLong(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Double variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Double',
                value: 1.5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'double',
                value: 1.5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isDouble(variable1);
        var result2 = $rootScope.isDouble(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Float variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Float',
                value: 1.5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'float',
                value: 1.5,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isFloat(variable1);
        var result2 = $rootScope.isFloat(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Boolean variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Boolean',
                value: true,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'boolean',
                value: true,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isBoolean(variable1);
        var result2 = $rootScope.isBoolean(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Null variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Null',
                value: null,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'null',
                value: null,
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isNull(variable1);
        var result2 = $rootScope.isNull(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Date variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Date',
                value: '2013-07-12T09:14:15',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'date',
                value: '2013-07-12T09:14:15',
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isDate(variable1);
        var result2 = $rootScope.isDate(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should return true for a Serializable variable', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = {
                name: 'value3',
                type: 'Serializable',
                value: {a: 'b', c: 'd'},
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        var variable2 = {
                name: 'value3',
                type: 'serializable',
                value: {a: 'b', c: 'd'},
                processInstanceId: 'aProcessInstanceId',
                executionId: 'instance_1',
                taskId: null,
                activityInstanceId: 'instance_1'
              };

        // when
        var result1 = $rootScope.isSerializable(variable1);
        var result2 = $rootScope.isSerializable(variable2);

        // then
        expect(result1).toBe(true);
        expect(result2).toBe(true);
      }));

      it('should mark variable instance as current editing', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];
        var variable2 = $rootScope.variables[1];
        var variable3 = $rootScope.variables[2];
        var variable4 = $rootScope.variables[3];
        var variable5 = $rootScope.variables[4];

        // when
        $rootScope.editVariable(variable1);

        // then
        expect($rootScope.isInEditMode(variable1)).toBe(true);

        expect($rootScope.isInEditMode(variable2)).toBe(false);
        expect($rootScope.isInEditMode(variable3)).toBe(false);
        expect($rootScope.isInEditMode(variable4)).toBe(false);
        expect($rootScope.isInEditMode(variable5)).toBe(false);

      }));

      it('should mark mutliple variable instances as current editing', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];
        var variable2 = $rootScope.variables[1];
        var variable3 = $rootScope.variables[2];
        var variable4 = $rootScope.variables[3];
        var variable5 = $rootScope.variables[4];

        // when
        $rootScope.editVariable(variable1);
        $rootScope.editVariable(variable2);

        // then
        expect($rootScope.isInEditMode(variable1)).toBe(true);
        expect($rootScope.isInEditMode(variable2)).toBe(true);

        expect($rootScope.isInEditMode(variable3)).toBe(false);
        expect($rootScope.isInEditMode(variable4)).toBe(false);
        expect($rootScope.isInEditMode(variable5)).toBe(false);

      }));

      it('should remove variable instance from array of editing (1)', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];
        var variable2 = $rootScope.variables[1];
        var variable3 = $rootScope.variables[2];
        var variable4 = $rootScope.variables[3];
        var variable5 = $rootScope.variables[4];

        // when
        $rootScope.editVariable(variable1);
        expect($rootScope.isInEditMode(variable1)).toBe(true);

        $rootScope.closeInPlaceEditing(variable1);
        // then
        expect($rootScope.isInEditMode(variable1)).toBe(false);

        expect($rootScope.isInEditMode(variable2)).toBe(false);
        expect($rootScope.isInEditMode(variable3)).toBe(false);
        expect($rootScope.isInEditMode(variable4)).toBe(false);
        expect($rootScope.isInEditMode(variable5)).toBe(false);

      }));

      it('should remove variable instance from array of editing (2)', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];
        var variable2 = $rootScope.variables[1];
        var variable3 = $rootScope.variables[2];
        var variable4 = $rootScope.variables[3];
        var variable5 = $rootScope.variables[4];

        // when
        $rootScope.editVariable(variable1);
        $rootScope.editVariable(variable2);
        expect($rootScope.isInEditMode(variable1)).toBe(true);
        expect($rootScope.isInEditMode(variable2)).toBe(true);

        $rootScope.closeInPlaceEditing(variable2);

        // then
        expect($rootScope.isInEditMode(variable1)).toBe(true);

        expect($rootScope.isInEditMode(variable2)).toBe(false);
        expect($rootScope.isInEditMode(variable3)).toBe(false);
        expect($rootScope.isInEditMode(variable4)).toBe(false);
        expect($rootScope.isInEditMode(variable5)).toBe(false);

      }));

      it('should remove variable instance from array of editing (2)', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];
        var variable2 = $rootScope.variables[1];
        var variable3 = $rootScope.variables[2];
        var variable4 = $rootScope.variables[3];
        var variable5 = $rootScope.variables[4];

        // when
        $rootScope.editVariable(variable1);
        $rootScope.editVariable(variable2);
        expect($rootScope.isInEditMode(variable1)).toBe(true);
        expect($rootScope.isInEditMode(variable2)).toBe(true);

        $rootScope.closeInPlaceEditing(variable2);

        // then
        expect($rootScope.isInEditMode(variable1)).toBe(true);

        expect($rootScope.isInEditMode(variable2)).toBe(false);
        expect($rootScope.isInEditMode(variable3)).toBe(false);
        expect($rootScope.isInEditMode(variable4)).toBe(false);
        expect($rootScope.isInEditMode(variable5)).toBe(false);

      }));

      it('should post new variable instance value', inject(function($rootScope, $controller, $httpBackend) {
        $httpBackend
          .when('POST', 'engine://engine/execution/instance_1/localVariables',
            {
              modifications : {value3: {value: 'newValue', type: 'String'}}
            })
          .respond(204, '');

        $httpBackend
          .when('GET', 'engine://engine/execution/instance_1/localVariables/value3')
          .respond({
            value:'newValue', type: 'String'
          });

        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];

        // when
        $rootScope.getCopy(variable1.id).value = 'newValue';
        $rootScope.submit(variable1);

        $rootScope.$digest();

        $httpBackend.flush();

        // then
        expect(variable1.value).toBe('newValue');

      }));

      it('should return status 500', inject(function($rootScope, $controller, $httpBackend) {

        $httpBackend
          .when('POST', 'engine://engine/execution/instance_1/localVariables',
            {
              modifications : {value3: {value: 'newValue', type: 'String'}}
            })
          .respond(500,
            {
              type: 'anyException', message: 'An exception occured'
            });

        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];

        // when
        $rootScope.getCopy(variable1.id).value = 'newValue';
        $rootScope.submit(variable1);

        $rootScope.$digest();

        $httpBackend.flush();

        // then
        expect($rootScope.getExceptionForVariableId(variable1.id)).toBeDefined();
        expect($rootScope.getExceptionForVariableId(variable1.id).type).toBe('anyException');
        expect($rootScope.getExceptionForVariableId(variable1.id).message).toBe('An exception occured');

      }));

      it('should select the corresponding activity instance', inject(function($rootScope, $controller, $httpBackend) {
        // given
        $rootScope.processInstance = { instanceIdToInstanceMap: instanceIdToInstanceMap };
        var pc = $controller('VariableInstancesController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var variable1 = $rootScope.variables[0];

        // when
        $rootScope.selectActivityInstance(variable1);

        // then
        expect($rootScope.selection.treeDiagramMapping.activityInstances).toBeDefined();
        expect($rootScope.selection.treeDiagramMapping.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0]).toBe(variable1.instance);
        expect($rootScope.selection.treeDiagramMapping.scrollTo).toBe(variable1.instance);
      }));

    });
  });
});
