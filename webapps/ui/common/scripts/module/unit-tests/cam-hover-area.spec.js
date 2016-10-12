'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var directiveFactory = require('../directives/cam-hover-area');

describe('cam-common cam-hover-area', function() {
  var linkFn;
  var $element;
  var $transclude;
  var content;

  beforeEach(function() {
    linkFn = directiveFactory().link;
    $element = {
      empty: sinon.spy(),
      append: sinon.spy()
    };
    content = 'content';
    $transclude = sinon.stub().callsArgWith(0, content);

    linkFn(null, $element, null, null, $transclude);
  });

  it('should call $transclude', function() {
    expect($transclude.calledOnce).to.eql(true);
  });

  it('should call empty $element on transclusion', function() {
    expect($element.empty.calledOnce).to.eql(true);
  });

  it('should append new content to $elmenent on transclusion', function() {
    expect($element.append.calledWith(content)).to.eql(true);
  });
});
