'use strict';

var Base = require('./base');

var LabelRow = function(node) {
  this.node = node;
};

LabelRow.prototype.getInputText = function(idx) {
  return this.node.all(by.css('td.input')).get(idx).getText();
};

var RuleRow = function(node) {
  this.node = node;
};
RuleRow.prototype.getCellText = function(idx) {
  return this.node.all(by.css('td')).get(idx).getText();
};

module.exports = Base.extend({

  tableElement: function() {
    return element(by.css('[cam-widget-dmn-viewer]'));
  },

  row: function(idx) {
    return this.tableElement().all(by.css('tbody > tr')).get(idx);
  },

  labelRow: function() {
    return new LabelRow(this.tableElement().element(by.css('tr.labels')));
  },

  ruleRow: function(idx) {
    return new RuleRow(this.row(idx));
  },

});
