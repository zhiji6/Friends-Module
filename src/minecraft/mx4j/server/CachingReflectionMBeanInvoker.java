/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import mx4j.util.MethodTernaryTree;

/**
 * Caching MBeanInvoker that uses reflection to invoke on MBean instances.
 * Attributes and operations lookup is cached to speedup invocations.
 *
 * @version $Revision: 1.3 $
 */
public class CachingReflectionMBeanInvoker extends ReflectionMBeanInvoker
{
   private final Map attributes = new HashMap();
   private final Map attributeNames = new HashMap();
   private final MethodTernaryTree operations = new MethodTernaryTree();
   private final MethodTernaryTree methods = new MethodTernaryTree();

   protected MBeanOperationInfo getStandardOperationInfo(MBeanMetaData metadata, String method, String[] signature)
   {
      MBeanOperationInfo oper = null;
      synchronized (operations)
      {
         oper = (MBeanOperationInfo)operations.get(method, signature);
      }
      if (oper != null) return oper;

      // The MBeanOperationInfo is not in the cache, look it up
      MBeanOperationInfo info = super.getStandardOperationInfo(metadata, method, signature);
      if (info != null)
      {
         synchronized (operations)
         {
            operations.put(method, signature, oper);
         }
      }
      return info;
   }

   protected MBeanAttributeInfo getStandardAttributeInfo(MBeanMetaData metadata, String attribute, boolean forWrite)
   {
      MBeanAttributeInfo attr = null;
      synchronized (attributes)
      {
         attr = (MBeanAttributeInfo)attributes.get(attribute);
      }

      if (attr == null)
      {
         attr = super.getStandardAttributeInfo(metadata, attribute, forWrite);
         if (attr == null) return null;

         synchronized (attributes)
         {
            attributes.put(attribute, attr);
         }
      }

      if (forWrite && attr.isWritable()) return attr;
      if (!forWrite && attr.isReadable()) return attr;

      return null;
   }

   protected String getMethodForAttribute(MBeanAttributeInfo attribute, boolean getter)
   {
      AttributeName attributeName = null;
      String name = attribute.getName();
      synchronized (attributeNames)
      {
         attributeName = (AttributeName)attributeNames.get(name);
      }
      if (attributeName == null)
      {
         attributeName = new AttributeName(super.getMethodForAttribute(attribute, true), super.getMethodForAttribute(attribute, false));
         synchronized (attributeNames)
         {
            attributeNames.put(name, attributeName);
         }
      }

      if (getter) return attributeName.getter;
      return attributeName.setter;
   }

   protected Method getStandardManagementMethod(MBeanMetaData metadata, String name, String[] signature) throws ReflectionException
   {
      Method method = null;
      synchronized (methods)
      {
         method = (Method)methods.get(name, signature);
      }
      if (method != null) return method;

      // Method is not in cache, look it up
      method = super.getStandardManagementMethod(metadata, name, signature);
      synchronized (methods)
      {
         methods.put(name, signature, method);
      }
      return method;
   }

   private static class AttributeName
   {
      private final String getter;
      private final String setter;

      public AttributeName(String getter, String setter)
      {
         this.getter = getter;
         this.setter = setter;
      }
   }
}
