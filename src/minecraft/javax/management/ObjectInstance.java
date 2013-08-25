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
 * Identifies an MBean registered in the MBeanServer. An ObjectInstance carries the information
 * about MBean's ObjectName and class name.
 *
 * @version $Revision: 1.7 $
 */
public class ObjectInstance implements Serializable
{
   private static final long serialVersionUID = -4099952623687795850L;

   /**
    * @serial The MBean class name
    */
   private String className;
   /**
    * @serial The MBean ObjectName
    */
   private ObjectName name;

   /**
    * Creates a new ObjectInstance
    *
    * @param objectName The ObjectName of the MBean
    * @param className  The class name of the MBean
    * @throws MalformedObjectNameException If the ObjectName string does not represent a valid ObjectName
    */
   public ObjectInstance(String objectName, String className) throws MalformedObjectNameException
   {
      this(new ObjectName(objectName), className);
   }

   /**
    * Creates a new ObjectInstance
    *
    * @param objectName The ObjectName of the MBean
    * @param className  The class name of the MBean
    */
   public ObjectInstance(ObjectName objectName, String className)
   {
      if (objectName == null || objectName.isPattern()) throw new RuntimeOperationsException(new IllegalArgumentException("Invalid object name"));
      if (className == null || className.trim().length() == 0) throw new RuntimeOperationsException(new IllegalArgumentException("Class name cannot be null or empty"));
      this.name = objectName;
      this.className = className;
   }

   public boolean equals(Object object)
   {
      if (object == null) return false;
      if (object == this) return true;

      try
      {
         ObjectInstance other = (ObjectInstance)object;
         return name.equals(other.name) && className.equals(other.className);
      }
      catch (ClassCastException ignored)
      {
      }
      return false;
   }

   public int hashCode()
   {
      return name.hashCode() ^ className.hashCode();
   }

   /**
    * Returns the ObjectName of the MBean
    */
   public ObjectName getObjectName()
   {
      return name;
   }

   /**
    * Returns the class name of the MBean
    *
    * @return
    */
   public String getClassName()
   {
      return className;
   }

   public String toString()
   {
      return getClassName() + "@" + getObjectName();
   }
}
