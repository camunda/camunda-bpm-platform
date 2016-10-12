'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var directiveFactory = require('../directives/cam-hoverable-title');

describe('cam-common cam-hoverable-title', function() {
  var linkFn;
  var $element;
  var title;
  var $attr;
  var removeHoverListener;
  var HoverArea;

  beforeEach(function() {
    linkFn = directiveFactory().link;
    $element = {
      on: sinon.stub(),
      addClass: sinon.spy(),
      removeClass: sinon.spy()
    };
    title = 'title';
    $attr = {
      hoverClass: 'hovered',
      $observe: sinon.stub().callsArgWith(1, title)
    };
    removeHoverListener = sinon.spy();
    HoverArea = {
      addHoverListener: sinon.stub().returns(removeHoverListener)
    };

    linkFn(null, $element, $attr, HoverArea);
  });

  it('should add $element $destroy listener', function() {
    expect($element.on.calledWith('$destroy')).to.eql(true);
  });

  it('should observe cam-hoverable-title attribute', function() {
    expect($attr.$observe.calledWith('camHoverableTitle')).to.eql(true);
  });

  it('should add hover title listener', function() {
    expect(HoverArea.addHoverListener.calledWith(title)).to.eql(true);
  });

  it('should clean old hover listener when title changes', function() {
    $attr.$observe.getCall(0).args[1]('new title');

    expect(removeHoverListener.calledOnce).to.eql(true);
  });

  it('should clean hover listener on $destroy event', function() {
    $element.on.getCall(0).args[1]();

    expect(removeHoverListener.calledOnce).to.eql(true);
  });

  it('should add hover class when title is hovered', function() {
    HoverArea.addHoverListener.getCall(0).args[1](true);

    expect($element.addClass.calledWith($attr.hoverClass)).to.eql(true);
  });

  it('should remove hover class when title is not hovered', function() {
    HoverArea.addHoverListener.getCall(0).args[1](false);

    expect($element.removeClass.calledWith($attr.hoverClass)).to.eql(true);
  });
});
