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
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.FormWrapper;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.PresentationTemplate;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UISelectList;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Website Project Form Templates dialog.
 * Launched from the Select Templates button on the Define Web Content Forms page.
 * 
 * @author Kevin Roast
 */
public class FormTemplatesDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 6383166841680919841L;

   private static final String COMPONENT_TEMPLATELIST = "template-list";

   private static final Log logger = LogFactory.getLog(FormTemplatesDialog.class);
   
   transient private AVMService avmService;
   protected CreateWebsiteWizard websiteWizard;
   
   /** datamodel for table of selected presentation templates */
   transient private DataModel templatesDataModel = null;
   
   /** list of objects describing the selected presentation templates*/
   private List<PresentationTemplate> templates = null;
   
   /** transient list of template UIListItem objects */
   private List<UIListItem> templateList = null;
   
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
    * @param wizard        The Create Website Wizard to set.
    */
   public void setCreateWebsiteWizard(CreateWebsiteWizard wizard)
   {
      this.websiteWizard = wizard;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      this.templatesDataModel = null;
      this.templates = new ArrayList<PresentationTemplate>(getActionForm().getTemplates().size());
      this.templates.addAll(getActionForm().getTemplates());
   }

   @Override
   public String getContainerDescription()
   {
      return MessageFormat.format(Application.getBundle(FacesContext.getCurrentInstance()).getString("form_template_templates_desc"), 
                                  this.getActionForm().getName(),
                                  this.websiteWizard.getName());
   }
   
   /**
    * @return an object representing the form for the current action
    */
   public FormWrapper getActionForm()
   {
      return this.websiteWizard.getActionForm();
   }
   
   /**
    * @return JSF data model wrapping the templates selected by the user
    */
   public DataModel getTemplatesDataModel()
   {
      if (this.templatesDataModel == null)
      {
         this.templatesDataModel = new ListDataModel();
      }
      
      // only set the wrapped data once otherwise the rowindex is reset
      if (this.templatesDataModel.getWrappedData() == null)
      {
         this.templatesDataModel.setWrappedData(this.templates);
      }
      
      return this.templatesDataModel;
   }

   /**
    * @param templatesDataModel  JSF data model wrapping the templates
    */
   public void setTemplatesDataModel(DataModel templatesDataModel)
   {
      this.templatesDataModel = templatesDataModel;
   }
   
   /**
    * @return List of UIListItem objects representing the available presentation templates for selection
    */
   public List<UIListItem> getTemplatesList()
   {
      Form form = getActionForm().getForm();
      List<RenderingEngineTemplate> engines = form.getRenderingEngineTemplates();
      List<UIListItem> items = new ArrayList<UIListItem>(engines.size());
      for (RenderingEngineTemplate engine : engines)
      {
         PresentationTemplate wrapper = new PresentationTemplate(engine);
         UIListItem item = new UIListItem();
         item.setValue(wrapper);
         item.setLabel(wrapper.getTitle() + " (" + engine.getMimetypeForRendition() + ")");
         item.setDescription(wrapper.getDescription());
         item.setImage(WebResources.IMAGE_TEMPLATE_32);
         items.add(item);
      }
      this.templateList = items;
      return items;
   }
   
   /**
    * Action handler to add a template to the list for this form
    */
   public void addTemplate(ActionEvent event)
   {
      UISelectList selectList = (UISelectList)event.getComponent().findComponent(COMPONENT_TEMPLATELIST);
      int index = selectList.getRowIndex();
      if (index != -1)
      {
         PresentationTemplate template = (PresentationTemplate)this.templateList.get(index).getValue();
         // clone the PresentationTemplate into one the user can modify
         this.templates.add(new PresentationTemplate(template.getRenderingEngineTemplate(), template.getOutputPathPattern()));
      }
   }
   
   /**
    * Remove a presentation template from the selected list
    */
   public void removeTemplate(ActionEvent event)
   {
      PresentationTemplate wrapper = (PresentationTemplate)this.getTemplatesDataModel().getRowData();
      if (wrapper != null)
      {
         this.templates.remove(wrapper);
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      List<PresentationTemplate> list = getActionForm().getTemplates();
      list.clear();
      for (PresentationTemplate wrapper : this.templates)
      {
         list.add(wrapper);
      }
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
}
