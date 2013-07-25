ngDefine('camunda.common.directives', [ 'angular', 'jquery' ], function(module, angular, $) {

  /**
   * A directive which conditionally displays a dialog 
   * and allows it to control it via a explicitly specified model.
   * 
   * <dialog model="aModel">
   *   <div class="model" ngm-if="aModel.renderHtml()">
   *     <!-- dialog contents ... -->
   *   </div>
   * </dialog>
   * 
   * <script>
   *   // outside the dialog
   *   aModel.open(); // openes the dialog (asynchronously)
   *   aModel.close(); // closes the dialog (immediately)
   *   
   *   // Or inside the dialog: 
   *   $model.close();
   * </script>
   */
  var ModalDialogDirective = function ($timeout) {
    return {
      restrict: 'EA',
      scope: {
        $model: '=model'
      }, 
      transclude: true, 
      template: '<div ng-transclude />', 
      link: function(scope, element, attrs) {
        /**
         * Obtain the dialog
         * @returns the dialog instance as a jQuery object
         */
        function dialog() {
          return angular.element(element.find(".modal"));
        }
        
        /**
         * Obtain the dialogs model
         * @returns the dialogs model
         */
        function model() {
          return scope.$model;
        }
        
        /**
         * Init (ie. register events / dialog functionality) and show the dialog.
         * @returns nothing
         */
        function initAndShow() {
          
          var options = model().autoClosable ? {} : {
            backdrop: 'static', 
            keyboard: false
          };
          
          dialog()
            .hide()
            // register events to make sure the model is updated 
            // when things happen to the dialog. We establish a two-directional mapping
            // between the dialog model and the bootstrap modal. 
            .on('hidden', function() {
              // Model is still opened; refresh it asynchronously
              if (model().status != "closed") {
                $timeout(function() {
                  model().setStatus("closed");
                });
              }
            })
            .on('shown', function() {            
              model().setStatus("open");            
            })
            // and show modal
            .modal(options);
        }

        /**
         * Hide (and destroys) the dialog
         * @returns nothing
         */
        function hide() {
          dialog().modal("hide");
        }
              
        /**
         * Watch the $model.status property in order to map it to the 
         * bootstrap modal dialog life cycle. The HTML has to be rendered first, 
         * for the dialog to appear and actual stuff can be done with the dialog.
         */
        scope.$watch("$model.status", function(newValue , oldValue) {
          
          // dialog lifecycle
          // closed -> opening -> open -> closing -> closed
          //            ^ html is about to exist       ^ dialog closed (no html)
          //                       ^ dialog operational and displayed
          //  ^ dialog closed (no html)    ^ dialog closing
          switch (newValue) {
            case "opening": 
              // dialog about to show and markup will be ready, soon
              // asynchronously initialize dialog and register events            
              $timeout(initAndShow);
              break;
            case "closing": 
              hide();
              break;    
          }
        });
      }
    };
  };

  ModalDialogDirective.$inject = [ '$timeout' ];

  module.directive('modalDialog', ModalDialogDirective);
});

/** 
 * Dialog model to be used along with the 
 * dialog directive and attaches it to the given scope
 */
function Dialog() {
  this.status = "closed";
  this.autoClosable = true;
}

Dialog.prototype = {
  
  open: function() {
    this.status = "opening";
  }, 

  close: function() {
    this.status = "closing";
  },

  setStatus: function(status) {
    this.status = status;
  }, 
  
  setAutoClosable: function(closable) {
    this.autoClosable = closable;
  }, 
  
  renderHtml: function() {
    return this.status != "closed";
  }
};