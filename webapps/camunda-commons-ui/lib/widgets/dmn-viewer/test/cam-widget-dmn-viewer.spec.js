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

/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, before: false, beforeEach: false, it: false,
          browser: false, element: false, expect: false, by: false, protractor: false */
'use strict';
var path = require('path');
var projectRoot = path.resolve(__dirname, '../../../../');
var pkg = require(path.join(projectRoot, 'package.json'));
var pageUrl = 'http://localhost:' + pkg.gruntConfig.connectPort +
              '/lib/widgets/dmn-viewer/test/cam-widget-dmn-viewer.spec.html';

var page = require('./cam-widget-dmn-viewer.page.js');

describe('Dmn Viewer', function() {
  beforeEach((function() {
    browser.get(pageUrl);
  }));

  describe('table display', function() {
    var table;

    beforeEach(function() {
      table = page.table('viewer1');
    });

    it('should display a table', function() {
      expect(table.isPresent()).to.eventually.eql(true);
    });
  });

  describe('drd display', function() {
    var drd;

    beforeEach(function() {
      drd = page.drdDiagram('example-4');
    });

    it('should display a table', function() {
      expect(drd.isPresent()).to.eventually.eql(true);
    });
  });
});
