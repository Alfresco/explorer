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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGroup rendered="#{WizardManager.bean.hasStartableWorkflows == false}">
   <f:verbatim>
      <%PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");%>
      <table><tr><td>
   </f:verbatim>
   <h:graphicImage url="/images/icons/info_icon.gif" />
   <f:verbatim>
      </td><td>
   </f:verbatim>
   <h:outputText value="#{msg.start_workflow_no_workflows}" />
   <f:verbatim>
      </td></tr></table>
      <%PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");%>
   </f:verbatim>
</h:panelGroup>

<h:panelGrid columns="1" styleClass="workflowSelection" rendered="#{WizardManager.bean.hasStartableWorkflows}">
   <h:outputText value="#{msg.available_workflows}:"/>
   <h:selectOneRadio id="selected-workflow" value="#{WizardManager.bean.selectedWorkflow}" 
                     layout="pageDirection">
      <f:selectItems value="#{WizardManager.bean.startableWorkflows}" />
   </h:selectOneRadio>
</h:panelGrid>

