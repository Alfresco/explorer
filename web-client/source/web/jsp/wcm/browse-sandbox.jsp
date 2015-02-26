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
<%@ taglib uri="/WEB-INF/wcm.tld" prefix="w" %>

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_browse_sandbox">

<script type="text/javascript">
   function applySizeFolders(e)
   {
      return submitAction(e, 'folders-apply');
   }
   
   function applySizeFiles(e)
   {
      return submitAction(e, 'files-apply');
   }
   
   function searchWebsite(e)
   {
      return submitAction(e, 'search-apply');
   }
   
   function submitAction(e, field)
   {
      var keycode;
      if (window.event) keycode = window.event.keyCode;
      else if (e) keycode = e.which;
      if (keycode == 13)
      {
         document.forms['browse-sandbox']['browse-sandbox:act'].value='browse-sandbox:' + field;
         document.forms['browse-sandbox'].submit();
         return false;
      }
      return true;
   }
</script>

<f:view>
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="browse-sandbox">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2" width="100%">

      <%-- Title bar --%>
      <tr>
         <td colspan=2>
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign=top>
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="<h:outputText value="#{NavigationBean.workAreaWidth}" />">
            <table cellspacing=0 cellpadding=0 width=100%>
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width=4></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing=4 cellpadding=0 width=100%>
                        <tr>
                           
                           <%-- actions for browse mode --%>
                           <a:panel id="browse-actions" rendered="#{AVMBrowseBean.searchContext == null}">
                              <td width=32>
                                 <h:graphicImage id="website-logo" url="#{AVMBrowseBean.icon}" width="32" height="32" />
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputText value="#{AVMBrowseBean.sandboxTitle}" id="msg2" /></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.sandbox_info}" id="msg3" /></div>
                                 <div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
                              </td>
                              <td style="white-space:nowrap">
                                 <a:actionLink value="#{msg.sandbox_preview}" image="/images/icons/preview_website.gif" href="#{AVMBrowseBean.sandboxPreviewUrl}" target="new" id="act-prev" />
                              </td>
                              <r:permissionEvaluator value="#{AVMBrowseBean.currentPathNode}" allow="CreateChildren" id="eval1">
                              <td style="padding-left:4px;white-space:nowrap" width="140">
                                 <%-- Create actions menu --%>
                                 <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:actions id="acts_create" value="avm_create_menu" context="#{AVMBrowseBean.currentPathNode}" />
                                 </a:menu>
                              </td>
                              </r:permissionEvaluator>
                           </a:panel>
                           
                           <%-- actions for search results mode --%>
                           <a:panel id="search-actions" rendered="#{AVMBrowseBean.searchContext != null}">
                              <td width=32>
                                 <img src="<%=request.getContextPath()%>/images/icons/search_results_large.gif" width=32 height=32>
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputText value="#{msg.search_results}" id="msg5" /></div>
                                 <div class="mainSubText"><h:outputFormat value="#{msg.search_detail}" id="msg6"><f:param value="#{AVMBrowseBean.searchContext.text}" id="param1" /></h:outputFormat></div>
                                 <h:panelGroup id="sandbox-info" rendered="#{!AVMBrowseBean.isStagingStore}">
                                    <div class="mainSubText">
                                       <h:graphicImage url="/images/icons/info_icon.gif" id="img-info" width="16" height="16" style="padding-right:3px;vertical-align:middle" />
                                       <h:outputText value="#{msg.search_sandbox_warn}" id="msg7" style="font-weight:bold" />
                                    </div>
                                 </h:panelGroup>
                              </td>
                              <td style="padding-right:4px" align=right>
                                 <%-- Close Search action --%>
                                 <a:actionLink value="#{msg.close_search}" image="/images/icons/action.gif" style="white-space:nowrap" actionListener="#{AVMBrowseBean.closeSearch}" id="act-close" />
                              </td>
                           </a:panel>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width=4></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width=4 height=9></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width=4 height=9></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%> 
                     <a:errors message="" infoClass="statusWarningText" errorClass="statusErrorText" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Website Path Breadcrumb --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding-left:8px;padding-top:4px;padding-bottom:4px">
                     <div style="float:left"><a:breadcrumb value="#{AVMBrowseBean.location}" styleClass="title" /></div>
                     <div style="float:right;padding-right:4px">
                        <h:outputText value="#{msg.search_website}:" id="txt-search" />
                        <h:inputText id="web-search" value="#{AVMBrowseBean.websiteQuery}" style="margin-left:4px;margin-right:4px;width:120px" maxlength="1024" onkeyup="return searchWebsite(event);" />
                        <a:actionLink id="search-apply" value="#{msg.go}" image="/images/icons/search_icon.gif" showLink="false" actionListener="#{AVMBrowseBean.searchWebsite}" />
                     </div>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Custom Template View --%>
               <a:panel id="custom-wrapper-panel" rendered="#{AVMBrowseBean.hasCustomView && AVMBrowseBean.searchContext == null}">
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     <a:panel id="custom-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              label="#{msg.custom_view}" progressive="true">
                        <r:webScript id="webscript" scriptUrl="#{AVMBrowseBean.currentNodeWebscript}" context="#{AVMBrowseBean.customWebscriptContext}" rendered="#{AVMBrowseBean.hasWebscriptView}" />
                        <r:template id="template" template="#{AVMBrowseBean.currentNodeTemplate}" model="#{AVMBrowseBean.templateModel}" rendered="#{!AVMBrowseBean.hasWebscriptView && AVMBrowseBean.hasTemplateView}" />
                     </a:panel>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               </a:panel>
               
               <%-- Details - Folders --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <h:panelGroup id="folder-panel-facets">
                        <f:facet name="title">
                           <a:panel id="page-controls1" style="font-size:9px">
                              <h:outputText value="#{msg.items_per_page}" id="items-txt1"/>
                              <h:inputText id="folder-pages" value="#{AVMBrowseBean.pageSizeFoldersStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeFolders(event);" />
                              <div style="display:none"><a:actionLink id="folders-apply" value="" actionListener="#{AVMBrowseBean.updateFoldersPageSize}" /></div>
                           </a:panel>
                        </f:facet>
                     </h:panelGroup>
                     <a:panel id="folders-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              facetsId="folder-panel-facets" label="#{msg.website_browse_folders}">
                        
                        <w:avmList id="folder-list" binding="#{AVMBrowseBean.foldersRichList}" viewMode="details" pageSize="#{AVMBrowseBean.pageSizeFolders}"
                              styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                              value="#{AVMBrowseBean.folders}" var="r">
                           
                           <%-- Primary column with folder name --%>
                           <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                              </f:facet>
                              <f:facet name="small-icon">
                                 <a:actionLink id="col1-act1" value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{AVMBrowseBean.clickFolder}" showLink="false">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </f:facet>
                              <a:actionLink id="col1-act2" value="#{r.name}" actionListener="#{AVMBrowseBean.clickFolder}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                              <w:avmLockIcon id="col1-lock" value="#{r.nodeRef}" align="absmiddle" />
                           </a:column>
                           
                           <%-- Path column for search results mode --%>
                           <a:column id="col2" style="text-align:left" rendered="#{AVMBrowseBean.searchContext != null}">
                              <f:facet name="header">
                                 <a:sortLink id="col2-sort" label="#{msg.path}" value="id" styleClass="header"/>
                              </f:facet>
                              <a:actionLink id="col2-act1" value="#{r.displayPath}" actionListener="#{AVMBrowseBean.clickFolder}">
                                 <f:param name="id" value="#{r.parentPath}" />
                              </a:actionLink>
                           </a:column>
                           
                           <%-- Creator column --%>
                           <a:column id="col5" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col5-sort" label="#{msg.creator}" value="creator" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col5-txt" value="#{r.creator}" />
                           </a:column>
                           
                           <%-- Created Date column --%>
                           <a:column id="col6" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col6-sort" label="#{msg.created_date}" value="created" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col6-txt" value="#{r.created}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Modifier column --%>
                           <a:column id="col6_1" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col6_1-sort" label="#{msg.modifier}" value="modifier" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col6_1-txt" value="#{r.modifier}" />
                           </a:column>
                           
                           <%-- Modified Date column --%>
                           <a:column id="col7" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col7-sort" label="#{msg.modified_date}" value="modified" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col7-txt" value="#{r.modified}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Type column --%>
                           <a:column id="col8" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col8-sort" label="#{msg.type}" value="folderType" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col8-text" value="#{r.folderType}"></h:outputText>
                           </a:column>
                           
                           <%-- Folder Actions column --%>
                           <a:column id="col9" actions="true" style="text-align:left">
                              <f:facet name="header">
                                 <h:outputText id="col9-txt" value="#{msg.actions}"/>
                              </f:facet>
                              
                              <%-- actions are configured in web-client-config-wcm-actions.xml --%>
                              <r:actions id="col9-acts1" value="avm_folder_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                           </a:column>
                           
                           <a:dataPager id="pager1" styleClass="pager" />
                        </w:avmList>
                        
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Files --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <h:panelGroup id="files-panel-facets">
                        <f:facet name="title">
                           <a:panel id="page-controls2" style="font-size:9px">
                              <h:outputText value="#{msg.items_per_page}" id="items-txt2"/>
                              <h:inputText id="files-pages" value="#{AVMBrowseBean.pageSizeFilesStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeFiles(event);" />
                              <div style="display:none"><a:actionLink id="files-apply" value="" actionListener="#{AVMBrowseBean.updateFilesPageSize}" /></div>
                           </a:panel>
                        </f:facet>
                     </h:panelGroup>
                     <a:panel id="files-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              facetsId="files-panel-facets" label="#{msg.website_browse_files}">
                        
                        <w:avmList id="files-list" binding="#{AVMBrowseBean.filesRichList}" viewMode="details" pageSize="#{AVMBrowseBean.pageSizeFiles}"
                              styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                              value="#{AVMBrowseBean.files}" var="r">
                           
                           <%-- Primary column for details view mode --%>
                           <a:column id="col10" primary="true" width="200" style="padding:2px;text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col10-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                              </f:facet>
                              <f:facet name="small-icon">
                                 <a:actionLink id="col10-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" showLink="false" styleClass="inlineAction" />
                              </f:facet>
                              <a:actionLink id="col10-act2" value="#{r.name}" href="#{r.url}" target="new" />
                              <w:avmLockIcon id="col10-lock" value="#{r.nodeRef}" align="absmiddle" />
                           </a:column>
                           
                           <%-- Path column for search results mode --%>
                           <a:column id="col11" style="text-align:left" rendered="#{AVMBrowseBean.searchContext != null}">
                              <f:facet name="header">
                                 <a:sortLink id="col11-sort" label="#{msg.path}" value="id" styleClass="header"/>
                              </f:facet>
                              <a:actionLink id="col11-act1" value="#{r.displayPath}" actionListener="#{AVMBrowseBean.clickFolder}">
                                 <f:param name="id" value="#{r.parentPath}" />
                              </a:actionLink>
                           </a:column>
                           
                           <%-- Size column --%>
                           <a:column id="col15" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col15-sort" label="#{msg.size}" value="size" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col15-txt" value="#{r.size}">
                                 <a:convertSize />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Creator column --%>
                           <a:column id="col15a" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col15a-sort" label="#{msg.creator}" value="creator" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col15a-txt" value="#{r.creator}" />
                           </a:column>
                           
                           <%-- Created Date column --%>
                           <a:column id="col16" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col16-sort" label="#{msg.created_date}" value="created" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col16-txt" value="#{r.created}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Modifier column --%>
                           <a:column id="col13" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col13-sort" label="#{msg.modifier}" value="modifier" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col13-txt" value="#{r.modifier}" />
                           </a:column>
                           
                           <%-- Modified Date column --%>
                           <a:column id="col17" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col17-sort" label="#{msg.modified_date}" value="modified" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col17-txt" value="#{r.modified}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Type column --%>
                           <a:column id="col18" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col18-sort" label="#{msg.type}" value="fileType" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col18-txt" value="#{r.fileType}"></h:outputText>
                           </a:column>
                           
                           <%-- Content Actions column --%>
                           <a:column id="col19" actions="true" style="text-align:left">
                              <f:facet name="header">
                                 <h:outputText id="col19-txt" value="#{msg.actions}"/>
                              </f:facet>
                              
                              <%-- actions are configured in web-client-config-wcm-actions.xml --%>
                              <r:actions id="col18-acts1" value="avm_file_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                           </a:column>
                           
                           <a:dataPager id="pager2" styleClass="pager" />
                           
                        </w:avmList>
                        
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width=4 height=4></td>
                  <td width=100% align=center style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width=4 height=4></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>
