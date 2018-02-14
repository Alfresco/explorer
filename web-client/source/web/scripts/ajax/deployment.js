var deployActionButtonPressed = false;

Alfresco.checkDeployConfigPage = function()
{
   // make sure the relevant fields are visible for current deploy type
   Alfresco.deployServerTypeChanged();
   
   // make sure add/edit button is disabled if no host has been supplied
   Alfresco.checkDeployConfigButtonState();
   
   var button = document.getElementById('wizard:wizard-body:deployActionButton');
   if (button != null)
   {
      document.getElementById("wizard").onsubmit = Alfresco.validateDeployConfig;
      button.onclick = function() {deployActionButtonPressed = true; clear_wizard();}
      document.getElementById('wizard:wizard-body:deployServerHost').focus();
   }
   
   // if a scroll to id has been set, scroll there
   if (window.SCROLL_TO_SERVER_CONFIG_ID)
   {
      Alfresco.scrollToEditServer(SCROLL_TO_SERVER_CONFIG_ID);
   }
}

Alfresco.checkDeployConfigButtonState = function()
{
   var host = document.getElementById('wizard:wizard-body:deployServerHost');
   var port = document.getElementById('wizard:wizard-body:deployServerPort');
   var button = document.getElementById('wizard:wizard-body:deployActionButton');
   
   if (button != null)
   {
	  var buttonState = false;
	   
	  if (port != null && port.value.length == 0)
	  {
		 buttonState = true;
	  }
	  if (host != null && host.value.length == 0)
      {
         buttonState = true;
      }
	  button.disabled = buttonState;
   }
}

Alfresco.validateDeployConfig = function()
{
   if (deployActionButtonPressed)
   {
      deployActionButtonPressed = false;
      
      var valid = true;
      var port = document.getElementById('wizard:wizard-body:deployServerPort');
      var localhost = document.getElementById('wizard:wizard-body:deployServerHost');
      
      if (port != null && port.value.length > 0)
      {
         if (isNaN(port.value))
         {
            alert(MSG_PORT_MUST_BE_NUMBER);
            port.focus();
            valid = false;
         }
      }
      
      if (localhost != null && localhost.value.length > 0)
      {
    	  if(!localhost.value.test(/^[A-Za-z0-9][A-Za-z0-9\.\-]*$/))
    	  {
    		  alert(MSG_HOST_WRONG_FORMAT);
    		  localhost.focus();
    		  valid = false;
    	  }
      }
        
      return valid;
   }
   else
   {
      return true;
   }
}

Alfresco.deployServerTypeChanged = function() 
{
   var typeDropDown = document.getElementById('wizard:wizard-body:deployServerType');
   if (typeDropDown != null)
   {
      var selectedType = typeDropDown.options[typeDropDown.selectedIndex].value;
      
      // show or hide the label
      var autoDeployLabel = document.getElementById('autoDeployLabel');
      if (autoDeployLabel != null)
      {
         if (selectedType == "test")
         {
            autoDeployLabel.style.display = "none";
         }
         else
         {
            autoDeployLabel.style.display = "inline";
         }
      }
      
      // show or hide the checkbox
      var autoDeployCheckbox = document.getElementById('wizard:wizard-body:autoDeployCheckbox');
      if (autoDeployCheckbox != null)
      {
         if (selectedType == "test")
         {
            autoDeployCheckbox.style.display = "none";
         }
         else
         {
            autoDeployCheckbox.style.display = "inline";
         }
      }
   }
}

Alfresco.scrollToEditServer = function(serverId)
{
   var serverForm = document.getElementById(serverId);
   if (serverForm != null)
   {
      var yPos = serverForm.offsetTop;
      window.scrollTo(0, yPos);
   }
}

Alfresco.toggleDeploymentDetails = function(icon, server) 
{
   var currentState = icon.className;
   var detailsDiv = document.getElementById(server + '-deployment-details');
   if (currentState == 'collapsed') 
   {
      icon.src = getContextPath() + '/images/icons/expanded.gif';
      icon.className = 'expanded';
      if (detailsDiv != null) 
      { 
         detailsDiv.style.display = 'block';
      }
   }
   else 
   {
      icon.src = getContextPath() + '/images/icons/collapsed.gif';
      icon.className = 'collapsed';
      if (detailsDiv != null) 
      { 
         detailsDiv.style.display = 'none';
      }
   }
}

