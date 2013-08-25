/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;

import mx4j.util.Utils;

/**
 * Metadata class for MBean notifications.
 *
 * @version $Revision: 1.14 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class MBeanNotificationInfo extends MBeanFeatureInfo implements Cloneable
public class MBeanNotificationInfo extends MBeanFeatureInfo implements Cloneable, Serializable
{
   private static final long serialVersionUID = -3888371564530107064L;

   /**
    * @serial The notification types
    */
   private String[] types;

   /**
    * Creates a new MBeanNotificationInfo
    *
    * @param notifsType  The types
    * @param name        The classname of the Notifications emitted
    * @param description The description for these notifications
    */
   public MBeanNotificationInfo(String[] notifsType, String name, String description) throws IllegalArgumentException
   {
      super(name, description);
      this.types = notifsType == null ? new String[0] : notifsType;
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
    * Returns the types of the notifications emitted.
    */
   public String[] getNotifTypes()
   {
      return types;
   }

   public int hashCode()
   {
      return super.hashCode() + 29 * Utils.arrayHashCode(getNotifTypes());
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;
      if (!(obj instanceof MBeanNotificationInfo)) return false;

      MBeanNotificationInfo other = (MBeanNotificationInfo)obj;
      return Utils.arrayEquals(getNotifTypes(), other.getNotifTypes());
   }
}
