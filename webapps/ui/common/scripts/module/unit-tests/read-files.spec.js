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
var sinon = require('sinon');
var angular = require('../../../../../camunda-commons-ui/vendor/angular');
var camCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common readFiles', function() {
  var $rootScope;
  var readAsText;
  var reader;
  var $window;
  var readFiles;

  beforeEach(module(camCommon.name));

  beforeEach(
    module(function($provide) {
      readAsText = sinon.spy();

      $window = {
        FileReader: function() {
          reader = this;

          this.readAsText = readAsText;
        }
      };

      $provide.value('$window', $window);
    })
  );

  beforeEach(inject(function($injector) {
    $rootScope = $injector.get('$rootScope');
    readFiles = $injector.get('readFiles');
  }));

  it('should read given files as text', function() {
    var files = ['1.txt', '2.txt'];

    readFiles(files);

    expect(readAsText.calledWith(files[0])).to.eql(true);
    expect(readAsText.calledWith(files[1])).to.eql(true);
  });

  it('should return resolved promise when file loaded', function(done) {
    var content = 'd';
    var file = '1.txt';

    readFiles([file]).then(function(files) {
      expect(files[0].file).to.eql(file);
      expect(files[0].content).to.eql(content);

      done();
    });

    reader.onload({
      target: {
        result: content
      }
    });

    $rootScope.$digest();
  });

  it('should return rejected promise when file loading fails', function(done) {
    var error = 'd';
    var file = '1.txt';

    readFiles([file]).catch(function(_error) {
      expect(_error).to.eql(error);

      done();
    });

    reader.onerror(error);

    $rootScope.$digest();
  });
});
