/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.management.MBeanServerConnection;

/**
 * @version $Revision: 1.3 $
 */
public class ClientProxy implements InvocationHandler
{
   private final MBeanServerConnection target;

   protected ClientProxy(MBeanServerConnection target)
   {
      this.target = target;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      try
      {
         return method.invoke(target, args);
      }
      catch (InvocationTargetException x)
      {
         throw x.getTargetException();
      }
   }
}
