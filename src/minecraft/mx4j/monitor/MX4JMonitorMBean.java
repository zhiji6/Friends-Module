/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.monitor;

import javax.management.ObjectName;

/**
 * @version $Revision: 1.2 $
 */
public interface MX4JMonitorMBean
{
   public void start();

   public void stop();

   public boolean isActive();

   public void addObservedObject(ObjectName object) throws IllegalArgumentException;

   public void removeObservedObject(ObjectName object);

   public boolean containsObservedObject(ObjectName object);

   public ObjectName[] getObservedObjects();

   public String getObservedAttribute();

   public void setObservedAttribute(String attribute);

   public long getGranularityPeriod();

   public void setGranularityPeriod(long period) throws java.lang.IllegalArgumentException;
}
