/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServerErrorException;

import mx4j.remote.ClientProxy;

/**
 * @version $Revision: 1.3 $
 */
public class ClientExceptionCatcher extends ClientProxy
{
   private ClientExceptionCatcher(MBeanServerConnection target)
   {
      super(target);
   }

   public static MBeanServerConnection newInstance(MBeanServerConnection target)
   {
      ClientExceptionCatcher handler = new ClientExceptionCatcher(target);
      return (MBeanServerConnection)Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{MBeanServerConnection.class}, handler);
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      try
      {
         return super.invoke(proxy, method, args);
      }
      catch (NoSuchObjectException x)
      {
         // The connection has been already closed by the server
         throw new IOException("Connection closed by the server");
      }
      catch (Exception x)
      {
         throw x;
      }
      catch (Error x)
      {
         throw new JMXServerErrorException("Error thrown during invocation", x);
      }
   }
}
