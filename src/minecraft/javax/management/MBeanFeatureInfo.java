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
 * Base class for the MBean metadata classes.
 *
 * @version $Revision: 1.10 $
 */
public class MBeanFeatureInfo implements Serializable
{
   private static final long serialVersionUID = 3952882688968447265L;

   /**
    * @serial The name of the feature
    */
   protected String name;
   /**
    * @serial The description of the feature
    */
   protected String description;

   /**
    * Creates a new MBean feature metadata object
    *
    * @param name        The name of the feature
    * @param description The description of the feature
    */
   public MBeanFeatureInfo(String name, String description) throws IllegalArgumentException
   {
      this.name = name;
      this.description = description;
   }

   /**
    * Returns the name of the MBean feature
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the description of the MBean feature
    */
   public String getDescription()
   {
      return description;
   }

   public int hashCode()
   {
      int hash = 0;
      String n = getName();
      if (n != null) hash = 29 * hash + n.hashCode();
      String d = getDescription();
      if (d != null) hash = 29 * hash + d.hashCode();
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (!(obj instanceof MBeanFeatureInfo)) return false;

      MBeanFeatureInfo other = (MBeanFeatureInfo)obj;
      String thisName = getName();
      String otherName = other.getName();
      if (thisName != null ? !thisName.equals(otherName) : otherName != null) return false;
      String thisDescr = getDescription();
      String otherDescr = other.getDescription();
      if (thisDescr != null ? !thisDescr.equals(otherDescr) : otherDescr != null) return false;
      return true;
   }
}
