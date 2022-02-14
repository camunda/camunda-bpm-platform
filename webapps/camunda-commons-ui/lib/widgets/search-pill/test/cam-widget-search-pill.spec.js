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
              '/lib/widgets/search-pill/test/cam-widget-search-pill.spec.html';

var page = require('./cam-widget-search-pill.page.js');

describe('Search Pill', function() {
  before(function() {
    browser.get(pageUrl);
  });
  var pill;

  describe('Basic', function() {
    before(function() {
      browser.get(pageUrl + '#basic');
      pill = page.pill('pill1');
    });

    it('should open a dropdown with available types', function() {

      var typeField = pill.typeField();

      typeField.click();
      typeField.click();

      expect(typeField.dropdown().isPresent()).to.eventually.eql(true);
      expect(typeField.dropdownOption(0).getText()).to.eventually.eql('Foo');
      expect(typeField.dropdownOption(1).getText()).to.eventually.eql('Bar');

      typeField.dropdownOption(0).click();

      expect(typeField.dropdown().isPresent()).to.eventually.eql(false);
      expect(typeField.text()).to.eventually.eql('Foo');
    });

    it('should open a dropdown with available operators', function() {

      var operatorField = pill.operatorField();

      operatorField.click();

      expect(operatorField.dropdown().isPresent()).to.eventually.eql(true);
      expect(operatorField.dropdownOption(0).getText()).to.eventually.eql('=');
      expect(operatorField.dropdownOption(1).getText()).to.eventually.eql('!=');

      operatorField.dropdownOption(1).click();

      expect(operatorField.dropdown().isPresent()).to.eventually.eql(false);
      expect(operatorField.text()).to.eventually.eql('!=');
    });

    it('should allow text input', function() {

      var valueField = pill.valueField();

      valueField.click();

      expect(valueField.inputField().isPresent()).to.eventually.eql(true);

      // workaround to check focus
      // see: http://stackoverflow.com/a/22756276
      browser.driver.switchTo().activeElement().getAttribute('ng-model').then(function(focusElement) {
        expect(valueField.inputField().getAttribute('ng-model')).to.eventually.eql(focusElement);
      });
    });

    it('should execute the update function and set the valid property', function() {

      expect(pill.isValid()).to.eventually.eql(false);

      pill.typeField().click();
      pill.typeField().dropdownOption(0).click();
      pill.operatorField().click();
      pill.operatorField().dropdownOption(1).click();
      pill.valueField().click();
      pill.valueField().type('test', protractor.Key.ENTER);

      expect(pill.isValid()).to.eventually.eql(true);
    });
  });

  describe('Date Enforced', function() {
    before(function() {
      browser.get(pageUrl + '#enforcing-dates');
      pill = page.pill('pill2');
    });

    it('should open a datepicker for the value field', function() {

      var valueField = pill.valueField();

      valueField.click();
      valueField.click();

      expect(valueField.datepicker().isPresent()).to.eventually.eql(true);
    });

    it('should format a date', function() {

      var valueField = pill.valueField();

      valueField.click();
      valueField.click();
      valueField.datepicker.day('15').click();
      valueField.okButton().click();

      expect(valueField.text()).to.eventually.include('January 15, 2015');
    });
  });

  describe('Date Allowed', function() {
    before(function() {
      browser.get(pageUrl + '#allow-dates');
      pill = page.pill('pill3');
    });

    it('should offer a calendar option', function() {

      var valueField = pill.valueField();

      valueField.click();
      valueField.click();

      expect(valueField.calendarButton().isPresent()).to.eventually.eql(true);

      valueField.calendarButton().click();

      expect(valueField.datepicker().isPresent()).to.eventually.eql(true);
    });
  });

  describe('Extended', function() {
    beforeEach(function() {
      browser.get(pageUrl + '#extended');
      pill = page.pill('pill4');
    });

    it('should provide a name field', function() {

      expect(pill.nameField().isPresent()).to.eventually.eql(false);

      pill.typeField().click();
      pill.typeField().click();
      pill.typeField().dropdownOption(1).click();

      expect(pill.nameField().isPresent()).to.eventually.eql(true);
    });

    it('name field should be text input', function() {

      pill.typeField().click();
      pill.typeField().click();
      pill.typeField().dropdownOption(1).click();

      pill.nameField().click();

      expect(pill.nameField().inputField().isPresent()).to.eventually.eql(true);

      // workaround to check focus
      // see: http://stackoverflow.com/a/22756276
      browser.driver.switchTo().activeElement().getAttribute('ng-model').then(function(focusElement) {
        expect(pill.nameField().inputField().getAttribute('ng-model')).to.eventually.eql(focusElement);
      });
    });

  });

  describe('Potential Names', function() {
    beforeEach(function() {
      browser.get(pageUrl + '#names');
      pill = page.pill('pill5');
    });

    it('name field should be dropdown', function() {
      var nameField = pill.nameField();

      nameField.click();
      nameField.click();

      expect(nameField.inputField().isPresent()).to.eventually.eql(true);

      expect(nameField.dropdown().isPresent()).to.eventually.eql(true);
      expect(nameField.dropdownOption(0).getText()).to.eventually.eql('Value 1 (name1)');
      expect(nameField.dropdownOption(1).getText()).to.eventually.eql('Value 2 (name2)');

      nameField.dropdownOption(0).click();

      expect(nameField.dropdown().isPresent()).to.eventually.eql(false);
      expect(nameField.text()).to.eventually.eql('Value 1 (name1)');
    });

  });

  describe('Basic Attribute', function() {
    beforeEach(function() {
      browser.get(pageUrl + '#basic-attribute');
      pill = page.pill('pill6');
    });

    it('should only contain a type element', function() {
      expect(pill.typeElement().isPresent()).to.eventually.eql(true);
      expect(pill.nameElement().isPresent()).to.eventually.eql(false);
      expect(pill.operatorElement().isPresent()).to.eventually.eql(false);
      expect(pill.valueElement().isPresent()).to.eventually.eql(false);

    });

  });

  describe('Options Attribute', function() {
    beforeEach(function() {
      browser.get(pageUrl + '#basic-attribute');
      pill = page.pill('pill8');
    });

    it('value field should be dropdown', function() {
      var valueField = pill.valueField();

      valueField.click();
      valueField.click();

      expect(valueField.inputField().isPresent()).to.eventually.eql(true);

      expect(valueField.dropdown().isPresent()).to.eventually.eql(true);
      expect(valueField.dropdownOption(0).getText()).to.eventually.eql('yes');
      expect(valueField.dropdownOption(1).getText()).to.eventually.eql('maybe');
      expect(valueField.dropdownOption(2).getText()).to.eventually.eql('no');

      valueField.dropdownOption(0).click();

      expect(valueField.dropdown().isPresent()).to.eventually.eql(false);
      expect(valueField.text()).to.eventually.eql('yes');
    });
  });

});
