/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var expect = require('chai').expect;
var sinon = require('sinon');
var angular = require('angular');
var testModule = require('./module');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common.external-tasks-common observeBpmnElements', function() {
  var $scope;
  var bpmnElements;
  var instance;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($injector) {
    var observeBpmnElements = $injector.get('observeBpmnElements');

    $scope = '$scope';
    bpmnElements = 'bpmn-elements';
    instance = {
      processData: {
        newChild: sinon.stub().returnsThis(),
        observe: sinon.stub().callsArgWith(1, bpmnElements)
      }
    };

    observeBpmnElements($scope, instance);
  }));

  it('should create new instance of processData on given scope', function() {
    expect(instance.processData.newChild.calledWith($scope)).to.eql(true);
  });

  it('should observe bpmnElements', function() {
    expect(instance.processData.observe.calledWith('bpmnElements')).to.eql(
      true
    );
  });

  it('should set bpmnElements on instance', function() {
    expect(instance.bpmnElements).to.eql(bpmnElements);
  });
});
