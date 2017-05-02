'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('camunda-commons-ui/vendor/angular');
var camCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common upload', function() {
  var $rootScope;
  var xhr;
  var requests;
  var upload;
  var files;
  var fields;
  var url;

  beforeEach(module(camCommon.name));

  beforeEach(inject(function($injector) {
    $rootScope = $injector.get('$rootScope');
    upload = $injector.get('upload');

    xhr = sinon.useFakeXMLHttpRequest();
    requests = [];

    xhr.onCreate = function(xhr) {
      requests.push(xhr);
    };

    files = [{
      file: {
        name: 't.txt'
      },
      content: 'content-of-a-file'
    }];

    fields = {
      someField: 'some-value'
    };

    url = 'some-url';
  }));

  afterEach(function() {
    xhr.restore();
  });

  it('should send request with file body', function() {
    upload(url, files);

    expect(requests[0].requestBody).to.contain(files[0].content);
  });

  it('should send fields', function() {
    upload(url, files, fields);

    expect(requests[0].requestBody).to.contain('name="someField"\r\n\r\n' + fields.someField);
  });

  it('should return promise with parsed response', function(done) {
    var expectedResponse = [
      {
        d: 1
      }
    ];

    upload(url, files, fields).then(function(response) {
      expect(response).to.eql(expectedResponse);

      done();
    });

    requests[0].respond(
      200,
      {
        'Content-Type': 'application/json'
      },
      JSON.stringify(expectedResponse)
    );

    $rootScope.$digest();
  });

  it('should broadcast authentication events on 401 response code', function(done) {
    var authChanged = sinon.spy();
    var loginRequired = sinon.spy();

    upload(url, files, fields).catch(function() {
      done();
    });

    requests[0].respond(
      401,
      {
        'Content-Type': 'application/json'
      },
      '""'
    );

    $rootScope.$on('authentication.changed', authChanged);
    $rootScope.$on('authentication.login.required', loginRequired);
    $rootScope.$digest();

    expect(authChanged.calledOnce).to.eql(true);
    expect(loginRequired.calledOnce).to.eql(true);
  });
});
