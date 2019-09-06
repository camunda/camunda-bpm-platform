/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var expect = require('chai').expect;

var request = require('superagent');
var mockConfig = require('../superagent-mock-config');

var requestListener = require('../helper/request');

describe('The SDK core', function() {

  var superagentMock;
  before(function() {
    superagentMock = require('superagent-mock')(request, mockConfig);
  });

  after(function() {
    superagentMock.unset();
  });

  var CamSDK, camClient, ProcessDefinition, processDefinition;

  it('does not blow when loading', function() {
    expect(function() {
      CamSDK = require('./../../lib/index');
    }).not.to.throw();
  });


  it('initializes', function() {
    expect(function() {
      camClient = new CamSDK.Client({
        apiUri: 'engine-rest/engine'
      });
    }).not.to.throw();
  });


  it('uses the mock HttpClient', function(done) {
    expect(function() {
      ProcessDefinition = camClient.resource('process-definition');
    }).not.to.throw();

    expect(ProcessDefinition.http).to.not.be.undefined;

    ProcessDefinition.list({
      nameLike: 'Bar'
    }, function(err, results) {
      expect(err).to.be.null;

      expect(results.count).to.not.be.undefined;

      expect(Array.isArray(results.items)).to.eql(true);

      done();
    });
  });

  it('has a baseUrl', function() {
    var baseUrl = ProcessDefinition.http.config.baseUrl;
    expect(baseUrl).to.eql('engine-rest/engine/engine/default');
  });

  it('returns promises', function() {
    expect(function() {
      ProcessDefinition = camClient.resource('process-definition');
    }).not.to.throw();

    return ProcessDefinition.list({nameLike: 'Bar'}).then(
      function(results) {
        expect(results.count).to.not.be.undefined;
        expect(Array.isArray(results.items)).to.eql(true);
      }
    );
  });


  it('has resources', function() {
    expect(function() {
      processDefinition = new ProcessDefinition();
    }).not.to.throw();

    expect(processDefinition.http).to.not.be.undefined;
    expect(processDefinition.http).to.eql(ProcessDefinition.http);
  });

  describe('http-client', function() {

    before(function() {
      requestListener.patchRequest(request);
    });

    it('uses default header when no custom headers provided', function(done) {

      camClient = new CamSDK.Client({
        apiUri: 'engine-rest/engine'
      });

      ProcessDefinition = camClient.resource('process-definition');

      var check = function(request) {
        expect(request.headers).to.eql({
          'Accept': 'application/hal+json, application/json; q=0.5'
        });
      };

      requestListener.register(check);
      ProcessDefinition.list({}, function() {
        requestListener.unregister(check);
        done();
      });
    });

    it('uses custom header when provided', function(done) {

      var header = {
        'Accept': 'text/plain',
        'Foo': 'Bar'
      };

      camClient = new CamSDK.Client({
        apiUri: 'engine-rest/engine',
        headers: header
      });

      ProcessDefinition = camClient.resource('process-definition');

      var check = function(request) {
        expect(request.headers).to.eql(header);
      };

      requestListener.register(check);
      ProcessDefinition.list({}, function() {
        requestListener.unregister(check);
        done();
      });
    });

    it('uses custom header for single request', function(done) {

      var header = {
        'Accept': 'text/plain',
        'Foo': 'Bar'
      };

      camClient = new CamSDK.Client({
        apiUri: 'engine-rest/engine'
      });

      var check = function(request) {
        expect(request.headers).to.eql(header);
      };
      requestListener.register(check);

      camClient.http.get('foo', {
        data: {},
        headers: header,
        accept: header.Accept,
        done: function() {
          requestListener.unregister(check);
          done();
        }
      });
    });
  });
});

describe('The custom configured SDK core', function() {

  var baseUrl = 'engine-rest/engine';
  var mockConfig = require('../superagent-mock-config');
  var superagentMock;

  before(function() {
    mockConfig[0].pattern = baseUrl + '/(.*)';
    superagentMock = require('superagent-mock')(request, mockConfig);
  });

  after(function() {
    superagentMock.unset();
  });

  var CamSDK, camClient, ProcessDefinition;

  it('does not blow when loading', function() {
    expect(function() {
      CamSDK = require('./../../lib/index');
    }).not.to.throw();
  });


  it('initializes', function() {
    expect(function() {
      camClient = new CamSDK.Client({
        engine: false,
        apiUri: baseUrl
      });
    }).not.to.throw();
  });

  it('has an unmodified baseUrl', function() {
    ProcessDefinition = camClient.resource('process-definition');
    expect(ProcessDefinition.http.config.baseUrl).to.eql(baseUrl);
  });

  it('uses the absolut apiUri', function(done) {
    ProcessDefinition.list({
      nameLike: 'Bar'
    }, function(err, results) {
      expect(err).to.be.null;

      expect(results.count).to.not.be.undefined;

      expect(Array.isArray(results.items)).to.eql(true);

      done();
    });
  });
});
