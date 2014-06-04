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
<aura:application render="SERVER">
    <aura:attribute name="expression" type="String" default="testServerSideRenderingOfBasicComponents"/>

    <div class="htmlEntities">
        <div class="nonBreakingSpace">      &nbsp;</div>
        <div class="copyRight">             &copy;</div>
        <div class="leftAngleQuotationMark">&laquo;</div>
        <div class="degree">                &deg;</div>
        <div class="spacingAcute">          &acute;</div>
        <div class="rightAngleQuotationMark">&raquo;</div>
        <div class="apostrophe">            &apos;</div>
        <div class="euro">                  &euro;</div>
    </div>
    <div class="XMLEntiies">
        <div class="greaterThan">&gt;</div>
        <div class="lessThan">&lt;</div>
        <div class="ampersand">&amp;</div>
    </div>

    <div class="textComponent">
        testServerSideRenderingOfBasicComponents
    </div>

    <div class="expressionComponent">
        {!v.expression}
    </div>

</aura:application>
<!--
    Tests using this component:
    ComponentRenderingUITest
-->
