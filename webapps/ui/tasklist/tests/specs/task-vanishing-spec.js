'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./task-vanishing-setup');
var dashboardPage = require('../pages/dashboard');

describe('Task Vanishing Spec', function() {
  describe('a task that has been removed or completed', function() {
    var tasklistItems;

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');

        tasklistItems = element.all(by.css('.tasks-list > li'));
        tasklistItems.first().element(by.css('.clickable')).click();
      });
    });


    it('disapears from list and shows a notification', function() {
      expect(tasklistItems.count()).to.eventually.eql(1);
      expect(element(by.css('.task-details .names > h2')).getText()).to.eventually.eql('Task 1');

      // simulates a background resolution
      browser.executeAsyncScript(function() {
        var callback = arguments[arguments.length - 1];
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
          if (xhr.readyState == 4) {
            callback();
          }
        };
        xhr.open('POST', '/camunda/api/engine/engine/default/task/1/submit-form', true);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.send('{"variables":{}}');
      })
      .then(function() {
        browser.sleep(2500);

        // refresh!
        tasklistItems = element.all(by.css('.tasks-list > li'));
        expect(tasklistItems.count()).to.eventually.eql(1);

        expect(element(by.css('.task-details .names > h2')).isPresent()).to.eventually.eql(true);

        var status = element(by.css('.page-notifications .status'));
        expect(status.isPresent()).to.eventually.eql(true);
        expect(status.getText()).to.eventually.eql('The task does not exist anymore');

        var dismissButton = element(by.css('.page-notifications a.dismiss'));
        dismissButton.click();

        // refresh!
        tasklistItems = element.all(by.css('.tasks-list > li'));
        expect(tasklistItems.count()).to.eventually.eql(0);

        expect(element(by.css('.task-details .names > h2')).isPresent()).to.eventually.eql(false);
      });
    });
  });
});
