/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.PersistentMBean;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.4 $
 */
public interface ModelMBean extends DynamicMBean, PersistentMBean, ModelMBeanNotificationBroadcaster
{
   public void setModelMBeanInfo(ModelMBeanInfo modelMBeanInfo) throws MBeanException, RuntimeOperationsException;

   public void setManagedResource(Object resource, String resourceType) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException;
}
