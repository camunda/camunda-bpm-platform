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
              '/lib/widgets/inline-field/test/cam-widget-inline-field.spec.html';

var page = require('./cam-widget-inline-field.page.js');

describe('Inline Edit Field', function() {
  var field;

  describe('Text Input', function() {
    before(function() {
      browser.get(pageUrl + '#text-edit');
      field = page.field('field1');
    });

    it('should apply a value on Enter', function() {
      var text = 'My Name';

      field
        .click()
        .click()
        .type(text, protractor.Key.ENTER);

      expect(field.text()).to.eventually.eql(text);
    });

    it('should apply a value on click on apply', function() {
      var text = 'My Other Name';

      field
        .click()
        .clear()
        .type(text)
        .okButton().click();

      expect(field.text()).to.eventually.eql(text);
    });

    it('should cancel the edit of value on click on cancel', function() {

      field.text().then(function(textBefore) {
        var text = 'more text';

        field
          .click()
          .type(text);

        expect(field.inputText()).to.eventually.eql(text);

        field.cancelButton().click();

        expect(field.text()).to.eventually.eql(textBefore);
      });
    });

    it('should cancel the edit of value on click outside the input field', function() {

      field.text().then(function(textBefore) {
        var text = 'more text';

        field
          .click()
          .type(text);

        expect(field.inputText()).to.eventually.eql(text);

        page.body().click();

        expect(field.text()).to.eventually.eql(textBefore);
      });
    });

  });

  describe('Date Input', function() {
    before(function() {
      browser.get(pageUrl + '#datepicker');
      field = page.field('field2');
    });

    it('should open and close a datepicker widget', function() {

      field.click();
      field.click();

      expect(field.datepicker().isPresent()).to.eventually.eql(true);

      page.body().click();

      expect(field.datepicker().isPresent()).to.eventually.eql(false);
    });

    it('should apply a date', function() {

      field.click();

      field.datepicker.day('11').click();
      field.timepicker.hoursField().clear().sendKeys('3');
      field.timepicker.minutesField().clear().sendKeys('14');

      field.okButton().click();

      expect(field.text()).to.eventually.eql('January 11, 2015 3:14 AM');
    });


  });

  describe('Options', function() {
    before(function() {
      browser.get(pageUrl + '#options');
      field = page.field('field3');
    });


    it('should show a dropdown with options', function() {

      field.click();
      field.click();

      expect(field.dropdown().isPresent()).to.eventually.eql(true);
      expect(field.dropdownOption(0).getText()).to.eventually.eql('foobar');
      expect(field.dropdownOption(1).getText()).to.eventually.eql('1');
      expect(field.dropdownOption(2).getText()).to.eventually.eql('2');
      expect(field.dropdownOption(3).getText()).to.eventually.eql('3');

      page.body().click();

      expect(field.dropdown().isPresent()).to.eventually.eql(false);
    });

    it('should apply when clicking on an option', function() {

      expect(field.text()).to.eventually.eql('foobar');

      field
        .click()
        .click()
        .dropdownOptionByText('2').click();

      expect(field.text()).to.eventually.eql('2');
    });

    it('should ignore values not in the options array', function() {
      field
        .click()
        .clear()
        .type('4', protractor.Key.ENTER);
      expect(field.text()).to.eventually.not.eql('4');
    });
  });

  describe('Options with allow non options', function() {
    before(function() {
      browser.get(pageUrl + '#options-allow');
      field = page.field('field6');
    });

    it('should apply when clicking on an option', function() {
      field.click().click().dropdownOptionByText('3').click();

      expect(field.text()).to.eventually.eql('3');
    });

    it('should apply entered values', function() {
      field
        .click()
        .clear()
        .type('4', protractor.Key.ENTER);
      expect(field.text()).to.eventually.eql('4');
    });
  });

  describe('Key Value Options', function() {
    before(function() {
      browser.get(pageUrl + '#options-key-value');
      field = page.field('field4');
    });


    it('should show values when open', function() {

      field.click();
      field.click();

      expect(field.dropdown().isPresent()).to.eventually.eql(true);
      expect(field.dropdownOption(0).getText()).to.eventually.eql('Barfoo');
      expect(field.dropdownOption(1).getText()).to.eventually.eql('One');
      expect(field.dropdownOption(2).getText()).to.eventually.eql('Two');
      expect(field.dropdownOption(3).getText()).to.eventually.eql('Three');

      page.body().click();

      expect(field.dropdown().isPresent()).to.eventually.eql(false);
    });

    it('should show keys and values when closed', function() {

      expect(field.text()).to.eventually.eql('foobar : Barfoo');

      field
        .click()
        .dropdownOptionByText('Three').click();

      expect(field.text()).to.eventually.eql('3 : Three');
    });
  });

  describe('Flexible Field', function() {
    before(function() {
      browser.get(pageUrl + '#flexible-combo');
      field = page.field('field5');
    });


    it('should allow toggling between text und datetime', function() {

      field.click();
      field.click();

      expect(field.inputField().isPresent()).to.eventually.eql(true);
      expect(field.datepicker().isDisplayed()).to.eventually.eql(false);

      field.calendarButton().click();

      expect(field.inputField().isPresent()).to.eventually.eql(false);
      expect(field.datepicker().isDisplayed()).to.eventually.eql(true);

      field.pencilButton().click();

      expect(field.inputField().isPresent()).to.eventually.eql(true);
      expect(field.datepicker().isDisplayed()).to.eventually.eql(false);

      page.body().click();

      expect(field.inputField().isPresent()).to.eventually.eql(false);
    });

    it('should allow editing a date in text mode', function() {

      field.click();
      field.calendarButton().click();
      field.datepicker.day('27').click();
      field.timepicker.hoursField().clear().sendKeys('8');
      field.timepicker.minutesField().clear().sendKeys('57');
      field.okButton().click();
      field.click();
      field.pencilButton().click();

      expect(field.inputText()).to.eventually.contain('27T08:57');

      page.body().click();
    });

    it('should apply a valid text date to the datepicker', function() {

      field
        .click()
        .clear()
        .type('2015-11-19T18:17:29', protractor.Key.ENTER)
        .click()
        .calendarButton().click();

      expect(field.datepicker().getText()).to.eventually.contain('November 2015');
      expect(field.datepicker.activeDay()).to.eventually.eql('19');

      // hours field depends on timezone setting of the execution environment
      // expect(field.timepicker.hoursValue()).to.eventually.eql('18');

      expect(field.timepicker.minutesValue()).to.eventually.eql('17');
    });

  });
});
