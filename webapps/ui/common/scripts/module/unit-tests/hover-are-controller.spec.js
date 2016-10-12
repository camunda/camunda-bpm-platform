'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common HoverAreaController', function() {
  var instance;
  var title;
  var listener;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function($controller) {
    title = 'some-title';
    listener = sinon.spy();
    instance = $controller('HoverAreaController');

    instance.addHoverListener(title, listener);
  }));

  describe('addHoverListener', function() {
    it('should call listener', function() {
      expect(listener.calledWith(false)).to.eql(true);
    });
  });

  describe('hoverTitle', function() {
    beforeEach(function() {
      listener.reset();
    });

    it('should call listener with false when other title is selected', function() {
      instance.hoverTitle(title + 'dd');

      expect(listener.calledOnce).to.eql(true);
      expect(listener.calledWith(false)).to.eql(true);
    });

    it('should call listener with true when correct title is selected', function() {
      instance.hoverTitle(title);

      expect(listener.calledOnce).to.eql(true);
      expect(listener.calledWith(true)).to.eql(true);
    });
  });

  describe('cleanHover', function() {
    beforeEach(function() {
      instance.hoverTitle(title);

      listener.reset();
    });

    it('should call listener with false', function() {
      instance.cleanHover();

      expect(listener.calledOnce).to.eql(true);
      expect(listener.calledWith(false)).to.eql(true);
    });
  });
});
