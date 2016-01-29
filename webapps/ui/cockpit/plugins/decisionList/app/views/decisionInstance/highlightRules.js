  'use strict';

  module.exports = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'highlightRules',
      initialize: function(data) {
        for(var i = 0; i < data.decisionInstance.outputs.length; i++) {
          data.tableControl.highlightRow(data.decisionInstance.outputs[i].ruleId, 'fired');
        }
      }
    });
  }];
