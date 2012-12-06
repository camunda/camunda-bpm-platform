//>>built
define("dojox/widget/PortletDialogSettings",["dojo/_base/declare","dojo/_base/kernel","dojo/fx","dijit/TitlePane","dijit/Dialog"],function(_1,_2,fx,_3,_4){
return _1("dojox.widget.PortletDialogSettings",[dojox.widget.PortletSettings],{dimensions:null,constructor:function(_5,_6){
this.dimensions=_5.dimensions||[300,100];
},toggle:function(){
if(!this.dialog){
this.dialog=new dijit.Dialog({title:this.title});
dojo.body().appendChild(this.dialog.domNode);
this.dialog.containerNode.appendChild(this.domNode);
dojo.style(this.dialog.domNode,{"width":this.dimensions[0]+"px","height":this.dimensions[1]+"px"});
dojo.style(this.domNode,"display","");
}
if(this.dialog.open){
this.dialog.hide();
}else{
this.dialog.show(this.domNode);
}
}});
});
