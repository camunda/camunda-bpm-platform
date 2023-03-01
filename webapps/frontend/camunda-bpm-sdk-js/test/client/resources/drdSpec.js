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
var sinon = require('sinon');
var DRD = require('../../../lib/api-client/resources/drd');

describe('DRD Resource', function() {
  var http;
  var done = function() {};
  var params = {
    a: 1
  };

  beforeEach(function() {
    http = {
      get: sinon.spy()
    };

    DRD.http = http;
  });

  it('should use decision-requirements-definition path', function() {
    expect(DRD.path).to.eql('decision-requirements-definition');
  });

  describe('count', function() {
    it('should use first argument as done callback if it is function', function() {
      DRD.count(done);

      var usedDone = http.get.getCall(0).args[1].done;

      expect(usedDone).to.eql(done);
    });

    it('should use given params', function() {
      DRD.count(params, done);

      var usedParams = http.get.getCall(0).args[1].data;

      expect(usedParams).to.eql(params);
    });

    it('should use /count path', function() {
      DRD.count(params, done);

      expect(http.get.calledWith(DRD.path + '/count')).to.eql(true);
    });
  });

  describe('list', function() {
    it('should use first argument as done callback if it is function', function() {
      DRD.list(done);

      var usedDone = http.get.getCall(0).args[1].done;

      expect(usedDone).to.eql(done);
    });

    it('should use given params', function() {
      DRD.list(params, done);

      var usedParams = http.get.getCall(0).args[1].data;

      expect(usedParams).to.eql(params);
    });

    it('should use path', function() {
      DRD.list(params, done);

      expect(http.get.calledWith(DRD.path)).to.eql(true);
    });
  });

  describe('get', function() {
    var id = 'id23';

    beforeEach(function() {
      DRD.get(id, done);
    });

    it('should add id to path', function() {
      expect(http.get.calledWith(DRD.path + '/' + id)).to.eql(true);
    });

    it('should pass done callback', function() {
      var usedDone = http.get.getCall(0).args[1].done;

      expect(usedDone).to.eql(done);
    });
  });

  describe('getXML', function() {
    var id = 'id23';

    beforeEach(function() {
      DRD.getXML(id, done);
    });

    it('should add id to path', function() {
      expect(http.get.calledWith(DRD.path + '/' + id + '/xml')).to.eql(true);
    });

    it('should pass done callback', function() {
      var usedDone = http.get.getCall(0).args[1].done;

      expect(usedDone).to.eql(done);
    });
  });

  describe('getByKey', function() {
    var key = 'id23';
    var tenantId = 'dd';

    beforeEach(function() {
      DRD.getByKey(key, tenantId, done);
    });

    it('should use correct path', function() {
      expect(http.get.calledWith(DRD.path + '/key/' + key + '/tenant-id/' + tenantId)).to.eql(true);
    });

    it('should pass done callback', function() {
      var usedDone = http.get.getCall(0).args[1].done;

      expect(usedDone).to.eql(done);
    });

    it('should tenant-id should be optional', function() {
      http.get.reset();
      DRD.getByKey(key, done);

      var usedDone = http.get.getCall(0).args[1].done;

      expect(http.get.calledWith(DRD.path + '/key/' + key )).to.eql(true);
      expect(usedDone).to.eql(done);
    });
  });


  describe('getXMLByKey', function() {
    var key = 'id23';
    var tenantId = 'dd';

    beforeEach(function() {
      DRD.getXMLByKey(key, tenantId, done);
    });

    it('should use correct path', function() {
      expect(http.get.calledWith(DRD.path + '/key/' + key + '/tenant-id/' + tenantId + '/xml')).to.eql(true);
    });

    it('should pass done callback', function() {
      var usedDone = http.get.getCall(0).args[1].done;

      expect(usedDone).to.eql(done);
    });

    it('should tenant-id should be optional', function() {
      http.get.reset();
      DRD.getXMLByKey(key, done);

      var usedDone = http.get.getCall(0).args[1].done;

      expect(http.get.calledWith(DRD.path + '/key/' + key + '/xml')).to.eql(true);
      expect(usedDone).to.eql(done);
    });
  });
});
