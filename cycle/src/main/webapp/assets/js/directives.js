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
							      	id :  attrs.id,
							           model: treeModel,
							           openOnClick: true,
								       onClick: function(item){
								    	   scope.selected = item;
								    	   scope.$digest();
							           },
							           showRoot: false,
							           persist: false
							       });
							    tree.placeAt(element[0]);
							    tree.startup();
							},
							function(error){
								console.log("An error occurred: " + error);
								alert(error);
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
    link:  function(scope, element, attrs, ngModel) {
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
 * A directive which conditionally displays a dialog 
 * and allows it to control it via a explicitly specified model.
 * 
 * <dialog model="aModel">
 *   <div class="model">
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
 * 
 * Inside the dialog it exposed the dialog model via the $model directive.
 * 
 * Controllers added inside a dialog may not rely on the normal parent / child scope relationship.
 * Instead they have to await the change of the $scope.data attribute which exposes the dialogs
 * model data to the controllers scope. 
 * 
 * Assume we have a controller <code>ParentController</code> and a controller <code>ChildController</code>. 
 * The dialog controller is defined in a subscope of the parent controller's scope, but inside a <dialog/> tag. 
 * 
 * Let's have a look at the following markup: 
 * 
 * <div ng-controller="ParentController">
 *   <dialog>
 *     <div class="modal" ng-controller="ChildController">
 *       <!-- dialog contents -->
 *     </div>
 *   </dialog>
 * </div>
 * 
 * The following controller definition does not work: 
 * 
 * function ParentController($scope) {
 *   $scope.name = "FOO";
 *   
 *   $scope.childDialog = new Dialog();
 *   // ...
 *   $scope.childDialog.open();
 * }
 * 
 * function ChildController($scope) {
 *   $scope.name; // is undefined, because parent and child are isolated
 * }
 * 
 * Instead the following must be done: 
 * 
 * function ParentController($scope) {
 *   $scope.name = "FOO";
 *   
 *   $scope.childDialog = new Dialog();
 *   // ...
 *   
 *   // Publish the name to the in-dialog controller
 *   $scope.childDialog.open({ name: $scope.name });
 * }
 * 
 * function ChildController($scope) {
 * 
 *   // And wait for the dialog data to initialize, 
 *   // which signals that the elements passed via open(data) was were bound to the current scope
 *   $scope.watch("data", function() {
 *     $scope.name; // "FOO";
 *   });
 * }
 */
.directive('dialog', function($http, $timeout) {
  return {
    restrict: 'E',
    scope: {
      $model: '=model'
    }, 
    transclude: true, 
    template: '<div ngm-if="$model.renderHtml()" ng-transclude />', 
    link:  function(scope, element, attrs) {
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

      function populateScope() {
        var m = model();
        if (m) {
          /**
           * Establish a uni-directional binding between this scope and
           * data defined in the dialog model
           */
          angular.forEach(m.data || {}, function(element, name) {
            console.log("Add scope var", name, element);
            scope[name] = element;
          });
          
          scope.data = m.data || {};
        }
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
            populateScope();
            
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
  self.data = {};
  
  this.open = function(data) {
    if (data) {
      self.data = data;
    }
    
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
