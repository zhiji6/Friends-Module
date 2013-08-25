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
public interface MX4JStringMonitorMBean extends MX4JMonitorMBean
{
   public String getDerivedGauge(ObjectName objectName);

   public long getDerivedGaugeTimeStamp(ObjectName objectName);

   public String getStringToCompare();

   public void setStringToCompare(String value) throws IllegalArgumentException;

   public boolean getNotifyMatch();

   public void setNotifyMatch(boolean value);

   public boolean getNotifyDiffer();

   public void setNotifyDiffer(boolean value);
}
