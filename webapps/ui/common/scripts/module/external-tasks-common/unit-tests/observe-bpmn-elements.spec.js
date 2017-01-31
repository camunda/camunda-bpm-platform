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
    expect(instance.processData.observe.calledWith('bpmnElements')).to.eql(true);
  });

  it('should set bpmnElements on instance', function() {
    expect(instance.bpmnElements).to.eql(bpmnElements);
  });
});
