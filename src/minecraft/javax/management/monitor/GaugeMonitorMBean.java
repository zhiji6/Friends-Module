/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.monitor;

import javax.management.ObjectName;

/**
 * @version $Revision: 1.7 $
 */
public interface GaugeMonitorMBean extends MonitorMBean
{
   /**
    * @deprecated
    */
   public Number getDerivedGauge();

   /**
    * @deprecated
    */
   public long getDerivedGaugeTimeStamp();

   public Number getDerivedGauge(ObjectName objectName);

   public long getDerivedGaugeTimeStamp(ObjectName objectName);

   public Number getHighThreshold();

   public Number getLowThreshold();

   public void setThresholds(Number highValue, Number lowValue) throws IllegalArgumentException;

   public boolean getNotifyHigh();

   public void setNotifyHigh(boolean value);

   public boolean getNotifyLow();

   public void setNotifyLow(boolean value);

   public boolean getDifferenceMode();

   public void setDifferenceMode(boolean value);
}
