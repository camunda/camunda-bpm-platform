'use strict';

ngDefine('cockpit.services', function(module) {
  
  var Service = function($filter) { 

    /**
     * Travers over the activityInstances and collect in an array
     * for each activity id the corresponding activityInstances.
     *
     * @return a map which contains for each activity id the activity instances.
     **/
    function aggregateActivityInstances(activityInstances) {
      var result = {};
      aggregateActivityInstancesHelper(activityInstances, result);
      return result;
    }

    /**
     * Helper to travers over the activityInstances.
     *
     **/
    function aggregateActivityInstancesHelper(activityInstance, result) {
      var children = activityInstance.childActivityInstances;
      if (children) {
        for (var i = 0; i < children.length; i++) {
          var child = children[i];
          aggregateActivityInstancesHelper(child, result);
          
          var mappings = result[child.activityId];
          if (!mappings) {
            mappings = [];
            result[child.activityId] = mappings;
          }
          mappings.push(child);
        }
      }

      var transitions = activityInstance.childTransitionInstances;
      if (transitions) {
        for (var i = 0; i < transitions.length; i++) {
          var transition = transitions[i];
          
          var mappings = result[transition.targetActivityId];
          if (!mappings) {
            mappings = [];
            result[transition.targetActivityId] = mappings;
          }
          mappings.push(transition);
        };        
      }

    }

    /**
     * Creates a activity instance tree from the assigned activity instances. There the child activity instances
     * and child transitions will be merged to a certain list of children of parent node.<p>
     * 
     * Furthermore, the assigned map <code>activityIdToNodeMap</code> will be filled during the creation of the node,
     * so that for each activity id the corresponding nodes will be collected. The map could look like this:
     * <code>{ServiceTask_1: [{id: 'instanceId_1', label: 'Service Task', ...}, {id: 'instanceId_2', label: 'Service Task', ...}], ...}</code>
     *
     * @param processDefinition The process definition to get the corresponding model from the assigned parameter 'semantic'.
     * @param semantic The bpmn model as object, containing all rendered bpmn elements.
     * @param activityInstances The activity instances which will be transformed to a tree.
     * @param activityIdToNodeMap The map which will contain for each activity id the corresponding nodes
     *
     * @return the root node, which contains the children (i.e. the tree will be returned).
     **/
    function createActivityInstanceTree(processDefinition, semantic, activityInstances, activityIdToNodeMap) {
      // get the corresponding semantic for the model.
      var model = null;

      for (var i = 0; i < semantic.length; i++) {
        var currentSemantic = semantic[i];
        if (currentSemantic.id === processDefinition.key && currentSemantic.type === 'process') {
          model = currentSemantic;
        }
      }

      // create and decorate root
      var root = createNode(activityInstances.id, model.id, getActivityName(model, model.id));
      
      // add new node to activityIdToNodeMap
      addNodeToMap(root, activityIdToNodeMap);
      
      // add children
      addChildren(root, model, activityInstances, activityIdToNodeMap);
      
      return root;
    }
    
    /**
     * Add the parent the children, i.e. merge the childTransitions and childActivityInstances of
     * the assigned <code>activityInstance</code> to one list containing the corresponding nodes.
     *
     * @param parent The parent, whom will be the children will added
     * @param semantic The bpmn model as object, containing all rendered bpmn elements.
     * @param activityInstance The activity instance which is the parent element.
     * @param activityIdToNodeMap The map which will contain for each activity id the corresponding nodes
     *
     **/
    function addChildren(parent, semantic, activityInstance, activityIdToNodeMap) {
      angular.forEach(activityInstance.childActivityInstances, function(childActivityInstance) {
        
        // create and decorate child node
        var childNode = createNode(childActivityInstance.id, childActivityInstance.activityId, getActivityName(semantic, childActivityInstance.activityId));

        // add new node to activityIdToNodeMap
        addNodeToMap(childNode, activityIdToNodeMap);
        
        // add parent the child node
        parent.children.push(childNode);
        
        // call recursive add children for child node as parent
        addChildren(childNode, semantic, childActivityInstance, activityIdToNodeMap);
      });
      
      angular.forEach(activityInstance.childTransitionInstances, function(childTransitionInstance) {
        
        // create and decorate child node
        var childNode = createNode(childTransitionInstance.id, childTransitionInstance.targetActivityId, getActivityName(semantic, childTransitionInstance.targetActivityId));
    
        // add new node to activityIdToNodeMap
        addNodeToMap(childNode, activityIdToNodeMap);
        
        // add parent the child node
        parent.children.push(childNode);
      });
    }

    /**
     * Add the node to the assigned activityIdToNodeMap.
     *
     **/
    function addNodeToMap(node, activityIdToNodeMap) {
      var instanceList = activityIdToNodeMap[node.activityId];
      if (!instanceList) {
        instanceList = activityIdToNodeMap[node.activityId] = [];
      }
        
      instanceList.push(node);   
    }
    
    /**
     * Creates a new node and decorates it with the assigned parameters.
     *
     **/
    function createNode(id, activityId, label) {
      var childNode = {};
      
      childNode.id = id;
      childNode.label = label;
      childNode.activityId = activityId;
      childNode.children = [];
      childNode.isOpen = true;
      
      return childNode;
    }
    
    /**
     * Returns the corresponding name to the assigned activity id
     * from the assigned element (i.e. bpmn model).
     *
     **/
    function getActivityName(element, activityId) {
      var name = null;

      if (activityId === element.id) {
        name = element.name;
        if (!name) {
          var shortenFilter = $filter('shorten');
          name = element.type + ' (' + shortenFilter(element.id, 8) + '...)';
        }
        return name;
      }

      if (element.baseElements) {
        for (var i = 0; i < element.baseElements.length; i++) {
          var currentElement = element.baseElements[i];
          name = getActivityName(currentElement, activityId);
          if (name) {
            return name;
          }   
        }        
      }
    }
 
    return {
      aggregateActivityInstances: aggregateActivityInstances,
      createActivityInstanceTree: createActivityInstanceTree
    };
    
  };

  module.factory('ActivityInstance', [ '$filter' , Service ]);
  // end config

  return module;

});