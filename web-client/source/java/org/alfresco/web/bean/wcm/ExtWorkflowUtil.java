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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

/**
 * AVM Specific workflow related helper methods.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public class ExtWorkflowUtil extends WorkflowUtil
{
   private static final Log logger = LogFactory.getLog(ExtWorkflowUtil.class);

   // cached configured lists
   private static List<WorkflowDefinition> configuredWorkflowDefs = null;
   
   /**
    * @return the list of WorkflowDefinition objects as configured in the wcm/workflows client config.
    */
   public static List<WorkflowDefinition> getConfiguredWorkflows()
   {
      if ((configuredWorkflowDefs == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         List<WorkflowDefinition> defs = Collections.<WorkflowDefinition>emptyList();
         ConfigElement config = Application.getConfigService(fc).getGlobalConfig().getConfigElement("wcm");
         if (config == null)
         {
            logger.warn("WARNING: Unable to find 'wcm' config element definition.");
         }
         else
         {
            ConfigElement workflowConfig = config.getChild("workflows");
            if (workflowConfig == null)
            {
               logger.warn("WARNING: Unable to find WCM 'workflows' config element definition.");
            }
            else
            {
               WorkflowService service = Repository.getServiceRegistry(fc).getWorkflowService();
               StringTokenizer t = new StringTokenizer(workflowConfig.getValue().trim(), ", ");
               defs = new ArrayList<WorkflowDefinition>(t.countTokens());
               while (t.hasMoreTokens())
               {
                  String wfName = t.nextToken();
                  WorkflowDefinition def = service.getDefinitionByName("jbpm$" + wfName);
                  if (def != null)
                  {
                     defs.add(def);
                  }
                  else
                  {
                     logger.warn("WARNING: Cannot find WCM workflow def for configured definition name: " + wfName); 
                  }
               }
            }
         }
         configuredWorkflowDefs = defs;
      }
      return configuredWorkflowDefs;
   }
}
