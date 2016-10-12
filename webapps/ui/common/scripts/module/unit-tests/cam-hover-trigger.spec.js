'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var directiveFactory = require('../directives/cam-hover-trigger');

describe('cam-common cam-hover-trigger', function() {
  var linkFn;
  var $scope;
  var $element;
  var $attr;
  var HoverArea;

  beforeEach(function() {
    linkFn = directiveFactory().link;
    $scope = {
      $apply: sinon.stub().callsArg(0)
    };
    $element = {
      on: sinon.stub()
    };
    $attr = {
      camHoverTrigger: 'title'
    };
    HoverArea = {
      hoverTitle: sinon.spy(),
      cleanHover: sinon.spy()
    };

    linkFn($scope, $element, $attr, HoverArea);
  });

  it('should add mouseneter and mouseleave listeners', function() {
    expect($element.on.callCount).to.eql(2);
    expect($element.on.calledWith('mouseneter'));
    expect($element.on.calledWith('mouseleave'));
  });

  describe('on mouseenter event', function() {
    beforeEach(function() {
      var call = findEventCall('mouseenter');

      call.args[1]();
    });

    it('should call $scope.$apply', function() {
      expect($scope.$apply.calledOnce).to.eql(true);
    });

    it('should hover given title', function() {
      expect(HoverArea.hoverTitle.calledWith($attr.camHoverTrigger)).to.eql(true);
    });
  });

  describe('on mouseleave event', function() {
    beforeEach(function() {
      var call = findEventCall('mouseleave');

      call.args[1]();
    });

    it('should call $scope.$apply', function() {
      expect($scope.$apply.calledOnce).to.eql(true);
    });

    it('should clean hover', function() {
      expect(HoverArea.cleanHover.calledOnce).to.eql(true);
    });
  });

  function findEventCall(event) {
    var len = $element.on.callCount;
    var i;
    var call;

    for(i = 0; i < len; i++) {
      call = $element.on.getCall(i);

      if (call.args[0] === event) {
        return call;
      }
    }

    return null;
  }
});
