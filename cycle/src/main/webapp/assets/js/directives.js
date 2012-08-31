'use strict';

/* Directives */

angular
.module('cycle.directives', [])
.directive('cycleTree', function(app) {
	return {
		restrict: "A",
		replace: false,
		transclude: false,
		require: '?connector',
		scope: {
			'connector' : "=",
			'selected' : "=",
			'id' : "@"	
		},
		link: function(scope, element, attrs, model) {
			
			require(["dojo/ready", 
			         "dojo/_base/window",
			         "dojo/_base/array",
			         "dojo/store/Memory",
			         "dijit/tree/ObjectStoreModel", 
			         "dijit/Tree",
			         "dojo/store/Observable",
			         "dojo/request",
			         "dijit/registry"], function(ready, window, array, Memory, ObjectStoreModel, Tree, Observable, request, registry) {
				ready(function () {
					
					scope.$watch("connector", function (newValue , oldValue) {
				    	if (newValue != undefined && newValue != oldValue) {
				    		
							request.get(app.uri("secured/resource/connector/" + newValue.connectorId + "/tree/root"), {
					            handleAs: "json"
					        }).then(function(requestData){
								
								var memoryStore = new Memory({
							        data: requestData,
							        getChildren: function(object) {
							        	return request.post(app.uri("secured/resource/connector/" + newValue.connectorId + "/tree/children"), {
								            data : {"parent" : object.id, "parentPath" : object.path},
							        		handleAs: "json"
								        }).then(function(childData){
								        	/**
								        	 * Dojo Tree will behave strange / loop forever without id attribute
								        	 */
								        	//array.forEach(childData, function (entry, index) {
								        	//	entry["id"] = entry["name"];
								        	//});
								        	return childData;
                        },
                        function(error) {
                          var e = error.response.data;
                          e.component = "tree";
                          scope.$emit("component-error", e);
                        });
							        }
							    });
								
								var observableStore = new Observable(memoryStore);
								
								// Create the model
							    var treeModel = new ObjectStoreModel({
							        store: observableStore,
							        query: {id: '/'},
							        labelAttr : "label",
							        mayHaveChildren: function(item){
							            return item.type=="FOLDER";
							        }
							    });
							    
							    var treeWidget = registry.byId(attrs.id);
							    if (treeWidget != undefined) {
							    	registry.byId(attrs.id).destroy();
	                                registry.remove(attrs.id);
							    }
							    
							    var tree = new Tree({
                    id: attrs.id,
                    model: treeModel,
                    openOnClick: true,
                    onClick: function(item){
                      scope.$apply(function() {
                        scope.selected = item;
                      });
                    },
                    showRoot: false,
                    persist: false
                  });
                  
							    tree.placeAt(element[0]);
							    tree.startup();
							},
  							function(error){
                  var e = error.response.data;
                  e.component = "tree";
                  scope.$emit("component-error", e);
  							});
				    	}
				    });
				});
			});
		}
	};
})
.directive('typeahead', function($http) {
  return {
    restrict: 'A',
    require: 'ngModel',
    scope: {
      values: '='
    },
    link: function(scope, element, attrs, ngModel) {
      var typeahead = element.typeahead({
        source: scope.values,
        updater: function(item) {
          scope.$apply(read(item));
          return item;
        }
      });
      
      // update model with selected value
      function read(item) {
        ngModel.$modelValue = item;
      }

      scope.$watch("values", function(newValue , oldValue) {
        typeahead.data('typeahead').source = newValue;
      });
    }
  };
})
/**
 * 
 */
.directive("bpmnDiagram", function(app) {
  return {
    restrict: 'E',
    scope: {
      roundtrip: '=', 
      diagram: '=', 
      identifier: '@'
    }, 
    templateUrl: app.uri("secured/view/partials/bpmn-diagram.html"), 
    controller: 'BpmnDiagramController', 
    link: function(scope, element, attrs) {
      scope.identifier = attrs.identifier;
    }
  }
})

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
.directive('dialog', function($http, $timeout) {
  return {
    restrict: 'E',
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
        dialog()
          .hide()
          // register events to make sure the model is updated 
          // when things happen to the dialog. We establish a two-directional mapping
          // between the dialog model and the bootstrap modal. 
          .on('hidden', function() {
            // Model is still opened; refresh it asynchronously
            if (model().status != "closed") {
              $timeout(function() {
                model().status = "closed";
              });
            }
          })
          .on('shown', function() {
            model().status = "open";
          })
          // and show modal
          .modal();
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
       * bootstrap modal dialog live cycle. The HTML has to be rendered first, 
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
  }
});

/** 
 * Dialog model to be used along with the 
 * dialog directive and attaches it to the given scope
 */
function Dialog() {
  
  var self = this;
  self.status = "closed";
  
  this.open = function() {
    self.status = "opening";
  };

  this.close = function() {
    self.data = {};
    self.status = "closing";
  };

  this.renderHtml = function() {
    return self.status != "closed";
  }
};
