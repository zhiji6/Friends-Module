/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.loading;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;

import mx4j.loading.ClassLoaderObjectInputStream;
import mx4j.loading.MLetParseException;
import mx4j.loading.MLetParser;
import mx4j.loading.MLetTag;
import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.28 $
 */
public class MLet extends URLClassLoader implements MLetMBean, MBeanRegistration, Externalizable
{
   private MBeanServer server;
   private ObjectName objectName;
   private boolean delegateToCLR;
   private ThreadLocal loadingOnlyLocally = new ThreadLocal();
   private ThreadLocal loadingWithRepository = new ThreadLocal();
   private String libraryDir;

   public MLet()
   {
      this(new URL[0]);
   }

   public MLet(URL[] urls)
   {
      this(urls, true);
   }

   public MLet(URL[] urls, boolean delegateToCLR)
   {
      super(urls);
      setDelegateToCLR(delegateToCLR);
      loadingWithRepository.set(Boolean.FALSE);
      loadingOnlyLocally.set(Boolean.FALSE);
   }

   public MLet(URL[] urls, ClassLoader parent)
   {
      this(urls, parent, true);
   }

   public MLet(URL[] urls, ClassLoader parent, boolean delegateToCLR)
   {
      super(urls, parent);
      setDelegateToCLR(delegateToCLR);
      loadingWithRepository.set(Boolean.FALSE);
      loadingOnlyLocally.set(Boolean.FALSE);
   }

   public MLet(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
   {
      this(urls, parent, factory, true);
   }

   public MLet(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory, boolean delegateToCLR)
   {
      super(urls, parent, factory);
      this.setDelegateToCLR(delegateToCLR);
      loadingWithRepository.set(Boolean.FALSE);
      loadingOnlyLocally.set(Boolean.FALSE);
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      this.server = server;
      objectName = name == null ? new ObjectName(this.server.getDefaultDomain(), "type", "MLet") : name;
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet service " + objectName + " preRegistered successfully");
      return objectName;
   }

   public void postRegister(Boolean registrationDone)
   {
      Logger logger = getLogger();
      if (!registrationDone.booleanValue())
      {
         server = null;
         if (logger.isEnabledFor(Logger.INFO)) logger.info("MLet service " + objectName + " was not registered");
      }
      else
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet service " + objectName + " postRegistered successfully");
      }
   }

   public void preDeregister() throws Exception
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet service " + objectName + " preDeregistered successfully");
   }

   public void postDeregister()
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet service " + objectName + " postDeregistered successfully");
   }

   public void addURL(String url) throws ServiceNotFoundException
   {
      addURL(createURL(url));
   }

   public void addURL(URL url)
   {
      Logger logger = getLogger();
      if (!Arrays.asList(getURLs()).contains(url))
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Adding URL to this MLet (" + objectName + ") classpath: " + url);
         super.addURL(url);
      }
      else
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("URL already present in this MLet (" + objectName + ") classpath: " + url);
      }
   }

   public Class loadClass(String name, ClassLoaderRepository repository) throws ClassNotFoundException
   {
      if (repository == null)
      {
         Class cls = loadClassLocally(name);
         return cls;
      }
      else
      {
         try
         {
            Class cls = loadClassLocally(name);
            return cls;
         }
         catch (ClassNotFoundException x)
         {
            // Not found locally, try the given repository
            Class cls = loadClassFromRepository(name, repository);
            return cls;
         }
      }
   }

   /**
    * Loads the given class from this MLet only (with the usual parent delegation mechanism); the
    * ClassLoaderRepository is not asked to load the class.
    *
    * @param name The name of the class to load.
    * @return The loaded class
    * @throws ClassNotFoundException
    */
   private Class loadClassLocally(String name) throws ClassNotFoundException
   {
      // Here I must call super.loadClass(name) but not delegate to the CLR when I arrive to MLet.findClass
      // I cannot call findClassLocally directly because otherwise the mechanism of parent delegation
      // implemented in loadClass() is skipped and the parent loaders do not have the chance to
      // load the given class

      try
      {
         loadingOnlyLocally.set(Boolean.TRUE);
         return loadClass(name);
      }
      finally
      {
         loadingOnlyLocally.set(Boolean.FALSE);
      }
   }

   /**
    * Loads the given class from the given non-null ClassLoaderRepository using
    * {@link ClassLoaderRepository#loadClassBefore}
    */
   private Class loadClassFromRepository(String name, ClassLoaderRepository repository) throws ClassNotFoundException
   {
      return repository.loadClassBefore(this, name);
   }

   protected Class findClass(String name) throws ClassNotFoundException
   {
      Logger logger = getLogger();
      boolean trace = logger.isEnabledFor(Logger.TRACE);

      // It may be possible that loading started with this MLet, then delegated to the CLR
      // and came again to query this MLet which, if not stopped, will delegate to the CLR
      // again in an endless loop. This is possible if this MLet is the parent of a child
      // MLet registered before its parent.
      if (loadingWithRepository.get() == Boolean.TRUE)
      {
         if (trace) logger.trace("MLet " + this + " is recursively calling itself to load class " + name + ": skipping further searches");
         throw new ClassNotFoundException(name);
      }

      if (trace) logger.trace("Finding class " + name + "...");

      try
      {
         Class cls = findClassLocally(name);
         if (trace) logger.trace("Class " + name + " found in this MLet's classpath " + this);
         return cls;
      }
      catch (ClassNotFoundException x)
      {
         if (!isDelegateToCLR())
         {
            if (trace) logger.trace("MLet " + this + " does not delegate to the ClassLoaderRepository");
            throw x;
         }

         if (loadingOnlyLocally.get() == Boolean.TRUE) throw x;

         if (server == null) throw x;

         if (trace) logger.trace("Class " + name + " not found in this MLet's classpath " + this + ", trying the ClassLoaderRepository...", x);
         try
         {
            loadingWithRepository.set(Boolean.TRUE);
            ClassLoaderRepository repository = server.getClassLoaderRepository();
            Class cls = loadClassFromRepository(name, repository);
            if (trace) logger.trace("Class " + name + " found with ClassLoaderRepository " + repository);
            return cls;
         }
         catch (ClassNotFoundException xx)
         {
            if (trace) logger.trace("Class " + name + " not found in ClassLoaderRepository, giving up", xx);
            throw new ClassNotFoundException(name);
         }
         finally
         {
            loadingWithRepository.set(Boolean.FALSE);
         }
      }
   }

   private Class findClassLocally(String name) throws ClassNotFoundException
   {
      return super.findClass(name);
   }

   public Set getMBeansFromURL(String url) throws ServiceNotFoundException
   {
      return getMBeansFromURL(createURL(url));
   }

   public Set getMBeansFromURL(URL url) throws ServiceNotFoundException
   {
      if (url == null) throw new ServiceNotFoundException("Cannot load MBeans from null URL");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet " + this + ", reading MLET file from " + url);

      InputStream is = null;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BufferedOutputStream os = new BufferedOutputStream(baos);
      try
      {
         is = url.openStream();
         readFromAndWriteTo(is, os);
      }
      catch (IOException x)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot read input stream from URL " + url, x);
         throw new ServiceNotFoundException(x.toString());
      }
      finally
      {
         try
         {
            if (is != null) is.close();
            os.close();
         }
         catch (IOException ignored)
         {
         }
      }

      String mletFileContent = null;
      try
      {
         mletFileContent = new String(baos.toByteArray(), "UTF-8");
      }
      catch (UnsupportedEncodingException x)
      {
         mletFileContent = baos.toString();
      }
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet File content is:\n" + mletFileContent);

      return parseMLetFile(mletFileContent, url);
   }

   private Set parseMLetFile(String content, URL mletFileURL) throws ServiceNotFoundException
   {
      Logger logger = getLogger();

      try
      {
         HashSet mbeans = new HashSet();
         MLetParser parser = new MLetParser(this);
         List tags = parser.parse(content);

         for (int i = 0; i < tags.size(); ++i)
         {
            MLetTag tag = (MLetTag)tags.get(i);

            // Add the MBean's codebase to the MLet classloader
            String[] jars = tag.parseArchive();
            for (int j = 0; j < jars.length; ++j)
            {
               String jar = jars[j];
               URL codebase = handleCheck(tag, jar, mletFileURL, mbeans);
               URL archiveURL = tag.createArchiveURL(codebase, jar);
               addURL(archiveURL);
            }

            // Create and register the MBean
            Object obj = createMBean(tag);
            mbeans.add(obj);
         }

         return mbeans;
      }
      catch (MLetParseException x)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot parse MLet file", x);
         throw new ServiceNotFoundException(x.toString());
      }
   }

   /**
    * This method handle the call to {@link #check}, a method that it's a big mistake in JMX 1.2
    */
   private URL handleCheck(MLetTag tag, String archive, URL mletFileURL, Set mbeans)
   {
      HashMap map = new HashMap();
      map.put("codebaseURL", tag.normalizeCodeBase(mletFileURL));
      map.put("codebase", tag.getCodeBase());
      map.put("archive", tag.getArchive());
      map.put("code", tag.getCode());
      map.put("object", tag.getObject());
      map.put("name", tag.getObjectName());
      map.put("version", tag.getVersion());
      MLetContent mletContent = new MLetContent(mletFileURL, map);

      try
      {
         // JMX 1.2 FAAAAANTASTIC check method
         return check(mletContent.getVersion(), mletContent.getCodeBase(), archive, mletContent);
      }
      catch (Throwable x)
      {
         mbeans.add(x);
         return null;
      }
   }

   /**
    * This method is called when an MLet file has been parsed and before its information is used by this MLet.
    * By overriding this method subclasses have the possibility to perform caching and versioning of jars,
    * but unfortunately it contains as parameter a package private class, that thus forbids overriding: a big
    * mistake in the JMX 1.2 specification.
    *
    * @since JMX 1.2
    */
   protected URL check(String version, URL codebase, String archive, MLetContent content) throws Exception
   {
      return codebase;
   }

   private Object createMBean(MLetTag tag) throws ServiceNotFoundException
   {
      if (server == null) throw new ServiceNotFoundException("MLet not registered on the MBeanServer");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MLet " + this + ", creating MBean from\n" + tag);

      try
      {
         Object mbean = null;
         if (tag.getObject() != null)
         {
            // Read the file from the codebase URLs of this classloader
            String name = tag.getObject();
            InputStream is = getResourceAsStream(name);
            if (is == null) throw new ServiceNotFoundException("Cannot find serialized MBean " + name + " in MLet " + this);

            InputStream bis = new BufferedInputStream(is);

            // Deserialize using the MLet classloader
            ObjectInputStream ois = new ClassLoaderObjectInputStream(bis, this);
            mbean = ois.readObject();
         }
         else
         {
            // Instantiate using the MLet classloader
            String clsName = tag.getCode();
            Object[] args = tag.getArguments();
            String[] params = tag.getSignature();
            mbean = server.instantiate(clsName, objectName, args, params);
         }

         ObjectName objectName = tag.getObjectName();
         ObjectInstance instance = server.registerMBean(mbean, objectName);
         return instance;
      }
      catch (Throwable t)
      {
         return t;
      }
   }

   protected String findLibrary(String libraryName)
   {
      // When asked to load a library, I should convert the library name to the system specific name
      // For example if libraryName == "stat", on Solaris the conversion yields libstat.so, on Windows yields stat.dll
      final String sysLibraryName = System.mapLibraryName(libraryName);

      // Try to load the native libaray from jar only using the library name
      String path = copyLibrary(sysLibraryName);
      if (path != null) return path;

      // Library not found so try to load the library using the os specific architecture
      String osPath = (String)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            StringBuffer buffer = new StringBuffer();
            buffer.append(System.getProperty("os.name")).append(File.separator);
            buffer.append(System.getProperty("os.arch")).append(File.separator);
            buffer.append(System.getProperty("os.version")).append(File.separator);
            buffer.append("lib").append(File.separator).append(sysLibraryName);
            return buffer.toString();
         }
      });

      osPath = removeSpaces(osPath);

      return copyLibrary(osPath);
   }

   private String copyLibrary(String library)
   {
      Logger logger = getLogger();

      library = library.replace('\\', '/');
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Loading library " + library);

      URL libraryURL = getResource(library);

      InputStream is = null;
      OutputStream os = null;
      try
      {
         try
         {
            is = getResourceAsStream(library);
            if (is == null) return null;

            if (!(is instanceof BufferedInputStream)) is = new BufferedInputStream(is);
            File localLibrary = new File(getLibraryDirectory(), library);
            URL localLibraryURL = localLibrary.toURL();

            // The library is local and its directory is in the classpath of this MLet
            if (localLibraryURL.equals(libraryURL)) return localLibrary.getCanonicalPath();

            // Copy the library (that can be remote) locally, overwriting old versions
            try
            {
               os = new BufferedOutputStream(new FileOutputStream(localLibrary));
               readFromAndWriteTo(is, os);
               return localLibrary.getCanonicalPath();
            }
            finally
            {
               if (os != null) os.close();
            }
         }
         finally
         {
            if (is != null) is.close();
         }
      }
      catch (IOException x)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot copy the library to the library directory " + getLibraryDirectory(), x);
         return null;
      }
   }

   private void readFromAndWriteTo(InputStream is, OutputStream os) throws IOException
   {
      byte[] buffer = new byte[64];
      int read = -1;
      while ((read = is.read(buffer)) >= 0) os.write(buffer, 0, read);
   }

   private String removeSpaces(String string)
   {
      int space = -1;
      StringBuffer buffer = new StringBuffer();
      while ((space = string.indexOf(' ')) >= 0)
      {
         buffer.append(string.substring(0, space));
         string = string.substring(space + 1);
      }
      buffer.append(string);
      return buffer.toString();
   }

   public String getLibraryDirectory()
   {
      return libraryDir;
   }

   public void setLibraryDirectory(String libdir)
   {
      libraryDir = libdir;
   }

   private boolean isDelegateToCLR()
   {
      return delegateToCLR;
   }

   private void setDelegateToCLR(boolean delegateToCLR)
   {
      this.delegateToCLR = delegateToCLR;
   }

   private URL createURL(String urlString) throws ServiceNotFoundException
   {
      try
      {
         URL url = new URL(urlString);
         return url;
      }
      catch (MalformedURLException x)
      {
         throw new ServiceNotFoundException(x.toString());
      }
   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   /**
    * Restores this MLet content from the given ObjectInput. Implementation of this method is optional,
    * and if not implemented throws an UnsupportedOperationException.
    */
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException("MLet.readExternal");
   }

   /**
    * Stores this MLet content in the given ObjectOutput. Implementation of this method is optional,
    * and if not implemented throws an UnsupportedOperationException.
    */
   public void writeExternal(ObjectOutput out) throws IOException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException("MLet.writeExternal");
   }
}
