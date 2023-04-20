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
    return this.historyList()
      .get(item)
      .element(by.binding('event.type'))
      .getText();
  },

  operationTime: function(item) {
    return this.historyList()
      .get(item)
      .element(by.binding('event.time'))
      .getText();
  },

  operationUser: function(item) {
    return this.historyList()
      .get(item)
      .element(by.binding('event.userId'))
      .getText();
  },

  commentMessage: function(item) {
    return this.historyList()
      .get(item)
      .element(by.css('[nl2br="event.message"]'))
      .getText();
  },

  historySubEventList: function(item) {
    return this.historyList()
      .get(item)
      .all(by.repeater('subEvent in event.subEvents'));
  },

  subEventType: function(item, subItem) {
    subItem = subItem || 0;
    return this.historySubEventList(item)
      .get(subItem)
      .element(by.css('.event-property'))
      .getText();
  },

  subEventNewValue: function(item, subItem) {
    subItem = subItem || 0;
    return this.historySubEventList(item)
      .get(subItem)
      .element(by.binding('subEvent.newValue'))
      .getText();
  },

  subEventOriginalValue: function(item, subItem) {
    subItem = subItem || 0;
    return this.historySubEventList(item)
      .get(subItem)
      .element(by.binding('subEvent.orgValue'))
      .getText();
  }
});
