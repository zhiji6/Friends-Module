/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.monitor;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import mx4j.monitor.MX4JMonitor;

/**
 * @version $Revision: 1.16 $
 */
public abstract class Monitor extends NotificationBroadcasterSupport implements MonitorMBean, MBeanRegistration
{
   /**
    * @deprecated
    */
   protected int alreadyNotified;
   protected int alreadyNotifieds[];
   protected static final int capacityIncrement = 16;
   /**
    * @deprecated
    */
   protected String dbgTag;
   protected int elementCount;
   protected static final int OBSERVED_ATTRIBUTE_ERROR_NOTIFIED = 2;
   protected static final int OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED = 4;
   protected static final int OBSERVED_OBJECT_ERROR_NOTIFIED = 1;
   protected static final int RESET_FLAGS_ALREADY_NOTIFIED = 0;
   protected static final int RUNTIME_ERROR_NOTIFIED = 8;

   // Fields above are a mistake in the spec: JMX 1.0 was poorly written and these fields
   // made their way into the specification. MX4J's implementation is different from RI's
   // and we don't value the fields above.

   protected MBeanServer server;

   private MX4JMonitor monitor;

   abstract MX4JMonitor createMX4JMonitor();

   synchronized MX4JMonitor getMX4JMonitor()
   {
      if (monitor == null)
      {
         monitor = createMX4JMonitor();
      }
      return monitor;
   }

   public abstract void start();

   public abstract void stop();

   /**
    * @deprecated
    */
   public ObjectName getObservedObject()
   {
      ObjectName[] observed = getObservedObjects();
      if (observed == null || observed.length < 1) return null;
      return observed[0];
   }

   /**
    * @deprecated
    */
   public void setObservedObject(ObjectName objectName) throws java.lang.IllegalArgumentException
   {
      MX4JMonitor monitor = getMX4JMonitor();
      synchronized (monitor)
      {
         monitor.clearObservedObjects();
         monitor.addObservedObject(objectName);
      }
   }

   public String getObservedAttribute()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      return monitor.getObservedAttribute();
   }

   public void setObservedAttribute(String attribute) throws java.lang.IllegalArgumentException
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.setObservedAttribute(attribute);
   }

   public long getGranularityPeriod()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      return monitor.getGranularityPeriod();
   }

   public void setGranularityPeriod(long period) throws java.lang.IllegalArgumentException
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.setGranularityPeriod(period);
   }

   public boolean isActive()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      return monitor.isActive();
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      this.server = server;
      MX4JMonitor monitor = getMX4JMonitor();
      return monitor.preRegister(server, name);
   }

   public void postRegister(Boolean registrationDone)
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.postRegister(registrationDone);
   }

   public void preDeregister() throws Exception
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.preDeregister();
   }

   public void postDeregister()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.postDeregister();
   }

   public void addObservedObject(ObjectName objectName) throws IllegalArgumentException
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.addObservedObject(objectName);
   }

   public ObjectName[] getObservedObjects()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      return monitor.getObservedObjects();
   }

   public boolean containsObservedObject(ObjectName objectName)
   {
      MX4JMonitor monitor = getMX4JMonitor();
      return monitor.containsObservedObject(objectName);
   }

   public void removeObservedObject(ObjectName objectName)
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.removeObservedObject(objectName);
   }
}
