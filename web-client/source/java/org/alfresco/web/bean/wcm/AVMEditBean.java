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
import java.text.MessageFormat;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the edit pages for a AVM node content.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public class AVMEditBean extends BaseDialogBean
{
   private static final long serialVersionUID = -6662866123545412556L;

   private static final Log LOGGER = LogFactory.getLog(AVMEditBean.class);
   
   private static final String MSG_ERROR_UPDATE = "error_update";
   private static final String MSG_UPLOAD_SUCCESS = "file_upload_success";
   private static final String MSG_APPLY_RSS_FEED= "apply_rss_feed";
   private static final String MSG_UPDATE = "update";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   private File file = null;
   private String fileName = null;

   transient private AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   transient private FormsService formsService;
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return avmService;
   }

   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   protected FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }


   /**
    * @return Returns the current AVM node context.
    */
   public AVMNode getAvmNode()
   {
      return this.avmBrowseBean.getAvmActionNode();
   }
   
   /**
    * @return Large file icon for current AVM node
    */
   public String getFileType32()
   {
      return FileTypeImageUtils.getFileTypeImage(getAvmNode().getName(), false);
   }
   
   /**
    * @return Small file icon for current AVM node
    */
   public String getFileType16()
   {
      return FileTypeImageUtils.getFileTypeImage(getAvmNode().getName(), true);
   }
   
   /**
    * @return Returns the name of the file
    */
   public String getFileName()
   {
      // try and retrieve the file and filename from the file upload bean
      // representing the file we previously uploaded.
      final FacesContext ctx = FacesContext.getCurrentInstance();
      final FileUploadBean fileBean = (FileUploadBean)
         ctx.getExternalContext().getSessionMap().get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         this.file = fileBean.getFile();
         this.fileName = fileBean.getFileName();
      }
      
      return this.fileName;
   }
   
   /**
    * @return Returns the message to display when a file has been uploaded
    */
   public String getFileUploadSuccessMsg()
   {
      final String msg = Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPLOAD_SUCCESS);
      return MessageFormat.format(msg, new Object[] { Utils.encode(this.getFileName()) });
   }

   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action called upon completion of the Update File page
    */
   public String updateFileOK()
   {
      String outcome = null;
      
      UserTransaction tx = null;
      
      AVMNode node = getAvmNode();
      if (node != null && this.getFileName() != null)
      {
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // get an updating writer that we can use to modify the content on the current node
            final ContentWriter writer = this.getAvmService().getContentWriter(node.getPath(), true);
            
            // also update the mime type in case a different type of file is uploaded
            String mimeType = Repository.getMimeTypeForFileName(context, this.fileName);
            writer.setMimetype(mimeType);
            writer.putContent(this.file);            
            
            // commit the transaction
            tx.commit();
            if (this.getAvmService().hasAspect(-1, node.getPath(), WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
            {
               this.regenerateRenditions();
            }
            // Possibly notify virt server
            AVMUtil.updateVServerWebapp(node.getPath(), false);
            
            // clear action context
            resetState();
            
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable err)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE) + err.getMessage(), err);
         }
      }
      
      return outcome;
   }
   
   /**
    * Deals with the cancel button being pressed on the upload file page
    */
   public String cancel()
   {
      // reset the state
      this.resetState();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   private void resetState()
   {
      // clean up and clear action context
      removeUploadedFile();
   }
   
   /**
    * Clear form state and upload file bean
    */
   public void removeUploadedFile()
   {
      // delete the temporary file we uploaded earlier
      if (this.file != null)
      {
         this.file.delete();
      }
      
      this.file = null;
      this.fileName = null;
      
      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
   }

   private void regenerateRenditions()
      throws FormNotFoundException
   {
      final String avmPath = this.getAvmNode().getPath();
      final FormInstanceData fid = this.getFormsService().getFormInstanceData(-1, avmPath);
      final List<FormInstanceData.RegenerateResult> result = fid.regenerateRenditions();
      for (FormInstanceData.RegenerateResult rr : result)
      {
         if (rr.getException() != null)
         {
            Utils.addErrorMessage("error regenerating rendition using " + rr.getRenderingEngineTemplate().getName() + 
                                  ": " + rr.getException().getMessage(),
                                  rr.getException());
         }
      }
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return updateFileOK();
   }
   
   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_UPDATE) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getAvmNode().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return getFileName() == null;
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      // TODO Auto-generated method stub
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPDATE);
   }
}
