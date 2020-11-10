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
              '/lib/widgets/search/test/cam-widget-search.spec.html';

var page = require('./cam-widget-search.page.js');

function parseQS(str) {
  return (function(a) {
    if (a === '') return {};
    var b = {};
    for (var i = 0; i < a.length; ++i) {
      var p = a[i].split('=');
      if (p.length != 2) continue;
      b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, ' '));
    }
    return b;
  })(str.split('&'));
}

function clearPills(example) {
  if (!example) return;

  example.node.all(by.css('.remove-search')).then(function(els) {
    els.forEach(function(el) {
      el.click();
    });
  });
}

describe('Search Widget', function() {

  beforeEach(function() {
    browser.get(pageUrl +'#example1');
  });

  // this is a problem encounter on IE11
  it('should not show a dropdown at initialization', function() {
    expect(page.example('example1').inputDropdown().isDisplayed()).to.eventually.eql(false);
  });

  it('should show dropdown on click', function() {
    page.example('example1').searchInput().click();
    expect(page.example('example1').inputDropdown().isDisplayed()).to.eventually.eql(true);
  });

  it('should create search pill on select in dropdown', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(2).click();
    expect(page.example('example1').searchPills().count()).to.eventually.eql(1);
  });

  it('should focus the value input of a newly created search pill', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    expect(page.example('example1').searchPill(0).valueField().inputField().isPresent()).to.eventually.eql(true);

    browser.driver.switchTo().activeElement().getAttribute('ng-model').then(function(val) {
      expect(page.example('example1').searchPill(0).valueField().inputField().getAttribute('ng-model')).to.eventually.eql(val);
    });
  });

  it('should focus the value input of a newly created search pill', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(2).click();
    expect(page.example('example1').searchPill(0).nameField().inputField().isPresent()).to.eventually.eql(true);

    browser.driver.switchTo().activeElement().getAttribute('ng-model').then(function(val) {
      expect(page.example('example1').searchPill(0).nameField().inputField().getAttribute('ng-model')).to.eventually.eql(val);
    });
  });

  it('should select the next invalid search pill on enter', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();

    page.example('example1').searchPill(1).valueField().type('nowValid', protractor.Key.ENTER);

    expect(page.example('example1').searchPill(0).valueField().inputField().isPresent()).to.eventually.eql(true);

    browser.driver.switchTo().activeElement().getAttribute('ng-model').then(function(val) {
      expect(page.example('example1').searchPill(0).valueField().inputField().getAttribute('ng-model')).to.eventually.eql(val);
    });
  });

  it('should return valid and all searches', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();

    page.example('example1').searchPill(1).valueField().type('nowValid', protractor.Key.ENTER);

    expect(page.example('example1').allSearchesCount()).to.eventually.eql('2');
    expect(page.example('example1').validSearchesCount()).to.eventually.eql('1');
  });

  it('should store valid searches in the URL', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    page.example('example1').searchPill(0).valueField().type('nowValid', protractor.Key.ENTER);

    browser.getCurrentUrl().then(function(url) {
      browser.get(url);
      expect(page.example('example1').searchPills().count()).to.eventually.eql(1);
    });
  });

  it('should adjust searches on changes in the URL', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    page.example('example1').searchPill(0).valueField().type('nowValid', protractor.Key.ENTER);

    browser.getCurrentUrl().then(function(url) {
      expect(url).to.contain('nowValid');

      var location = url.substr(url.indexOf('#') + 2);

      location = location.replace('nowValid', 'anotherString');

      browser.setLocation(location).then(function() {
        expect(page.example('example1').searchPill(0).valueField().text()).to.eventually.eql('anotherString');
      });

    });
  });

  it('should retail invalid searches when adjusting searches on changes in the URL', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    page.example('example1').searchPill(0).valueField().type('nowValid', protractor.Key.ENTER);

    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();

    browser.getCurrentUrl().then(function(url) {
      expect(url).to.contain('nowValid');

      var location = url.substr(url.indexOf('#') + 2);

      location = location.replace('nowValid', 'anotherString');

      browser.setLocation(location).then(function() {
        expect(page.example('example1').searchPills().count()).to.eventually.eql(2);
      });

    });
  });

  it('should use default type', function() {
    var input = 'I am ignoring the typeahead';

    page.example('example1').searchInput().click();
    page.example('example1').searchInput().sendKeys(input, protractor.Key.ENTER);

    expect(page.example('example1').searchPill(0).typeField().text()).to.eventually.eql('Predefined Operators');
    expect(page.example('example1').searchPill(0).valueField().text()).to.eventually.eql(input);
  });

  it('should display operators depending on value type', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(2).click();

    var pill = page.example('example1').searchPill(0);

    // boolean
    pill.valueField().click();
    pill.valueField().type('true', protractor.Key.ENTER);
    expect(pill.operatorField().isPresent()).to.eventually.eql(true);
    expect(pill.operatorField().text()).to.eventually.eql('=');

    // number
    pill.valueField().click();
    pill.valueField().type('4', protractor.Key.ENTER);
    pill.operatorField().click();
    expect(pill.operatorField().dropdownOptionCount()).to.eventually.eql(4);

    // undefined
    pill.valueField().click();
    pill.valueField().clear().type(protractor.Key.ENTER);
    pill.operatorField().click();
    expect(pill.operatorField().dropdownOptionCount()).to.eventually.eql(2);
  });

  it('should store valid searches for multiple widget instances', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(0).click();
    page.example('example1').searchPill(0).valueField().type('nowValidSearch1', protractor.Key.ENTER);

    page.example('example2').searchInput().click();
    page.example('example2').inputDropdownOption(0).click();
    page.example('example2').searchPill(0).valueField().type('nowValidSearch2', protractor.Key.ENTER);

    browser.getCurrentUrl().then(function(url) {
      browser.get(url);
      expect(page.example('example1').searchPill(0).valueField().text()).to.eventually.eql('nowValidSearch1');
      expect(page.example('example2').searchPill(0).valueField().text()).to.eventually.eql('nowValidSearch2');
    });
  });

  it('should add a valid search pill with type basic', function() {
    page.example('example1').searchInput().click();
    page.example('example1').inputDropdownOption(3).click();

    expect(page.example('example1').validSearchesCount()).to.eventually.eql('1');
  });

  describe('Groups', function() {
    it('should show all available groups initially', function() {
      page.example('example2').searchInput().click();
      expect(page.example('example2').inputDropdownOptionCount()).to.eventually.eql(3);
    });

    it('should show only matching options in the input dropdown', function() {
      page.example('example2').searchInput().click();
      page.example('example2').inputDropdownOption(0).click();
      page.example('example2').searchInput().click();

      expect(page.example('example2').inputDropdownOption(0).getText()).to.eventually.eql('A');
      expect(page.example('example2').inputDropdownOption(1).getText()).to.eventually.eql('C');
    });

    it('should allow type change of existing search pill only within valid group', function() {
      page.example('example2').searchInput().click();
      page.example('example2').inputDropdownOption(1).click();
      page.example('example2').searchPill(0).typeField().click();

      expect(page.example('example2').searchPill(0).typeField().dropdownOption(0).getText()).to.eventually.eql('B');
      expect(page.example('example2').searchPill(0).typeField().dropdownOption(1).getText()).to.eventually.eql('C');
    });

    it('should update allowed groups', function() {
      page.example('example2').searchInput().click();
      page.example('example2').inputDropdownOption(1).click();

      page.example('example2').searchPill(0).typeField().click();

      expect(page.example('example2').searchPill(0).typeField().dropdownOptionCount()).to.eventually.eql(2);

      page.example('example2').searchPill(0).typeField().dropdownOption(1).click();
      page.example('example2').searchPill(0).typeField().click();

      expect(page.example('example2').searchPill(0).typeField().dropdownOptionCount()).to.eventually.eql(3);

      page.example('example2').searchPill(0).typeField().dropdownOption(0).click();
      page.example('example2').searchPill(0).typeField().click();

      expect(page.example('example2').searchPill(0).typeField().dropdownOptionCount()).to.eventually.eql(2);
    });
  });


  describe('local persitence', function() {
    it('is not accessible when the widget does not have search pill and no record can be found', function() {
      expect(page.example('example1').storageDropdownButton().isEnabled()).to.eventually.eql(false);
    });


    it('is accessible when the widget has at least 1 search pill', function() {
      var example = page.example('example1');

      example.searchInput().click();
      example.inputDropdownOption(3).click();
      expect(example.storageDropdownButton().isEnabled()).to.eventually.eql(true);
      example.searchPill(0).removeButton().click();
      expect(example.storageDropdownButton().isEnabled()).to.eventually.eql(false);
    });


    it('allows to save a set of search criteria', function() {
      browser.executeScript('localStorage.removeItem("camunda")');

      var example = page.example('example1');

      example.searchInput().click();
      example.inputDropdownOption(0).click();

      var pill = example.searchPill(0);
      pill.valueField().type('something', protractor.Key.ENTER);

      example.searchInput().click();
      example.inputDropdownOption(3).click();

      example.storageDropdownButton().click();
      expect(example.storageDropdownMenu().isDisplayed()).to.eventually.eql(true);

      var input = example.storageDropdownInput();
      var btn = example.storageDropdownInputButton();

      expect(input.isDisplayed()).to.eventually.eql(true);
      expect(input.isEnabled()).to.eventually.eql(true);
      expect(btn.isEnabled()).to.eventually.eql(false);


      input.sendKeys('tractor');
      expect(btn.isEnabled()).to.eventually.eql(true);

      btn.click();
      expect(example.storageDropdownMenu().isDisplayed()).eventually.eql(true);

      var items = example.storageDropdownMenuItems();
      expect(items.count()).to.eventually.eql(3);

      var label = example.storageDropdownMenuItemName(2);
      expect(label.getText()).to.eventually.eql('tractor');

      browser.executeScript('return JSON.parse(localStorage.getItem("camunda"))')
        .then(function(result) {
          expect(result.searchCriteria.search1.tractor).to.be.an('array');
        })
      ;
    });


    it('can restore a set of saved criteria', function() {
      var example = page.example('example1');

      clearPills(example);

      expect(example.searchPills().count()).to.eventually.eql(0);

      expect(example.storageDropdownButton().isEnabled()).to.eventually.eql(true);
      example.storageDropdownButton().click();

      var items = example.storageDropdownMenuItems();
      expect(items.count()).to.eventually.eql(3);

      var label = example.storageDropdownMenuItemName(2);
      expect(label.getText()).to.eventually.eql('tractor');
      label.click();

      browser.getCurrentUrl().then(function(url) {
        var query = parseQS(url.split('?').pop());
        var criteria = JSON.parse(decodeURIComponent(query.search1Query));
        expect(criteria).to.be.an('array');
        expect(criteria).to.be.have.length(2);
        expect(criteria[0]).to.have.keys([
          'type',
          'operator',
          'value',
          'name'
        ]);
        expect(criteria[0].type).to.eql('PredefinedOperators');
        expect(criteria[0].operator).to.eql('eq');
        expect(criteria[0].value).to.eql('something');

        expect(criteria[1].type).to.eql('basicQuery');
        expect(criteria[1].operator).to.eql('eq');
      });
    });


    it('allows to drop a set of search criteria', function() {
      var example = page.example('example1');
      expect(example.storageDropdownButton().isEnabled()).to.eventually.eql(true);
      example.storageDropdownButton().click();

      var items = example.storageDropdownMenuItems();
      expect(items.count()).to.eventually.eql(3);

      example.storageDropdownMenuItemRemove(2).click();

      items = example.storageDropdownMenuItems();
      expect(items.count()).to.eventually.eql(1);

      browser.executeScript('return JSON.parse(localStorage.getItem("camunda"))')
        .then(function(result) {
          expect(result.searchCriteria.search1).to.be.an('object');
          expect(Object.keys(result.searchCriteria.search1)).to.have.length(0);
        })
      ;
    });


    describe('with groups', function() {
      var example, input, ddBtn, saveBtn;

      before(function() {
        example = page.example('example2');
      });

      it('stores the sets in separate groups', function() {
        browser.executeScript('localStorage.removeItem("camunda")')
          .then(function(res) {
            input = example.storageDropdownInput();
            ddBtn = example.storageDropdownButton();
            saveBtn = example.storageDropdownInputButton();



            clearPills(example);

            example.searchInput().click();
            example.inputDropdownOption(0).click();
            example.searchPill(0).valueField().type('X', protractor.Key.ENTER);

            example.searchInput().click();
            example.inputDropdownOption(1).click();
            example.searchPill(1).valueField().type('Z', protractor.Key.ENTER);


            ddBtn.click();
            input.sendKeys('A1');
            saveBtn.click();



            clearPills(example);

            example.searchInput().click();
            example.inputDropdownOption(0).click();
            example.searchPill(0).valueField().type('X', protractor.Key.ENTER);

            ddBtn.click();
            input.sendKeys('A2');
            saveBtn.click();




            clearPills(example);

            example.searchInput().click();
            example.inputDropdownOption(1).click();
            example.searchPill(0).valueField().type('Y', protractor.Key.ENTER);

            ddBtn.click();
            input.sendKeys('B1');
            saveBtn.click();



            clearPills(example);



            browser.executeScript('return JSON.parse(localStorage.getItem("camunda"))')
              .then(function(result) {
              // console.info('result', result);
                expect(result.searchCriteria).to.have.keys(['A', 'B', 'search1']);
                expect(result.searchCriteria.A).to.have.keys(['A1', 'A2']);
                expect(result.searchCriteria.B).to.have.keys(['B1']);
              })
            ;
          });

      });
    });
  });
});
