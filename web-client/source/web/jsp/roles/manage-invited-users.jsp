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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>


<f:verbatim>
<table cellspacing="0" cellpadding="0" width="100%">

   <%-- Details --%>
   <tr valign=top>
      <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
      <td>
      <table cellspacing="2" cellpadding="2" border="0" width="100%">
         <tr>
            <td width="100%" valign="top"></f:verbatim>
            <a:panel id="users-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.users_and_groups}">

               <a:richList id="users-list" binding="#{DialogManager.bean.usersRichList}" viewMode="details" pageSize="10" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
                  width="100%" value="#{DialogManager.bean.users}" var="r" initialSortColumn="userName" initialSortDescending="true">
                    
                     <%-- Primary column with full name --%>
                     <a:column primary="true" width="200" style="padding:2px;text-align:left">
                        <f:facet name="header">
                           <a:sortLink label="#{msg.name}" value="fullName" mode="case-insensitive" styleClass="header"/>
                        </f:facet>
                        <f:facet name="small-icon">
                           <h:graphicImage url="#{r.icon}" />
                        </f:facet>
                        <a:actionLink id="user-link" value="#{r.fullName}" actionListener="#{UsersDialog.setupUserAction}" action="dialog:userProfile" rendered="#{!r.isGroup}">
                           <f:param name="id" value="#{r.id}" />
                        </a:actionLink>
                        <h:outputText id="user-txt" value="#{r.fullName}" rendered="#{r.isGroup}" />
                     </a:column>
                     
                     <%-- Username column --%>
                     <a:column width="120" style="text-align:left">
                        <f:facet name="header">
                           <a:sortLink label="#{msg.authority}" value="userNameLabel" styleClass="header"/>
                        </f:facet>
                        <h:outputText value="#{r.userNameLabel}" />
                     </a:column>
                     
                     <%-- Roles column --%>
                     <a:column style="text-align:left">
                        <f:facet name="header">
                           <a:sortLink label="#{msg.roles}" value="roles" styleClass="header"/>
                        </f:facet>
                        <h:outputText value="#{r.roles}" />
                     </a:column>
                     
                     <%-- Actions column --%>
                     <a:column actions="true" style="text-align:left">
                        <f:facet name="header">
                           <h:outputText value="#{msg.actions}"/>
                        </f:facet>
                        <a:booleanEvaluator value="#{!r.inherited}">
                           <a:actionLink value="#{msg.change_roles}" image="/images/icons/edituser.gif" showLink="false" action="dialog:editUserRoles" actionListener="#{EditUserRolesDialog.setupUserAction}">
                              <f:param name="userName" value="#{r.userName}" />
                              <f:param name="userNameLabel" value="#{r.userNameLabel}" />
                           </a:actionLink>
                           <a:actionLink value="#{msg.remove}" image="/images/icons/delete_person.gif" showLink="false" action="dialog:removeInvitedUser" actionListener="#{RemoveInvitedUserDialog.setupUserAction}">
                              <f:param name="userName" value="#{r.userName}" />
                              <f:param name="userNameLabel" value="#{r.userNameLabel}" />
                           </a:actionLink>
                        </a:booleanEvaluator>
                     </a:column>
                     
                     <a:dataPager styleClass="pager" />
                  </a:richList>
                              
            </a:panel><f:verbatim></td>
       </tr>
      </table>
      </td>
      <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
   </tr>

   <tr>
      <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
      <td>
      <table cellspacing="2" cellpadding="0" border="0" width="100%">
         <tr>
            <td></f:verbatim><h:selectBooleanCheckbox id="chkPermissions" value="#{DialogManager.bean.inheritPermissions}" valueChangeListener="#{DialogManager.bean.inheritPermissionsValueChanged}" onclick="document.forms['dialog'].submit(); return true;"
               disabled="#{!DialogManager.bean.hasChangePermissions}" /><f:verbatim></td>
            <td width=100%>&nbsp;</f:verbatim><h:outputText value="#{msg.inherit_permissions}" /><f:verbatim></td>
         </tr>
      </table>
      </td>
      <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
   </tr>

</table>
</f:verbatim>