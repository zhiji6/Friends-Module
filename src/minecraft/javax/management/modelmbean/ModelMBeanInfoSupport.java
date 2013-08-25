/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.19 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class ModelMBeanInfoSupport extends MBeanInfo implements ModelMBeanInfo
public class ModelMBeanInfoSupport extends MBeanInfo implements ModelMBeanInfo, Serializable
{
   private static final MBeanAttributeInfo[] EMPTY_ATTRS = {};
   private static final MBeanConstructorInfo[] EMPTY_CTRS = {};
   private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = {};
   private static final MBeanOperationInfo[] EMPTY_OPS = {};

   private static final long serialVersionUID = -1935722590756516193L;

   private Descriptor modelMBeanDescriptor;

   // The following data members are duplicated from parent class, since there is no way for
   // the serialization mechanism to set the deserialized objects to the parent object.
   private MBeanAttributeInfo[] modelMBeanAttributes;
   private MBeanConstructorInfo[] modelMBeanConstructors;
   private MBeanNotificationInfo[] modelMBeanNotifications;
   private MBeanOperationInfo[] modelMBeanOperations;

   public ModelMBeanInfoSupport(String className, String description, ModelMBeanAttributeInfo[] attributes, ModelMBeanConstructorInfo[] constructors, ModelMBeanOperationInfo[] operations, ModelMBeanNotificationInfo[] notifications)
   {
      this(className, description, attributes, constructors, operations, notifications, null);
   }

   public ModelMBeanInfoSupport(String className, String description, ModelMBeanAttributeInfo[] attributes, ModelMBeanConstructorInfo[] constructors, ModelMBeanOperationInfo[] operations, ModelMBeanNotificationInfo[] notifications, Descriptor mbeanDescriptor)
   {
      super(className, description, attributes, constructors, operations, notifications);
      modelMBeanAttributes = attributes != null ? attributes : EMPTY_ATTRS;
      modelMBeanConstructors = constructors != null ? constructors : EMPTY_CTRS;
      modelMBeanNotifications = notifications != null ? notifications : EMPTY_NOTIFICATIONS;
      modelMBeanOperations = operations != null ? operations : EMPTY_OPS;
      checkAndSetDescriptor(mbeanDescriptor);
   }

   public ModelMBeanInfoSupport(ModelMBeanInfo model)
   {
      super(model.getClassName(), model.getDescription(), model.getAttributes(), model.getConstructors(), model.getOperations(), model.getNotifications());
      if (model.getAttributes() != null)
      {
         // cannot assume they are already ModelMBeanAttributeInfo
         MBeanAttributeInfo attributes[] = model.getAttributes();
         modelMBeanAttributes = new ModelMBeanAttributeInfo[attributes.length];
         for (int i = 0; i < attributes.length; i++)
         {
            MBeanAttributeInfo attribute = attributes[i];
            if (attribute instanceof ModelMBeanAttributeInfo)
               modelMBeanAttributes[i] = new ModelMBeanAttributeInfo((ModelMBeanAttributeInfo)attribute);
            else
               modelMBeanAttributes[i] = new ModelMBeanAttributeInfo(attribute.getName(), attribute.getType(), attribute.getDescription(), attribute.isReadable(), attribute.isWritable(), attribute.isIs());
         }
      }
      if (model.getConstructors() != null)
      {
         // cannot assume they are already ModelMBeanConstructorInfo
         MBeanConstructorInfo constructors[] = model.getConstructors();
         modelMBeanConstructors = new ModelMBeanConstructorInfo[constructors.length];
         for (int i = 0; i < constructors.length; i++)
         {
            MBeanConstructorInfo constructor = constructors[i];
            if (constructor instanceof ModelMBeanConstructorInfo)
               modelMBeanConstructors[i] = new ModelMBeanConstructorInfo((ModelMBeanConstructorInfo)constructor);
            else
               modelMBeanConstructors[i] = new ModelMBeanConstructorInfo(constructor.getName(), constructor.getDescription(), constructor.getSignature());
         }
      }
      if (model.getOperations() != null)
      {
         // cannot assume they are already ModelMBeanOperationInfo
         MBeanOperationInfo operations[] = model.getOperations();
         modelMBeanOperations = new ModelMBeanOperationInfo[operations.length];
         for (int i = 0; i < operations.length; i++)
         {
            MBeanOperationInfo operation = operations[i];
            if (operation instanceof ModelMBeanOperationInfo)
               modelMBeanOperations[i] = new ModelMBeanOperationInfo((ModelMBeanOperationInfo)operation);
            else
               modelMBeanOperations[i] = new ModelMBeanOperationInfo(operation.getName(), operation.getDescription(), operation.getSignature(), operation.getReturnType(), operation.getImpact());
         }
      }
      if (model.getNotifications() != null)
      {
         // cannot assume they are already ModelMBeanNotificationInfo
         MBeanNotificationInfo notifications[] = model.getNotifications();
         modelMBeanNotifications = new ModelMBeanNotificationInfo[notifications.length];
         for (int i = 0; i < notifications.length; i++)
         {
            MBeanNotificationInfo notification = notifications[i];
            if (notification instanceof ModelMBeanNotificationInfo)
               modelMBeanNotifications[i] = new ModelMBeanNotificationInfo((ModelMBeanNotificationInfo)notification);
            else
               modelMBeanNotifications[i] = new ModelMBeanNotificationInfo(notification.getNotifTypes(), notification.getName(), notification.getDescription());
         }
      }
      Descriptor mBeanDescriptor = null;
      try
      {
         mBeanDescriptor = model.getMBeanDescriptor();
      }
      catch (Exception e)
      {
         // if there is an exception we use null
      }
      checkAndSetDescriptor(mBeanDescriptor);
   }

   public Object clone()
   {
      return new ModelMBeanInfoSupport(this);
   }

   public Descriptor[] getDescriptors(String type) throws MBeanException, RuntimeOperationsException
   {
      // On the type the 'role' is not used, so for constructor and operation there
      // will be type=constructor and type=operation respectively
      // If type == null, means all descriptors
      if (type == null)
      {
         Descriptor[] attrs = getDescriptors("attribute");
         Descriptor[] opers = getDescriptors("operation");
         Descriptor[] ctors = getDescriptors("constructor");
         Descriptor[] notifs = getDescriptors("notification");
         Descriptor[] all = new Descriptor[attrs.length + opers.length + ctors.length + notifs.length + 1];
         int i = 0;
         all[i] = getMBeanDescriptor();
         ++i;
         System.arraycopy(attrs, 0, all, i, attrs.length);
         i += attrs.length;
         System.arraycopy(opers, 0, all, i, opers.length);
         i += opers.length;
         System.arraycopy(ctors, 0, all, i, ctors.length);
         i += ctors.length;
         System.arraycopy(notifs, 0, all, i, notifs.length);

         return all;
      }
      else if (type.equals("mbean"))
      {
         return new Descriptor[]{getMBeanDescriptor()};
      }
      else if (type.equals("attribute"))
      {
         MBeanAttributeInfo[] attrs = modelMBeanAttributes;
         if (attrs == null)
         {
            return new Descriptor[0];
         }
         Descriptor[] attributes = new Descriptor[attrs.length];
         for (int i = 0; i < attrs.length; ++i)
         {
            ModelMBeanAttributeInfo attr = (ModelMBeanAttributeInfo)attrs[i];
            // It's already cloned
            attributes[i] = attr.getDescriptor();
         }
         return attributes;
      }
      else if (type.equals("operation"))
      {
         MBeanOperationInfo[] opers = modelMBeanOperations;
         if (opers == null)
         {
            return new Descriptor[0];
         }
         Descriptor[] operations = new Descriptor[opers.length];
         for (int i = 0; i < opers.length; ++i)
         {
            ModelMBeanOperationInfo oper = (ModelMBeanOperationInfo)opers[i];
            // It's already cloned
            operations[i] = oper.getDescriptor();
         }
         return operations;
      }
      else if (type.equals("constructor"))
      {
         MBeanConstructorInfo[] ctors = modelMBeanConstructors;
         if (ctors == null)
         {
            return new Descriptor[0];
         }
         Descriptor[] constructors = new Descriptor[ctors.length];
         for (int i = 0; i < ctors.length; ++i)
         {
            ModelMBeanConstructorInfo ctor = (ModelMBeanConstructorInfo)ctors[i];
            // It's already cloned
            constructors[i] = ctor.getDescriptor();
         }
         return constructors;
      }
      else if (type.equals("notification"))
      {
         MBeanNotificationInfo[] notifs = modelMBeanNotifications;
         if (notifs == null)
         {
            return new Descriptor[0];
         }
         Descriptor[] notifications = new Descriptor[notifs.length];
         for (int i = 0; i < notifs.length; ++i)
         {
            ModelMBeanNotificationInfo notif = (ModelMBeanNotificationInfo)notifs[i];
            // It's already cloned
            notifications[i] = notif.getDescriptor();
         }
         return notifications;
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid descriptor type"));
      }
   }

   public void setDescriptors(Descriptor[] descriptors) throws MBeanException, RuntimeOperationsException
   {
      if (descriptors == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Descriptors cannot be null"));
      }
      RuntimeOperationsException x = null;
      for (int i = 0; i < descriptors.length; ++i)
      {
         // PENDING: what should I do in case of exception setting one descriptor ?
         // Going on with the other descriptors or let the exception out ?
         try
         {
            setDescriptor(descriptors[i], null);
         }
         catch (RuntimeOperationsException ignored)
         {
            x = ignored;
         }
      }
      // PENDING: don't know if this is a suitable solution, anyhow...
      if (x != null)
      {
         throw x;
      }
   }

   public Descriptor getDescriptor(String name) throws MBeanException, RuntimeOperationsException
   {
      return getDescriptor(name, null);
   }

   public Descriptor getDescriptor(String name, String type) throws MBeanException, RuntimeOperationsException
   {
      if (name == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor name cannot be null"));
      }
      //if (type == null) {throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor type cannot be null"));}

      if ("mbean".equals(type))
      {
         return getMBeanDescriptor();
      }
      else if (type != null)
      {
         Descriptor[] descrs = getDescriptors(type);
         for (int i = 0; i < descrs.length; ++i)
         {
            Descriptor descr = descrs[i];
            if (name.equals(descr.getFieldValue("name")))
            {
               // Found, no need to clone it.
               return descr;
            }
         }
      }
      else
      {
         // will have to check them all
         Descriptor result = findDescriptorByName(modelMBeanAttributes, name);
         if (result != null)
         {
            return result;
         }
         result = findDescriptorByName(modelMBeanConstructors, name);
         if (result != null)
         {
            return result;
         }
         result = findDescriptorByName(modelMBeanNotifications, name);
         if (result != null)
         {
            return result;
         }
         result = findDescriptorByName(modelMBeanOperations, name);
         if (result != null)
         {
            return result;
         }
      }
      return null;
   }

   public void setDescriptor(Descriptor descriptor, String descriptorType) throws MBeanException, RuntimeOperationsException
   {
      // PENDING: should throw instead of returning ?
      if (descriptor == null)
      {
         return;
      }
      if (descriptorType == null)
      {
         descriptorType = (String)descriptor.getFieldValue("descriptorType");
         // Still null ?
         if (descriptorType == null)
         {
            throw new RuntimeOperationsException(new IllegalArgumentException("Field descriptorType in the given descriptor is not valid"));
         }

         if (descriptorType.equals("operation"))
         {
            // Take the role to distinguish between operation and constructor
            String role = (String)descriptor.getFieldValue("role");
            if (role == null)
            {
               throw new RuntimeOperationsException(new IllegalArgumentException("Field role in the given descriptor is not valid"));
            }

            descriptorType = role;
         }
      }

      String name = (String)descriptor.getFieldValue("name");
      if (name == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Field name in the given descriptor is not valid"));
      }

      // Now decide from the descriptor type where to find the right descriptor
      if ("mbean".equals(descriptorType))
      {
         setMBeanDescriptor(descriptor);
      }
      else if ("attribute".equals(descriptorType))
      {
         MBeanAttributeInfo[] attrs = modelMBeanAttributes;
         if (attrs != null)
         {
            for (int i = 0; i < attrs.length; ++i)
            {
               ModelMBeanAttributeInfo attr = (ModelMBeanAttributeInfo)attrs[i];
               if (name.equals(attr.getName()))
               {
                  // Found the right one
                  attr.setDescriptor(descriptor);
                  break;
               }
            }
         }
      }
      else if ("notification".equals(descriptorType))
      {
         MBeanNotificationInfo[] notifs = modelMBeanNotifications;
         if (notifs != null)
         {
            for (int i = 0; i < notifs.length; ++i)
            {
               ModelMBeanNotificationInfo notif = (ModelMBeanNotificationInfo)notifs[i];
               if (name.equals(notif.getName()))
               {
                  // Found the right one
                  notif.setDescriptor(descriptor);
                  break;
               }
            }
         }
      }
      else if ("constructor".equals(descriptorType))
      {
         MBeanConstructorInfo[] ctors = modelMBeanConstructors;
         if (ctors != null)
         {
            for (int i = 0; i < ctors.length; ++i)
            {
               ModelMBeanConstructorInfo ctor = (ModelMBeanConstructorInfo)ctors[i];
               if (name.equals(ctor.getName()))
               {
                  // Found the right one
                  ctor.setDescriptor(descriptor);
                  break;
               }
            }
         }
      }
      else if ("operation".equals(descriptorType)/* || descriptorType.equals("getter") || descriptorType.equals("setter")*/)
      {
         MBeanOperationInfo[] opers = modelMBeanOperations;
         if (opers != null)
         {
            for (int i = 0; i < opers.length; ++i)
            {
               ModelMBeanOperationInfo oper = (ModelMBeanOperationInfo)opers[i];
               if (name.equals(oper.getName()))
               {
                  // Found the right one
                  oper.setDescriptor(descriptor);
                  break;
               }
            }
         }
      }
   }

   public ModelMBeanAttributeInfo getAttribute(String name) throws MBeanException, RuntimeOperationsException
   {
      if (name == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Name argument cannot be null"));
      }
      MBeanAttributeInfo[] attrs = modelMBeanAttributes;
      if (attrs != null)
      {
         for (int i = 0; i < attrs.length; ++i)
         {
            ModelMBeanAttributeInfo attr = (ModelMBeanAttributeInfo)attrs[i];
            if (name.equals(attr.getName()))
            {
               // Clone, since the returned attribute is modifiable
               return (ModelMBeanAttributeInfo)attr.clone();
            }
         }
      }
      // Not found, return null
      return null;
   }

   public ModelMBeanOperationInfo getOperation(String name) throws MBeanException, RuntimeOperationsException
   {
      if (name == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Name argument cannot be null"));
      }
      MBeanOperationInfo[] opers = modelMBeanOperations;
      if (opers != null)
      {
         for (int i = 0; i < opers.length; ++i)
         {
            ModelMBeanOperationInfo oper = (ModelMBeanOperationInfo)opers[i];
            if (name.equals(oper.getName()))
            {
               // Clone, since the returned operation is modifiable
               return (ModelMBeanOperationInfo)oper.clone();
            }
         }
      }
      // Not found, return null
      return null;
   }

   public ModelMBeanConstructorInfo getConstructor(String name) throws MBeanException, RuntimeOperationsException
   {
      if (name == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Name argument cannot be null"));
      }
      MBeanConstructorInfo[] ctors = modelMBeanConstructors;
      if (ctors != null)
      {
         for (int i = 0; i < ctors.length; ++i)
         {
            ModelMBeanConstructorInfo ctor = (ModelMBeanConstructorInfo)ctors[i];
            if (name.equals(ctor.getName()))
            {
               // Clone, since the returned operation is modifiable
               return (ModelMBeanConstructorInfo)ctor.clone();
            }
         }
      }
      // Not found, return null
      return null;
   }

   public ModelMBeanNotificationInfo getNotification(String name) throws MBeanException, RuntimeOperationsException
   {
      if (name == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Name argument cannot be null"));
      }
      MBeanNotificationInfo[] notifs = modelMBeanNotifications;
      if (notifs != null)
      {
         for (int i = 0; i < notifs.length; ++i)
         {
            ModelMBeanNotificationInfo notif = (ModelMBeanNotificationInfo)notifs[i];
            if (name.equals(notif.getName()))
            {
               // Clone, since the returned operation is modifiable
               return (ModelMBeanNotificationInfo)notif.clone();
            }
         }
      }
      // Not found, return null
      return null;
   }

   public Descriptor getMBeanDescriptor() throws MBeanException, RuntimeOperationsException
   {
      return (Descriptor)modelMBeanDescriptor.clone();
   }

   public void setMBeanDescriptor(Descriptor descriptor) throws MBeanException, RuntimeOperationsException
   {
      if (descriptor == null)
      {
         // Replace with default descriptor
         modelMBeanDescriptor = createDefaultMBeanDescriptor();
      }
      else
      {
         if (isDescriptorValid(descriptor))
         {
            modelMBeanDescriptor = (Descriptor)descriptor.clone();
         }
         else
         {
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid descriptor"));
         }
      }
   }

   public MBeanConstructorInfo[] getConstructors()
   {
      // I should clone, since MBeanConstructorInfo is immutable, but ModelMBeanConstructorInfo it isn't
      MBeanConstructorInfo[] ctors = modelMBeanConstructors;
      if (ctors == null)
      {
         return null;
      }
      ModelMBeanConstructorInfo[] constructors = new ModelMBeanConstructorInfo[ctors.length];
      for (int i = 0; i < ctors.length; ++i)
      {
         ModelMBeanConstructorInfo ctor = (ModelMBeanConstructorInfo)ctors[i];
         constructors[i] = (ModelMBeanConstructorInfo)ctor.clone();
      }
      return constructors;
   }

   public MBeanAttributeInfo[] getAttributes()
   {
      // I should clone, since MBeanAttributeInfo is immutable, but ModelMBeanAttributeInfo it isn't
      MBeanAttributeInfo[] attrs = modelMBeanAttributes;
      if (attrs == null)
      {
         return null;
      }
      ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[attrs.length];
      for (int i = 0; i < attrs.length; ++i)
      {
         ModelMBeanAttributeInfo attr = (ModelMBeanAttributeInfo)attrs[i];
         attributes[i] = (ModelMBeanAttributeInfo)attr.clone();
      }
      return attributes;
   }

   public MBeanOperationInfo[] getOperations()
   {
      // I should clone, since MBeanOperationInfo is immutable, but ModelMBeanOperationInfo it isn't
      MBeanOperationInfo[] opers = modelMBeanOperations;
      if (opers == null)
      {
         return null;
      }
      ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[opers.length];
      for (int i = 0; i < opers.length; ++i)
      {
         ModelMBeanOperationInfo oper = (ModelMBeanOperationInfo)opers[i];
         operations[i] = (ModelMBeanOperationInfo)oper.clone();
      }
      return operations;
   }

   public MBeanNotificationInfo[] getNotifications()
   {
      // I should clone, since MBeanNotificationInfo is immutable, but ModelMBeanNotificationInfo it isn't
      MBeanNotificationInfo[] notifs = modelMBeanNotifications;
      if (notifs == null)
      {
         return null;
      }
      ModelMBeanNotificationInfo[] notifications = new ModelMBeanNotificationInfo[notifs.length];
      for (int i = 0; i < notifs.length; ++i)
      {
         ModelMBeanNotificationInfo notif = (ModelMBeanNotificationInfo)notifs[i];
         notifications[i] = (ModelMBeanNotificationInfo)notif.clone();
      }
      return notifications;
   }

   private void checkAndSetDescriptor(Descriptor descriptor)
   {
      if (descriptor == null)
      {
         modelMBeanDescriptor = createDefaultMBeanDescriptor();
      }
      else if (isDescriptorValid(descriptor))
      {
         modelMBeanDescriptor = addRequiredFields(descriptor);
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

      // Mandatory fields are: name, descriptorType, persistLocation(?), persistName(?), log(?), logFile(?)
      String[] names = descriptor.getFieldNames();

      if (!containsIgnoreCase(names, "name") ||
          !containsIgnoreCase(names, "descriptortype")/* ||
         !containsIgnoreCase(names, "persistlocation") ||
         !containsIgnoreCase(names, "persistname") ||
         !containsIgnoreCase(names, "log") ||
         !containsIgnoreCase(names, "logfile")*/)
      {
         return false;
      }

      // The spec is unclear on what the field name should contain: the name of the MBean or its class name ?
      // For now I stay loose, but since it is a dynamic MBean the className cannot be null; this check will be done
      // by RequiredModelMBean
//		String name = getClassName();
//		if (name == null) {return false;}
//		if (!name.equals(descriptor.getFieldValue("name"))) {return false;}

      // Descriptor type must be 'MBean'
      String desctype = (String)descriptor.getFieldValue("descriptortype");
      if (desctype.compareToIgnoreCase("mbean") != 0) return false;

      return true;
   }

   private Descriptor createDefaultMBeanDescriptor()
   {
      // The spec and the javadoc are misaligned WRT the default mbean descriptor:
      // Spec says the values of fields are case sensitive, javadoc does not care...
      // For field 'export', spec says that a value of null means not visible to other Agent and that any other value means
      // that is visible, while javadoc says 'F' means not visible...
      // Go with the Javadoc to mimic the RI (WkH)
      String[] names = new String[]{"name", "descriptorType", "displayName", "persistPolicy", "log", "export", "visibility"};
      int index = getClassName().lastIndexOf('.') + 1;
      Object[] values = new Object[]{getClassName().substring(index), "mbean", getClassName(), "Never", "F", "F", "1"};
      return new DescriptorSupport(names, values);
   }


   private Descriptor findDescriptorByName(MBeanFeatureInfo[] features, String name)
   {
      if (features != null)
      {
         for (int i = 0; i < features.length; ++i)
         {
            MBeanFeatureInfo feature = features[i];
            if (feature != null && feature.getName().equals(name) && feature instanceof DescriptorAccess)
            {
               return ((DescriptorAccess)feature).getDescriptor();
            }
         }
      }
      return null;
   }

   private Descriptor addRequiredFields(Descriptor d)
   {
      Descriptor result = (Descriptor)d.clone();
      String[] reqfields = {
         "displayname",
         "persistpolicy",
         "log",
         "export",
         "visibility"
      };
      String[] defvalues = {
         (String)d.getFieldValue("name"),
         "never",
         "F",
         "F",
         "1"
      };
      List fields = Arrays.asList(d.getFieldNames());
      for (int i = 0; i < reqfields.length; i++)
      {
         if (fields.contains(reqfields[i]) == false)
         {
            result.setField(reqfields[i], defvalues[i]);
         }
      }
      return result;
   }

   static boolean containsIgnoreCase(String[] fields, String field)
   {
      for (int i = 0; i < fields.length; ++i)
      {
         if (fields[i].equalsIgnoreCase(field)) return true;
      }
      return false;
   }
}
