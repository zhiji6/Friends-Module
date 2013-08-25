/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.IOException;
import java.util.Map;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.security.auth.Subject;

/**
 * @version $Revision: 1.6 $
 */
public interface JMXConnector
{
   public static final String CREDENTIALS = "jmx.remote.credentials";

   public void connect() throws IOException, SecurityException;

   public void connect(Map environment) throws IOException, SecurityException;

   public MBeanServerConnection getMBeanServerConnection() throws IOException;

   public MBeanServerConnection getMBeanServerConnection(Subject delegate) throws IOException;

   public void close() throws IOException;

   public String getConnectionId() throws IOException;

   public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback);

   public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException;

   public void removeConnectionNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException;
}
