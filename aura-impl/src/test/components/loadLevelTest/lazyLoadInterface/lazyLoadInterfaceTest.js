/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
({
    //W-1300966
    _testLazyLoadingInterface:{
        test:[function(cmp){
                $A.test.assertEquals("markup://aura:placeholder", cmp.find("interface").getDef().getDescriptor().getQualifiedName());
                $A.test.addWaitFor("markup://loadLevelTest:simpleImplementation1", function(){
                    return cmp.find("interface").getDef().getDescriptor().getQualifiedName();
                },function(){
                    $A.test.assertEquals("Implementation 1",cmp.find('interface').find("divInImp1").getElement().title);
                })
            }
        ]
    }
})
