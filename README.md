Alfresco Explorer
=========
The legacy JSF UI for Alfresco Community Edition.

Alfresco stopped enhancing the legacy JSF Explorer UI with the release of Alfresco
Share in 3.0. With the release of AlfrescoOne 5.0 (and Community Edition 5.0.b), the Explorer interface was removed.

This was extracted from 5.0.a, and is not currently in a buildable state.

Without additional changes, it will not run on versions of Alfresco newer than
5.0.a, including Alfresco Enterprise Edition 5.0.0.

Limitations
------------
The Explorer interface used a variety of internal undocumented APIs. With the
removal of the Explorer interface, those APIs are likely to change. The legacy
Explorer needs to be adapted to use services that are part of the documented API
to meet those needs.

Areas of likely future breakage (5.1 or beyond)
* Authentication and single sign-on

Notes
-----
* Removed the AVM related UI tests

TODO
----
* Remove references to deprecated modules:
  http://wiki.alfresco.com/wiki/Alfresco_Community_5.0.b_Release_Notes
* Package Alfresco Explorer as an AMP using Maven, to be deployed within the
  5.0.c Alfresco WAR
* List on addons.alfresco.com
