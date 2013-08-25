/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import mx4j.util.Utils;

/**
 */
public class MBeanServerInvocationHandler implements InvocationHandler
{
   private final MBeanServerConnection connection;
   private final ObjectName objectName;

   public MBeanServerInvocationHandler(MBeanServerConnection connection, ObjectName objectName)
   {
      this.connection = connection;
      this.objectName = objectName;
   }

   public static Object newProxyInstance(MBeanServerConnection connection, ObjectName name, Class mbeanInterface, boolean notificationBroadcaster)
   {
      if (mbeanInterface == null) throw new IllegalArgumentException("MBean interface cannot be null");
      if (!mbeanInterface.isInterface()) throw new IllegalArgumentException("Class parameter must be an interface");
      if (name == null) throw new IllegalArgumentException("MBean ObjectName cannot be null");
      if (connection == null) throw new IllegalArgumentException("MBeanServerConnection cannot be null");

      Class[] interfaces = null;
      if (notificationBroadcaster && !(mbeanInterface.equals(NotificationEmitter.class)))
      {
         if ((mbeanInterface.equals(NotificationBroadcaster.class)))
         {
            interfaces = new Class[]{NotificationEmitter.class};
         }
         else
         {
            interfaces = new Class[]{mbeanInterface, NotificationEmitter.class};
         }
      }
      else
      {
         interfaces = new Class[]{mbeanInterface};
      }

      // The client must be able to cast the returned object to the mbeanInterface it passes,
      // so the classloader must be the same
      ClassLoader loader = mbeanInterface.getClassLoader();
      return Proxy.newProxyInstance(loader, interfaces, new MBeanServerInvocationHandler(connection, name));
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      Class[] declared = method.getExceptionTypes();

      Class declaringClass = method.getDeclaringClass();
      if (declaringClass.equals(NotificationBroadcaster.class) || declaringClass.equals(NotificationEmitter.class))
      {
         return invokeNotificationMethod(proxy, method, args, declared);
      }

      // No need to check for consistency between the signature and the args parameter,
      // since the invocation it is not done by reflection, but statically

      if (Utils.isAttributeSetter(method))
      {
         String name = method.getName().substring(3);
         Attribute attribute = new Attribute(name, args[0]);
         try
         {
            connection.setAttribute(objectName, attribute);
            return null;
         }
         catch (Throwable x)
         {
            unwrapThrowable(x, declared);
         }
      }
      else if (Utils.isAttributeGetter(method))
      {
         String n = method.getName();
         String name = null;
         if (n.startsWith("is"))
            name = n.substring(2);
         else
            name = n.substring(3);

         try
         {
            return connection.getAttribute(objectName, name);
         }
         catch (Throwable x)
         {
            unwrapThrowable(x, declared);
         }
      }
      else
      {
         Class[] parameters = method.getParameterTypes();
         String[] params = new String[parameters.length];
         for (int i = 0; i < parameters.length; ++i)
         {
            params[i] = parameters[i].getName();
         }

         try
         {
            return connection.invoke(objectName, method.getName(), args, params);
         }
         catch (Throwable x)
         {
            unwrapThrowable(x, declared);
         }
      }

      return null;
   }

   /**
    * Convenience method that invokes Notification-related methods.
    *
    * @param proxy    Proxy object created by the newProxyInstance method
    * @param method   The <code>java.lang.Method</code> to be invoked
    * @param args     The method's arguments
    * @param declared The method's declared exceptions
    */
   private Object invokeNotificationMethod(Object proxy, Method method, Object[] args, Class[] declared)
           throws Throwable
   {
      String methodName = method.getName();
      int numArgs = (args == null) ? 0 : args.length;

      if (methodName.equals("addNotificationListener"))
      {
         try
         {
            connection.addNotificationListener(objectName, (NotificationListener)args[0], (NotificationFilter)args[1], args[2]);
         }
         catch (Throwable t)
         {
            unwrapThrowable(t, declared);
         }
         return null;
      }
      else if (methodName.equals("removeNotificationListener"))
      {
         switch (numArgs)
         {
            case 1:
               try
               {
                  connection.removeNotificationListener(objectName, (NotificationListener)args[0]);
               }
               catch (Throwable t)
               {
                  unwrapThrowable(t, declared);
               }
               return null;

            case 3:
               try
               {
                  connection.removeNotificationListener(objectName, (NotificationListener)args[0], (NotificationFilter)args[1], args[2]);
               }
               catch (Throwable t)
               {
                  unwrapThrowable(t, declared);
               }
               return null;

            default :
               throw new IllegalArgumentException("Method removeNotificationListener must have 1 or 3 arguments");
         }

      }
      else if (methodName.equals("getNotificationInfo"))
      {
         try
         {
            MBeanInfo info = connection.getMBeanInfo(objectName);
            return info.getNotifications();
         }
         catch (Throwable t)
         {
            unwrapThrowable(t, declared);
         }
         return null;
      }
      else
      {
         throw new IllegalArgumentException("Method " + methodName + " not known to MBean: " + objectName);
      }
   }

   /**
    * Rethrows 'as is' the given throwable.  If it is an instance of one of the given <code>declared</code> classes,
    * this method tries to (recursively) unwrap it and rethrow it.
    *
    * @param x        The <code>java.lang.Throwable</code> to unwrap
    * @param declared An array of <code>java.lang.Class</code> objects representing the declared exceptions
    *                 of the invoked method.
    * @throws java.lang.Throwable
    */
   private void unwrapThrowable(Throwable x, Class[] declared) throws Throwable
   {
      if (declared != null)
      {
         // See if the exception is declared by the method
         // If so, just rethrow it.
         for (int i = 0; i < declared.length; ++i)
         {
            Class exception = declared[i];
            if (exception.isInstance(x))
               throw x;
         }
      }

      // The exception is not declared, try to unwrap it
      if (x instanceof MBeanException)
      {
         unwrapThrowable(((MBeanException)x).getTargetException(), declared);
      }
      else if (x instanceof ReflectionException)
      {
         unwrapThrowable(((ReflectionException)x).getTargetException(), declared);
      }
      else if (x instanceof RuntimeOperationsException)
      {
         unwrapThrowable(((RuntimeOperationsException)x).getTargetException(), declared);
      }
      else if (x instanceof RuntimeMBeanException)
      {
         unwrapThrowable(((RuntimeMBeanException)x).getTargetException(), declared);
      }
      else if (x instanceof RuntimeErrorException)
      {
         unwrapThrowable(((RuntimeErrorException)x).getTargetError(), declared);
      }
      else
      {
         // Rethrow as is. Since this exception is not declared by the methods of the interface,
         // if it is checked it will be thrown as an UndeclaredThrowableException, if it is unchecked
         // it will be rethrown as is.
         throw x;
      }
   }
}
