//>>built
define("dojox/dgauges/ScaleBase",["dojo/_base/lang","dojo/_base/declare","dojox/gfx","dojo/_base/array","dojox/widget/_Invalidating"],function(_1,_2,_3,_4,_5){
return _2("dojox.dgauges.ScaleBase",_5,{scaler:null,font:null,labelPosition:null,labelGap:1,tickStroke:null,_gauge:null,_gfxGroup:null,_bgGroup:null,_fgGroup:null,_indicators:null,_indicatorsIndex:null,_indicatorsRenderers:null,constructor:function(){
this._indicators=[];
this._indicatorsIndex={};
this._indicatorsRenderers={};
this._gauge=null;
this._gfxGroup=null;
this.tickStroke={color:"black",width:0.5};
this.addInvalidatingProperties(["scaler","font","labelGap","labelPosition","tickShapeFunc","tickLabelFunc","tickStroke"]);
this.watch("scaler",_1.hitch(this,this._watchScaler));
},postscript:function(_6){
this.inherited(arguments);
if(_6&&_6.scaler){
this._watchScaler("scaler",null,_6.scaler);
}
},_watchers:null,_watchScaler:function(_7,_8,_9){
_4.forEach(this._watchers,_1.hitch(this,function(_a){
_a.unwatch();
}));
var _b=_9.watchedProperties;
this._watchers=[];
_4.forEach(_b,_1.hitch(this,function(_c){
this._watchers.push(_9.watch(_c,_1.hitch(this,this.invalidateRendering)));
}));
},_getFont:function(){
var _d=this.font;
if(!_d){
_d=this._gauge.font;
}
if(!_d){
_d=_3.defaultFont;
}
return _d;
},positionForValue:function(_e){
return 0;
},valueForPosition:function(_f){
},tickLabelFunc:function(_10){
if(_10.isMinor){
return null;
}else{
return String(_10.value);
}
},tickShapeFunc:function(_11,_12,_13){
return _11.createLine({x1:0,y1:0,x2:_13.isMinor?6:10,y2:0}).setStroke(this.tickStroke);
},getIndicatorRenderer:function(_14){
return this._indicatorsRenderers[_14];
},removeIndicator:function(_15){
var _16=this._indicatorsIndex[_15];
if(_16){
_16._gfxGroup.removeShape();
var idx=this._indicators.indexOf(_16);
this._indicators.splice(idx,1);
_16._disconnectListeners();
delete this._indicatorsIndex[_15];
delete this._indicatorsRenderers[_15];
}
this.invalidateRendering();
return _16;
},getIndicator:function(_17){
return this._indicatorsIndex[_17];
},addIndicator:function(_18,_19,_1a){
if(this._indicatorsIndex[_18]&&this._indicatorsIndex[_18]!=_19){
this.removeIndicator(_18);
}
this._indicators.push(_19);
this._indicatorsIndex[_18]=_19;
if(!this._ticksGroup){
this._createSubGroups();
}
var _1b=_1a?this._bgGroup:this._fgGroup;
_19._gfxGroup=_1b.createGroup();
_19.scale=this;
return this.invalidateRendering();
},_createSubGroups:function(){
if(!this._gfxGroup||this._ticksGroup){
return;
}
this._bgGroup=this._gfxGroup.createGroup();
this._ticksGroup=this._gfxGroup.createGroup();
this._fgGroup=this._gfxGroup.createGroup();
},refreshRendering:function(){
if(!this._ticksGroup){
this._createSubGroups();
}
}});
});
