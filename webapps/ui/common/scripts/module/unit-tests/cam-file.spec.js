'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var directiveFactory = require('../directives/cam-file')[1];

describe('cam-common cam-file', function() {
  var files;
  var readFiles;
  var $event;
  var $scope;
  var $element;
  var linkFn;

  beforeEach(function() {
    files = 'files';
    readFiles = sinon.stub().returns({
      then: sinon.stub().callsArgWith(0, files)
    });

    $scope = {
      $apply: sinon.stub().callsArg(0),
      onChange: sinon.spy()
    };

    $event = '$event';
    $element = [
      {
        addEventListener: sinon.stub().callsArgWith(1, $event),
        files: files
      }
    ];

    linkFn = directiveFactory(readFiles).link;

    linkFn($scope, $element);
  });

  it('should add change event listener on element', function() {
    expect($element[0].addEventListener.calledWith('change')).to.eql(true);
  });

  it('should read element files', function() {
    expect(readFiles.calledWith(files)).to.eql(true);
  });

  it('should call onChange function with read files and $event', function() {
    expect($scope.onChange.calledWith({
      $event: $event,
      files: files
    })).to.eql(true);
  });
});
