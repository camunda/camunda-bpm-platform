'use strict';


angular.module('cycle.controllers', []);

function DefaultController($scope, $http, $location, App, Event, Error) {
  $scope.appErrors = function () {
    return Error.errors;
  };
  
  // TODO: get from cookie
  $scope.currentUser = null;
  
  $http.get(App.uri('currentUser')).success(function(data) {
    $scope.currentUser = data;
  });
  
  $scope.$on(Event.userChanged, function(event, user) {
    $scope.currentUser = user;
  });
  
  // Bread Crumb 
  var breadCrumbs = $scope.breadCrumbs = [];

  $scope.$on(Event.navigationChanged, function(event, navigationItem) {
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

function HomeController($scope, Event) {
  $scope.$emit(Event.navigationChanged);
}

function RoundtripDetailsController($scope, $routeParams, RoundtripDetails, Commons, Event) {
  $scope.currentPicture = 'leftHandSide';
  
  $scope.diagramDetailsDialog = new Dialog();
  
  $scope.syncDialog = new Dialog();
  $scope.syncDialog.setAutoClosable(false);

  //get roundtrip details and forward to main page if bad request (ie. invalid id) occurs
  $scope.roundtrip = RoundtripDetails.get({id: $routeParams.roundtripId }, function() {}, function(response) {
    if (response.status == 400) {
      $location.path("/");
    }
  });

  $scope.canSync = function() {
    
    var roundtrip = $scope.roundtrip,
        lhsModeSyncStatus = null,
        rhsModeSyncStatus = null;
    
    if (!roundtrip) {
      return false;
    }
    
    if (roundtrip.rightHandSide && roundtrip.leftHandSide) {
      lhsModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
      rhsModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
      return lhsModeSyncStatus != "UNAVAILABLE" && rhsModeSyncStatus != "UNAVAILABLE"; 
    }
    
    return false;
  };
  
  /**
   * Return true if the managed roundtrip can be created (LTR or RTL)
   */
  $scope.canCreate = function(mode) {
    var sourceModeSyncStatus = null,
        targetModeSyncStatus = null,
        roundtrip = $scope.roundtrip;
    
    if (!roundtrip) {
      return false;
    }
    
    if (mode == "LEFT_TO_RIGHT") {
      if (roundtrip.leftHandSide && !roundtrip.rightHandSide) {
        // added this additional check to avoid an error: roundrip.leftHandSide.syncStatus is undefined
        if (roundtrip.leftHandSide.syncStatus) { 
          sourceModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus != "UNAVAILABLE";
        }
      } else if (roundtrip.leftHandSide && roundtrip.rightHandSide) {
        if (roundtrip.rightHandSide.syncStatus && roundtrip.leftHandSide.syncStatus) {
          sourceModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
          targetModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus == "UNAVAILABLE" && targetModeSyncStatus != "UNAVAILABLE";
        }        
      }
    } else if (mode == "RIGHT_TO_LEFT") {
      if (roundtrip.rightHandSide && !roundtrip.leftHandSide) {
        // added this additional check to avoid an error: roundrip.rightHandSide.syncStatus is undefined
        if (roundtrip.rightHandSide.syncStatus) {
          sourceModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus != "UNAVAILABLE";
        }
      } else if (roundtrip.leftHandSide && roundtrip.rightHandSide) {
        if (roundtrip.leftHandSide.syncStatus && roundtrip.rightHandSide.syncStatus) { 
          sourceModeSyncStatus = roundtrip.leftHandSide.syncStatus.status || "UNAVAILABLE";
          targetModeSyncStatus = roundtrip.rightHandSide.syncStatus.status || "UNAVAILABLE";
          return sourceModeSyncStatus == "UNAVAILABLE" && targetModeSyncStatus != "UNAVAILABLE";
        }
      }
    }
    
    return false;
  };
  
  $scope.activeClass = function(side) {
    return side == $scope.currentPicture ? "active" : "";
  };
  
  function fullScreenShowDiagram(side) {
    $scope.setCurrentPicture(side);
    $scope.diagramDetailsDialog.open();
  }

  $scope.$on(Event.modelImageClicked, function(event, side) {
    fullScreenShowDiagram(side);
  });

  $scope.openSyncDialog = function (syncMode) {
    $scope.syncMode = syncMode;
    $scope.syncDialog.open();
  };
  
  $scope.createNewDiagram = function(diagram) {
    var roundtrip = $scope.roundtrip;
    
    diagram.diagramTemplate = null;
    
    switch (diagram.identifier) {
    case "leftHandSide":
      diagram.diagramTemplate = { label: roundtrip.rightHandSide.label };
      break;
    case "rightHandSide":
      diagram.diagramTemplate = { label: roundtrip.leftHandSide.label };
    }
    
    diagram.editDialogMode = "CREATE_NEW_DIAGRAM";
    diagram.editDiagramDialog.open();
  };

  $scope.delayedSetCurrentPicture = function (picture) {
    setTimeout(function() {
      console.log(picture);
      $scope.setCurrentPicture(picture);
      
      // Same bug as with the tree; DO NOT DELETE the following two lines!
      $scope.$digest();
      $scope.$apply();
    }, 800);
  };
  
  $scope.setCurrentPicture = function (picture) {
    $scope.currentPicture = picture;
  };
}

function SyncRoundtripController($scope, $http, $q, App, Event) {
  
  var SYNC_SUCCESS = "synchronizationSuccess",
      SYNC_FAILED = "synchronizationFailed",
      PERFORM_SYNC = "performSynchronize",
      BEFORE_SYNC = "beforeStart";
  
  $scope.status = BEFORE_SYNC;
  
  $scope.cancel = function () {
    $scope.syncDialog.close();
  };
  
  $scope.syncNoteCls = function(mode) {
    return mode == 'LEFT_TO_RIGHT' ? 'ltr' : 'rtl';
  };
  
  $scope.performSync = function() {
    $scope.status = PERFORM_SYNC;
    
    var Delay = function(delayMs) {
      var deferred = $q.defer();
      
      setTimeout(function() {
        deferred.resolve();
        $scope.$apply();
      }, delayMs);
      
      return deferred.promise;
    };
    
    var delayed = new Delay(2000);
    
    $http.post(App.uri('secured/resource/roundtrip/' + $scope.roundtrip.id + '/sync?syncMode=' + $scope.syncMode)).
      success(function(data) {
        delayed.then(function() {
          $scope.roundtrip.$get({id: $scope.roundtrip.id });
          $scope.status = SYNC_SUCCESS;
        });
      }).error(function (data) {
        delayed.then(function() {
          $scope.status = SYNC_FAILED;
        });
      });
  };
}

/**
 * Works along with the bpmn-diagram directive to manage a single bpmn-diagram in the roundtrip view.
 */
function BpmnDiagramController($scope, Commons, Event, $http, App) {

  var SYNC_SUCCESS = "synchronizationSuccess",
      SYNC_FAILED = "synchronizationFailed";
  
  $scope.imageStatus = "UNKNOWN";
  $scope.modelStatus = "UNKNOWN";

  function changeModelStatus(status) {
    $scope.modelStatus = status;
  }

  $scope.editDiagramDialog = new Dialog();

  $scope.addDiagram = function() {
    $scope.editDialogMode = "ADD_DIAGRAM";
    $scope.editDiagramDialog.open();
  };

  $scope.cancelAddDiagram = function() {
    $scope.editDiagramDialog.close();
  };

  $scope.saveDiagram = function(diagram) {
    $scope.roundtrip[$scope.identifier] = diagram;

    $scope.roundtrip.$save(function() {
      $scope.diagram = $scope.roundtrip[$scope.identifier];
      $scope.editDiagramDialog.close();
    });
  };
  
  $scope.createDiagram = function(diagram) {
    switch ($scope.handle) {
    case "rightDiagram":
      $scope.syncMode = "LEFT_TO_RIGHT";
      break;
    case "leftDiagram":
      $scope.syncMode = "RIGHT_TO_LEFT";
    }
    
    $http.post(App.uri('secured/resource/roundtrip/' + $scope.roundtrip.id + '/create/?diagramlabel=' + diagram.label + '&syncMode=' + $scope.syncMode + '&modeler=' + diagram.modeler + '&connectorId=' + diagram.connectorNode.connectorId + '&parentFolderId=' + diagram.connectorNode.id))
    .success(function(data) {
        $scope.roundtrip.$get({id: $scope.roundtrip.id });
        $scope.status = SYNC_SUCCESS;
    })
    .error(function (data) {
        $scope.status = SYNC_FAILED;
    });
	
	$scope.editDiagramDialog.close();

  };

  $scope.diagramClass = function(diagram) {
    return $scope.modelStatus == "UNAVAILABLE" ? "error" : "";
  };

  $scope.showImage = function(side) {
    $scope.$emit(Event.modelImageClicked, side);
  };

  $scope.$watch("diagram", function(newDiagramValue) {
    // Check availability only when diagram is saved
    if (newDiagramValue && newDiagramValue.id) {
      $scope.checkContentAvailable(newDiagramValue);
    }
  });

 /**
  * Refresh status of the selected diagram. 
  * That includes: 
  *  * Check image availability
  *  * Check synchronization status
  */
  $scope.refreshStatus = function(diagram) {
    $scope.imageStatus = "UNKNOWN";
    $scope.checkContentAvailable(diagram);
  };
  
  $scope.checkContentAvailable = function(diagram) {
    Commons.getDiagramStatus(diagram).success(function(data) {
      $scope.diagram.syncStatus = data;
      console.log($scope.diagram);
      changeModelStatus(data.status);
    });
  };
}

/**
 * Realizes the edit operation of a bpmn diagram inside the respective dialog.
 */
function EditDiagramController($scope,Commons,Event) {
  
  var FOX_DESIGNER = "fox designer", 
      RIGHT_HAND_SIDE = "rightHandSide";
  
  // Error to be displayed in dialog
  $scope.error = null;
  
  // Can the modeler name be edited?
  var canEditModeler = $scope.canEditModeler = function() {
    return !!($scope.identifier != RIGHT_HAND_SIDE || ($scope.editDiagram.modeler && $scope.editDiagram.modeler != FOX_DESIGNER));
  };
    
  // is the dialog model valid and can be submitted?
  var isValid = $scope.isValid = function() {
    var editDiagram = $scope.editDiagram;
    var valid = !!editDiagram.modeler && $scope.addModelForm.$valid && $scope.selectedNode && $scope.selectedNode.type == "BPMN_FILE";
    return valid;
  };
  
  var isValidAndFolder = $scope.isValidAndFolder = function() {
	var editDiagram = $scope.editDiagram;
	var validAndfolder = !!editDiagram.modeler && $scope.addModelForm.$valid && $scope.selectedNode && $scope.selectedNode.type == "FOLDER";
	return validAndfolder;
  };
  
  $scope.acceptedChildTypes = function() {
    if ($scope.editDialogMode == "ADD_DIAGRAM") {
      return [ "PNG_FILE", "BPMN_FILE", "FOLDER" ];
    }
    if ($scope.editDialogMode == "CREATE_NEW_DIAGRAM") {
      return [ "FOLDER" ];
    }
    return [ "ANY_FILE", "FOLDER" ];
  };
  
  $scope.cancel = function() {
    $scope.cancelAddDiagram();
  };
  
  // save the dialog 
  $scope.save = function() {
    if (!isValid()) {
      return;
    }
    $scope.saveDiagram($scope.editDiagram);
  };
  
  $scope.create = function() {
  	  $scope.createDiagram($scope.editDiagram);
  };

  $scope.modelerNames = [];
  $scope.connectors = [];
  
  // make a copy of the diagram to edit / add
  $scope.editDiagram = angular.extend(angular.copy($scope.diagram || {}), $scope.diagramTemplate || {});

  // Watch for component error  
  $scope.$on(Event.componentError, function(event, error) {
    $scope.error = error;
  });
  
  $scope.$on(Event.selectedConnectorChanged, function(event) {
    if ($scope.error) {
      $scope.error = null;
    }
    $scope.selectedNode = null;
  });
  
  // Watch for change in diagram path
  $scope.$watch('selectedNode', function(newValue) {
    if (newValue) {
      $scope.editDiagram.connectorNode = newValue;
    }
  });
  
  // set modeler name as fox designer whenever a right hand side 
  // diagram with no name is edited
  // relaxed implements AT in HEMERA-2549
  if (!canEditModeler()) {
    $scope.editDiagram.modeler = FOX_DESIGNER;
  }
  
  // TODO: nre: On update: How to initially display the right folder structure?
  // 
  // get required data
  Commons.getModelerNames().then(function(data) {
    // filter out FOX_DESIGNER
    for (var i = data.length-1; i >= 0; i--) {
      if (angular.equals(data[i], FOX_DESIGNER)) {
        data.splice(i, 1);
        break;
      }
    }

    $scope.modelerNames = data;
    // set default value
    if (data.length > 0 && canEditModeler()) {
      $scope.editDiagram.modeler = data[0];
    }
  });
  
  $scope.connectors = Commons.getConnectors();
}

/**
 * Responsible for adding a new roundtrip from within the roundtrip list
 * 
 */
function CreateNewRoundtripController($scope, $q, $http, $location, Debouncer, App, Roundtrip, Event) {

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
      $scope.$emit(Event.roundtripAdded, roundtrip);
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
  }, 500);
  
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
function ListRoundtripsController($scope, $routeParams, $http, $location, Roundtrip, Event, App) {
  
  // TODO: Add documentation page
  $scope.roundtrips = Roundtrip.query();
  $scope.newRoundtripDialog = new Dialog();
  
  $scope.deleteRoundtripDialog = new Dialog();
  $scope.deleteRoundtripDialog.setAutoClosable(false);
  
  var selectedRoundtripId = null;
  
  // Update the selected roundtrip on route change
  $scope.$watch(function() { return $routeParams.roundtripId; }, function(newValue, oldValue) {
    if (newValue) {
      selectedRoundtripId = parseInt(newValue);
      if (isNaN(selectedRoundtripId)) {
        selectedRoundtripId = -1;
      } else {
        angular.forEach($scope.roundtrips, function(item) {
          if (item.id == $routeParams.roundtripId) {
            // find the roundtripname for this roundtrip-id
            $scope.$emit(Event.navigationChanged, {name:item.name});
          }
        });
      }
    } else {
      selectedRoundtripId = -1;
    }
  });
  
  $scope.createNew = function() {
    $scope.newRoundtripDialog.open();
  };
  
  $scope.deleteRoundtrip = function() {
     $scope.deleteRoundtripDialog.open();
  };
  
  $scope.activeClass = function(roundtrip) {
    return (roundtrip.id == selectedRoundtripId ? 'active' : '');
  };
  
  $scope.$on(Event.roundtripAdded, function(event, roundtrip) {
    $scope.roundtrips.push(roundtrip);
  });
  
};

/**
 * Responsible to delete roundtrips
 * 
 */
function DeleteRoundtripController($scope, $routeParams, $http, $location, App) {
	
  var PERFORM_DEL = "performRoundtripDeletion",
  	  DEL_SUCCESS = "deletionSuccess",
  	  DEL_FAILED = "deletionFailed";
  
  $scope.toBeDeleted = PERFORM_DEL;
  
  function findRoundtripById(roundtrips, roundtripId) {
	  var roundtrip = null;
	  
	  angular.forEach(roundtrips, function(e, i) {
		  if (e.id == roundtripId) {
			  roundtrip = e;
		  }
	  });
	  
	  return roundtrip;
  }
  
  $scope.performDeletion = function() {
	if (!$routeParams.roundtripId) {
		return;
	}
	
	var roundtrip = findRoundtripById($scope.roundtrips, $routeParams.roundtripId);
	
    $http.post(App.uri("secured/resource/roundtrip/" + $routeParams.roundtripId + "/delete"))    
    .success(function(data) {
    	 $scope.toBeDeleted = DEL_SUCCESS;
    	 $scope.roundtrips.splice($scope.roundtrips.indexOf(roundtrip), 1);
		 $location.path("/");
	})
	.error(function(data) {
		$scope.toBeDeleted = DEL_FAILED;
	});
  };
};

function ConnectorSetupController($scope, App, Event) {
	$scope.createNewConnectorDialog = new Dialog();
	
	$scope.$emit(Event.navigationChanged, {name:"Connector setup"});
	
	$scope.createNewConnector = function() {
		$scope.createNewConnectorDialog.open();
	};
	
};

function CreateNewConnectorController($scope, App) {
	
};
