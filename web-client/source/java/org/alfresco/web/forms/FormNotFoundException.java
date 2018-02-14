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

import java.io.FileNotFoundException;
import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

/**
 * Error when a form cannot be resolved.
 *
 * @author Ariel Backenroth
 */
public class FormNotFoundException
   extends FileNotFoundException
{
   private final String formName;
   private final FormInstanceData fid;

   public FormNotFoundException(final String formName)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_found"),
                                 formName));
      this.formName = formName;
      this.fid = null;
   }

   public FormNotFoundException(final String formName, final FormInstanceData fid)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_found_for_form_instance_data"),
                                 formName,
                                 fid.getPath()));
      this.formName = formName;
      this.fid = fid;
   }

   public String getFormName()
   {
      return this.formName;
   }
}
