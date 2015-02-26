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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Create Web Project in the Websites folder
 * 
 * @author Kevin Roast
 */
public class CreateWebProjectEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 9061864145360361349L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ServiceRegistry services = Repository.getServiceRegistry(fc);
      NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
      
      // get the path to the current name - compare last element with the Website folder assoc name
      Path path = navigator.getCurrentNode().getNodePath();
      Path.Element element = path.get(path.size() - 1);
      String endPath = element.getPrefixedString(services.getNamespaceService());
      
      if (Application.getWebsitesFolderName(fc).equals(endPath))
      {
         // check we have the permission to create nodes in that Website folder
         return navigator.getCurrentNode().hasPermission(PermissionService.ADD_CHILDREN);
      }
      return false;
   }
}
