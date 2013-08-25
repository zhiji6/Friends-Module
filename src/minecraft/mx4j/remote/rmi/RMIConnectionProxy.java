/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.management.remote.rmi.RMIConnection;

/**
 * Base class for RMIConnection dynamic proxies.
 *
 * @version $Revision: 1.5 $
 */
public class RMIConnectionProxy implements InvocationHandler
{
   private RMIConnection nested;

   protected RMIConnectionProxy(RMIConnection nested)
   {
      this.nested = nested;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      try
      {
         return method.invoke(nested, args);
      }
      catch (InvocationTargetException x)
      {
         throw x.getTargetException();
      }
   }
}
