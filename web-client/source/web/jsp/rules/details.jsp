<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<script type="text/javascript">
   function checkButtonState()
   {
      if (document.getElementById("wizard:wizard-body:title").value.length == 0)
      {
         document.getElementById("wizard:next-button").disabled = true;
         document.getElementById("wizard:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
         document.getElementById("wizard:finish-button").disabled = false;
      }
   }
</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.type}:"/>
         <f:verbatim>
      </td>
      <td width="90%">
         </f:verbatim>
         <h:selectOneMenu id="rule-type" value="#{WizardManager.bean.type}" 
                          disabled="#{WizardManager.bean.ruleTypeDisabled}">
            <f:selectItems value="#{WizardManager.bean.types}" />
         </h:selectOneMenu>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.title}:"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="title" value="#{WizardManager.bean.title}" size="35" maxlength="1024"
                      onkeyup="checkButtonState();" /><f:verbatim>&nbsp;*
      </td>
   </tr>
   <tr>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.description}:"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText value="#{WizardManager.bean.description}" size="35" maxlength="1024" />
         <f:verbatim>
      </td>
   </tr>
   <tr><td colspan="2" class="paddingRow"></td></tr>
   <tr>
      <td colspan="2" class="wizardSectionHeading">
         </f:verbatim>
         <h:outputText value="#{msg.other_options}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td colspan="2">
         </f:verbatim>
         <h:panelGrid columns="2">
            <h:selectBooleanCheckbox value="#{WizardManager.bean.applyToSubSpaces}" />
            <h:outputText value="#{msg.apply_to_sub_spaces}" />
         </h:panelGrid>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td colspan="2">
         </f:verbatim>
         <h:panelGrid columns="2">
            <h:selectBooleanCheckbox value="#{WizardManager.bean.runInBackground}" />
            <h:outputText value="#{msg.run_in_background}" />
         </h:panelGrid>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td colspan="2">
         <div id="rule-info" style="padding-left: 26px;">
            </f:verbatim>
            <h:graphicImage alt="" value="/images/icons/info_icon.gif" style="vertical-align: middle;" />
            <h:outputText value="&nbsp;" escape="false" />
            <h:outputText value="#{msg.rule_background_info}" />
            <f:verbatim>
         </div>
      </td>
   </tr>
   <tr>
      <td colspan="2">
         </f:verbatim>
         <h:panelGrid columns="2">
            <h:selectBooleanCheckbox value="#{WizardManager.bean.ruleDisabled}" />
            <h:outputText value="#{msg.rule_disabled}" />
         </h:panelGrid>
         <f:verbatim>
      </td>
   </tr>
</table>
</f:verbatim>