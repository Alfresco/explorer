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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.RenderingEngineTemplateImpl;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.RenderingEngine.TemplateNotFoundException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UIListItems;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author arielb
 */
public class RegenerateRenditionsWizard
   extends BaseWizardBean
{
   private static final long serialVersionUID = -8573877482412328963L;
   
   public final String REGENERATE_SCOPE_ALL = "all";
   public final String REGENERATE_SCOPE_FORM = "form";
   public final String REGENERATE_SCOPE_RENDERING_ENGINE_TEMPLATE = "rendering_engine_template";

   private final static Log logger = LogFactory.getLog(RegenerateRenditionsWizard.class); 

   transient protected WebProjectService wpService;
   transient private AVMLockingService avmLockingService;
   transient private AVMService avmService;
   transient private AVMSyncService avmSyncService;
   transient private SearchService searchService;
   transient private FormsService formsService;
   transient private PermissionService permissionService;
   
   private WebProject selectedWebProject;
   private String[] selectedForms;
   private String[] selectedRenderingEngineTemplates;
   private UIRichList renditionChoicesRichList;
   private List<Rendition> regeneratedRenditions;
   private String regenerateScope;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(final FacesContext context, final String outcome)
      throws Exception
   {
      if (this.regeneratedRenditions != null)
      {
         final List<AVMDifference> diffList = new ArrayList<AVMDifference>(this.regeneratedRenditions.size());
         for (final Rendition r : this.regeneratedRenditions)
         {
            diffList.add(new AVMDifference(-1, r.getPath(), 
                                           -1, AVMUtil.getCorrespondingPathInMainStore(r.getPath()), 
                                           AVMDifference.NEWER));
         }
         
         if (logger.isDebugEnabled())
         {
            logger.debug("updating " + diffList.size() + " renditions in staging");
         }
         
         getAvmSyncService().update(diffList, null, true, true, true, true, null, null);
         String description = null;
         final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
         if (this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
         {
            description = MessageFormat.format(bundle.getString("regenerate_renditions_snapshot_description_scope_form"),
                                               StringUtils.join(this.selectedForms, ", "));
         }
         else if (this.regenerateScope.equals(REGENERATE_SCOPE_RENDERING_ENGINE_TEMPLATE))
         {
            description = MessageFormat.format(bundle.getString("regenerate_renditions_snapshot_description_scope_rendering_engine_template"),
                                               StringUtils.join(this.selectedRenderingEngineTemplates, ", "));
         }
         else
         {
            description = MessageFormat.format(bundle.getString("regenerate_renditions_snapshot_description_scope_web_project"),
                                               this.selectedWebProject.getName());
         }
         getAvmService().createSnapshot(this.selectedWebProject.getStoreId(),
                                        MessageFormat.format(bundle.getString("regenerate_renditions_snapshot_short_description"), 
                                                             diffList.size()),
                                        description);
      }
      return outcome;
   }

   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      this.selectedWebProject = null;
      this.selectedForms = null;
      this.selectedRenderingEngineTemplates = null;
      this.renditionChoicesRichList = null;
      this.regeneratedRenditions = null;
      this.regenerateScope = REGENERATE_SCOPE_ALL;
      if (this.browseBean.getDocument() != null)
      {
         if (this.browseBean.getDocument().hasAspect(WCMAppModel.ASPECT_FORM))
         {
            this.selectedForms = new String[] { this.browseBean.getDocument().getName() };
         }
      }
      else if (this.browseBean.getActionSpace() != null)
      {
         this.selectedForms = new String[] { this.browseBean.getActionSpace().getName() };
      }
   }
   
   @Override
   public String next()
   {
      final int step = Application.getWizardManager().getCurrentStep();
      if (step == 2)
      {
         try
         {
            this.regeneratedRenditions = this.regenerateRenditions();
         }
         catch (Exception e)
         {
            Application.getWizardManager().getState().setCurrentStep(step - 1);
            Utils.addErrorMessage(e.getMessage(), e);
         }
      }
      return super.next();
   }

   @Override
   public String cancel()
   {
      if (this.selectedWebProject != null)
      {
         final String stagingStoreName = this.selectedWebProject.getStoreId();
         final String previewStoreName = AVMUtil.getCorrespondingPreviewStoreName(stagingStoreName);
         getAvmSyncService().resetLayer(AVMUtil.buildStoreRootPath(previewStoreName));
      }
      return super.cancel();
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      boolean disabled = false;
      if ("select_renditions".equals(Application.getWizardManager().getCurrentStepName()))
      {
         disabled = this.selectedWebProject == null;
      }
      return disabled;
   }

   @Override
   public String getStepDescription()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      final String stepName = Application.getWizardManager().getCurrentStepName();
      if ("summary".equals(stepName))
      {
         final String s = this.selectedWebProject.getTitle();
         return MessageFormat.format(bundle.getString("regenerate_renditions_summary_desc"), 
                                     this.regeneratedRenditions.size(),
                                     s != null && s.length() != 0 ? s : this.selectedWebProject.getName());
      }
      else
      {
         return super.getContainerDescription();
      }
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   public String getRegenerateScope()
   {
      return this.regenerateScope;
   }

   public void setRegenerateScope(final String regenerateScope)
   {
      this.regenerateScope = regenerateScope;
   }

   public List<SelectItem> getWebProjectChoices()
   {
      List<WebProjectInfo> wpInfos = getWebProjectService().listWebProjects();
      List<SelectItem> result = new ArrayList<SelectItem>(wpInfos.size());
      
      QuickSort sorter = new QuickSort((List<WebProjectInfo>)wpInfos, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      for (WebProjectInfo wpInfo : wpInfos)
      {
         if(getPermissionService().hasPermission(wpInfo.getNodeRef(), PermissionService.WCM_CONTENT_MANAGER) == AccessStatus.ALLOWED) {
             // display only web projects to which the authenticated user has CONTENT MANAGER rights
             String s = wpInfo.getTitle();
             if (this.selectedWebProject == null)
             {
                this.selectedWebProject = new WebProject(wpInfo.getNodeRef());
             }
             result.add(new SelectItem(wpInfo.getNodeRef().toString(), s != null && s.length() != 0 ? s : wpInfo.getName()));
         }
      }
      return result;
   }

   public String getSelectedWebProject()
   {
      return this.selectedWebProject == null ? null : this.selectedWebProject.getNodeRef().toString();
   }
   
   public void setSelectedWebProject(final String webProject)
   {
      this.selectedWebProject = (webProject == null || webProject.length() == 0 
                                 ? null 
                                 : new WebProject(new NodeRef(webProject)));

      final UIViewRoot c = FacesContext.getCurrentInstance().getViewRoot();
      ((UIListItems)c.findComponent("wizard:wizard-body:select_list_form_choices:list_items_form_choices")).setValue(null);
      ((UIListItems)c.findComponent("wizard:wizard-body:select_list_rendering_engine_template_choices:list_items_rendering_engine_template_choices")).setValue(null);
      this.renditionChoicesRichList = null;
   }

   public List<UIListItem> getFormChoices()
   {
      final List<UIListItem> result = new LinkedList<UIListItem>();
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      if (this.selectedWebProject != null)
      {
         for (final Form f : this.selectedWebProject.getForms())
         {
            final UIListItem item = new UIListItem();
            item.setValue(f.getName());
            item.setLabel(f.getTitle());
            final List<FormInstanceData> fids = this.getRelatedFormInstanceData(this.selectedWebProject, f);

            item.setDescription(MessageFormat.format(bundle.getString("regenerate_renditions_select_renditions_select_item_desc"), 
                                                     fids.size() * f.getRenderingEngineTemplates().size(),
                                                     this.selectedWebProject.getName()));
            item.setImage("/images/icons/webform_large.gif");
            result.add(item);
         }
      }
      return result;
   }

   public String[] getSelectedForms()
   {
      return this.selectedForms;
   }

   public void setSelectedForms(final String[] forms)
   {
      this.selectedForms = forms == null || forms.length == 0 ? null : forms;
   }

   public String getSelectedForm()
   {
      return this.selectedForms != null && this.selectedForms.length != 0 ? this.selectedForms[0] : null;
   }

   public void setSelectedForm(final String form)
   {
      this.selectedForms = form == null || form.length() == 0 ? null : new String[] { form };
      this.renditionChoicesRichList = null;
   }

   public List<UIListItem> getRenderingEngineTemplateChoices()
   {
      final List<UIListItem> result = new LinkedList<UIListItem>();      
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      for (final Form f : this.selectedWebProject.getForms())
      {
         for (final RenderingEngineTemplate ret : f.getRenderingEngineTemplates())
         {
            final UIListItem item = new UIListItem();
            item.setValue(f.getName() + ":" + ret.getName());
            item.setLabel(ret.getTitle() + "(" + ret.getMimetypeForRendition() + ")");
            final List<Rendition> rs = this.getRelatedRenditions(this.selectedWebProject, ret);
            item.setDescription(MessageFormat.format(bundle.getString("regenerate_renditions_select_renditions_select_item_desc"), 
                                                     rs.size(),
                                                     this.selectedWebProject.getName()));
            item.setImage(FileTypeImageUtils.getFileTypeImage(ret.getName(), false));
            result.add(item);
         }
      }
      return result;
   }

   public String[] getSelectedRenderingEngineTemplates()
   {
      return this.selectedRenderingEngineTemplates;
   }

   public void setSelectedRenderingEngineTemplates(final String[] renderingEngineTemplates)
   {
      this.selectedRenderingEngineTemplates = renderingEngineTemplates;
      this.renditionChoicesRichList = null;
   }


   public List<Rendition> getRegeneratedRenditions()
   {
      return this.regeneratedRenditions;
   }

   public void setRegeneratedRenditionsRichList(UIRichList richList)
   {
      this.renditionChoicesRichList = richList;
   }
   
   public UIRichList getRegeneratedRenditionsRichList()
   {
      return this.renditionChoicesRichList;
   }

   // ------------------------------------------------------------------------------
   // Service Injection

   /**
    * @param wpService The WebProjectService to set.
    */
   public void setWebProjectService(WebProjectService wpService)
   {
      this.wpService = wpService;
   }
   
   protected WebProjectService getWebProjectService()
   {
      if (wpService == null)
      {
          wpService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWebProjectService();
      }
      return wpService;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   private AVMService getAvmService()
   {
      if (this.avmService == null)
      {
         this.avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return this.avmService;
   }

   /**
    * @param avmLockingService       The AVMLockingService to set.
    */
   public void setAvmLockingService(final AVMLockingService avmLockingService)
   {
      this.avmLockingService = avmLockingService;
   }

   private AVMLockingService getAvmLockingService()
   {
      if (this.avmLockingService == null)
      {
         this.avmLockingService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingService();
      }
      return this.avmLockingService;
   }

   /**
    * @param avmSyncService       The AVMSyncService to set.
    */
   public void setAvmSyncService(final AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }

   private AVMSyncService getAvmSyncService()
   {
      if (this.avmSyncService == null)
      {
         this.avmSyncService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMSyncService();
      }
      return this.avmSyncService;
   }

   /**
    * @param searchService       The SearchService to set.
    */
   public void setSearchService(final SearchService searchService)
   {
      this.searchService = searchService;
   }

   protected SearchService getSearchService()
   {
      if (this.searchService == null)
      {
         this.searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
      }
      return this.searchService;
   }

   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   private FormsService getFormsService()
   {
      if (this.formsService == null)
      {
         this.formsService = (FormsService)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return this.formsService;
   }

   /**
    * @param formsService    The FormsService to set.
    */
   public void setPermissionService(final PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   private PermissionService getPermissionService()
   {
      if (this.permissionService == null)
      {
         this.permissionService = (PermissionService)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "PermissionService");
      }
      return this.permissionService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper Methods

   private List<FormInstanceData> getRelatedFormInstanceData(final WebProject webProject, final Form f)
   {
      final SearchParameters sp = new SearchParameters();
      final StoreRef storeRef = AVMNodeConverter.ToStoreRef(webProject.getStagingStore());
      sp.addStore(storeRef);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      final StringBuilder query = new StringBuilder(256);
      query.append("+ASPECT:\"" + WCMAppModel.ASPECT_FORM_INSTANCE_DATA + "\"");
      query.append(" -ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      query.append(" +@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_FORM_NAME) + 
                   ":\"" + f.getName() + "\"");
      
      if (logger.isDebugEnabled())
      {
         logger.debug("running query " + query);
      }
      
      sp.setQuery(query.toString());
      final ResultSet rs = getSearchService().query(sp);
      try
      {
         final List<FormInstanceData> result = new ArrayList<FormInstanceData>(rs.length());
         for (final ResultSetRow row : rs)
         {
            final String avmPath = AVMNodeConverter.ToAVMVersionPath(row.getNodeRef()).getSecond();
            final String previewAvmPath = AVMUtil.getCorrespondingPathInPreviewStore(avmPath);
            
            try
            {
                FormInstanceData fid = getFormsService().getFormInstanceData(-1, previewAvmPath);
                result.add(fid);
            }
            catch (final FormNotFoundException fnfe)
            {
                // ignore
            }
         }
         
         return result;
      }
      finally
      {
         rs.close();
      }
   }

   private List<Rendition> getRelatedRenditions(final WebProject webProject, final RenderingEngineTemplate ret)
   {
      final SearchParameters sp = new SearchParameters();
      final StoreRef storeRef = AVMNodeConverter.ToStoreRef(webProject.getStagingStore());
      sp.addStore(storeRef);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      final StringBuilder query = new StringBuilder();
      query.append("+ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      query.append(" +@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE) + 
                   ":\"" + ((RenderingEngineTemplateImpl)ret).getNodeRef() + "\"");
      
      if (logger.isDebugEnabled())
      {
         logger.debug("running query " + query);
      }
      
      sp.setQuery(query.toString());
      final ResultSet rs = getSearchService().query(sp);
      try
      {
         final List<Rendition> result = new ArrayList<Rendition>(rs.length()); 
         for (final ResultSetRow row : rs)
         {
            final String avmPath = AVMNodeConverter.ToAVMVersionPath(row.getNodeRef()).getSecond();
            final String previewAvmPath = AVMUtil.getCorrespondingPathInPreviewStore(avmPath);
            result.add(getFormsService().getRendition(-1, previewAvmPath));
         }
         return result;
      }
      finally
      {
         rs.close();
      }
   }

   private List<Rendition> regenerateRenditions()
   {
      final String stagingStoreName = this.selectedWebProject.getStoreId();
      final String previewStoreName = AVMUtil.getCorrespondingPreviewStoreName(stagingStoreName);
      getAvmSyncService().resetLayer(AVMUtil.buildStoreRootPath(previewStoreName));

      final SearchParameters sp = new SearchParameters();
      final StoreRef storeRef = AVMNodeConverter.ToStoreRef(this.selectedWebProject.getStagingStore());
      sp.addStore(storeRef);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      StringBuilder query = new StringBuilder(128);
      if (this.regenerateScope.equals(REGENERATE_SCOPE_ALL) ||
          this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
      {
         query.append("+ASPECT:\"" + WCMAppModel.ASPECT_FORM_INSTANCE_DATA + "\"");
         query.append(" -ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      }
      else
      {
         query.append("+ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      }

      if (this.regenerateScope.equals(REGENERATE_SCOPE_FORM) && 
          this.selectedForms != null)
      {
         query.append(" +(");
         for (int i = 0; i < this.selectedForms.length; i++)
         {
            query.append("@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_FORM_NAME) + 
                         ":\"" + this.selectedForms[i] + "\"");
            if (i != this.selectedForms.length - 1)
            {
               query.append(" OR ");
            }
         }
         query.append(") ");
      }
      
      if (this.regenerateScope.equals(REGENERATE_SCOPE_RENDERING_ENGINE_TEMPLATE) &&
          this.selectedRenderingEngineTemplates != null)
      {
         query.append(" +(");
         for (int i = 0; i < this.selectedRenderingEngineTemplates.length; i++)
         {
            String[] parts = this.selectedRenderingEngineTemplates[i].split(":");
            String formName = parts[0];
            String templateName = parts[1];
            try
            {
               Form f = this.selectedWebProject.getForm(formName);
               RenderingEngineTemplate ret = 
                  f.getRenderingEngineTemplate(templateName);
               query.append("@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE) + 
                            ":\"" + ((RenderingEngineTemplateImpl)ret).getNodeRef() + "\"");
               
               if (i != this.selectedRenderingEngineTemplates.length - 1)
               {
                  query.append(" OR ");
               }
            }
            catch (FormNotFoundException fnfe)
            {
               logger.warn("regenerating renditions of template " + templateName + ": " + fnfe.getMessage(), fnfe);
            }
         }
         query.append(") ");
      }

      if (logger.isDebugEnabled())
      {
         logger.debug("running query " + query);
      }
      
      sp.setQuery(query.toString());
      final ResultSet rs = getSearchService().query(sp);
      try
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("received " + rs.length() + " results");
         }
         
         final List<Rendition> result = new ArrayList<Rendition>(rs.length());
         for (final ResultSetRow row : rs)
         {
            final String avmPath = AVMNodeConverter.ToAVMVersionPath(row.getNodeRef()).getSecond();
            final String previewAvmPath = AVMUtil.getCorrespondingPathInPreviewStore(avmPath);
            if (this.regenerateScope.equals(REGENERATE_SCOPE_ALL) ||
                  this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
            {
               FormInstanceData fid = null;
               try
               {
                  fid = this.formsService.getFormInstanceData(-1, previewAvmPath);
                  final List<FormInstanceData.RegenerateResult> regenResults = fid.regenerateRenditions();
                  for (final FormInstanceData.RegenerateResult rr : regenResults)
                  {
                     if (rr.getException() != null)
                     {
                        Utils.addErrorMessage("error regenerating rendition using " + 
                              rr.getRenderingEngineTemplate().getName() + 
                              ": " + rr.getException().getMessage(),
                              rr.getException());
                     }
                     else
                     {
                        result.add(rr.getRendition());
                     }
                     if (rr.getRendition() != null)
                     {
                         getAvmLockingService().removeLock(AVMUtil.getStoreId(rr.getRendition().getPath()),
                              AVMUtil.getStoreRelativePath(rr.getRendition().getPath()));
                     }
                  }
               }
               catch (FormNotFoundException fnfe)
               {
                  logger.warn("regenerating renditions of " + previewAvmPath + ": " + fnfe.getMessage(), fnfe);
               }
            }
            else
            {
               final Rendition r = this.formsService.getRendition(-1, previewAvmPath);
               try
               {
                  r.regenerate();
                  result.add(r);
                  
                  getAvmLockingService().removeLock(AVMUtil.getStoreId(r.getPath()), AVMUtil.getStoreRelativePath(r.getPath()));
               }
               catch (TemplateNotFoundException tnfe)
               {
                   logger.warn("regenerating renditions of " + previewAvmPath + ": " + tnfe.getMessage(), tnfe);
               }
               catch (IllegalArgumentException iae)
               {
                   logger.warn("regenerating renditions of " + previewAvmPath + ": " + iae.getMessage(), iae);
               }
               catch (Exception e)
               {
                  Utils.addErrorMessage("error regenerating rendition using " + 
                        r.getRenderingEngineTemplate().getName() + 
                        ": " + e.getMessage(),
                        e);
               }
            }
         }
         return result;
      }
      finally
      {
         rs.close();
      }
   }
}
