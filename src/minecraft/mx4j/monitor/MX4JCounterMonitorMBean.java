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
 * @version $Revision: 1.3 $
 */
public interface MX4JCounterMonitorMBean extends MX4JMonitorMBean
{
   public Number getDerivedGauge(ObjectName objectName);

   public long getDerivedGaugeTimeStamp(ObjectName objectName);

   public Number getThreshold(ObjectName objectName);

   public Number getInitThreshold();

   public void setInitThreshold(Number value) throws IllegalArgumentException;

   public Number getOffset();

   public void setOffset(Number value) throws IllegalArgumentException;

   public Number getModulus();

   public void setModulus(Number value) throws IllegalArgumentException;

   public boolean getNotify();

   public void setNotify(boolean value);

   public boolean getDifferenceMode();

   public void setDifferenceMode(boolean value);
}
