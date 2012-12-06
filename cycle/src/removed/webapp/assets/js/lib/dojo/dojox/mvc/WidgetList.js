//>>built
define("dojox/mvc/WidgetList",["require","dojo/_base/array","dojo/_base/lang","dojo/_base/declare","dijit/_Container","dijit/_WidgetBase","dojox/mvc/Templated"],function(_1,_2,_3,_4,_5,_6,_7){
var _8="data-mvc-child-type",_9="data-mvc-child-mixins",_a="data-mvc-child-props",_b="data-mvc-child-bindings";
function _c(_d){
return eval("({"+_d+"})");
};
function _e(w){
for(var h=null;h=(w._handles||[]).pop();){
h.unwatch();
}
};
var _f=_4("dojox.mvc.WidgetList",[_6,_5],{childClz:null,childType:"",childMixins:"",childParams:null,childBindings:null,children:null,templateString:"",partialRebuild:false,_relTargetProp:"children",postMixInProperties:function(){
this.inherited(arguments);
if(this[_8]){
this.childType=this[_8];
}
if(this[_9]){
this.childMixins=this[_9];
}
},startup:function(){
this.inherited(arguments);
this._setChildrenAttr(this.children);
},_setChildrenAttr:function(_10){
var _11=this.children;
this._set("children",_10);
if(this._started&&(!this._builtOnce||_11!=_10)){
_e(this);
this._builtOnce=true;
this._buildChildren(_10);
if(_3.isArray(_10)){
var _12=this;
!this.partialRebuild&&_3.isFunction(_10.watchElements)&&(this._handles=this._handles||[]).push(_10.watchElements(function(idx,_13,_14){
_12._buildChildren(_10);
}));
_10.watch!=={}.watch&&(this._handles=this._handles||[]).push(_10.watch(function(_15,old,_16){
if(!isNaN(_15)){
var w=_12.getChildren()[_15-0];
w&&w.set(w._relTargetProp||"target",_16);
}
}));
}
}
},_buildChildren:function(_17){
for(var cw=this.getChildren(),w=null;w=cw.pop();){
this.removeChild(w);
w.destroy();
}
if(!_3.isArray(_17)){
return;
}
var _18=_3.hitch(this,function(seq){
if(this._buildChildrenSeq>seq){
return;
}
var clz=_4([].slice.call(arguments,1),{}),_19=this;
function _1a(_1b,_1c){
_2.forEach(_2.map(_1b,function(_1d,idx){
var _1e={ownerDocument:_19.ownerDocument,parent:_19,indexAtStartup:_1c+idx};
_1e[(_19.childParams||_19[_a]&&_c.call(_1e,_19[_a])||{})._relTargetProp||clz.prototype._relTargetProp||"target"]=_1d;
var _1f=_19.childParams||_19[_a]&&_c.call(_1e,_19[_a]),_20=_19.childBindings||_19[_b]&&_c.call(_1e,_19[_b]);
if(_19.templateString&&!_1e.templateString&&!clz.prototype.templateString){
_1e.templateString=_19.templateString;
}
if(_20&&!_1e.bindings&&!clz.prototype.bindings){
_1e.bindings=_20;
}
return new clz(_3.mixin(_1e,_1f));
}),function(_21,idx){
_19.addChild(_21,_1c+idx);
});
};
_1a(_17,0);
if(this.partialRebuild){
_3.isFunction(_17.watchElements)&&(this._handles=this._handles||[]).push(_17.watchElements(function(idx,_22,_23){
for(var i=0,l=(_22||[]).length;i<l;++i){
_19.removeChild(idx);
}
_1a(_23,idx);
}));
}
},this._buildChildrenSeq=(this._buildChildrenSeq||0)+1);
if(this.childClz){
_18(this.childClz);
}else{
if(this.childType){
_1([this.childType].concat(this.childMixins&&this.childMixins.split(",")||[]),_18);
}else{
_18(_7);
}
}
},destroy:function(){
_e(this);
this.inherited(arguments);
}});
_f.prototype[_8]=_f.prototype[_9]=_f.prototype[_a]=_f.prototype[_b]="";
return _f;
});
