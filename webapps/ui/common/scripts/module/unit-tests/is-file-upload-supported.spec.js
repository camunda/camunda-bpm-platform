'use strict';

var chai = require('chai');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
var camCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common isFileUploadSupported', function() {
  var FileReader;
  var isFileUploadSupported;

  beforeEach(module(camCommon.name));

  beforeEach(module(function($provide) {
    FileReader = function() {};
    FileReader.prototype.readAsText = function() {};

    var $window = {
      FileReader: FileReader
    };

    $provide.value('$window', $window);
  }));

  beforeEach(inject(function($injector) {
    isFileUploadSupported = $injector.get('isFileUploadSupported');
  }));

  it('should return true if FileRead supports readAsText method', function() {
    expect(isFileUploadSupported()).to.eql(true);
  });

  it('should return true if FileRead does not supports readAsText method', function() {
    delete FileReader.prototype.readAsText;

    expect(isFileUploadSupported()).to.eql(false);
  });
});
