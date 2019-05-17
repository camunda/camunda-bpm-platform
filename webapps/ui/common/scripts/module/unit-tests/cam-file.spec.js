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
var directiveFactory = require('../directives/cam-file')[1];

describe('cam-common cam-file', function() {
  var files;
  var readFiles;
  var $event;
  var $scope;
  var $element;
  var linkFn;

  beforeEach(function() {
    files = 'files';
    readFiles = sinon.stub().returns({
      then: sinon.stub().callsArgWith(0, files)
    });

    $scope = {
      $apply: sinon.stub().callsArg(0),
      onChange: sinon.spy()
    };

    $event = '$event';
    $element = [
      {
        addEventListener: sinon.stub().callsArgWith(1, $event),
        files: files
      }
    ];

    linkFn = directiveFactory(readFiles).link;

    linkFn($scope, $element);
  });

  it('should add change event listener on element', function() {
    expect($element[0].addEventListener.calledWith('change')).to.eql(true);
  });

  it('should read element files', function() {
    expect(readFiles.calledWith(files)).to.eql(true);
  });

  it('should call onChange function with read files and $event', function() {
    expect(
      $scope.onChange.calledWith({
        $event: $event,
        files: files
      })
    ).to.eql(true);
  });
});
