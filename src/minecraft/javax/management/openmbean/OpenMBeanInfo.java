/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

/**
 * The <code>OpenMBeanInfo</code> describes an OpenMBean.
 *
 * @version $Revision: 1.4 $
 * @see javax.management.MBeanInfo
 */
public interface OpenMBeanInfo
{
   /**
    * Return the fully qualified classname that this OpenMBeanInfo describes
    *
    * @return String The fully qualified classname
    */
   public String getClassName();

   /**
    * Returns a human readable description
    *
    * @return String The human readable description
    */
   public String getDescription();

   /**
    * Returns an instance of MBeanAttributeInfo ( OpenMBeanAttributeInfo )
    *
    * @return MBeanAttributeInfo the OpenMBeanAttributeInfo array
    * @see OpenMBeanAttributeInfo
    * @see javax.management.MBeanAttributeInfo
    */
   public MBeanAttributeInfo[] getAttributes();

   /**
    * Returns an instance of MBeanOperationInfo ( OpenMBeanOperationInfo )
    *
    * @return MBeanOperationInfo the OpenMBeanOperationInfo array
    */
   public MBeanOperationInfo[] getOperations();


   /**
    * Returns an array of MBeanConstructorInfo ( OpenMBeanConstructorInfo )
    *
    * @return MBeanConstructorInfo the OpenMBeanConstructorInfo array
    */
   public MBeanConstructorInfo[] getConstructors();

   /**
    * Returns an array of MBeanNotificationInfo which describes notifications
    * by this </code>OpenMBeanInfo</code>
    *
    * @return MBeanNotificationInfo An array of notifications
    */
   public MBeanNotificationInfo[] getNotifications();


   /**
    * Checks if the given Object is equal with this <code>OpenMBeanInfo</code>
    *
    * @return boolean If equal
    */
   public boolean equals(Object obj);

   /**
    * Returns the hashCode of this OpenMBean info
    *
    * @return int the hashcode
    */
   public int hashCode();

   /**
    * Returns a String representation
    *
    * @return String The String representation of this OpenMBeanInfo
    */
   public String toString();
}
