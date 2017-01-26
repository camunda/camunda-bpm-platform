'use strict';

var chai = require('chai');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
var camCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common params', function() {
  var params;

  beforeEach(module(camCommon.name));

  beforeEach(inject(function(_params_) {
    params = _params_;
  }));

  it('should produce query string from query object', function() {
    var query = {
      a: 1,
      b: 2
    };

    expect(params(query)).to.eql('a=1&b=2');
  });

  it('should encode values', function() {
    var query = {
      a: '{}'
    };

    expect(params(query)).to.eql('a=%7B%7D');
  });
});
