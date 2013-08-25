/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.timer;

import java.util.Date;
import javax.management.timer.TimerNotification;

/**
 * A subclass of TimeTask for the JMX Timer service.
 *
 * @version $Revision: 1.8 $
 */
public abstract class TimerTask extends TimeTask
{
   private TimerNotification m_notification;
   private long m_date;
   private long m_period;
   private long m_occurrences;
   private long m_initialOccurrences;
   private int m_hash;
   private boolean m_fixedRate;

   public TimerTask(TimerNotification n, Date date, long period, long occurrences, boolean fixedRate)
   {
      m_notification = n;
      m_date = date.getTime();
      m_period = period;
      m_occurrences = occurrences;
      m_initialOccurrences = occurrences;
      m_fixedRate = fixedRate;

      // Pre calculate hash code so that it does not reflect the fact that occurrences decrease
      m_hash = new Long(getDate()).hashCode() ^ new Long(getPeriod()).hashCode() ^ new Long(getInitialOccurrences()).hashCode();

      setNextExecutionTime(getDate());
   }

   public TimerNotification getNotification()
   {
      return m_notification;
   }

   public boolean isFinished()
   {
      return super.isFinished();
   }

   public void setFinished(boolean value)
   {
      super.setFinished(value);
   }

   public long getPeriod()
   {
      return m_period;
   }

   public boolean isPeriodic()
   {
      boolean periodic = getPeriod() > 0 && (getInitialOccurrences() == 0 || getOccurrences() > 0);
      return periodic;
   }

   public long getNextExecutionTime()
   {
      return super.getNextExecutionTime();
   }

   public void setNextExecutionTime(long time)
   {
      super.setNextExecutionTime(time);
      --m_occurrences;
   }

   public int hashCode()
   {
      return m_hash;
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;

      try
      {
         TimerTask other = (TimerTask)obj;
         return getDate() == other.getDate() && getPeriod() == other.getPeriod() && getInitialOccurrences() == other.getInitialOccurrences();
      }
      catch (ClassCastException x)
      {
         return false;
      }
   }

   public long getOccurrences()
   {
      return m_occurrences;
   }

   private long getInitialOccurrences()
   {
      return m_initialOccurrences;
   }

   public long getDate()
   {
      return m_date;
   }

   public boolean getFixedRate()
   {
      return m_fixedRate;
   }
}
