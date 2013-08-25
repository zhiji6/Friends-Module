/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import java.lang.reflect.Method;
import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.15 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class ModelMBeanAttributeInfo extends MBeanAttributeInfo implements DescriptorAccess
public class ModelMBeanAttributeInfo extends MBeanAttributeInfo implements DescriptorAccess, Cloneable
{
   private static final long serialVersionUID = 6181543027787327345L;

   private Descriptor attrDescriptor;

   public ModelMBeanAttributeInfo(String name, String description, Method getter, Method setter) throws IntrospectionException
   {
      this(name, description, getter, setter, null);
   }

   public ModelMBeanAttributeInfo(String name, String description, Method getter, Method setter, Descriptor descriptor) throws IntrospectionException
   {
      super(name, description, getter, setter);
      checkAndSetDescriptor(descriptor);
   }

   public ModelMBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs)
   {
      this(name, type, description, isReadable, isWritable, isIs, null);
   }

   public ModelMBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs, Descriptor descriptor)
   {
      super(name, type, description, isReadable, isWritable, isIs);
      checkAndSetDescriptor(descriptor);
   }

   public ModelMBeanAttributeInfo(ModelMBeanAttributeInfo copy)
   {
      super(copy.getName(), copy.getType(), copy.getDescription(), copy.isReadable(), copy.isWritable(), copy.isIs());
      checkAndSetDescriptor(copy.getDescriptor());
   }

   public Object clone()
   {
      return new ModelMBeanAttributeInfo(this);
   }

   public Descriptor getDescriptor()
   {
      return (Descriptor)attrDescriptor.clone();
   }

   public void setDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         attrDescriptor = createDefaultDescriptor();
      }
      else
      {
         if (isDescriptorValid(descriptor))
         {
            attrDescriptor = (Descriptor)descriptor.clone();
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
         attrDescriptor = createDefaultDescriptor();
      }
      else if (isDescriptorValid(descriptor))
      {
         attrDescriptor = (Descriptor)descriptor.clone();
         if (attrDescriptor.getFieldValue("displayname") == null)
         {
            attrDescriptor.setField("displayname", this.getName());
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

      // Mandatory fields are: name, descriptorType
      String[] names = descriptor.getFieldNames();

      if (!ModelMBeanInfoSupport.containsIgnoreCase(names, "name") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "descriptortype"))
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
      // Descriptor type must be 'attribute'
      String desctype = (String)descriptor.getFieldValue("descriptortype");
      if (desctype.compareToIgnoreCase("attribute") != 0) return false;

      return true;
   }

   private Descriptor createDefaultDescriptor()
   {
      String[] names = new String[]{"name", "descriptorType", "displayName"};
      Object[] values = new Object[]{getName(), "attribute", getName()};
      return new DescriptorSupport(names, values);
   }
}
