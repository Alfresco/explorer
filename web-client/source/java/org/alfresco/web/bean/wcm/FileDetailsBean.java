/*
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
 */
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.NodeListUtils;
import org.alfresco.web.ui.common.NodePropertyComparator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean for File Details page.
 * 
 * @author Kevin Roast
 */
public class FileDetailsBean extends BaseDialogBean implements NavigationSupport
{
   private static final long serialVersionUID = -3263315503769148385L;
   
   /** Action service bean reference */
   transient private ActionService actionService;
   
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param actionService    The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   private ActionService getActionService()
   {
      if (actionService == null)
      {
         actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
   }
   
   /**
    * @return a Node wrapper of the AVM File Node - for property sheet support
    */
   public Node getDocument()
   {
	   return this.browseBean.getActionSpace();
   }
   
   /**
    * Returns the URL to the content for the current document
    *  
    * @return Content url to the current document
    */
   public String getBrowserUrl()
   {
      return DownloadContentServlet.generateBrowserURL(getDocument().getNodeRef(), getDocument().getName());
   }

   /**
    * Returns the download URL to the content for the current document
    *  
    * @return Download url to the current document
    */
   public String getDownloadUrl()
   {
      return DownloadContentServlet.generateDownloadURL(getDocument().getNodeRef(), getDocument().getName());
   }
   
   /**
    * @return The 32x32 filetype icon for the file
    */
   public String getFileType32()
   {
      return FileTypeImageUtils.getFileTypeImage(getDocument().getName(), false);
   }
   
   /**
    * @return version history list for a node
    */
   public List<Map<String, Object>> getVersionHistory()
   {
       // FIXME use VersionService
      List<Map<String, Object>> wrappers = new ArrayList<Map<String, Object>>(0);
      return wrappers;
   }
   
   /**
    * Revert a node back to a previous version
    */
   public void revertNode(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      int version = Integer.parseInt(params.get("version"));
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, false);
         tx.begin();
         
         // FIXME revert using VersionService
         
         tx.commit();
      }
      catch (Throwable err)
      {
         err.printStackTrace(System.err);
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();      
       return Application.getMessage(fc, "details_of") + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   public String getCurrentItemId()
   {
      return getDocument().getId();
   }

   public String getOutcome()
   {
      return "dialog:close:dialog:showFileDetails";
   }
   
   /**
    * Navigates to next item in the list of items for the current folder
    */
   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String path = params.get("id");
      if (path != null && path.length() != 0)
      {
         this.browseBean.setupContentAction(getCurrentItemId(), false);
         List<Node> nodes = this.browseBean.getNodes();
         if (nodes.size() > 1)
         {
            String currentSortColumn;
            boolean currentSortDescending;
            if (nodes.get(0).hasProperty(ContentModel.PROP_CONTENT.toPrefixString(this.getNamespaceService())))
            {
               currentSortColumn = this.browseBean.getContentRichList().getCurrentSortColumn();
               currentSortDescending = this.browseBean.getContentRichList().isCurrentSortDescending();
            }
            else
            {
               currentSortColumn = this.browseBean.getSpacesRichList().getCurrentSortColumn();
               currentSortDescending = this.browseBean.getSpacesRichList().isCurrentSortDescending();
            }

            if (currentSortColumn != null)
            {
               Collections.sort(nodes, new NodePropertyComparator(currentSortColumn, !currentSortDescending));
            }
                  
            Node next = NodeListUtils.nextItem(nodes, path);
            this.browseBean.setupContentAction(next.getPath(), false);
         }
      }
   }
   
   /**
    * Navigates to the previous item in the list of items for the current folder
    */
   public void previousItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String path = params.get("id");
      if (path != null && path.length() != 0)
      {
         this.browseBean.setupContentAction(getCurrentItemId(), false);
         List<Node> nodes = this.browseBean.getNodes();
         if (nodes.size() > 1)
         {
            String currentSortColumn;
            boolean currentSortDescending;
            if (nodes.get(0).hasProperty(ContentModel.PROP_CONTENT.toPrefixString(this.getNamespaceService())))
            {
               currentSortColumn = this.browseBean.getContentRichList().getCurrentSortColumn();
               currentSortDescending = this.browseBean.getContentRichList().isCurrentSortDescending();
            }
            else
            {
               currentSortColumn = this.browseBean.getSpacesRichList().getCurrentSortColumn();
               currentSortDescending = this.browseBean.getSpacesRichList().isCurrentSortDescending();
            }

            if (currentSortColumn != null)
            {
               Collections.sort(nodes, new NodePropertyComparator(currentSortColumn, !currentSortDescending));
            }
                  
            Node previous = NodeListUtils.previousItem(nodes, path);
            this.browseBean.setupContentAction(previous.getPath(), false);
         }
      }
   }
}
