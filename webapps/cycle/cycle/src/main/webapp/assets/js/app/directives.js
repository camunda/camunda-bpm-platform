'use strict';

/* Directives */

angular
.module('cycle.directives', [])
.directive('cycleTree', function(App, Event, $http) {
  return {
    restrict: "A",
    replace: false,
    transclude: false,
    require: '?connector',
    scope: {
      'connector' : "=",
      'selected' : "=",
      'filter' : "&", 
      'id' : "@"
    },
    link: function(scope, element, attrs, model) {
      
      var resourceTypes = scope.filter();
      var filterParam = "";
      
      angular.forEach(resourceTypes, function(e, i) {
        if (!filterParam) {
          filterParam = "type=" + e;
        } else {
          filterParam += "&type=" + e;
        }
      });
      
      /**
       * Pre bootstraping the tree (loading root elements)
       * and displaying the tree to the user
       */
      function preBootstrapTree(Memory, ObjectStoreModel, Tree, Deferred, Observable, registry) {
        
        // id of the current connector
        var connectorId = null;
        
        function removeTree() {
          var treeWidget = registry.byId(attrs.id);
          if (treeWidget) {
            registry.byId(attrs.id).destroy();
            registry.remove(attrs.id);
          }
        }

        function handleTreeError(error) {
          removeTree();
        }

        function getRootContents(connectorId) {
          var deferred = new Deferred();

          $http.get(App.uri("secured/resource/connector/" + connectorId + "/root?" + filterParam))
            .success(function(data, status, headers, config) {
              deferred.resolve(data);
            })
            .error(function(data, status, headers, config) {
              handleTreeError(data);
              deferred.reject(data);
            });

          return deferred.promise;
        }

        function getNodeContents(node) {
          var deferred = new Deferred();
          
          $http.get(App.uri("secured/resource/connector/" + connectorId + "/children?nodeId=" + encodeURI(node.id) + "&" + filterParam))
            .success(function(data, status, headers, config) {
              deferred.resolve(data);
            })
            .error(function(data, status, headers, config) {
              handleTreeError(data);
              deferred.reject(data);
            });
          return deferred.promise;
        }
        
        /**
         * Actual function performing the tree bootstrap
         * after the trees root elements have been loaded
         */
        function bootstrapTree(roots) {
          var memoryStore = new Memory({
            data: roots,
            getChildren: getNodeContents
          });

          // Create the model
          var treeModel = new ObjectStoreModel({
            store: new Observable(memoryStore),
            query: {id: '/'},
            labelAttr : "label",
            mayHaveChildren: function(item) {
              return item.type == "FOLDER";
            }
          });

          // remove old tree
          removeTree();

          var tree = new Tree({
            id: attrs.id,
            model: treeModel,
            openOnClick: false,
            onClick: function(item, node) {
              if (node.isExpandable) {
                this._onExpandoClick({node: node});
              }

              if (item.type == "BPMN_FILE" || item.type == "FOLDER") {
                scope.selected = item;
              } else {
                // FIXME see description above!
                scope.selected = null;
              }
              

              // FIXME digest should to the $apply, 
              // but obviously its not resulting in a model update for the add button binding
              // probably a bug in angularjs ?
              // if you change something here, please test the following:
              // first enter the modeler name, THEN select the file, add button should be enabled
              // not working without $apply after digest, $apply function must be empty also
              scope.$digest();
              scope.$apply();
            },
            showRoot: false,
            persist: false
          });

          tree.placeAt(element[0]);
          tree.startup();
        }

        // actual pre bootstrap code whenever 
        // selected connector changes
        scope.$watch("connector", function (newValue, oldValue) {
          if (!newValue) {
            return;
          }

          if (newValue != oldValue) {
            if (oldValue) {
              scope.$emit(Event.selectedConnectorChanged);
            }
            
            connectorId = newValue.connectorId;
            
            getRootContents(connectorId)
              .then(bootstrapTree, handleTreeError);
          }
        });
      }
      
      // Bootstrap the dojo tree
      require(["dojo/store/Memory",
               "dijit/tree/ObjectStoreModel", 
               "dijit/Tree",
               "dojo/Deferred",
               "dojo/store/Observable",
               "dijit/registry"], preBootstrapTree);
    }
  };
})
.directive("available", function(Debouncer) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function (scope, element, attrs, model) {
      var checkAvailable = scope[attrs.available];
      
      if (!checkAvailable) {
        throw new Error("No availability check #" + attrs.available);
      }
      
      var validateDeferred = Debouncer.debounce(function() {
        var promise = checkAvailable(model.$modelValue);
        
        promise.then(function(available) {
          model.$setValidity("checked", true);
          model.$setValidity("available", available);
        });
      }, 500);
      
      scope.$watch(function() { return model.$modelValue; }, function(newValue) {
        if (newValue) {
          model.$setValidity("checked", false);
          validateDeferred();
        }
      });
    }
  };
})
.directive("matches", function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function (scope, element, attrs, model) {
      
      var match = attrs["matches"];

      function validateMatch(a, b) {
        model.$setValidity("matches", a == b);
      }

      scope.$watch(function() { return model.$modelValue; }, function(newValue) {
        validateMatch(newValue, scope.$eval(match));
      });

      scope.$watch(match, function(newValue) {
        validateMatch(model.$modelValue, newValue);
      });
    }
  };
})
/**
 * Email validation via email attribute
 */
.directive("email", function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function (scope, element, attrs, model) {
      
      var EMAIL_REGEX = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      
      model.$parsers.unshift(function(viewValue) {
        if (EMAIL_REGEX.test(viewValue)) {
          model.$setValidity('email', true);
          return viewValue;
        } else {
          model.$setValidity('email', false);
          return null;
        }
      });
    }
  };
})
.directive('ngCombobox', function(Event) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function (scope, elm, attrs, model) {
      if (model) {
        elm.on(Event.ngChange, function() {
          scope.$apply(function() {
            var input = getInputText();
            if (input) {
              // catch user typed values (not selected ones)
              model.$setViewValue(input.value);
            }
          });
        });
      }

      // value list changed, update combobox
      scope.$watch(attrs.values, function() {
        var comboboxContainer = getComboboxContainer();
        if (comboboxContainer) {
          // combobox already presents, remove old one
          var select = elm.detach();
          $(comboboxContainer).parent().prepend(select);
          $(comboboxContainer).remove();
        }
        // init new combobox
        elm.combobox({
          template: '<div class="combobox-container"><input type="text" autocomplete="off" class="dropdown-toggle" /><span class="" data-dropdown="dropdown"></span></div>'
        });
      });

      // update input with model when default value is set
      scope.$watch(model, function() {
        var input = getInputText();
        if (input) {
          var oldValue = input.value;
          if (oldValue !== model.$modelValue) {
            $(input).val(model.$modelValue);
          }
        }
      });

      // do some cleanup
      scope.$on(Event.destroy, function () {
        $('ul.typeahead.dropdown-menu').each(function(){
          $(this).remove();
        });
        elm.unbind($().combobox());
      });

      // get container which holds the combobox elements
      function getComboboxContainer() {
        var comboboxContainer = elm.parent('.combobox-container');
        if (comboboxContainer.length == 1) {
          return comboboxContainer[0];
        } else {
          return;
        }
      }

      // get combobox's input text
      function getInputText() {
        var comboboxContainer = getComboboxContainer();
        if (comboboxContainer) {
          var input = $(comboboxContainer).children('input')[0];
          if (input) {
            return input;
          }
        }

        return;
      }
    }
  };
})
/**
 * Realizes a bpmn diagram ui component in the roundtrip details dialog.
 * 
 * @param roundtrip reference to the roundtrip the diagram belongs to
 * @param diagram or null the diagram which is managed / displayed
 * @param identifier the identifier of the diagram (eighter leftHandSide or rightHandSide)
 * 
 * Usage:
 * 
 * <bpmn-diagram handle="leftDiagram" roundtrip="myRoundtrip" diagram="myRoundtrip.leftHandSide" identifier="leftHandSide" />
 */
.directive("bpmnDiagram", function(App) {
  return {
    restrict: 'E',
    scope: {
      roundtrip: '=', 
      diagram: '=',
      handle : '@',
      identifier: '@'
    }, 
    templateUrl: App.uri("secured/view/partials/bpmn-diagram.html"),
    controller: 'BpmnDiagramController', 
    link: function(scope, element, attrs) {
      scope.identifier = attrs.identifier;
      
      if (attrs.handle) {
    	  scope.$parent[attrs.handle] = scope;
   	  }
    }
  };
})
.directive("help", function(App) {
  return {
    restrict: 'A',    
    scope : {
		helpText: "@",
		helpTitle: "@", 
		helpTextVar: "&",
		helpTitleVar: "&", 
		colorInvert: "@"
    },
    template: '<span ng-transclude></span><span class="help-toggle"><i class="icon-question-sign" ng-class="colorInvertCls()"></i></span>',
    transclude: true, 		
    link: function(scope, element, attrs) {
      var help = attrs.helpText || scope.helpTextVar, 
          helpTitle = attrs.helpTitle || scope.helpTitleVar, 
          colorInvert = !!attrs.colorInvert;
      
      scope.colorInvertCls = function() {
    	  return (colorInvert ? 'icon-white' : '');
      };
      
      var p = "right";
      if(attrs.helpPlacement) {
        p = scope.$eval(attrs.helpPlacement);
      }
            
      $(element).find(".help-toggle").popover({content: help, title: helpTitle, delay: { show: 0, hide: 0 }, placement: p});
    }
  };
})
.directive("diagramImage", function(App, Commons) {
  return {
    restrict: 'E',
    replace : true,
    scope : {
      diagram: "=",
      status: "=", 
      click: "&"
    },
    templateUrl: App.uri("secured/view/partials/diagram-image.html"),
    link: function(scope, element, attrs) {

      function changeImageStatus(newStatus) {
          scope.status = newStatus;
          
          // FIXME workaround for a angular bug!
          scope.$digest();
          scope.$apply();
      }
      
      function performImageClick() {
        scope.$apply(function() {
          scope.click();
        });
      }
      
      function fixDiagramImageHeight(element) {
        // fix image height if it is higher than the diagram container
        var e = $(element);
        var imgHeight = parseInt(e.css("height"), 10);
        var containerHeight = parseInt(e.parents(".diagram").css("height"), 10);

        if (imgHeight > containerHeight) {
          e.css("height", containerHeight + "px");
        }
      }
      
      // register image load interceptor
      $(element)
        .find("img")
        .css({ width: "auto", height: "auto" })
        .bind({
          load: function() {
            fixDiagramImageHeight(this);
            changeImageStatus("LOADED");
          },
          error: function(){
            changeImageStatus("UNAVAILABLE");
          }, 
          click: performImageClick
        });

      // $scope.checkImageAvailable();
//      scope.checkImageAvailable = function () {
//        if (scope.diagram) {
//          Commons.isImageAvailable(scope.diagram.connectorNode).then(function (data) {
//            scope.imageAvailable = data.available && ((data.lastModified + 5000) >= scope.diagram.lastModified);
//            scope.$emit(Event.imageAvailable, scope.imageAvailable, scope.identifier);
//          });
//        }
//      };

      function updateImage(diagram) {
        scope.status = "LOADING";
        $(element).find("img").attr("src", Commons.getImageUrl(diagram, true));
      };

      scope.$watch("diagram", function (newDiagramValue) {
        if (newDiagramValue && newDiagramValue.id) {
          updateImage(newDiagramValue);
        }
      });

      /**
       * Update image status when it is set back to unknown
       */
      scope.$watch("status", function (newStatus, oldStatus) {
        if (scope.diagram && newStatus == "UNKNOWN" && oldStatus) {
          updateImage(scope.diagram);
        }
      });
    }
  };
})

.directive('ifAdmin', function(Credentials) {
  return {
    restrict: 'A',
    scope: { }, 
    transclude: true, 
    template: '<span ngm-if="isAdmin" ng-transclude></span>', 
    link: function(scope, element, attrs) {
      scope.$watch(Credentials.watchCurrent, function(newValue) {
        scope.isAdmin = Credentials.isAdmin();
      });
    }
  };
})

.directive('reqAware', function(RequestStatus) {
  	
  return {	  
    link: function(scope, element, attrs) {
      
      var DISABLE_AJAX_LOADER = "disableAjaxLoader";
      
      var formName;
      var showAjaxLoader = true;
      
      if (attrs.reqAware) {
        var params = attrs.reqAware.split(",");
        formName = $.trim(params[0]);
        if (params[1] && $.trim(params[1]) == DISABLE_AJAX_LOADER) {
          showAjaxLoader = false;
        }
      };
      
      function setFormValidity(valid) {
          var form  = scope[formName];
          if(!!form) {
            form.$setValidity("request", valid);
          }     
        }
                   
      function setFormFieldsDisabled(disable) {
        var formElement = $('form[name="'+formName+'"]');
      	if(disable) {
      	  $(":input", formElement).attr("disabled", "disabled");     	  
      	} else {
      	  $(":input", formElement).removeAttr("disabled");	
      	}
      }
    	
      scope.$watch(RequestStatus.watchBusy, function(newValue) {
        scope.isBusy = newValue;
        if(scope.isBusy) { 
        	
      	  if(!!formName) {
            setFormValidity(false);       
            setFormFieldsDisabled(true);           
          }
        	
          if ($(element).is("button")) {
            if(!formName) {
              $(element).attr("disabled", "disabled");	  
            }
          }
        
        } else {
        	
          if(!!formName) {
        	setFormValidity(true);       
        	setFormFieldsDisabled(false);  
          }
          	  
          if ($(element).is("button")) {
            if(!formName) {
              $(element).removeAttr("disabled");	  
            }
            if (showAjaxLoader) {
          	  $(".icon-loading", element).remove();
            }
          } 
        }
      });   
      
      if($(element).is("button") && showAjaxLoader) {
        $(element)
        .bind({    
          click: function() {
            $(element).append("<i class=\"icon-loading\" style=\"margin-left:5px\"></i>");
          }
        });
      }
    }
  };
})
.directive('errorPanel', function(Error, App) {
  return {
    link: function(scope, element, attrs, $destroy) {

      $(element).addClass("errorPanel");
    	
      var errorConsumer = function(error) {
    	var html = "<div class=\"alert alert-error\">";
    	html += "<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>";
      	
      	if (error.status && error.config) {
      		html += "<strong>"+error.status+":</strong> ";
      		html += "<span>"+error.config+"</span>";
      		if (error.type == 'org.camunda.bpm.cycle.exception.CycleMissingCredentialsException') {
      		  html += "<span>(<a style=\"color: #827AA2;\" href=\"" + App.uri("secured/view/profile") + "\">add user credentials</a>)</span>";
      		}
      	} else {
      		html += "An error occured, try refreshing the page or relogin.";
      	}
      	
      	html += "</div>";
      	  
      	element.append(html);
      };
      
      Error.registerErrorConsumer(errorConsumer);      
      scope.$on($destroy, function() {
        Error.unregisterErrorConsumer(errorConsumer);
      });
      
    }
  };
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
.directive('modalDialog', function($http, $timeout, Error) {
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
