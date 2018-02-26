Alfresco Explorer
=========
The legacy JSF UI for Alfresco Community Edition.

Alfresco stopped enhancing the legacy JSF Explorer UI with the release of Alfresco
Share in v3.0. With the release of Alfresco One v5.0 (and Community Edition v5.0.b), the Explorer interface was removed.

This was extracted from v5.0.a.

The source was modified, removing the since deprecated AVM and WCM references.  This allowed it to build against v5.0.x and later versions of Alfresco.  This now supports each of those versions with a profile build for each.

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
* Removed the AVM references in the source
* Removed the WCM references in the source
* Merged web.xml for v5.0, v5.1, and v5.2 builds

Build
-----
```sh
git clone https://github.com/Alfresco/explorer.git
cd explorer/web-client
mvn -Palf50 clean install
mvn -Palf51 clean install
mvn -Palf52 clean install
```

Installation
------------
Use the AMP from `.m2/repository/org/alfresco/explorer/VERSION/explorer-VERSION-alf5X.amp`.  If you build your own WAR, include that AMP in your build.  If you place AMPs in the environment's `amps` and `amps_share` directories, place the AMP in the `amps` directory.  Unfortunately, you must install the AMP using the `-force` flag.  This is required because Explorer requires web.xml changes.  This should be the only file replaced by use of the `-force` flag.

The AMP has a module.properties that restricts installation to the correct versions of Alfresco.  The build with classifier `alf51` will not install on Alfresco v5.0 or v5.2.  The versions are specific to the major/minor version due to the `web.xml` modifications.  In the future, it could be source related reasons and not just `web.xml`.

TODO
----
* List on addons.alfresco.com
