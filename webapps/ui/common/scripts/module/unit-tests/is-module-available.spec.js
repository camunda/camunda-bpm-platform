'use strict';

var chai = require('chai');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common isModuleAvailable service', function() {
  beforeEach(module(drdCommon.name));

  it('should return false when module is not available', function() {
    inject(function(isModuleAvailable) {
      expect(isModuleAvailable('some-non-existing-module')).to.eql(false);
    });
  });

  it('should return true when module is available', function() {
    var mod = 'mod';

    angular.module(mod, []);

    module(mod);

    inject(function(isModuleAvailable) {
      expect(isModuleAvailable(mod)).to.eql(true);
    });
  });
});
