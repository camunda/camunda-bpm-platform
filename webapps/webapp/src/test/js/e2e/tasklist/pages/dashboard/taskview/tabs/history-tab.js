'use strict';

var Page = require('./../current-task');

module.exports = Page.extend({

  historyFormElement: function() {
    return element(by.css('[class="history-pane ng-scope"]'));
  },

  historyList: function() {
    return this.historyFormElement().all(by.repeater('event in day.events'));
  },

  getHistoryEventType: function(item) {
    return this.historyList().get(item).element(by.binding('event.type')).getText();
  },

  getHistoryOperationUser: function(item) {
    return this.historyList().get(item).element(by.binding('event.userId')).getText();
  },

  getHistoryCommentMessage: function(item) {
    return this.historyList().get(item).element(by.css('[nl2br="event.message"]')).getText();
  },

  historySubEventList: function(item) {
    return this.historyList().get(item).all(by.repeater('subEvent in event.subEvents'));
  },

  getHistoryAssignee: function(item) {
    return this.historySubEventList(item).get(0).element(by.binding('subEvent.orgValue')).getText();
  },

  getHistoryClaimee: function(item) {
    return this.historySubEventList(item).get(0).element(by.binding('subEvent.newValue')).getText();
  }

});