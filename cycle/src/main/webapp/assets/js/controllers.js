'use strict';


angular.module('cycle.controllers', []);

function DefaultController($scope, $http, $location, App) {
  
  // TODO: get from cookie
  $scope.currentUser = null;
  
  $http.get(App.uri('currentUser')).success(function(data) {
    $scope.currentUser = data;
  });
  
  $scope.$on("cycle.userChanged", function(event, user) {
    $scope.currentUser = user;
  });
  
  // Bread Crumb 
  var breadCrumbs = $scope.breadCrumbs = [];
  	
  $scope.$on("navigation-changed", function(evt, navigationItem) {
    if(navigationItem==undefined) {
      breadCrumbs.splice(0, breadCrumbs.length);
    } else {
      var contains = false;
      var remove = 0;
      angular.forEach(breadCrumbs, function(item) {
        if(item.name == navigationItem.name) {
          contains = true;			
        }
        if(item.href.indexOf($location.path()) != 0) {
          remove++;
        }
      });

      for (var i = 0; i < remove; i++) {
        breadCrumbs.pop();						
      }

      if(!contains) {
        breadCrumbs.push({name:navigationItem.name, href:$location.path()});
      }		
    }
  });
  // end Bread Crumb
};

function HomeController($scope, $routeParams) {
  $scope.$emit("navigation-changed");
}

function RoundtripDetailsController($scope, $routeParams, RoundtripDetails, app, $http) {
  $scope.roundtrip = RoundtripDetails.get({id: $routeParams.roundtripId });
};

/**
 * Works along with the bpmn-diagram directive to manage a single bpmn-diagram in
 * the roundtrip view. 
 */
function BpmnDiagramController($scope, $http) {
  $scope.editDiagramDialog = new Dialog();
  
  $scope.addDiagram = function() {
    $scope.editDiagramDialog.open();
  };
 
  $scope.cancelAddDiagram = function() {
    $scope.editDiagramDialog.close();
  };
  
  $scope.saveDiagram = function(diagram) {
    $scope.roundtrip[$scope.identifier] = diagram;
    
    $scope.roundtrip.$save(function() {
      $scope.editDiagramDialog.close();
    });
  };
}

/**
 * Realizes the edit operation of a bpmn diagram inside the respective 
 * dialog. 
 */
function EditDiagramController($scope, $q, $http, Debouncer, App) {
  
  var FOX_DESIGNER = "fox designer", 
    RIGHT_HAND_SIDE = "rightHandSide";

  // Can the modeler name be edited?
  var canEditModeler = $scope.canEditModeler = function() {
    return !!($scope.identifier != RIGHT_HAND_SIDE || ($scope.editDiagram.modeler && $scope.editDiagram.modeler != FOX_DESIGNER));
  };
  
  function getModelerNames() {
    $http.get(App.uri('secured/resource/diagram/modelerNames')).success(function(data) {
      $scope.modelerNames = data;
      // set default value, when only one entry
      if (data.length == 1 && canEditModeler()) {
        $scope.editDiagram.modeler = data[0];
      }
    });
  }

  function getConnectors() {
    $http.get(App.uri("secured/resource/connector/list")).success(function(data) {
      $scope.connectors = data;
    });
  }
    
  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    var editDiagram = $scope.editDiagram;
    return !!editDiagram.modeler && ($scope.addModelForm.$valid !== false) && (editDiagram.diagramPath ? editDiagram.diagramPath.type == 'FILE' : false);
  };
  
  $scope.cancel = function() {
    $scope.cancelAddDiagram();
  };
  
  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    
    // Update diagram path
    $scope.editDiagram.diagramPath.connectorId = $scope.connector.connectorId;
    
    $scope.saveDiagram($scope.editDiagram);
  };

  $scope.modelerNames = [];
  $scope.connectors = [];
  
  // make a copy of the diagram to edit / add
  $scope.editDiagram = angular.copy($scope.diagram || {});

  // Watch for component error  
  $scope.$on("component-error", function(event, error) {
    $scope.error = error;
  });
  
  // Watch for change in diagram path
  $scope.$watch('editDiagram.diagramPath', function(newValue) {
    if (newValue) {
      console.log("editDiagram.diagramPath: " + newValue.name)
    }
  });
  
  // set modeler name as fox designer whenever a right hand side 
  // diagram with no name is edited
  // relaxed implements AT in HEMERA-2549
  if (!canEditModeler()) {
    console.log("SET FOX DESIGNER AS MODELLER");
    $scope.editDiagram.modeler = FOX_DESIGNER;
  }
  
  // Error to be displayed in dialog
  $scope.error = null;
  
  // TODO: nico.rehwaldt: On update: How to initially display the right folder structure?
  // 
  // get required data
  getModelerNames();
  getConnectors();
}

/**
 * Responsible for adding a new roundtrip from within the 
 * roundtrip list
 * 
 */
function CreateNewRoundtripController($scope, $q, $http, $location, Debouncer, App, Roundtrip) {

  $scope.name = '';
  $scope.nameChecked = false;
  
  $scope.errorClass = function(form) {
    return form.$valid || !form.$dirty ? '' : 'error';
  };
  
  // watch the name to check its validity on change
  $scope.$watch('name', function(newValue, oldValue) {
    $scope.nameChecked = false;
    $scope.newRoundtripForm.name.$setValidity("unused", true);
    checkName(newValue, oldValue);
  });
  
  // cancel the add operation áka close the dialog
  $scope.cancel = function() {
    $scope.newRoundtripDialog.close();
  };
  
  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    return $scope.newRoundtripForm.$valid && $scope.nameChecked;
  };
  
  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    
    var roundtrip = new Roundtrip({ name: $scope.name });
    
    // redirect to created roundtrip after save and close dialog
    roundtrip.$save(function() {
      $scope.newRoundtripDialog.close();
      
      $location.path("/roundtrip/" + roundtrip.id);
      $scope.$emit("roundtrip-added", roundtrip);
    });
  };
  
  /**
   * Checking the name of an argument maximum every 1000ms
   * and update the model respectively
   */
  var checkName = Debouncer.debounce(function(name) {
    isNameValid(name).then(function(valid) {
      $scope.nameChecked = true;
      
      if ($scope.newRoundtripForm.name) {
        $scope.newRoundtripForm.name.$setValidity("unused", valid);
      }
    });
  }, 1000);
  
  /**
   * Checks the validity of a name in the backend.
   * Returns a promise which is fulfilled when the check was done. 
   * 
   * Usage: 
   * isNameValid("Walter").then(function(nameOk) {
   *   console.log("Name 'Walter' is ok? ", nameOk);
   * });
   * 
   * @param name to be checked
   * 
   * @returns promise to be fulfilled when the check was done
   */ 
  function isNameValid(name) {
    var deferred = $q.defer();
    
    if (!name || name == "") {
      deferred.resolve(true);
    } else {
      $http.get(App.uri("secured/resource/roundtrip/isNameValid?name=" + name)).success(function(data) {
        deferred.resolve(data == "true");
      });
    }
    
    return deferred.promise;
  }
};

/**
 * Responsible for listing the roundtrips and updating the currently selected one
 * 
 */
function ListRoundtripsController($scope, $route, $routeParams, Roundtrip) {
  $scope.roundtrips = Roundtrip.query();
  $scope.newRoundtripDialog = new Dialog();
  
  var selectedRoundtripId = -1; 
  
  // Update the selected roundtrip on route change
  $scope.$watch(function() { return $routeParams.roundtripId; }, function(newValue, oldValue) {
    selectedRoundtripId = parseInt(newValue);    
    if ($routeParams.roundtripId != undefined) {
      angular.forEach($scope.roundtrips, function(item) {
        if (item.id == $routeParams.roundtripId) {
          // find the roundtripname for this roundtrip-id
          $scope.$emit("navigation-changed", {name:item.name});
        }
      });
    }
  });
  
  $scope.createNew = function() {
    $scope.newRoundtripDialog.open();
  };
  
  $scope.activeClass = function(roundtrip) {
    return (roundtrip.id == selectedRoundtripId ? 'active' : '');
  };
  
  $scope.$on("roundtrip-added", function(event, roundtrip) {
    $scope.roundtrips.push(roundtrip);
  });
};
