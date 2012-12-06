//>>built
define("dijit/form/_SearchMixin",["dojo/data/util/filter","dojo/_base/declare","dojo/_base/event","dojo/keys","dojo/_base/lang","dojo/query","dojo/sniff","dojo/string","dojo/when","../registry"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a){
return _2("dijit.form._SearchMixin",null,{pageSize:Infinity,store:null,fetchProperties:{},query:{},searchDelay:200,searchAttr:"name",queryExpr:"${0}*",ignoreCase:true,_abortQuery:function(){
if(this.searchTimer){
this.searchTimer=this.searchTimer.remove();
}
if(this._queryDeferHandle){
this._queryDeferHandle=this._queryDeferHandle.remove();
}
if(this._fetchHandle){
if(this._fetchHandle.abort){
this._cancelingQuery=true;
this._fetchHandle.abort();
this._cancelingQuery=false;
}
if(this._fetchHandle.cancel){
this._cancelingQuery=true;
this._fetchHandle.cancel();
this._cancelingQuery=false;
}
this._fetchHandle=null;
}
},_processInput:function(_b){
if(this.disabled||this.readOnly){
return;
}
var _c=_b.charOrCode;
if(_b.altKey||((_b.ctrlKey||_b.metaKey)&&(_c!="x"&&_c!="v"))||_c==_4.SHIFT){
return;
}
var _d=false;
this._prev_key_backspace=false;
switch(_c){
case _4.DELETE:
case _4.BACKSPACE:
this._prev_key_backspace=true;
this._maskValidSubsetError=true;
_d=true;
break;
default:
_d=typeof _c=="string"||_c==229;
}
if(_d){
if(!this.store){
this.onSearch();
}else{
this.searchTimer=this.defer("_startSearchFromInput",1);
}
}
},onSearch:function(){
},_startSearchFromInput:function(){
this._startSearch(this.focusNode.value.replace(/([\\\*\?])/g,"\\$1"));
},_startSearch:function(_e){
this._abortQuery();
var _f=this,_6=_5.clone(this.query),_10={start:0,count:this.pageSize,queryOptions:{ignoreCase:this.ignoreCase,deep:true}},qs=_8.substitute(this.queryExpr,[_e]),q,_11=function(){
var _12=_f._fetchHandle=_f.store.query(_6,_10);
if(_f.disabled||_f.readOnly||(q!==_f._lastQuery)){
return;
}
_9(_12,function(res){
_f._fetchHandle=null;
if(!_f.disabled&&!_f.readOnly&&(q===_f._lastQuery)){
_9(_12.total,function(_13){
res.total=_13;
var _14=_f.pageSize;
if(isNaN(_14)||_14>res.total){
_14=res.total;
}
res.nextPage=function(_15){
_10.direction=_15=_15!==false;
_10.count=_14;
if(_15){
_10.start+=res.length;
if(_10.start>=res.total){
_10.count=0;
}
}else{
_10.start-=_14;
if(_10.start<0){
_10.count=Math.max(_14+_10.start,0);
_10.start=0;
}
}
if(_10.count<=0){
res.length=0;
_f.onSearch(res,_6,_10);
}else{
_11();
}
};
_f.onSearch(res,_6,_10);
});
}
},function(err){
_f._fetchHandle=null;
if(!_f._cancelingQuery){
console.error(_f.declaredClass+" "+err.toString());
}
});
};
_5.mixin(_10,this.fetchProperties);
if(this.store._oldAPI){
q=qs;
}else{
q=_1.patternToRegExp(qs,this.ignoreCase);
q.toString=function(){
return qs;
};
}
this._lastQuery=_6[this.searchAttr]=q;
this._queryDeferHandle=this.defer(_11,this.searchDelay);
},constructor:function(){
this.query={};
this.fetchProperties={};
},postMixInProperties:function(){
if(!this.store){
var _16=this.list;
if(_16){
this.store=_a.byId(_16);
}
}
this.inherited(arguments);
}});
});
