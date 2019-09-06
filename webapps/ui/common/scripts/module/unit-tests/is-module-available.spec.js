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
