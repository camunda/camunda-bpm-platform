  'use strict';

  module.exports = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realInput',
      initialize: function(data) {
        var viewer = data.tableControl.getViewer().table;

        var elementRegistry = viewer.get('elementRegistry');

        var clauseRow = elementRegistry.filter(function(element) {
          return element.isClauseRow;
        })[0];

        if (clauseRow && clauseRow.id) {
          viewer.get('eventBus').on('cell.render', function(event) {

            var inputIdx = data.decisionInstance.inputs.map(function(inputObject) {
              return inputObject.clauseId;
            }).indexOf(event.data.column.id);

            if (event.data.row === clauseRow &&
              inputIdx !== -1) {

              var input = data.decisionInstance.inputs[inputIdx];

              var realInput = document.createElement('span');
              if (input.type !== 'Object' &&
                input.type !== 'Bytes' &&
                input.type !== 'File') {

                realInput.className = 'dmn-input';
                realInput.textContent = ' = ' + input.value;

              } else {
                realInput.className = 'dmn-input-object';
                realInput.setAttribute('title', 'Variable value of type ' + input.type + ' is not shown');
                realInput.textContent = ' = [' + input.type + ']';
              }
              event.gfx.firstChild.appendChild(realInput);
            }
          });

          viewer.get('graphicsFactory').update('row', clauseRow, elementRegistry.getGraphics(clauseRow.id));
        }
      }
    });
  }];
