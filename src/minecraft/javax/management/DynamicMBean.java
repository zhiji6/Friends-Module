/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.7 $
 */
public interface DynamicMBean
{
   public MBeanInfo getMBeanInfo();

   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException;

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException;

   public AttributeList getAttributes(String[] attributes);

   public AttributeList setAttributes(AttributeList attributes);

   public Object invoke(String method, Object[] arguments, String[] params) throws MBeanException, ReflectionException;
}
