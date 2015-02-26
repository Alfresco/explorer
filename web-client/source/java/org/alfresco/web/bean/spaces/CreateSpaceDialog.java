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
package org.alfresco.web.bean.spaces;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;

/**
 * Dialog bean to create a space. 
 * Uses the CreateSpaceWizard and just overrides the finish button label
 * and the default outcomes.
 * 
 * @author gavinc
 */
public class CreateSpaceDialog extends CreateSpaceWizard
{
   private static final long serialVersionUID = 4659583264588102372L;
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "create_space");
   }
   
   @Override
   protected String getDefaultCancelOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
}
