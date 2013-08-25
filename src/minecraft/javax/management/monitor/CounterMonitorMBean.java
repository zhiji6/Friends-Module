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
public interface CounterMonitorMBean extends MonitorMBean
{
   /**
    * @deprecated
    */
   public Number getDerivedGauge();

   /**
    * @deprecated
    */
   public long getDerivedGaugeTimeStamp();

   /**
    * @deprecated
    */
   public Number getThreshold();

   /**
    * @deprecated
    */
   public void setThreshold(Number value) throws java.lang.IllegalArgumentException;

   public Number getDerivedGauge(ObjectName objectName);

   public long getDerivedGaugeTimeStamp(ObjectName objectName);

   public Number getThreshold(ObjectName objectName);

   public Number getInitThreshold();

   public void setInitThreshold(Number value) throws IllegalArgumentException;

   public Number getOffset();

   public void setOffset(Number value) throws IllegalArgumentException;

   public Number getModulus();

   public void setModulus(Number value) throws java.lang.IllegalArgumentException;

   public boolean getNotify();

   public void setNotify(boolean value);

   public boolean getDifferenceMode();

   public void setDifferenceMode(boolean value);
}
