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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.NodeListUtils;
import org.alfresco.web.ui.common.NodePropertyComparator;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean for Folder Details page.
 * 
 * @author Kevin Roast
 */
public class FolderDetailsBean extends BaseDialogBean implements NavigationSupport
{
   private static final long serialVersionUID = -2668158215990649862L;

   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   /**
    * @return a Node wrapper of the AVM Folder Node - for property sheet support
    */
   public Node getFolder()
   {
      return this.browseBean.getActionSpace();
   }
   
   /**
    * @return true if the folder is a layered folder with a primary indirection
    */
   public boolean getIsPrimaryLayeredFolder()
   {
      return false;
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
       return Application.getMessage(fc, "details_of") + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getFolder().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   public String getCurrentItemId()
   {
      return getFolder().getId();
   }

   public String getOutcome()
   {
      return "dialog:close:dialog:showFolderDetails";
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
