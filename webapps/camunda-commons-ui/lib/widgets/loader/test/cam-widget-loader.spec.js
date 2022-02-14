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
/* global __dirname: false, describe: false, before: false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';
var path = require('path');
var projectRoot = path.resolve(__dirname, '../../../../');
var pkg = require(path.join(projectRoot, 'package.json'));
var pageUrl = 'http://localhost:' + pkg.gruntConfig.connectPort +
              '/lib/widgets/loader/test/cam-widget-loader.spec.html';

var page = require('./cam-widget-loader.page.js');

describe('loader', function() {
  var loader;

  before(function() {
    browser.get(pageUrl + '#interactive-container');
    loader = page.loader('#interactive-container');
  });


  describe('initial state', function() {
    it('hides everything', function() {
      expect(loader.isPresent()).to.eventually.eql(true);
      expect(loader.stateText()).to.eventually.eql('INITIAL');
      expect(loader.defaultPanel().isPresent()).to.eventually.eql(true);
      expect(loader.defaultPanel().isDisplayed()).to.eventually.eql(false);
    });
  });


  describe('loading state', function() {
    it('shows the icon', function() {
      loader.reloadButton().click(function() {
        expect(loader.stateText()).to.eventually.eql('LOADING');
        expect(loader.loadingNotice().isDisplayed()).to.eventually.eql(true);
        expect(loader.defaultPanel().isDisplayed()).to.eventually.eql(false);
      });
    });
  });


  describe('loaded state', function() {
    it('shows the transclusion content', function() {
      loader.reloadButton().click(function() {
        browser.sleep(1200).then(function() {
          expect(loader.stateText()).to.eventually.eql('LOADED');
          expect(loader.loadingNotice().isDisplayed()).to.eventually.eql(false);
          expect(loader.defaultPanel().isDisplayed()).to.eventually.eql(true);
        });
      });
    });
  });


  describe('loaded empty state', function() {
    it('does not show the transclusion content', function() {
      loader.reloadEmptyButton().click(function() {
        browser.sleep(1200).then(function() {
          expect(loader.stateText()).to.eventually.eql('LOADED');
          expect(loader.loadingNotice().isDisplayed()).to.eventually.eql(false);
          expect(loader.defaultPanel().isDisplayed()).to.eventually.eql(false);
        });
      });
    });
  });


  describe('error state', function() {
    it('does not show the transclusion content', function() {
      loader.reloadButton().click(function() {
        loader.failButton().then(function() {
          expect(loader.stateText()).to.eventually.eql('ERROR');
          expect(loader.loadingNotice().isDisplayed()).to.eventually.eql(false);
          expect(loader.defaultPanel().isDisplayed()).to.eventually.eql(false);
          expect(loader.errorNotice().isDisplayed()).to.eventually.eql(true);
        });
      });
    });
  });
});
