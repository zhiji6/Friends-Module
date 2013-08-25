/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.loading;

import java.net.URL;
import java.util.Map;

/**
 * This class is supposed to represent an MLET tag in a MLet file; a big
 * mistake in JMX 1.2 specification introduced this class in a poorly designed
 * {@link MLet#check} method, but left this class package private, so that it is totally
 * useless. Furthermore, this class is so implementation dependent that should have never
 * seen the light in the public API. But must be present for spec compliance.
 *
 * @version $Revision: 1.3 $
 */
class MLetContent
{
   private Map attributes;
   private URL mletFileURL;
   private URL codebaseURL;

   public MLetContent(URL mletFileURL, Map attributes)
   {
      this.mletFileURL = mletFileURL;
      this.attributes = attributes;

      codebaseURL = (URL)attributes.remove("codebaseURL");
   }

   public Map getAttributes()
   {
      return attributes;
   }

   public URL getDocumentBase()
   {
      return mletFileURL;
   }

   public URL getCodeBase()
   {
      return codebaseURL;
   }

   public String getJarFiles()
   {
      return (String)getParameter("archive");
   }

   public String getCode()
   {
      return (String)getParameter("code");
   }

   public String getSerializedObject()
   {
      return (String)getParameter("object");
   }

   public String getName()
   {
      return (String)getParameter("name");
   }

   public String getVersion()
   {
      return (String)getParameter("version");
   }

   public Object getParameter(String s)
   {
      return attributes.get(s.toLowerCase());
   }
}
