/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanNotificationInfo;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.12 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class ModelMBeanNotificationInfo extends MBeanNotificationInfo implements DescriptorAccess
public class ModelMBeanNotificationInfo extends MBeanNotificationInfo implements DescriptorAccess, Cloneable
{
   private static final long serialVersionUID = -7445681389570207141L;

   private Descriptor notificationDescriptor;

   public ModelMBeanNotificationInfo(String[] types, String name, String description)
   {
      this(types, name, description, null);
   }

   public ModelMBeanNotificationInfo(String[] types, String name, String description, Descriptor descriptor)
   {
      super(types, name, description);
      checkAndSetDescriptor(descriptor);
   }

   public ModelMBeanNotificationInfo(ModelMBeanNotificationInfo copy)
   {
      super(copy.getNotifTypes(), copy.getName(), copy.getDescription());
      checkAndSetDescriptor(copy.getDescriptor());
   }

   public Object clone()
   {
      return new ModelMBeanNotificationInfo(this);
   }

   public Descriptor getDescriptor()
   {
      return (Descriptor)notificationDescriptor.clone();
   }

   public void setDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         notificationDescriptor = createDefaultDescriptor();
      }
      else
      {
         if (isDescriptorValid(descriptor))
         {
            notificationDescriptor = (Descriptor)descriptor.clone();
         }
         else
         {
            // Not sure what to do here: javadoc says IllegalArgument, but for example ModelMBeanInfo throws RuntimeOperations
            // which is consistent with the fact that all exception thrown by the JMX implementation should be JMX exceptions
//				throw new IllegalArgumentException("Invalid descriptor");
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid descriptor"));
         }
      }
   }

   private void checkAndSetDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         notificationDescriptor = createDefaultDescriptor();
      }
      else if (isDescriptorValid(descriptor))
      {
         notificationDescriptor = (Descriptor)descriptor.clone();
         if (notificationDescriptor.getFieldValue("displayname") == null)
         {
            notificationDescriptor.setField("displayname", getName());
         }
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor"));
      }
   }

   private boolean isDescriptorValid(Descriptor descriptor)
   {
      if (!descriptor.isValid())
      {
         return false;
      }

      // Spec compliance checks

      // Mandatory fields are: name, descriptorType, severity, messageId(?), log(?), logFile(?)
      String[] names = descriptor.getFieldNames();

      if (!ModelMBeanInfoSupport.containsIgnoreCase(names, "name") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "descriptortype") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "severity")/* ||
         !ModelMBeanInfoSupport.containsIgnoreCase(names, "messageid") ||
         !ModelMBeanInfoSupport.containsIgnoreCase(names, "log")/* ||
         !ModelMBeanInfoSupport.containsIgnoreCase(names, "logfile")*/)
      {
         return false;
      }
      // Case sensitive name
      String name = getName();
      if (name == null)
      {
         return false;
      }
      if (!name.equals(descriptor.getFieldValue("name")))
      {
         return false;
      }
      // Descriptor type must be 'notification'
      String desctype = (String)descriptor.getFieldValue("descriptortype");
      if (desctype.compareToIgnoreCase("notification") != 0) return false;
      // Severity needn't be checked. It was checked in
      // descriptor.isValid()
      int severity = objectToInt(descriptor.getFieldValue("severity"));
      if (severity < 0 || severity > 6)
      {
         return false;
      }

      return true;
   }

   private Descriptor createDefaultDescriptor()
   {
      String[] names = new String[]{"name", "descriptorType", "severity", "displayName"/*, "messageId", "log", "logfile"*/};
      Object[] values = new Object[]{getName(), "notification", "5", getName()/*, "0", "???", "???"*/};
      return new DescriptorSupport(names, values);
   }

   private int objectToInt(Object value)
   {
      if (value == null)
      {
         return -1;
      }

      if (value instanceof Number)
      {
         return ((Number)value).intValue();
      }
      else
      {
         try
         {
            return Integer.parseInt(value.toString());
         }
         catch (NumberFormatException x)
         {
            return -1;
         }
      }
   }
}
