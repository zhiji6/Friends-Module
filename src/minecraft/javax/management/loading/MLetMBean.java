/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.loading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import javax.management.ServiceNotFoundException;

/**
 * Management interface for MLets
 *
 * @version $Revision: 1.6 $
 */
public interface MLetMBean
{
   /**
    * @throws ServiceNotFoundException If the specified string URL is malformed
    * @see #addURL(URL)
    */
   public void addURL(String url) throws ServiceNotFoundException;

   /**
    * @see java.net.URLClassLoader#addURL
    */
   public void addURL(URL url);

   /**
    * @see java.net.URLClassLoader#getURLs
    */
   public URL[] getURLs();

   /**
    * @see java.net.URLClassLoader#getResource
    */
   public URL getResource(String name);

   /**
    * @see java.net.URLClassLoader#getResourceAsStream
    */
   public InputStream getResourceAsStream(String name);

   /**
    * @see java.net.URLClassLoader#getResources
    */
   public Enumeration getResources(String name) throws IOException;

   /**
    * Shortcut for {@link #getMBeansFromURL(URL)}
    */
   public Set getMBeansFromURL(String url) throws ServiceNotFoundException;

   /**
    * Registers, in the MBeanServer where this MLet is registered, the MBeans specified in the MLet
    * file pointed by the given URL.
    *
    * @param url The URL of the MLet file to load
    * @return A Set containing the ObjectInstances of the successfully registered MBeans,
    *         or Throwables that specifies why the MBean could not be registered.
    * @throws ServiceNotFoundException If the given URL is invalid, or does not point to a valid MLet file
    */
   public Set getMBeansFromURL(URL url) throws ServiceNotFoundException;

   /**
    * Returns the directory used to store native libraries
    *
    * @see #setLibraryDirectory
    */
   public String getLibraryDirectory();

   /**
    * Sets the directory used to store native libraries
    *
    * @see #getLibraryDirectory
    */
   public void setLibraryDirectory(String libdir);
}
