'use strict';

var Tab = require('./tab');

module.exports = Tab.extend({

  tabIndex: 1,

  historyFormElement: function() {
    return element(by.css('[class="history-pane ng-scope"]'));
  },

  historyList: function() {
    return this.historyFormElement().all(by.repeater('event in day.events'));
  },

  eventType: function(item) {
    return this.historyList().get(item).element(by.binding('event.type')).getText();
  },

  operationTime: function(item) {
    return this.historyList().get(item).element(by.binding('event.time')).getText();
  },

  operationUser: function(item) {
    return this.historyList().get(item).element(by.binding('event.userId')).getText();
  },

  commentMessage: function(item) {
    return this.historyList().get(item).element(by.css('[nl2br="event.message"]')).getText();
  },

  historySubEventList: function(item) {
    return this.historyList().get(item).all(by.repeater('subEvent in event.subEvents'));
  },

  subEventType: function(item, subItem) {
    subItem = subItem || 0;
    return this.historySubEventList(item).get(subItem).element(by.css('.event-property')).getText();
  },

  subEventNewValue: function(item, subItem) {
    subItem = subItem || 0;
    return this.historySubEventList(item).get(subItem).element(by.binding('subEvent.newValue')).getText();
  },

  subEventOriginalValue: function(item, subItem) {
    subItem = subItem || 0;
    return this.historySubEventList(item).get(subItem).element(by.binding('subEvent.orgValue')).getText();
  }

});