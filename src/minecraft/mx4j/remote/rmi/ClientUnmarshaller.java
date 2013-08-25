/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.management.MBeanServerConnection;

import mx4j.remote.ClientProxy;

/**
 * An MBeanServerConnection proxy that performs the setting of the appropriate context classloader
 * to allow classloading of classes sent by the server but not known to the client, in methods like
 * {@link MBeanServerConnection#getAttribute}, {@link MBeanServerConnection#invoke} and so on.
 *
 * @version $Revision: 1.4 $
 */
public class ClientUnmarshaller extends ClientProxy
{
   private final ClassLoader classLoader;

   private ClientUnmarshaller(MBeanServerConnection target, ClassLoader loader)
   {
      super(target);
      this.classLoader = loader;
   }

   public static MBeanServerConnection newInstance(MBeanServerConnection target, ClassLoader loader)
   {
      ClientUnmarshaller handler = new ClientUnmarshaller(target, loader);
      return (MBeanServerConnection)Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{MBeanServerConnection.class}, handler);
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (classLoader == null)
      {
         return chain(proxy, method, args);
      }
      else
      {
         ClassLoader old = Thread.currentThread().getContextClassLoader();
         try
         {
            setContextClassLoader(classLoader);
            return chain(proxy, method, args);
         }
         finally
         {
            setContextClassLoader(old);
         }
      }
   }

   private Object chain(Object proxy, Method method, Object[] args) throws Throwable
   {
      return super.invoke(proxy, method, args);
   }

   private void setContextClassLoader(final ClassLoader loader)
   {
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            Thread.currentThread().setContextClassLoader(loader);
            return null;
         }
      });
   }
}
