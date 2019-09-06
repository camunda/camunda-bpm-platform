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

describe('cam-common isFileUploadSupported', function() {
  var FileReader;
  var isFileUploadSupported;

  beforeEach(module(camCommon.name));

  beforeEach(
    module(function($provide) {
      FileReader = function() {};
      FileReader.prototype.readAsText = function() {};

      var $window = {
        FileReader: FileReader
      };

      $provide.value('$window', $window);
    })
  );

  beforeEach(inject(function($injector) {
    isFileUploadSupported = $injector.get('isFileUploadSupported');
  }));

  it('should return true if FileRead supports readAsText method', function() {
    expect(isFileUploadSupported()).to.eql(true);
  });

  it('should return true if FileRead does not supports readAsText method', function() {
    delete FileReader.prototype.readAsText;

    expect(isFileUploadSupported()).to.eql(false);
  });
});
