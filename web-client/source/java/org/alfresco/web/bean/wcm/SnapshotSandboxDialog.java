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
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Snaphost Sandbox dialog.
 * 
 * @author Kevin Roast
 */
public class SnapshotSandboxDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -7325435181889945320L;

   private static final Log logger = LogFactory.getLog(SnapshotSandboxDialog.class);
   
   private static final String MSG_SNAPSHOT_FAILURE = "snapshot_failure";
   private static final String MSG_SNAPSHOT_SUCCESS = "snapshot_success";

   transient private AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   
   private String label;
   private String description;
   
   
   /**
    * @param avmBrowseBean The avmBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (this.avmService == null)
      {
         this.avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return this.avmService;
   }

   /**
    * @return Returns the snapshot description.
    */
   public String getDescription()
   {
      return this.description;
   }

   /**
    * @param description   The snapshot description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return Returns the snaphost label.
    */
   public String getLabel()
   {
      return this.label;
   }

   /**
    * @param label   The snapshot label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      this.label = null;
      this.description = null;
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // find the previous version - to see if a snapshot was acutally performed
      int oldVersion = this.getAvmService().getLatestSnapshotID(this.avmBrowseBean.getSandbox());
      int version = this.getAvmService().createSnapshot(
            this.avmBrowseBean.getSandbox(), this.label, this.description)
            .get(this.avmBrowseBean.getSandbox());
      if (version > oldVersion)
      {
         // a new snapshot was created
         String msg = MessageFormat.format(Application.getMessage(
               context, MSG_SNAPSHOT_SUCCESS), this.label, this.avmBrowseBean.getSandbox());
         this.avmBrowseBean.displayStatusMessage(context, msg);
      }
      else
      {
         // no changes had occured - no snapshot was required
         String msg = Application.getMessage(context, MSG_SNAPSHOT_FAILURE);
         this.avmBrowseBean.displayStatusMessage(context, msg);
      }
      
      return outcome;
   }
}
