/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import java.lang.reflect.Constructor;
import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.14 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class ModelMBeanConstructorInfo extends MBeanConstructorInfo implements DescriptorAccess
public class ModelMBeanConstructorInfo extends MBeanConstructorInfo implements DescriptorAccess, Cloneable
{
   private static final long serialVersionUID = 3862947819818064362L;

   private Descriptor consDescriptor;

   public ModelMBeanConstructorInfo(String description, Constructor constructor)
   {
      this(description, constructor, null);
   }

   public ModelMBeanConstructorInfo(String description, Constructor constructor, Descriptor descriptor)
   {
      super(description, constructor);
      checkAndSetDescriptor(descriptor);
   }

   public ModelMBeanConstructorInfo(String name, String description, MBeanParameterInfo[] params)
   {
      this(name, description, params, null);
   }

   public ModelMBeanConstructorInfo(String name, String description, MBeanParameterInfo[] params, Descriptor descriptor)
   {
      super(name, description, params);
      checkAndSetDescriptor(descriptor);
   }

   ModelMBeanConstructorInfo(ModelMBeanConstructorInfo copy)
   {
      super(copy.getName(), copy.getDescription(), copy.getSignature());
      checkAndSetDescriptor(copy.getDescriptor());
   }

   public Object clone()
   {
      return new ModelMBeanConstructorInfo(this);
   }

   public Descriptor getDescriptor()
   {
      return (Descriptor)consDescriptor.clone();
   }

   public void setDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         consDescriptor = createDefaultDescriptor();
      }
      else
      {
         if (isDescriptorValid(descriptor))
         {
            consDescriptor = (Descriptor)descriptor.clone();
         }
         else
         {
            // Not sure what to do here: javadoc says IllegalArgument, but for example ModelMBeanInfo throws RuntimeOperations
            // which is consistent with the fact that all exception thrown by the JMX implementation should be JMX exceptions
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid descriptor"));
         }
      }
   }

   private void checkAndSetDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         consDescriptor = createDefaultDescriptor();
      }
      else if (isDescriptorValid(descriptor))
      {
         consDescriptor = (Descriptor)descriptor.clone();
         if (consDescriptor.getFieldValue("displayName") == null)
         {
            consDescriptor.setField("displayName", getName());
         }
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid descriptor"));
      }
   }

   private boolean isDescriptorValid(Descriptor descriptor)
   {
      if (!descriptor.isValid())
      {
         return false;
      }

      // Spec compliance checks

      // Mandatory fields are: name, descriptorType, role
      String[] names = descriptor.getFieldNames();

      if (!ModelMBeanInfoSupport.containsIgnoreCase(names, "name") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "descriptortype") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "role"))
      {
         return false;
      }

      if (ModelMBeanInfoSupport.containsIgnoreCase(names, "persistpolicy") ||
          ModelMBeanInfoSupport.containsIgnoreCase(names, "currencytimelimit"))
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
      // Descriptor type must be 'operation'
      String desctype = (String)descriptor.getFieldValue("descriptortype");
      if (desctype.compareToIgnoreCase("operation") != 0) return false;
      // Role must be 'constructor'
      String role = (String)descriptor.getFieldValue("role");
      if (role.compareTo("constructor") != 0) return false;

      return true;
   }

   private Descriptor createDefaultDescriptor()
   {
      String[] names = new String[]{"name", "descriptorType", "role", "displayName"/*, "lastReturnedTimeStamp"*/};
      Object[] values = new Object[]{getName(), "operation", "constructor", getName()/*, "0"*/};
      return new DescriptorSupport(names, values);
   }
}
