/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.9 $
 */
public abstract class JMXConnectorServer extends NotificationBroadcasterSupport implements JMXConnectorServerMBean, MBeanRegistration
{
   public static final String AUTHENTICATOR = "jmx.remote.authenticator";

   private static final MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[]{new MBeanNotificationInfo
           (new String[]{JMXConnectionNotification.OPENED, JMXConnectionNotification.CLOSED, JMXConnectionNotification.FAILED},
            JMXConnectionNotification.class.getName(),
            "Notifications emitted by the JMXConnectorServer MBean upon opening, closing or failing of a connection")};

   private static long notificationSequenceNumber;

   private MBeanServer server;
   private ObjectName name;
   private final HashSet connections = new HashSet();

   public JMXConnectorServer()
   {
   }

   public JMXConnectorServer(MBeanServer server)
   {
      this.server = server;
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name)
   {
      if (name == null) throw new NullPointerException("ObjectName cannot be null");
      if (this.server == null) this.server = server;
      this.name = name;
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister() throws Exception
   {
      if (isActive()) stop();
   }

   public void postDeregister()
   {
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return notifications;
   }

   public MBeanServer getMBeanServer()
   {
      return server;
   }

   public void setMBeanServerForwarder(MBeanServerForwarder forwarder) throws IllegalArgumentException
   {
      if (forwarder.getMBeanServer() == null)
      {
         if (server == null) throw new IllegalStateException("This JMXConnectorServer is not attached to an MBeanServer");
      }
      forwarder.setMBeanServer(server);
      this.server = forwarder;
   }

   public JMXConnector toJMXConnector(Map environment) throws IOException
   {
      JMXServiceURL address = getAddress();
      return JMXConnectorFactory.newJMXConnector(address, environment);
   }

   public String[] getConnectionIds()
   {
      Set copy = null;
      synchronized (connections)
      {
         copy = (Set)connections.clone();
      }
      return (String[])copy.toArray(new String[copy.size()]);
   }

   protected void connectionOpened(String connectionId, String message, Object userData)
   {
      synchronized (connections)
      {
         boolean added = connections.add(connectionId);
         if (!added) throw new IllegalStateException("Duplicate connection ID: " + connectionId);
      }

      Object source = name;
      if (source == null) source = this;
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, connectionId, getNextSequenceNumber(), message, userData);
      sendNotification(notification);
   }

   protected void connectionClosed(String connectionId, String message, Object userData)
   {
      synchronized (connections)
      {
         boolean removed = connections.remove(connectionId);
         if (!removed) throw new IllegalStateException("Connection ID not present: " + connectionId);
      }

      Object source = name;
      if (source == null) source = this;
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.CLOSED, source, connectionId, getNextSequenceNumber(), message, userData);
      sendNotification(notification);
   }

   protected void connectionFailed(String connectionId, String message, Object userData)
   {
      synchronized (connections)
      {
         boolean removed = connections.remove(connectionId);
         if (!removed) throw new IllegalStateException("Connection ID not present: " + connectionId);
      }

      Object source = name;
      if (source == null) source = this;
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.FAILED, source, connectionId, getNextSequenceNumber(), message, userData);
      sendNotification(notification);
   }

   private long getNextSequenceNumber()
   {
      synchronized (JMXConnectorServer.class)
      {
         return ++notificationSequenceNumber;
      }
   }
}
