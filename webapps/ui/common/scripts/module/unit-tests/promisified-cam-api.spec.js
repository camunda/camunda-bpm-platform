'use strict';

var chai = require('chai');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
var createCamApiMock = require('../../../unit-tests/create-cam-api-mock');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common promisifiedCamApi service', function() {
  var $rootScope;
  var camAPI;
  var promisifiedCamAPI;

  beforeEach(module(function($provide) {
    camAPI = createCamApiMock();

    $provide.value('camAPI', camAPI);
  }));

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function(_$rootScope_, _promisifiedCamAPI_) {
    $rootScope = _$rootScope_;
    promisifiedCamAPI = _promisifiedCamAPI_;
  }));

  describe('resource', function() {
    var resource;

    beforeEach(function() {
      resource = promisifiedCamAPI.resource('whatever');
    });

    it('should call the resource method of camAPI', function() {
      expect(camAPI.resource.calledOnce).to.be.eql(true);
    });

    it('should promisify the fake resouce', function(done) {
      resource
        .get(12)
        .then(function(result) {
          expect(result).to.eql([12]);

          done();
        });

      $rootScope.$digest();
    });
  });
});
