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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
<tr>
<td class="mainSubTitle"><h:outputText value="#{msg.change_user_roles}" /></td>
</tr>
<tr><td class="paddingRow"></td></tr>
<tr>
<td>1.&nbsp;</f:verbatim><h:outputText value="#{msg.select_role}" /><f:verbatim></td>
</tr>
<tr>
<td>
</f:verbatim><h:selectOneListbox id="roles" style="width:250px" size="5">
<f:selectItems value="#{InviteContentUsersWizard.roles}" />
</h:selectOneListbox>
<f:verbatim>
</td>
</tr>
<tr>
<td>
2.&nbsp;</f:verbatim><h:commandButton value="#{msg.add_to_list_button}" actionListener="#{DialogManager.bean.addRole}" />
<f:verbatim>
</td>
</tr>
<tr><td class="paddingRow"></td></tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.selected_roles}" /><f:verbatim></td>
</tr>
<tr>
<td>
</f:verbatim>
<h:dataTable value="#{DialogManager.bean.personRolesDataModel}" var="row"
rowClasses="selectedItemsRow,selectedItemsRowAlt"
styleClass="selectedItems" headerClass="selectedItemsHeader"
cellspacing="0" cellpadding="4"
rendered="#{DialogManager.bean.personRolesDataModel.rowCount != 0}">
<h:column>
<f:facet name="header">
<h:outputText value="#{msg.name}" />
</f:facet>
<h:outputText value="#{row.role}" />
</h:column>
<h:column>
<a:actionLink actionListener="#{DialogManager.bean.removeRole}" image="/images/icons/delete.gif"
value="#{msg.remove}" showLink="false" style="padding-left:6px" />
</h:column>
</h:dataTable>
<a:panel id="no-items" rendered="#{DialogManager.bean.personRolesDataModel.rowCount == 0}">
<f:verbatim>
<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
<tr>
<td colspan='2' class='selectedItemsHeader'><h:outputText id="no-items-name" value="#{msg.name}" /></td>
</tr>
<tr>
<td class='selectedItemsRow'><h:outputText id="no-items-msg" value="#{msg.no_selected_items}" /></td>
</tr>
</table>
</f:verbatim>
</a:panel>
<f:verbatim>
</td>
</tr>
<tr><td colspan=2 class="paddingRow"></td></tr>
</table>
</f:verbatim>
