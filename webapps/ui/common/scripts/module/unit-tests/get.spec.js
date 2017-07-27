'use strict';

var chai = require('chai');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common get', function() {
  var get;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function($injector) {
    get = $injector.get('get');
  }));

  it('should return value corresponding to path', function() {
    expect(
      get(
        {
          b: {
            c: 1
          }
        },
        ['b', 'c']
      )
    ).to.eql(1);
  });

  it('should return value from nested array', () => {
    expect(
      get(
        {
          b: {
            c: [1, 2, 3]
          }
        },
        ['b', 'c', 2]
      )
    ).to.eql(3);
  });

  it('should return value undefined for incorrect path', () => {
    expect(
      get(
        {
          b: {
            c: [1, 2, 3]
          }
        },
        ['a', 'c', 2]
      )
    ).to.eql(undefined);
  });

  it('should return value default value for incorrect path', () => {
    expect(
      get(
        {
          b: {
            c: [1, 2, 3]
          }
        },
        ['a', 'c', 2],
        'dd'
      )
    ).to.eql('dd');
  });
});
