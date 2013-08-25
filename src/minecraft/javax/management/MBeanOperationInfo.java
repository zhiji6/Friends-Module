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
 * Metadata class for an MBean operation
 *
 * @version $Revision: 1.14 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class MBeanOperationInfo extends MBeanFeatureInfo implements Cloneable
public class MBeanOperationInfo extends MBeanFeatureInfo implements Cloneable, Serializable
{
   private static final long serialVersionUID = -6178860474881375330L;

   /**
    * This impact means the operation is read-like.
    */
   public static final int INFO = 0;
   /**
    * This impact means the operation is write-like.
    */
   public static final int ACTION = 1;
   /**
    * This impact means the operation is both read-like and write-like.
    */
   public static final int ACTION_INFO = 2;
   /**
    * This impact means the operation impact is unknown.
    */
   public static final int UNKNOWN = 3;

   /**
    * @serial The operation signature
    */
   private MBeanParameterInfo[] signature;
   /**
    * The operation return type
    */
   private String type;
   /**
    * The operation impact
    */
   private int impact;

   /**
    * Creates a new MBeanOperationInfo.
    *
    * @param description The operation description
    * @param method      The method
    */
   public MBeanOperationInfo(String description, Method method) throws IllegalArgumentException
   {
      super(method.getName(), description);
      Class[] params = method.getParameterTypes();
      this.signature = new MBeanParameterInfo[params.length];
      for (int i = 0; i < params.length; ++i)
      {
         this.signature[i] = new MBeanParameterInfo("", params[i].getName(), "");
      }
      this.type = method.getReturnType().getName();
      this.impact = UNKNOWN;
   }

   /**
    * Creates a new MBeanOperationInfo
    *
    * @param name        The operation name
    * @param description The operation description
    * @param signature   The operation signature
    * @param type        The operation return type
    * @param impact      The operation impact
    */
   public MBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature, String type, int impact)  throws IllegalArgumentException
   {
      super(name, description);
      this.signature = signature == null ? new MBeanParameterInfo[0] : signature;
      this.type = type;
      this.impact = impact;
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

   /**
    * Returns the return type of the operation
    */
   public String getReturnType()
   {
      return type;
   }

   /**
    * Returns the signature of the operation
    */
   public MBeanParameterInfo[] getSignature()
   {
      return signature;
   }

   /**
    * Returns the impact of the operation
    */
   public int getImpact()
   {
      return impact;
   }

   public int hashCode()
   {
      int hash = super.hashCode();

      String type = getReturnType();
      if (type != null) hash = 29 * hash + type.hashCode();
      hash = 29 * hash + Utils.arrayHashCode(getSignature());
      hash = 29 * hash + getImpact();

      return hash;
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;
      if (!(obj instanceof MBeanOperationInfo)) return false;

      MBeanOperationInfo other = (MBeanOperationInfo)obj;

      String thisType = getReturnType();
      String otherType = other.getReturnType();
      if (thisType != null ? !thisType.equals(otherType) : otherType != null) return false;
      if (!Utils.arrayEquals(getSignature(), other.getSignature())) return false;
      if (getImpact() != other.getImpact()) return false;

      return true;
   }
}
