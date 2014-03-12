/* global
    describe: false,
    xdescribe: false,
    ddescribe: false,
    it: false,
    xit: false,
    element: false,
    expect: false,
    by: false,
    browser: false,
    beforeEach: false,
    afterEach: false
*/
/* jshint node: true, unused: false */
describe('cockpit dashboard', function() {
  'use strict';
  var utils = require('./../utils');

  function fullView() {
    var items = element.all(by.repeater('provider in providers'));

    expect(items.get(0).getText()).toEqual('History');
    items.get(0).click();
    expect(items.get(0).getAttribute('class')).toMatch('active');
    expect(items.get(1).getAttribute('class')).not.toMatch('active');
  }

  function liveView() {
    var items = element.all(by.repeater('provider in providers'));

    expect(items.get(1).getText()).toEqual('Runtime');
    items.get(1).click();
    expect(items.get(1).getAttribute('class')).toMatch('active');
    expect(items.get(0).getAttribute('class')).not.toMatch('active');
  }

  function deselectActivityInInstanceTree(activityName) {
    var treeNodeButton = element(by.css('.instance-tree *[id^=' + '"' + activityName + ':"' + '] button'));
    treeNodeButton.click();
  }

  function elementHasClass(selector, className) {
    expect(element(by(selector)).getAttribute('class')).toMatch(className);
  }


// ---- start page ----
  xdescribe('start page', function() {
    it('should validate start page', function() {
      //TODO
    });
  });

// ---- user login ----
  describe('user login', function() {
    it('should validate user credentials', function() {
      //set preconditions
      browser.driver.manage().window().maximize();

      utils.login('jonny1', 'jonny3', false);
      utils.login('jonny1', 'jonny1', true);
    });
  });

// ---- start page plugin view ----
  describe('start page plugin view', function() {
    it('should check existence of plugins', function() {
      var plugins = element.all(by.repeater('dashboardProvider in dashboardProviders'));

      expect(plugins.count()).toBe(2);
      expect(plugins.get(0).findElement(by.css('.page-header')).getText()).toEqual('Deployed Processes (List)');
      expect(plugins.get(1).findElement(by.css('.page-header')).getText()).toEqual('Deployed Processes (Icons)');
    });

    it('should find process in Deployed Processes (List)', function() {
      var items = element(by.repeater('statistic in statistics').row(0).column('definition.name'));

      expect(items.getText()).toEqual('Another Failing Process');
    });

    it('should count running instances of single process', function() {
      var runningInstances = element(by.css('.table'))
                                    .findElement(by.repeater('statistic in statistics').row(0).column('.instances'));

      expect(runningInstances.getText()).toEqual('10');
    });

    xit('should count deployed processes', function() {
      //TODO
    });
  });

// ---- process defintions view
  describe('process defintions view', function() {
    it('it should select process in Deployed Process (List)', function() {
      var items = element(by.repeater('statistic in statistics').row(4).column('definition.name'));

      expect(items.getText()).toEqual('Cornercases Process');

      items.click();


      var processDefintionName = element(by.binding('processDefinition.name'));

      expect(processDefintionName.getText()).toEqual('Cornercases Process');
    });

    it('should validate action buttons', function() {
      var items = element.all(by.repeater('actionProvider in processDefinitionActions'));

      expect(items.count()).toBe(1);
    });

    it('it should select process instance in Process Instances Table', function() {
      var instance = element(by.repeater('processInstance in processInstances').row(1).column('id'));
      var instanceId = instance.getAttribute('title');   //instance.getText();

      instance.click();
      expect(element(by.binding('processInstance.id')).getText()).toBe(instanceId);
    });
  });

// ---- instance detail view
  describe('instance detail view', function() {
    it('should select activity in diagram', function() {
      utils.selectActivityInDiagram('UserTask_2');
      expect(element(by.css('.process-diagram *[data-activity-id="UserTask_2"]'))
                  .getAttribute('class')).toMatch('activity-highlight');
    });

    it('should switch tab in instance details view', function() {
      element(by.repeater('tabProvider in processInstanceTabs').row(3).column('label')).click();

      var tabContent = element(by.css('view[provider=selectedTab]'))
                            .findElement(by.repeater('userTask in userTasks')
                            .row(0).column('name'));
      expect(tabContent.getText()).toEqual('Inner Task');
    });

    it('should dispaly variables of selected activity only', function() {
      element(by.css('view[provider=selectedTab]'))
            .findElement(by.repeater('userTask in userTasks').row(0).column('name')).click();

      var items = element.all(by.repeater('userTask in userTasks'));
      expect(items.count()).toBe(1);
    });

    it('should deselect activity in instance tree', function() {
      deselectActivityInInstanceTree('UserTask_2');
      expect(element(by.css('.process-diagram *[data-activity-id="UserTask_2"]'))
                  .getAttribute('class')).not.toMatch('activity-highlight');
    });

    it('should select Activity in instance details view and highlight element in renderer', function() {
      element(by.css('view[provider=selectedTab]'))
            .findElement(by.repeater('userTask in userTasks').row(5).column('name')).click();

      expect(element(by.css('.process-diagram *[data-activity-id="UserTask_5"]'))
                  .getAttribute('class')).toMatch('activity-highlight');

      var items = element.all(by.repeater('userTask in userTasks'));
      expect(items.count()).toBe(1);
    });

    it('should select Activity in diagram and and count tasks in details view', function() {
      utils.selectActivityInDiagram('UserTask_5');
      expect(element(by.css('.process-diagram *[data-activity-id="UserTask_5"]'))
                .getAttribute('class')).toMatch('activity-highlight');

      var items = element.all(by.repeater('userTask in userTasks'));
      expect(items.count()).toBe(5);

      var tabContent = element(by.css('view[provider=selectedTab]'))
                            .findElement(by.repeater('userTask in userTasks').row(0).column('name'));
      expect(tabContent.getText()).toEqual('Run some service');
    });

    xit('should manage groups for user task', function() {
      //TODO
    });
  });

// ---- history view ----
  describe('history view', function() {
    it('should switch to full view', function() {
      var items = element.all(by.repeater('tabProvider in processInstanceActions'));

      expect(items.count()).toBe(4);
      fullView();
      expect(items.count()).toBe(0);
    });

    it('should switch to live view', function() {
      var items = element.all(by.repeater('tabProvider in processInstanceActions'));

      expect(items.count()).toBe(0);
      liveView();
      expect(items.count()).toBe(4);
    });
  });

  // ---- instance tree view ----
  describe('instance tree view', function() {
    it('should validate activity instance tree filter - sidebar', function() {
      var filterSidebar = element(by.css('.filters'));

      expect(filterSidebar.findElement(by.tagName('h5')).getText()).toBe('Filter');
    });

    it('should validate activity instance tree filter - clear button', function() {
      var filterSidebar = element(by.css('.filters'));

      filterSidebar.findElement(by.model('name')).clear();
      filterSidebar.findElement(by.model('name')).sendKeys('some task');

      //filterSidebar.findElement(by.css('.icon-search')).click(); --> movement of vertical divider bar needed!!!
      filterSidebar.findElement(by.model('name')).clear();
      expect(filterSidebar.findElement(by.model('name')).getText()).not.toBe('some task');
    });

    it('should validate activity instance tree filter - filter activities', function() {
      var filterSidebar = element(by.css('.filters'));
      var filterResult = filterSidebar.findElement(by.css('.filter'));

      filterSidebar.findElement(by.model('name')).clear();
      filterSidebar.findElement(by.model('name')).sendKeys('some task');

      expect(filterResult.getText()).toBe('Nothing selected');

      utils.selectActivityInDiagram('UserTask_2');
      expect(filterResult.getText()).toBe('1 activity instance selected');

      deselectActivityInInstanceTree('UserTask_2');
      expect(filterResult.getText()).toBe('Nothing selected');
    });
  });

  describe('filtering and selection', function() {
    it('goes on a corner cases process instance', function() {
      var url = '/camunda/app/cockpit/default/#/dashboard';

      utils.loginAndGoTo(url);

      element(by.css('a[href*="cornercasesProcess"]'))
        .click();

      element(by.css('.ctn-tabbed-content [ng-repeat="processInstance in processInstances"] a'))
        .click();

      utils.selectActivityInDiagram('UserTask_3');

      utils.waitForElementToBePresent('.filters [type="text"]')
        .then(function() {
          var nameInput = element(by.css('.filters [type="text"]'));
          nameInput.clear();
          nameInput.sendKeys('do some work');

          expect(
            element(by.css('.filters [id^="UserTask_3"]'))
              .findElement(by.css('.tree-node-label'))
                .getAttribute('class')
          ).toMatch('selected');

          utils.selectActivityInDiagram('UserTask_2');

          var treeActivity = element(by.css('.filters [id^="UserTask_2"]'));
          var activityLabel = treeActivity
              .findElement(by.css('.tree-node-label'));

          expect(
            activityLabel
              .isDisplayed()
          ).toBe(true);

          expect(
            activityLabel
              .getAttribute('class')
          ).toMatch('selected');
        });
    });
  });
});
