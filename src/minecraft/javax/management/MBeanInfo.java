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
 * @version $Revision: 1.16 $
 */
public class MBeanInfo implements Cloneable, Serializable
{
   private static final long serialVersionUID = -6451021435135161911L;

   private static final MBeanConstructorInfo[] EMPTY_CONSTRUCTORS = new MBeanConstructorInfo[0];
   private static final MBeanAttributeInfo[] EMPTY_ATTRIBUTES = new MBeanAttributeInfo[0];
   private static final MBeanOperationInfo[] EMPTY_OPERATIONS = new MBeanOperationInfo[0];
   private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = new MBeanNotificationInfo[0];

   /**
    * @serial The MBean class name
    */
   private String className;
   /**
    * @serial The MBean description
    */
   private String description;
   /**
    * @serial The MBean constructors
    */
   private MBeanConstructorInfo[] constructors;
   /**
    * The MBean attributes
    */
   private MBeanAttributeInfo[] attributes;
   /**
    * The MBean operations
    */
   private MBeanOperationInfo[] operations;
   /**
    * The MBean notifications
    */
   private MBeanNotificationInfo[] notifications;

   public MBeanInfo(String className, String description, MBeanAttributeInfo[] attributes, MBeanConstructorInfo[] constructors, MBeanOperationInfo[] operations, MBeanNotificationInfo[] notifications)
           throws IllegalArgumentException
   {
      this.className = className;
      this.description = description;
      this.constructors = constructors == null || constructors.length == 0 ? EMPTY_CONSTRUCTORS : constructors;
      this.attributes = attributes == null || attributes.length == 0 ? EMPTY_ATTRIBUTES : attributes;
      this.operations = operations == null || operations.length == 0 ? EMPTY_OPERATIONS : operations;
      this.notifications = notifications == null || notifications.length == 0 ? EMPTY_NOTIFICATIONS : notifications;
   }

   public Object clone()
   {
      // This class is read only, so no need to clone also data members
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException x)
      {
         return null;
      }
   }

   public String getClassName()
   {
      return className;
   }

   public String getDescription()
   {
      return description;
   }

   public MBeanConstructorInfo[] getConstructors()
   {
      return constructors;
   }

   public MBeanAttributeInfo[] getAttributes()
   {
      return attributes;
   }

   public MBeanOperationInfo[] getOperations()
   {
      return operations;
   }

   public MBeanNotificationInfo[] getNotifications()
   {
      return notifications;
   }

   public int hashCode()
   {
      int hash = 0;
      String cn = getClassName();
      if (cn != null) hash = 29 * hash + cn.hashCode();
      String de = getDescription();
      if (de != null) hash = 29 * hash + de.hashCode();
      MBeanConstructorInfo[] co = getConstructors();
      if (co != null) hash = 29 * hash + Utils.arrayHashCode(co);
      MBeanAttributeInfo[] at = getAttributes();
      if (at != null) hash = 29 * hash + Utils.arrayHashCode(at);
      MBeanOperationInfo[] op = getOperations();
      if (op != null) hash = 29 * hash + Utils.arrayHashCode(op);
      MBeanNotificationInfo[] no = getNotifications();
      if (no != null) hash = 29 * hash + Utils.arrayHashCode(no);
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (!(obj instanceof MBeanInfo)) return false;

      MBeanInfo other = (MBeanInfo)obj;
      String thisClassName = getClassName();
      String otherClassName = other.getClassName();
      if (thisClassName != null ? !thisClassName.equals(otherClassName) : otherClassName != null) return false;
      String thisDescription = getDescription();
      String otherDescription = other.getDescription();
      if (thisDescription != null ? !thisDescription.equals(otherDescription) : otherDescription != null) return false;
      if (!Utils.arrayEquals(getConstructors(), other.getConstructors())) return false;
      if (!Utils.arrayEquals(getAttributes(), other.getAttributes())) return false;
      if (!Utils.arrayEquals(getOperations(), other.getOperations())) return false;
      if (!Utils.arrayEquals(getNotifications(), other.getNotifications())) return false;
      return true;
   }
}
