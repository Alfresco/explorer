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

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides management of forms.
 *
 * @author Ariel Backenroth
 */
public final class FormsService 
{
   private static final Log LOGGER = LogFactory.getLog(FormsService.class);
   
   private static final RenderingEngine[] RENDERING_ENGINES = new RenderingEngine[] 
   {
      new FreeMarkerRenderingEngine(),
      new XSLTRenderingEngine(),
      new XSLFORenderingEngine()
   };
   
   private final ContentService contentService;
   private final NodeService nodeService;
   private final NamespaceService namespaceService;
   private final SearchService searchService;

   private NodeRef contentFormsNodeRef;
   private NodeRef webContentFormsNodeRef;
   
   /** instantiated using spring */
   public FormsService(final ContentService contentService,
                       final NodeService nodeService,
                       final NamespaceService namespaceService,
                       final SearchService searchService,
                       final PolicyComponent policyComponent)
   {
      this.contentService = contentService;
      this.nodeService = nodeService;
      this.namespaceService = namespaceService;
      this.searchService = searchService;
   }
   
   /**
    * Provides all registered rendering engines.
    */
   public RenderingEngine[] getRenderingEngines()
   {
      return FormsService.RENDERING_ENGINES;
   }

   /**
    * Returns the rendering engine with the given name.
    *
    * @param name the name of the rendering engine.
    *
    * @return the rendering engine or <tt>null</tt> if not found.
    */
   public RenderingEngine getRenderingEngine(final String name)
   {
      for (RenderingEngine re : this.getRenderingEngines())
      {
         if (re.getName().equals(name))
         {
            return re;
         }
      }
      return null;
   }

   public RenderingEngine guessRenderingEngine(final String fileName)
   {
      for (RenderingEngine re : this.getRenderingEngines())
      {
         if (fileName.endsWith(re.getDefaultTemplateFileExtension()))
         {
            return re;
         }
      }
      return null;
   }

   /**
    * @return the cached reference to the WCM Content Forms folder
    */
   public NodeRef getContentFormsNodeRef()
   {
      if (this.contentFormsNodeRef == null)
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final String xpath = (Application.getRootPath(fc) + "/" +
                               Application.getGlossaryFolderName(fc) + "/" +
                               Application.getContentFormsFolderName(fc));

         this.contentFormsNodeRef = getNodeRefFromXPath(xpath);
      }
      return this.contentFormsNodeRef;
   }
   
   /**
    * @return the cached reference to the WCM Content Forms folder
    */
   public NodeRef getWebContentFormsNodeRef()
   {
      if (this.webContentFormsNodeRef == null)
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final String xpath = (Application.getRootPath(fc) + "/" +
                               Application.getGlossaryFolderName(fc) + "/" +
                               Application.getWebContentFormsFolderName(fc));
         
         this.webContentFormsNodeRef = getNodeRefFromXPath(xpath);
      }
      return this.webContentFormsNodeRef;
   }
   
   private NodeRef getNodeRefFromXPath(String xpath)
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("locating noderef at " + xpath);
      final List<NodeRef> results = 
         searchService.selectNodes(this.nodeService.getRootNode(Repository.getStoreRef()),
                                   xpath,
                                   null,
                                   namespaceService,
                                   false);
      return (results != null && results.size() == 1 ? results.get(0) : null);
   }
}
