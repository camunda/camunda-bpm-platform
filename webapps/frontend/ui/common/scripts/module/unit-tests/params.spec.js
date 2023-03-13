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

var chai = require('chai');
var expect = chai.expect;
var angular = require('../../../../../camunda-commons-ui/vendor/angular');
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
