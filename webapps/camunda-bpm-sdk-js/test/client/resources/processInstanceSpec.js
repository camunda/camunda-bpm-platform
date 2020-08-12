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

describe('The ProcessInstance resource', function() {
  var ProcessInstance;

  it('does not blow when loading', function() {
    expect(function() {
      ProcessInstance = require('./../../../lib/api-client/resources/process-instance');
    }).not.to.throw();
  });


  it('has a `path` static property', function() {
    expect(ProcessInstance.path).to.eql('process-instance');
  });


  describe('instance', function() {
    var instance;

    it('does not blow when instanciating', function() {
      expect(function() {
        instance = new ProcessInstance();
      }).not.to.throw();
    });
  });
});
