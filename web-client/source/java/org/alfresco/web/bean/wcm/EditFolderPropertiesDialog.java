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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.LayeredFolderType;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.EditSpaceDialog;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Backing bean for the Edit Folder Properties dialog.
 * 
 * @author Kevin Roast
 */
public class EditFolderPropertiesDialog extends EditSpaceDialog
{
   private static final long serialVersionUID = -6423913727249054187L;
   
   protected AVMBrowseBean avmBrowseBean;
   transient private AVMService avmService;
   
   
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
         avmService = (AVMService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "AVMLockingAwareService");
      }
      return avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.spaces.EditSpaceDialog#initEditableNode()
    */
   @Override
   protected Node initEditableNode()
   {
      return new Node(this.avmBrowseBean.getAvmActionNode().getNodeRef());
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // update the existing node in the repository
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // handle the name property separately, it is a special case for AVM nodes
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         editedProps.remove(ContentModel.PROP_NAME);
      }
      
      // get the current set of properties from the repository
      Map<QName, Serializable> repoProps = this.getNodeService().getProperties(nodeRef);
      
      // add the "uifacets" aspect if required, properties will get set below
      if (this.getNodeService().hasAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS) == false)
      {
         this.getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, null);
      }
      
      // overwrite the current properties with the edited ones
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
      
      // perform the rename last as for an AVM it changes the NodeRef
      if (name != null)
      {
         // OnMoveNodePolicy behavior differs for Web-Client and external calls (FTP, CIFS)
         // Flag is set for the current thread
         Boolean wasIssuedByWebClient = LayeredFolderType.isIssuedByWebClient();
         try
         {
            LayeredFolderType.setIssuedByWebClient(true);
            this.getFileFolderService().rename(nodeRef, name);
            editedProps.put(ContentModel.PROP_NAME.toString(), name);
         }
         finally
         {
            LayeredFolderType.setIssuedByWebClient(wasIssuedByWebClient);
         }

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
   
   public List<UIListItem> getIcons()
   {
      List<UIListItem> icons = new ArrayList<UIListItem>(1);

      UIListItem item = new UIListItem();
      item.setValue(DEFAULT_SPACE_ICON_NAME);
      item.setImage("/images/icons/" + DEFAULT_SPACE_ICON_NAME + ".gif");
      icons.add(item);

      return icons;
   }
}
