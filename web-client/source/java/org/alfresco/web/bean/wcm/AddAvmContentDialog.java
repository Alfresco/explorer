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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.AddContentDialog;
import org.alfresco.web.bean.repository.Repository;

/**
 * Add/upload content dialog for AVM browse screens.
 * 
 * @author Kevin Roast
 */
public class AddAvmContentDialog extends AddContentDialog
{
   private static final long serialVersionUID = 4019639621892035132L;

   private static final String MSG_OK = "ok";

   /** The AVMService bean reference */
   transient private AVMService avmService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;

   /**  */
   protected String path;
   
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingAwareService();
      }
      return avmService;
   }
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * Save the specified content using the currently set wizard attributes
    * 
    * @param fileContent      File content to save
    * @param strContent       String content to save
    */
   protected void saveContent(File fileContent, String strContent) throws Exception
   {
      // get the AVM path that will contain the content
      String parent = this.avmBrowseBean.getCurrentPath();
      
      // create the file
      this.getAvmService().createFile(parent, this.fileName).close();
      this.path = parent + '/' + this.fileName;
      NodeRef fileNodeRef = AVMNodeConverter.ToNodeRef(-1, this.path);
      
      if (logger.isDebugEnabled())
         logger.debug("Created AVM file: " + this.path);
      
      // apply the titled aspect - title and description
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.title);
      titledProps.put(ContentModel.PROP_DESCRIPTION, this.description);
      this.getNodeService().addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);
      
      // get a writer for the content and put the file
      ContentWriter writer = getContentService().getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(this.mimeType);
      writer.setEncoding(this.encoding);
      if (fileContent != null)
      {
         writer.putContent(fileContent);
      }
      else 
      {
         writer.putContent(strContent == null ? "" : strContent);
      }
      
      // remember the created node now
      this.createdNode = fileNodeRef;
   }

   /**
    * @see org.alfresco.web.bean.content.AddContentDialog#doPostCommitProcessing(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      clearUpload();

      // Notify virtualization server 
      //
      // This must be done in doPostCommitProcessing so that the notification
      // can only be received by the virtualization server *after* the content
      // update transaction within the AVM has completed.  Otherwise, there's 
      // a race condition that can cause the virtualization server to not be 
      // able to read the new (or modified) web.xml file within the virtual 
      // webapps being relaoded via the call to updateVServerWebapp.

      if (logger.isDebugEnabled())
      {
         logger.debug("Reloading virtualisation server on path: " + this.path);
      }

      AVMUtil.updateVServerWebapp(this.path, false);
      
      return outcome;
   }

   /**
    * @see org.alfresco.web.bean.content.AddContentDialog#getDefaultFinishOutcome()
    */
   @Override
   protected String getDefaultFinishOutcome()
   {
      return "dialog:close";
   }
   
   @Override
   public String cancel()
   {
      super.cancel();
      return getDefaultCancelOutcome();
   }
   
   @Override
   public String getFinishButtonLabel()
   {
    
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_OK);
   }
}
