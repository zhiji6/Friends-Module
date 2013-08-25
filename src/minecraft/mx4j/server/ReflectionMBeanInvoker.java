/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

import mx4j.ImplementationException;
import mx4j.util.Utils;

/**
 * Stateless MBeanInvoker that uses reflection to invoke on MBean instances.
 *
 * @version $Revision: 1.6 $
 */
public class ReflectionMBeanInvoker implements MBeanInvoker
{
   /**
    * A zero-length String[] that indicates a parameterless signature of a method.
    */
   protected static final String[] EMPTY_PARAMS = new String[0];
   /**
    * A zero-length Object[] that indicates a parameterless argument list of a method
    */
   protected static final Object[] EMPTY_ARGS = new Object[0];

   public Object invoke(MBeanMetaData metadata, String method, String[] params, Object[] args) throws MBeanException, ReflectionException
   {
      MBeanOperationInfo oper = getStandardOperationInfo(metadata, method, params);
      if (oper != null)
      {
         try
         {
            return doInvoke(metadata, method, params, args);
         }
         catch (BadArgumentException x)
         {
            throw new RuntimeOperationsException(x.nested);
         }
      }
      else
      {
         throw new ReflectionException(new NoSuchMethodException("Operation " + method + " does not belong to the management interface"));
      }
   }

   public Object getAttribute(MBeanMetaData metadata, String attribute) throws MBeanException, AttributeNotFoundException, ReflectionException
   {
      MBeanAttributeInfo attr = getStandardAttributeInfo(metadata, attribute, false);
      if (attr != null)
      {
         String methodName = getMethodForAttribute(attr, true);
         try
         {
            return doInvoke(metadata, methodName, EMPTY_PARAMS, EMPTY_ARGS);
         }
         catch (BadArgumentException x)
         {
            // Never thrown, since there are no arguments
            throw new ImplementationException();
         }
      }
      else
      {
         throw new AttributeNotFoundException(attribute);
      }
   }

   public void setAttribute(MBeanMetaData metadata, Attribute attribute) throws MBeanException, AttributeNotFoundException, InvalidAttributeValueException, ReflectionException
   {
      String name = attribute.getName();
      MBeanAttributeInfo attr = getStandardAttributeInfo(metadata, name, true);
      if (attr != null)
      {
         String methodName = getMethodForAttribute(attr, false);
         try
         {
            doInvoke(metadata, methodName, new String[]{attr.getType()}, new Object[]{attribute.getValue()});
         }
         catch (BadArgumentException x)
         {
            throw new InvalidAttributeValueException("Invalid value for attribute " + name + ": " + attribute.getValue());
         }
      }
      else
      {
         throw new AttributeNotFoundException(name);
      }
   }

   /**
    * Centralizes exception handling necessary to convert exceptions thrown by MBean's methods to
    * JMX exceptions. Delegates the actual invocation to {@link #invokeImpl}
    */
   protected Object doInvoke(MBeanMetaData metadata, String method, String[] signature, Object[] args) throws ReflectionException, MBeanException, BadArgumentException
   {
      try
      {
         return invokeImpl(metadata, method, signature, args);
      }
      catch (ReflectionException x)
      {
         throw x;
      }
      catch (MBeanException x)
      {
         throw x;
      }
      catch (BadArgumentException x)
      {
         throw x;
      }
      catch (InvocationTargetException x)
      {
         Throwable t = x.getTargetException();
         if (t instanceof RuntimeException) throw new RuntimeMBeanException((RuntimeException)t);
         if (t instanceof Exception) throw new MBeanException((Exception)t);
         throw new RuntimeErrorException((Error)t);
      }
      catch (Throwable t)
      {
         if (t instanceof RuntimeException) throw new RuntimeMBeanException((RuntimeException)t);
         if (t instanceof Exception) throw new MBeanException((Exception)t);
         throw new RuntimeErrorException((Error)t);
      }
   }

   /**
    * Performs the actual invocation of the MBean's method.
    * Exceptions thrown by the MBean's methods should not be catched, since {@link #doInvoke}
    * takes care of converting them to JMX exceptions.
    */
   protected Object invokeImpl(MBeanMetaData metadata, String method, String[] signature, Object[] args) throws Throwable
   {
      Method m = getStandardManagementMethod(metadata, method, signature);
      try
      {
         return m.invoke(metadata.getMBean(), args);
      }
      catch (IllegalAccessException x)
      {
         throw new ReflectionException(x);
      }
      catch (IllegalArgumentException x)
      {
         throw new BadArgumentException(x);
      }
   }

   /**
    * Returns the MBeanOperationInfo for the given operation, or null if the operation
    * is not a management operation.
    */
   protected MBeanOperationInfo getStandardOperationInfo(MBeanMetaData metadata, String method, String[] signature)
   {
      MBeanOperationInfo[] opers = metadata.getMBeanInfo().getOperations();
      if (opers != null)
      {
         for (int i = 0; i < opers.length; ++i)
         {
            MBeanOperationInfo oper = opers[i];
            String name = oper.getName();
            if (method.equals(name))
            {
               // Same method name, check number of parameters
               MBeanParameterInfo[] params = oper.getSignature();
               if (signature.length == params.length)
               {
                  boolean match = true;
                  for (int j = 0; j < params.length; ++j)
                  {
                     MBeanParameterInfo param = params[j];
                     if (!signature[j].equals(param.getType()))
                     {
                        match = false;
                        break;
                     }
                  }
                  if (match) return oper;
               }
            }
         }
      }
      return null;
   }

   /**
    * Returns the MBeanAttributeInfo for the given attribute, or null if the attribute
    * is not a management attribute.
    */
   protected MBeanAttributeInfo getStandardAttributeInfo(MBeanMetaData metadata, String attribute, boolean forWrite)
   {
      MBeanAttributeInfo[] attrs = metadata.getMBeanInfo().getAttributes();
      if (attrs != null)
      {
         for (int i = 0; i < attrs.length; ++i)
         {
            MBeanAttributeInfo attr = attrs[i];
            String name = attr.getName();
            if (attribute.equals(name))
            {
               if (forWrite && attr.isWritable()) return attr;
               if (!forWrite && attr.isReadable()) return attr;
            }
         }
      }
      return null;
   }

   /**
    * Returns the method name for the given attribute.
    */
   protected String getMethodForAttribute(MBeanAttributeInfo attribute, boolean forRead)
   {
      String name = attribute.getName();
      String attributeName = null;
      if (forRead)
      {
         String prefix = attribute.isIs() ? "is" : "get";
         attributeName = prefix + name;
      }
      else
      {
         attributeName = "set" + name;
      }
      return attributeName;
   }

   /**
    * Returns a java.lang.reflect.Method object for the given method name and signature.
    */
   protected Method getStandardManagementMethod(MBeanMetaData metadata, String name, String[] signature) throws ReflectionException
   {
      try
      {
         Class[] params = Utils.loadClasses(metadata.getClassLoader(), signature);
         Method method = metadata.getMBeanInterface().getMethod(name, params);
         return method;
      }
      catch (ClassNotFoundException x)
      {
         throw new ReflectionException(x);
      }
      catch (NoSuchMethodException x)
      {
         throw new ReflectionException(x);
      }
   }

   private static class BadArgumentException extends Exception
   {
      private final IllegalArgumentException nested;

      private BadArgumentException(IllegalArgumentException nested)
      {
         this.nested = nested;
      }
   }
}
