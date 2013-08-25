/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;

/**
 * The metadata class for a parameter of MBean constructors and operations.
 *
 * @version $Revision: 1.12 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class MBeanParameterInfo extends MBeanFeatureInfo implements Cloneable
public class MBeanParameterInfo extends MBeanFeatureInfo implements Cloneable, Serializable
{
   private static final long serialVersionUID = 7432616882776782338L;

   /**
    * @serial The parameter type
    */
   private String type;

   /**
    * Creates a new MBeanParameterInfo
    *
    * @param name        The parameter name
    * @param type        The parameter type
    * @param description The parameter description
    */
   public MBeanParameterInfo(String name, String type, String description) throws IllegalArgumentException
   {
      super(name, description);
      this.type = type;
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
    * Returns the parameter type
    *
    * @return
    */
   public String getType()
   {
      return type;
   }

   public int hashCode()
   {
      int hash = super.hashCode();
      String t = getType();
      if (t != null) hash = 29 * hash + t.hashCode();
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;
      if (!(obj instanceof MBeanParameterInfo)) return false;

      MBeanParameterInfo other = (MBeanParameterInfo)obj;
      String thisType = getType();
      String otherType = other.getType();
      if (thisType != null ? !thisType.equals(otherType) : otherType != null) return false;

      return true;
   }
}
