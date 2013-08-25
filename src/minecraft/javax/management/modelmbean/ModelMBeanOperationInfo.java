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
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.11 $
 */
public class ModelMBeanOperationInfo extends MBeanOperationInfo implements DescriptorAccess
{
   private static final long serialVersionUID = 6532732096650090465L;

   private Descriptor operationDescriptor;

   public ModelMBeanOperationInfo(String description, Method method)
   {
      this(description, method, null);
   }

   public ModelMBeanOperationInfo(String description, Method method, Descriptor descriptor)
   {
      super(description, method);
      checkAndSetDescriptor(descriptor);
   }

   public ModelMBeanOperationInfo(String name, String description, MBeanParameterInfo[] params, String type, int impact)
   {
      this(name, description, params, type, impact, null);
   }

   public ModelMBeanOperationInfo(String name, String description, MBeanParameterInfo[] params, String type, int impact, Descriptor descriptor)
   {
      super(name, description, params, type, impact);
      checkAndSetDescriptor(descriptor);
   }

   public ModelMBeanOperationInfo(ModelMBeanOperationInfo copy)
   {
      super(copy.getName(), copy.getDescription(), copy.getSignature(), copy.getReturnType(), copy.getImpact());
      checkAndSetDescriptor(copy.getDescriptor());
   }

   public Object clone()
   {
      return new ModelMBeanOperationInfo(this);
   }

   public Descriptor getDescriptor()
   {
      return (Descriptor)operationDescriptor.clone();
   }

   public void setDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         operationDescriptor = createDefaultDescriptor();
      }
      else
      {
         if (isDescriptorValid(descriptor))
         {
            operationDescriptor = (Descriptor)descriptor.clone();
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
         operationDescriptor = createDefaultDescriptor();
      }
      else if (isDescriptorValid(descriptor))
      {
         operationDescriptor = (Descriptor)descriptor.clone();
         if (operationDescriptor.getFieldValue("displayName") == null)
         {
            operationDescriptor.setField("displayName", getName());
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

      // Mandatory fields are: name, descriptorType, role, lastReturnedTimeStamp(?)
      String[] names = descriptor.getFieldNames();

      if (!ModelMBeanInfoSupport.containsIgnoreCase(names, "name") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "descriptortype") ||
          !ModelMBeanInfoSupport.containsIgnoreCase(names, "role"))
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
      // Role must be 'getter' or 'setter' or 'operation'
      if (!"getter".equals(descriptor.getFieldValue("role")) &&
          !"setter".equals(descriptor.getFieldValue("role")) &&
          !"operation".equals(descriptor.getFieldValue("role")))
      {
         return false;
      }

      return true;
   }

   private Descriptor createDefaultDescriptor()
   {
      String[] names = new String[]{"name", "descriptorType", "role", "displayName"/*, "lastReturnedTimeStamp"*/};
      Object[] values = new Object[]{getName(), "operation", "operation", getName()/*, "0"*/};
      return new DescriptorSupport(names, values);
   }
}
