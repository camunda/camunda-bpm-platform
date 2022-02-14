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

/* jshint ignore:start */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./decision-setup');

var dashboardPage = require('../pages/dashboard');
var decisionsPage = require('../pages/decisions');

describe('Sidebar Maximize/Restore Spec', function() {
  var diagram;

  before(function() {
    return testHelper(setupFile.setup1, function() {
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.authentication.userLogin('admin', 'admin');
      dashboardPage.goToSection('Decisions');
      decisionsPage.deployedDecisionsList.decisionName(0);
      decisionsPage.deployedDecisionsList.selectDecision(0);

      browser.sleep(1000);

      diagram = element(by.css('[decision-table]'));
    });
  });

  it('maximize and restore element with controll buttons', function(done) {
    var start = getDimensions(diagram);

    clickMaximizeBtn();
    browser.sleep(1000); //wait for animation to end

    var maximized = getDimensions(diagram);

    clickRestoreBtn();
    browser.sleep(1000); //wait for animation to end

    var restored = getDimensions(diagram);

    protractor.promise
      .all([start, maximized, restored])
      .then(function(dimensions) {
        var start = dimensions[0];
        var maximized = dimensions[1];
        var restored = dimensions[2];

        expect(start).to.eql(restored);
        expect(maximized.width).to.be.above(start.width);
        expect(maximized.height).to.be.above(start.height);

        done();
      });
  });
});

function clickMaximizeBtn() {
  element(by.css('.maximize-collapsable')).click();
}

function clickRestoreBtn() {
  element(by.css('.restore-collapsable')).click();
}

function getDimensions(element) {
  var width = element.getCssValue('width');
  var height = element.getCssValue('height');

  return protractor.promise.all([width, height]).then(function(dimensions) {
    return {
      width: extractPxNumber(dimensions[0]),
      height: extractPxNumber(dimensions[1])
    };
  });
}

function extractPxNumber(value) {
  var pattern = /^([0-9]+)/;
  var matched = value.match(pattern);

  if (!matched) {
    return;
  }

  return +matched[1];
}
