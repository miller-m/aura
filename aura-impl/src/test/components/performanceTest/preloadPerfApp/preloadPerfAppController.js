({
    pushComponent:function(cmp){
        var a = $A.get("c.aura://ComponentController.getApplication");
        a.setParams({
            "name" : 'performanceTest:perfApp',
            "attributes" : {'start': 5 }
        });
        a.setCallback(cmp,function(a){
            var c = $A.newCmpDeprecated(a.getReturnValue());
            cmp.find('placeHolder').getValue('v.body').push(c);
            $A.endMark("Fetch preloaded component");
        });
        $A.mark("Fetch preloaded component");
        $A.enqueueAction(a);
    }
})
