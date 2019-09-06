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
