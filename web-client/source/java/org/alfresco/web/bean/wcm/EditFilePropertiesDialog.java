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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.content.EditContentPropertiesDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.ui.common.Utils;

/**
 * Backing bean for the Edit File Properties dialog.
 * 
 * @author Kevin Roast
 */
public class EditFilePropertiesDialog extends EditContentPropertiesDialog
{
   private static final long serialVersionUID = 635722726225138092L;
   
   protected AVMBrowseBean avmBrowseBean;
   transient private AVMService avmService;
   transient private FormsService formsService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
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
    * @param formsService       The FormsService to set.
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

   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation

   /**
    * @see org.alfresco.web.bean.content.EditContentPropertiesDialog#initEditableNode()
    */
   protected Node initEditableNode()
   {
      return new Node(this.avmBrowseBean.getAvmActionNode().getNodeRef());
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // handle the name property separately, it is a special case for AVM nodes
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         editedProps.remove(ContentModel.PROP_NAME);
      }
      
      // we need to put all the properties from the editable bag back into 
      // the format expected by the repository
      Map<QName, Serializable> repoProps = this.getNodeService().getProperties(nodeRef);
      
      // but first extract and deal with the special mimetype property for ContentData
      String mimetype = (String)editedProps.get(TEMP_PROP_MIMETYPE);
      if (mimetype != null)
      {
         // remove temporary prop from list so it isn't saved with the others
         editedProps.remove(TEMP_PROP_MIMETYPE);
         ContentData contentData = (ContentData)editedProps.get(ContentModel.PROP_CONTENT);
         if (contentData != null)
         {
            contentData = ContentData.setMimetype(contentData, mimetype);
            editedProps.put(ContentModel.PROP_CONTENT.toString(), contentData);
         }
      }
      
      // add the "titled" aspect if required, properties will get set below
      if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
      {
         getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
      }
      
      // add the remaining properties
      Iterator<String> iterProps = editedProps.keySet().iterator();
      while (iterProps.hasNext())
      {
         String propName = iterProps.next();
         QName qname = QName.createQName(propName);
         
         // make sure the property is represented correctly
         Serializable propValue = (Serializable)editedProps.get(propName);
         
         // check for empty strings when using number types, set to null in this case
         if ((propValue != null) && (propValue instanceof String) && 
             (propValue.toString().length() == 0))
         {
            PropertyDefinition propDef = this.getDictionaryService().getProperty(qname);
            if (propDef != null)
            {
               if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                   propDef.getDataType().getName().equals(DataTypeDefinition.INT) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
               {
                  propValue = null;
               }
            }
         }
         
         repoProps.put(qname, propValue);
      }
      
      // Translate to what AVMService wants to take.
      DictionaryService dd = Repository.getServiceRegistry(context).getDictionaryService();
      Map<QName, PropertyValue> avmProps = new HashMap<QName, PropertyValue>();
      for (Map.Entry<QName, Serializable> entry : repoProps.entrySet())
      {
         PropertyDefinition propDef = dd.getProperty(entry.getKey());
         if (propDef != null)
         {
             avmProps.put(entry.getKey(), new PropertyValue(propDef.getDataType().getName(), entry.getValue()));
         }
      }
      // send the properties back to the repository
      this.getAvmService().setNodeProperties(AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond(), avmProps);
      
      // perform the rename last as for an AVM it changes the NodeRef, but only if the name has changed!
      String path = AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond();
      final String parentPath = AVMNodeConverter.SplitBase(path)[0];
      final String oldName = AVMNodeConverter.SplitBase(path)[1];
      
      if (name != null && name.equals(oldName) == false)
      {
         if (this.getNodeService().hasAspect(nodeRef, WCMAppModel.ASPECT_RENDITION))
         {
            throw new UnsupportedOperationException(this.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME) +
                                                    " is a " + WCMAppModel.ASPECT_RENDITION +
                                                    " and cannot be renamed");
         }

         // need to find out if it's a form instance data before rename.  for whatever reason,
         // afterwards it claims it is not
         if (this.getNodeService().hasAspect(nodeRef, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            final FormInstanceData fid = this.getFormsService().getFormInstanceData(nodeRef);
            // delete all existing renditions
            for (final Rendition r : fid.getRenditions())
            {
               this.getAvmService().removeNode(r.getPath());
            }
            this.getNodeService().removeProperty(nodeRef, WCMAppModel.PROP_RENDITIONS);
         }

         this.getAvmService().rename(parentPath, oldName, parentPath, name);
         nodeRef = AVMNodeConverter.ToNodeRef(-1, AVMNodeConverter.ExtendAVMPath(parentPath, name));

         if (this.getNodeService().hasAspect(nodeRef, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            final FormInstanceData fid = this.getFormsService().getFormInstanceData(nodeRef);
            for (final FormInstanceData.RegenerateResult rr : fid.regenerateRenditions())
            {
               if (rr.getException() != null)
               {
                  outcome = null;
                  Utils.addErrorMessage("error regenerating rendition using " + rr.getRenderingEngineTemplate().getName() + 
                                        ": " + rr.getException().getMessage(),
                                        rr.getException());
               }
            }
         }
      }
      
      // add the name property back to the properties map
      if (name != null)
      {
         editedProps.put(ContentModel.PROP_NAME.toString(), name);
      }
      
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // a rename may have occured - we need to reset the NodeRef of the modified AVM Node
      // as an AVM NodeRef contains the name as part of ref - which can therefore change! 
      String name = this.editableNode.getName();
      String oldPath = AVMNodeConverter.ToAVMVersionPath(this.editableNode.getNodeRef()).getSecond();
      String newPath = oldPath.substring(0, oldPath.lastIndexOf('/') + 1) + name;
      this.avmBrowseBean.setAvmActionNode(new AVMNode(this.getAvmService().lookup(-1, newPath)));
      
      return outcome;
   }
   
   /**
    * Formats the error message to display if an error occurs during finish processing
    * 
    * @param exception The exception
    * @return The formatted message
    */
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS), 
               ((FileExistsException)exception).getName());
      }
      else if (exception instanceof InvalidNodeRefException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), 
               new Object[] {this.avmBrowseBean.getAvmActionNode().getPath()});
      }
      else
      {
         return super.formatErrorMessage(exception);
      }
   }
}
