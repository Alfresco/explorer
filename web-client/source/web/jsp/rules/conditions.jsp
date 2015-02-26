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
   function itemSelected(inputField)
   {
      if (inputField.selectedIndex == 0)
      {
         document.getElementById("wizard:wizard-body:set-add-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:wizard-body:set-add-button").disabled = false;
      }
      
      // also check to see if the 'no-condition' option has been selected, if it has, change
      // the explanation text and the button label
      var short_text = "</f:verbatim><h:outputText value='#{msg.click_add_to_list}' /><f:verbatim>";
      var long_text = "</f:verbatim><h:outputText value='#{msg.click_set_and_add}' /><f:verbatim>";
      var short_label = "</f:verbatim><h:outputText value='#{msg.add_to_list_button}' /><f:verbatim>";
      var long_label = "</f:verbatim><h:outputText value='#{msg.set_and_add_button}' /><f:verbatim>";
      
      if (inputField.value == "no-condition")
      {
         document.getElementById("wizard:wizard-body:set-add-button").value = short_label;
         document.getElementById("wizard:wizard-body:instruction-text").innerHTML = short_text;
      }
      else
      {
         document.getElementById("wizard:wizard-body:set-add-button").value = long_label;
         document.getElementById("wizard:wizard-body:instruction-text").innerHTML = long_text;
      }
   }
</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td>1.</td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.select_condition}"/>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>&nbsp;</td>
      <td width="98%">
         </f:verbatim>
         <h:selectOneMenu value="#{WizardManager.bean.condition}" 
                          id="condition" onchange="javascript:itemSelected(this);">
            <f:selectItems value="#{WizardManager.bean.conditions}" />
         </h:selectOneMenu>
         <f:verbatim>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td>2.</td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.click_set_and_add}" id="instruction-text"/>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>&nbsp;</td>
      <td>
         </f:verbatim>
         <h:commandButton id="set-add-button" value="#{msg.set_and_add_button}" 
                          action="#{WizardManager.bean.promptForConditionValues}"
                          disabled="true"/>
         <f:verbatim>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td colspan='2'>
         </f:verbatim>
         <h:outputText value="#{msg.selected_conditions}"/>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td colspan='2'>
         </f:verbatim>
         <h:dataTable value="#{WizardManager.bean.allConditionsDataModel}" var="row"
                      rowClasses="selectedItemsRow,selectedItemsRowAlt"
                      styleClass="selectedItems" headerClass="selectedItemsHeader"
                      cellspacing="0" cellpadding="4" 
                      rendered="#{WizardManager.bean.allConditionsDataModel.rowCount != 0}">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="#{msg.summary}" />
               </f:facet>
               <h:outputText value="#{row.conditionSummary}" />
            </h:column>
            <h:column>
               <a:actionLink action="#{WizardManager.bean.removeCondition}" image="/images/icons/delete.gif"
                             value="#{msg.remove}" showLink="false" style="padding-left:6px;padding-right:2px" />
               <a:actionLink action="#{WizardManager.bean.editCondition}" image="/images/icons/edit_icon.gif"
                             value="#{msg.change}" showLink="false" rendered='#{row.noParamsMarker == null}' />
            </h:column>
         </h:dataTable>
         <a:panel id="no-items" rendered="#{WizardManager.bean.allConditionsDataModel.rowCount == 0}">
            <f:verbatim>
            <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
               <tr>
                  <td colspan='2' class='selectedItemsHeader'>
                     </f:verbatim>
                     <h:outputText id="no-items-name" value="#{msg.summary}" />
                     <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td class='selectedItemsRow'>
                     </f:verbatim>
                     <h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
                     <f:verbatim>
                  </td>
               </tr>
            </table>
            </f:verbatim>
         </a:panel>
         <f:verbatim>
      </td>
   </tr>
</table>
</f:verbatim>
