//>>built
define("dijit/_CssStateMixin",["dojo/_base/array","dojo/_base/declare","dojo/dom","dojo/dom-class","dojo/_base/lang","dojo/on","dojo/ready","dojo/_base/window","./registry"],function(_1,_2,_3,_4,_5,on,_6,_7,_8){
var _9=_2("dijit._CssStateMixin",[],{cssStateNodes:{},hovering:false,active:false,_applyAttributes:function(){
this.inherited(arguments);
_1.forEach(["disabled","readOnly","checked","selected","focused","state","hovering","active","_opened"],function(_a){
this.watch(_a,_5.hitch(this,"_setStateClass"));
},this);
for(var ap in this.cssStateNodes){
this._trackMouseState(this[ap],this.cssStateNodes[ap]);
}
this._trackMouseState(this.domNode,this.baseClass);
this._setStateClass();
},_cssMouseEvent:function(_b){
if(!this.disabled){
switch(_b.type){
case "mouseover":
this._set("hovering",true);
this._set("active",this._mouseDown);
break;
case "mouseout":
this._set("hovering",false);
this._set("active",false);
break;
case "mousedown":
case "touchstart":
this._set("active",true);
break;
case "mouseup":
case "touchend":
this._set("active",false);
break;
}
}
},_setStateClass:function(){
var _c=this.baseClass.split(" ");
function _d(_e){
_c=_c.concat(_1.map(_c,function(c){
return c+_e;
}),"dijit"+_e);
};
if(!this.isLeftToRight()){
_d("Rtl");
}
var _f=this.checked=="mixed"?"Mixed":(this.checked?"Checked":"");
if(this.checked){
_d(_f);
}
if(this.state){
_d(this.state);
}
if(this.selected){
_d("Selected");
}
if(this._opened){
_d("Opened");
}
if(this.disabled){
_d("Disabled");
}else{
if(this.readOnly){
_d("ReadOnly");
}else{
if(this.active){
_d("Active");
}else{
if(this.hovering){
_d("Hover");
}
}
}
}
if(this.focused){
_d("Focused");
}
var tn=this.stateNode||this.domNode,_10={};
_1.forEach(tn.className.split(" "),function(c){
_10[c]=true;
});
if("_stateClasses" in this){
_1.forEach(this._stateClasses,function(c){
delete _10[c];
});
}
_1.forEach(_c,function(c){
_10[c]=true;
});
var _11=[];
for(var c in _10){
_11.push(c);
}
tn.className=_11.join(" ");
this._stateClasses=_c;
},_subnodeCssMouseEvent:function(_12,_13,evt){
if(this.disabled||this.readOnly){
return;
}
function _14(_15){
_4.toggle(_12,_13+"Hover",_15);
};
function _16(_17){
_4.toggle(_12,_13+"Active",_17);
};
function _18(_19){
_4.toggle(_12,_13+"Focused",_19);
};
switch(evt.type){
case "mouseover":
_14(true);
break;
case "mouseout":
_14(false);
_16(false);
break;
case "mousedown":
case "touchstart":
_16(true);
break;
case "mouseup":
case "touchend":
_16(false);
break;
case "focus":
case "focusin":
_18(true);
break;
case "blur":
case "focusout":
_18(false);
break;
}
},_trackMouseState:function(_1a,_1b){
_1a._cssState=_1b;
}});
_6(function(){
function _1c(evt){
if(!_3.isDescendant(evt.relatedTarget,evt.target)){
for(var _1d=evt.target;_1d&&_1d!=evt.relatedTarget;_1d=_1d.parentNode){
if(_1d._cssState){
var _1e=_8.getEnclosingWidget(_1d);
if(_1e){
if(_1d==_1e.domNode){
_1e._cssMouseEvent(evt);
}else{
_1e._subnodeCssMouseEvent(_1d,_1d._cssState,evt);
}
}
}
}
}
};
function _1f(evt){
evt.target=evt.srcElement;
_1c(evt);
};
var _20=_7.body();
_1.forEach(["mouseover","mouseout","mousedown","touchstart","mouseup","touchend"],function(_21){
if(_20.addEventListener){
_20.addEventListener(_21,_1c,true);
}else{
_20.attachEvent("on"+_21,_1f);
}
});
on(_20,"focusin, focusout",function(evt){
var _22=evt.target;
if(_22._cssState&&!_22.getAttribute("widgetId")){
var _23=_8.getEnclosingWidget(_22);
_23._subnodeCssMouseEvent(_22,_22._cssState,evt);
}
});
});
return _9;
});
