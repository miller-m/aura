<!--

    Copyright (C) 2013 salesforce.com, inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<aura:application extensible="true" implements="auratest:testInterface" extends="auratest:testApplication3" controller="java://org.auraframework.impl.java.controller.JavaTestController" model="java://org.auraframework.impl.java.model.TestJavaModel">
    <aura:attribute name="myString" type="String" default="Default String"/>
    <auratest:testComponent2 myInteger="1"/>
</aura:application>
