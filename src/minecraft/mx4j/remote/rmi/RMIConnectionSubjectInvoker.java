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
import java.rmi.MarshalledObject;
import java.security.AccessControlContext;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Map;
import javax.management.ObjectName;
import javax.management.remote.JMXServerErrorException;
import javax.management.remote.rmi.RMIConnection;
import javax.security.auth.Subject;

import mx4j.remote.MX4JRemoteUtils;

/**
 * An RMIConnection proxy that wraps the call into a {@link Subject#doAsPrivileged} invocation,
 * in order to execute the code under subject-based security, and to perform subject delegation.
 *
 * @version $Revision: 1.10 $
 */
public class RMIConnectionSubjectInvoker extends RMIConnectionProxy
{
   public static RMIConnection newInstance(RMIConnection nested, Subject subject, AccessControlContext context, Map environment)
   {
      RMIConnectionSubjectInvoker handler = new RMIConnectionSubjectInvoker(nested, subject, context, environment);
      return (RMIConnection)Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{RMIConnection.class}, handler);
   }

   private final Subject subject;
   private final AccessControlContext context;
   private Map environment;

   private RMIConnectionSubjectInvoker(RMIConnection nested, Subject subject, AccessControlContext context, Map environment)
   {
      super(nested);
      this.subject = subject;
      this.context = context;
      this.environment = environment;
   }

   public Object invoke(final Object proxy, final Method method, final Object[] args)
           throws Throwable
   {
      String methodName = method.getName();
      if ("fetchNotifications".equals(methodName) || "close".equals(methodName) || "getConnectionId".equals(methodName)) return chain(proxy, method, args);

      if ("addNotificationListeners".equals(methodName))
      {
         Subject[] delegates = (Subject[])args[args.length - 1];
         if (delegates == null || delegates.length == 0) return chain(proxy, method, args);

         if (delegates.length == 1) return subjectInvoke(proxy, method, args, delegates[0]);

         ArrayList ids = new ArrayList();
         for (int i = 0; i < delegates.length; ++i)
         {
            ObjectName name = ((ObjectName[])args[0])[i];
            MarshalledObject filter = ((MarshalledObject[])args[1])[i];
            Subject delegate = delegates[i];
            Object[] newArgs = new Object[]{new ObjectName[]{name}, new MarshalledObject[]{filter}, new Subject[]{delegate}};
            Integer id = ((Integer[])subjectInvoke(proxy, method, newArgs, delegate))[0];
            ids.add(id);
         }
         return (Integer[])ids.toArray(new Integer[ids.size()]);
      }
      else
      {
         // For all other methods, the subject is always the last argument
         Subject delegate = (Subject)args[args.length - 1];
         return subjectInvoke(proxy, method, args, delegate);
      }
   }

   private Object subjectInvoke(final Object proxy, final Method method, final Object[] args, Subject delegate) throws Exception
   {
      return MX4JRemoteUtils.subjectInvoke(subject, delegate, context, environment, new PrivilegedExceptionAction()
      {
         public Object run() throws Exception
         {
            return chain(proxy, method, args);
         }
      });
   }

   private Object chain(Object proxy, Method method, Object[] args) throws Exception
   {
      try
      {
         return super.invoke(proxy, method, args);
      }
      catch (Throwable x)
      {
         if (x instanceof Exception) throw (Exception)x;
         throw new JMXServerErrorException("Error thrown during invocation", (Error)x);
      }
   }
}
