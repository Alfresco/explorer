Alfresco Explorer
=========
The legacy JSF UI for Alfresco Community Edition.

This was extracted from 5.0.b, and is not currently in a buildable state because
it still depends on being in the same WAR as Alfresco Explorer.

Does not currently run on 5.0.c and needs additional changes.

Limitations
------------
Alfresco stopped enhancing the legacy JSF Explorer UI with the release of Alfresco
Share in 3.0. With the release of AlfrescoOne 5.0 (and Community Edition 5.0.c), the Explorer interface was removed.

The Explorer interface used a variety of internal undocumented APIs. With the
removal of the Explorer interface, those APIs are likely to change. The legacy
Explorer needs to be adapted to use services that are part of the documented API
to meet those needs.

Areas of likely breakage:
* Authentication and single sign-on

TODO
----
* Package Alfresco Explorer as an AMP using Maven, to be deployed within the 4.2.c
  Alfresco WAR
* Get the Explorer AMP to run against 5.0.c.
* List on addons.alfresco.com
