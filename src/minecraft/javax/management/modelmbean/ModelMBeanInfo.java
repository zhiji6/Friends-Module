/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.6 $
 */
public interface ModelMBeanInfo
{
   public Descriptor[] getDescriptors(String descriptorType) throws MBeanException, RuntimeOperationsException;

   public void setDescriptors(Descriptor[] descriptors) throws MBeanException, RuntimeOperationsException;

   public Descriptor getDescriptor(String descriptorName, String descriptorType) throws MBeanException, RuntimeOperationsException;

   public void setDescriptor(Descriptor descriptor, String descriptorType) throws MBeanException, RuntimeOperationsException;

   public Descriptor getMBeanDescriptor() throws MBeanException, RuntimeOperationsException;

   public void setMBeanDescriptor(Descriptor descriptor) throws MBeanException, RuntimeOperationsException;

   public ModelMBeanAttributeInfo getAttribute(String name) throws MBeanException, RuntimeOperationsException;

   public ModelMBeanOperationInfo getOperation(String name) throws MBeanException, RuntimeOperationsException;

   // The following method should be there for symmetry at least, but it's not present in the specification
// public ModelMBeanConstructorInfo getConstructor(String name) throws MBeanException, RuntimeOperationsException;

   public ModelMBeanNotificationInfo getNotification(String name) throws MBeanException, RuntimeOperationsException;

   public Object clone();

   public String getClassName();

   public String getDescription();

   public MBeanConstructorInfo[] getConstructors();

   public MBeanAttributeInfo[] getAttributes();

   public MBeanOperationInfo[] getOperations();

   public MBeanNotificationInfo[] getNotifications();
}
