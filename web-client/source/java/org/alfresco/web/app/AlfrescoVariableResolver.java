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
package org.alfresco.web.app;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.DelegatingVariableResolver;

/**
 * JSF VariableResolver that first delegates to the Spring JSF variable 
 * resolver. The sole purpose of this variable resolver is to look out
 * for the <code>Container</code> variable. If this variable is encountered
 * the current viewId is examined. If the current viewId matches a 
 * configured dialog or wizard container the appropriate manager object is
 * returned i.e. DialogManager or WizardManager.
 * 
 * <p>Configure this resolver in your <code>faces-config.xml</code> file as follows:
 *
 * <pre>
 * &lt;application&gt;
 *   ...
 *   &lt;variable-resolver&gt;org.alfresco.web.app.AlfrescoVariableResolver&lt;/variable-resolver&gt;
 * &lt;/application&gt;</pre>
 * 
 * @see org.alfresco.web.bean.dialog.DialogManager
 * @see org.alfresco.web.bean.wizard.WizardManager
 * @author gavinc
 */
public class AlfrescoVariableResolver extends DelegatingVariableResolver
{
   protected List<String> dialogContainers = null;
   protected List<String> wizardContainers = null;
   
   private static final String CONTAINER = "Container";
   
   private static final Log logger = LogFactory.getLog(AlfrescoVariableResolver.class);
   
   /**
    * Creates a new VariableResolver.
    * 
    * @param originalVariableResolver The original variable resolver
    */
   public AlfrescoVariableResolver(VariableResolver originalVariableResolver)
   {
      super(originalVariableResolver);
   }
   
   /**
    * Resolves the variable with the given name.
    * <p>
    * This implementation will first delegate to the Spring variable resolver.
    * If the variable is not found by the Spring resolver and the variable name
    * is <code>Container</code> the current viewId is examined.
    * If the current viewId matches a configured dialog or wizard container 
    * the appropriate manager object is returned i.e. DialogManager or WizardManager.
    * 
    * @param context FacesContext
    * @param name The name of the variable to resolve
    */
   public Object resolveVariable(FacesContext context, String name) 
      throws EvaluationException 
   {
      Object variable = super.resolveVariable(context, name);
      
      if (variable == null)
      {
         // if the variable was not resolved see if the name is "Container"
         if (name.equals(CONTAINER))
         {
            // get the current view id and the configured dialog and wizard 
            // container pages
            String viewId = context.getViewRoot().getViewId();
            List<String> dialogContainers = getDialogContainers(context);
            List<String> wizardContainers = getWizardContainers(context);
            
            // see if we are currently in a wizard or a dialog
            if (dialogContainers.contains(viewId))
            {
               variable = Application.getDialogManager();
            }
            else if (wizardContainers.contains(viewId))
            {
               variable = Application.getWizardManager();   
            }
            
            if (variable != null && logger.isDebugEnabled())
            {
               logger.debug("Resolved 'Container' variable to: " + variable);
            }
         }
      }
      
      return variable;
   }
   
   /**
    * Retrieves the list of configured dialog container pages
    * 
    * @param context FacesContext
    * @return The container pages
    */
   protected List<String> getDialogContainers(FacesContext context)
   {
	   if ((this.dialogContainers == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
	      this.dialogContainers = new ArrayList<String>(2);
	      
         ConfigService configSvc = Application.getConfigService(context);
         Config globalConfig = configSvc.getGlobalConfig();
         
         if (globalConfig != null)
         {
            this.dialogContainers.add(globalConfig.getConfigElement("dialog-container").getValue());
            this.dialogContainers.add(globalConfig.getConfigElement("plain-dialog-container").getValue());
         }
      }
      
      return this.dialogContainers;
   }
   
   /**
    * Retrieves the list of configured wizard container pages
    * 
    * @param context FacesContext
    * @return The container page
    */
   protected List<String> getWizardContainers(FacesContext context)
   {
	   if ((this.wizardContainers == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
	      this.wizardContainers = new ArrayList<String>(2);
	      
         ConfigService configSvc = Application.getConfigService(context);
         Config globalConfig = configSvc.getGlobalConfig();
         
         if (globalConfig != null)
         {
            this.wizardContainers.add(globalConfig.getConfigElement("wizard-container").getValue());
            this.wizardContainers.add(globalConfig.getConfigElement("plain-wizard-container").getValue());
         }
      }
      
      return this.wizardContainers;
   }
}
