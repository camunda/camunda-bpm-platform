//>>built
define("dijit/Destroyable",["dojo/_base/array","dojo/aspect","dojo/_base/declare"],function(_1,_2,_3){
return _3("dijit.Destroyable",null,{destroy:function(_4){
this._destroyed=true;
},own:function(){
_1.forEach(arguments,function(_5){
var _6="destroyRecursive" in _5?"destroyRecursive":"destroy" in _5?"destroy":"remove";
_5._odh=_2.before(this,"destroy",function(_7){
_5._odh.remove();
_5[_6](_7);
});
_2.after(_5,_6,function(){
_5._odh.remove();
});
},this);
return arguments;
}});
});
