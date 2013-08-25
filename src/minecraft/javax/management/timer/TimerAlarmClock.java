/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.timer;

import java.util.Date;
import java.util.TimerTask;

/**
 * This class is here only to provide signature compatibility for
 * {@link TimerAlarmClockNotification}.
 *
 * @version $Revision: 1.3 $
 */
class TimerAlarmClock extends TimerTask
{
   public TimerAlarmClock(Timer timer, Date nextOccurrence)
   {
   }

   public TimerAlarmClock(Timer timer, long timeout)
   {
   }

   public void run()
   {
   }
}
