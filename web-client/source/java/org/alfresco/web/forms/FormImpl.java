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
package org.alfresco.web.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.XMLUtil;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.AVMWorkflowUtil;
import org.alfresco.web.forms.xforms.XFormsProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import freemarker.ext.dom.NodeModel;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;

public class FormImpl implements Form
{
   private static final long serialVersionUID = 7654769105419391840L;
   
   private static final Log LOGGER = LogFactory.getLog(FormImpl.class);
   
   private final NodeRef folderNodeRef;
   private transient FormsService formsService;
   private transient Map<String, RenderingEngineTemplate> renderingEngineTemplates;

   private transient static LinkedList<FormProcessor> PROCESSORS = null;
   
   
   protected FormImpl(final NodeRef folderNodeRef, final FormsService formsService)
   {
      if (folderNodeRef == null)
      {
         throw new NullPointerException();
      }
      if (formsService == null)
      {
         throw new NullPointerException();
      }
      this.folderNodeRef = folderNodeRef;
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
   
   public String getName()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef, 
                                             ContentModel.PROP_NAME);
   }

   public String getTitle()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef, 
                                             ContentModel.PROP_TITLE);
   }

   public String getDescription()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef, 
                                             ContentModel.PROP_DESCRIPTION);
   }
   
   public String getOutputPathPattern()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef,
                                             WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
   }

   public WorkflowDefinition getDefaultWorkflow()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final WorkflowService workflowService = this.getServiceRegistry().getWorkflowService();

      final NodeRef workflowRef = this.getDefaultWorkflowNodeRef();
      final String workflowName = 
         (workflowRef != null 
          ? (String)nodeService.getProperty(workflowRef, WCMAppModel.PROP_WORKFLOW_NAME)
          : null);
               
      return workflowName != null ? workflowService.getDefinitionByName(workflowName) : null;
   }

   public Map<QName, Serializable> getDefaultWorkflowParameters()
   {
      final NodeRef workflowRef = this.getDefaultWorkflowNodeRef();
      return (Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(workflowRef);
   }

   protected NodeRef getDefaultWorkflowNodeRef()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final List<ChildAssociationRef> workflowRefs = 
         nodeService.getChildAssocs(this.folderNodeRef,
                                    WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                    RegexQNamePattern.MATCH_ALL);
      if (workflowRefs.size() == 0)
      {
         return null;
      }

      assert workflowRefs.size() == 1 : "found more than one workflow parameters node for " + this.getName();

      return workflowRefs.get(0).getChildRef();
   }

   public String getOutputPathForFormInstanceData(final Document formInstanceData,
                                                  final String formInstanceDataName,
                                                  final String parentAVMPath,
                                                  final String webappName)
   {
      final String outputPathPattern = (FreeMarkerUtil.buildNamespaceDeclaration(formInstanceData) +
                                        this.getOutputPathPattern());
      final Map<String, Object> root = new HashMap<String, Object>();
      root.put("webapp", webappName);
      root.put("xml", NodeModel.wrap(formInstanceData));
      root.put("extension", "xml");
      root.put("name", formInstanceDataName);
      root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));
      root.put("cwd", AVMUtil.getWebappRelativePath(parentAVMPath));

      final TemplateService templateService = this.getServiceRegistry().getTemplateService();

      String result = null;
      try
      {
         if (LOGGER.isDebugEnabled())
         {

            LOGGER.debug("processing " + outputPathPattern + 
                         " using name " + formInstanceDataName +
                         " in webapp " + webappName +
                         " and xml data " + XMLUtil.toString(formInstanceData));
         }
         result = templateService.processTemplateString("freemarker", 
                                                        outputPathPattern, 
                                                        new SimpleHash(root));
      }
      catch (TemplateException te)
      {
         LOGGER.error(te.getMessage(), te);
         throw new AlfrescoRuntimeException("Error processing output path pattern " + outputPathPattern + 
                                            " for " + formInstanceDataName + 
                                            " in webapp " + webappName +
                                            ":\n" + te.getMessage(), 
                                            te);
      }
      result = AVMUtil.buildPath(parentAVMPath, 
                                 result,
                                 AVMUtil.PathRelation.SANDBOX_RELATIVE);
      result = AVMUtil.normalizePath(result);
      LOGGER.debug("processed pattern " + outputPathPattern + " as " + result);
      return result;
   }

   public String getSchemaRootElementName()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(folderNodeRef, 
                                             WCMAppModel.PROP_XML_SCHEMA_ROOT_ELEMENT_NAME);
   }

   public Document getSchema()
      throws IOException, 
      SAXException
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      NodeRef schemaNodeRef = (NodeRef)
         nodeService.getProperty(folderNodeRef, WCMAppModel.PROP_XML_SCHEMA);
      if (schemaNodeRef == null)
      {
         LOGGER.debug(WCMAppModel.PROP_XML_SCHEMA + " not set on " + folderNodeRef +
                      ", checking " + WCMAppModel.PROP_XML_SCHEMA_OLD);
         schemaNodeRef = (NodeRef)
            nodeService.getProperty(folderNodeRef, WCMAppModel.PROP_XML_SCHEMA_OLD);
         if (schemaNodeRef != null)
         {
            nodeService.setProperty(folderNodeRef, WCMAppModel.PROP_XML_SCHEMA, schemaNodeRef);
         }
      }
      if (schemaNodeRef == null)
      {
         throw new NullPointerException("expected property " + WCMAppModel.PROP_XML_SCHEMA +
                                        " of " + folderNodeRef + 
                                        " for form " + this.getName() +
                                        " not to be null.");
      }
      return XMLUtil.parse(schemaNodeRef,
                           this.getServiceRegistry().getContentService());
   }

   public List<FormProcessor> getFormProcessors()
   {
      synchronized (FormImpl.class)
      {
         if (PROCESSORS == null)
         {
             PROCESSORS = new LinkedList<FormProcessor>();
             PROCESSORS.add(new XFormsProcessor());
         }
      }
      return PROCESSORS;
   }

   public void addRenderingEngineTemplate(final RenderingEngineTemplate ret)
   {
      throw new UnsupportedOperationException();
   }

   public List<RenderingEngineTemplate> getRenderingEngineTemplates()
   {
      if (this.renderingEngineTemplates == null)
      {
         this.renderingEngineTemplates = this.loadRenderingEngineTemplates();
      }
      return Collections.unmodifiableList(new ArrayList(this.renderingEngineTemplates.values()));
   }

   public RenderingEngineTemplate getRenderingEngineTemplate(final String name)
   {
      if (this.renderingEngineTemplates == null)
      {
         this.renderingEngineTemplates = this.loadRenderingEngineTemplates();
      }
      return this.renderingEngineTemplates.get(name);
   }

   public NodeRef getNodeRef()
   {
      return this.folderNodeRef;
   }
   
   public boolean isWebForm()
   {
      boolean isWebForm = true;
      NodeService nodeService = this.getServiceRegistry().getNodeService();
      if (nodeService.getPrimaryParent(this.folderNodeRef).getParentRef().equals(this.formsService.getContentFormsNodeRef()))
      {
         // ECM form
         isWebForm = false;
      }
      return isWebForm;
   }

   public int hashCode() 
   {
      return this.getName().hashCode();
   }

   public String toString()
   {
      return (this.getClass().getName() + "{" +
              "name: " + this.getName() + "," +
              "schemaRootElementName: " + this.getSchemaRootElementName() + "," +
              "renderingEngineTemplates: " + this.getRenderingEngineTemplates() +
              "}");
   }

   public boolean equals(final Object other)
   {
      if (other == null || !(other instanceof FormImpl))
      {
         return false;
      }
      return this.getNodeRef().equals(((FormImpl)other).getNodeRef());
   }

   protected ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }

   protected Map<String, RenderingEngineTemplate> loadRenderingEngineTemplates()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final List<AssociationRef> refs = nodeService.getTargetAssocs(this.folderNodeRef, 
                                                                    WCMAppModel.ASSOC_RENDERING_ENGINE_TEMPLATES);
      final Map<String, RenderingEngineTemplate> result = new HashMap<String, RenderingEngineTemplate>(refs.size(), 1.0f);
      for (AssociationRef assoc : refs)
      {
         final NodeRef retNodeRef = assoc.getTargetRef();
         for (ChildAssociationRef assoc2 : nodeService.getChildAssocs(retNodeRef,
                                                                      WCMAppModel.ASSOC_RENDITION_PROPERTIES,
                                                                      RegexQNamePattern.MATCH_ALL))
         {
            final NodeRef renditionPropertiesNodeRef = assoc2.getChildRef();
            
            final RenderingEngineTemplate ret = 
               new RenderingEngineTemplateImpl(retNodeRef, renditionPropertiesNodeRef, this.getFormsService());
            LOGGER.debug("loaded rendering engine template " + ret);
            result.put(ret.getName(), ret);
         }
      }
      return result;
   }
}
