/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import mx4j.util.Utils;

/**
 * Metadata class for an MBean constructor
 *
 * @version $Revision: 1.14 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class MBeanConstructorInfo extends MBeanFeatureInfo implements Cloneable
public class MBeanConstructorInfo extends MBeanFeatureInfo implements Cloneable, Serializable
{
   private static final long serialVersionUID = 4433990064191844427L;

   /**
    * @serial The signature of the constructor
    */
   private MBeanParameterInfo[] signature;

   /**
    * Creates a new MBeanConstructorInfo
    *
    * @param description The constructor description
    * @param constructor The constructor
    */
   public MBeanConstructorInfo(String description, Constructor constructor)
   {
      super(constructor.getName(), description);
      Class[] params = constructor.getParameterTypes();
      this.signature = new MBeanParameterInfo[params.length];
      for (int i = 0; i < params.length; ++i)
      {
         this.signature[i] = new MBeanParameterInfo("", params[i].getName(), "");
      }
   }

   /**
    * Creates a new MBeanConstructorInfo
    *
    * @param name        The constructor's name, normally equals to the class name
    * @param description The constructor description
    * @param signature   The constructor signature
    */
   public MBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature) throws IllegalArgumentException
   {
      super(name, description);
      this.signature = signature == null ? new MBeanParameterInfo[0] : signature;
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
    * Returns the signature of this MBeanConstructorInfo
    */
   public MBeanParameterInfo[] getSignature()
   {
      return signature;
   }

   public int hashCode()
   {
      return super.hashCode() + 29 * Utils.arrayHashCode(getSignature());
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;
      if (!(obj instanceof MBeanConstructorInfo)) return false;

      MBeanConstructorInfo other = (MBeanConstructorInfo)obj;
      return Utils.arrayEquals(getSignature(), other.getSignature());
   }
}
