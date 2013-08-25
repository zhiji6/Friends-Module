/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.5 $
 */
public interface ModelMBeanNotificationBroadcaster extends NotificationBroadcaster
{
   public void sendNotification(Notification notification) throws MBeanException, RuntimeOperationsException;

   public void sendNotification(String message) throws MBeanException, RuntimeOperationsException;

   public void addAttributeChangeNotificationListener(NotificationListener listener, String attributeName, Object handback) throws MBeanException, RuntimeOperationsException, IllegalArgumentException;

   public void removeAttributeChangeNotificationListener(NotificationListener listener, String attributeName) throws MBeanException, RuntimeOperationsException, ListenerNotFoundException;
//	public void removeAttributeChangeNotificationListener(NotificationListener listener, String attributeName, Object handback) throws MBeanException, RuntimeOperationsException, ListenerNotFoundException;
   public void sendAttributeChangeNotification(AttributeChangeNotification notification) throws MBeanException, RuntimeOperationsException;

   public void sendAttributeChangeNotification(Attribute oldValue, Attribute newValue) throws MBeanException, RuntimeOperationsException;
}
