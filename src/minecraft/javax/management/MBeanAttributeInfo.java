/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;
import java.lang.reflect.Method;

import mx4j.util.Utils;

/**
 * @version $Revision: 1.15 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class MBeanAttributeInfo extends MBeanFeatureInfo implements Cloneable
public class MBeanAttributeInfo extends MBeanFeatureInfo implements Cloneable, Serializable
{
   private static final long serialVersionUID = 8644704819898565848L;

   /**
    * @serial The full qualified class name of the attribute's type
    */
   private String attributeType;
   /**
    * @serial The readability of the attribute
    */
   private boolean isRead;
   /**
    * @serial The writability of the attribute
    */
   private boolean isWrite;
   /**
    * @serial The boolean flag that says the attribute's type is boolean
    */
   private boolean is;

   /**
    * Creates a new MBeanAttributeInfo
    *
    * @param name        The attribute name
    * @param description The attribute description
    * @param getter      The getter method, or null if write only
    * @param setter      The setter method, or null if read only
    * @throws IntrospectionException If the introspection of the attribute fails
    */
   public MBeanAttributeInfo(String name, String description, Method getter, Method setter) throws IntrospectionException
   {
      super(name, description);

      String getterType = null;
      String setterType = null;

      if (getter != null)
      {
         if (Utils.isAttributeGetter(getter))
         {
            this.isRead = true;
            if (getter.getName().startsWith("is")) this.is = true;
            getterType = getter.getReturnType().getName();
         }
         else
            throw new IntrospectionException("Bad getter method");
      }
      if (setter != null)
      {
         if (Utils.isAttributeSetter(setter))
         {
            this.isWrite = true;
            setterType = setter.getParameterTypes()[0].getName();
         }
         else
            throw new IntrospectionException("Bad setter method");
      }

      this.attributeType = reconcileAttributeType(getterType, setterType);
   }

   /**
    * Creates a new MBeanAttributeInfo
    *
    * @param name        The attribute name
    * @param description The attribute description
    * @param isReadable  The attribute's readability
    * @param isWritable  The attribute's writability
    * @param isIs        The flag if the attribute's type is boolean
    */
   public MBeanAttributeInfo(String name, String className, String description, boolean isReadable, boolean isWritable, boolean isIs)
           throws IllegalArgumentException
   {
      super(name, description);

      this.attributeType = className;
      this.isRead = isReadable;
      this.isWrite = isWritable;
      this.is = isIs;
   }

   public Object clone()
   {
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException ignored)
      {
         return null;
      }
   }

   public String getType()
   {
      return attributeType;
   }

   public boolean isReadable()
   {
      return isRead;
   }

   public boolean isWritable()
   {
      return isWrite;
   }

   public boolean isIs()
   {
      return is;
   }

   public int hashCode()
   {
      int hash = super.hashCode();

      String t = getType();
      if (t != null) hash = 29 * hash + t.hashCode();

      hash = 29 * hash + 3 * (isReadable() ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());
      hash = 29 * hash + 5 * (isWritable() ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());
      hash = 29 * hash + 7 * (isIs() ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());

      return hash;
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;
      if (!(obj instanceof MBeanAttributeInfo)) return false;

      MBeanAttributeInfo other = (MBeanAttributeInfo)obj;

      String thisType = getType();
      String otherType = other.getType();
      if (thisType != null ? !thisType.equals(otherType) : otherType != null) return false;

      if (isReadable() ^ other.isReadable()) return false;
      if (isWritable() ^ other.isWritable()) return false;
      if (isIs() ^ other.isIs()) return false;

      return true;
   }

   private String reconcileAttributeType(String getterType, String setterType)
           throws IntrospectionException
   {
      String result = null; // only unchanged if getterType == null && setterType == null

      if (getterType == null && setterType != null)
      {
         result = setterType;
      }
      else if (getterType != null && setterType == null)
      {
         result = getterType;
      }
      else if (getterType != null && setterType != null)
      {
         if (getterType.compareToIgnoreCase(setterType) == 0)
         {
            result = getterType;
         }
         else
         {
            throw new IntrospectionException("Attribute setter/getter types don't match");
         }
      }

      return result;
   }
}
