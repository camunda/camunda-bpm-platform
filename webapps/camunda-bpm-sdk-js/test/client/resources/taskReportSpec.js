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

describe('The Task Report resource usage', function() {
  var TaskReport;

  it('does not blow when loading', function() {
    expect(function() {
      TaskReport = require('./../../../lib/api-client/resources/task-report');
    }).not.to.throw();
  });

  it('has a `path` static property', function() {
    expect(TaskReport.path).to.eql('task/report');
  });

  it('has a `countByCandidateGroup` method', function() {
    expect(TaskReport.countByCandidateGroup).to.be.a('function');
  });

  it('has a `countByCandidateGroupAsCsv` method', function() {
    expect(TaskReport.countByCandidateGroup).to.be.a('function');
  });

  xit('has a `resolve` method', function() {
    expect(TaskReport.resolve).to.be.a('function');
  });


  xit('has a `complete` method', function() {
    expect(TaskReport.complete).to.be.a('function');
  });


  xdescribe('instance', function() {
    var instance;

    it('does not blow when instatiating', function() {
      expect(function() {
        instance = new TaskReport();
        console.info('instance', instance);
      }).not.to.throw();
    });
  });
});
