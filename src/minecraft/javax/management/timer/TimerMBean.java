/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.timer;

import java.util.Date;
import java.util.Vector;
import javax.management.InstanceNotFoundException;

/**
 * Management interface for the {@link Timer} class.
 *
 * @version $Revision: 1.6 $
 */
public interface TimerMBean
{
   /**
    * Shortcut for {@link #addNotification(String,String,Object,Date,long,long,boolean) addNotification(type, message, userData, date, 0L, 0L, false)}
    */
   public Integer addNotification(String type, String message, Object userData, Date date) throws IllegalArgumentException;

   /**
    * Shortcut for {@link #addNotification(String,String,Object,Date,long,long,boolean) addNotification(type, message, userData, date, period, 0L, false)}
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period) throws IllegalArgumentException;

   /**
    * Shortcut for {@link #addNotification(String,String,Object,Date,long,long,boolean) addNotification(type, message, userData, date, period, occurences, false)}
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period, long occurences) throws IllegalArgumentException;

   /**
    * Adds a notification to this Timer. <br>
    * If the date is before the current date, and the notification is one-shot, it will be delivered immediately.
    *
    * @param type       The type of the notification
    * @param message    The message of the notification
    * @param userData   The custom user data of the notification
    * @param date       The date at which the notification should be delivered the first time
    * @param period     The period of time between notifications delivers
    * @param occurences The number of occurrences the notification should be delivered
    * @param fixedRate  True if the periodic notification should be sent at fixed rate, false if should be sent at fixed delay
    * @return The identifier for the notification
    * @throws IllegalArgumentException If some of the parameters has illegal value
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period, long occurences, boolean fixedRate) throws IllegalArgumentException;

   /**
    * Returns all identifiers for notifications added to this Timer
    */
   public Vector getAllNotificationIDs();

   /**
    * Returns the date for the notification with the given identifier
    *
    * @param id The notification identifier
    */
   public Date getDate(Integer id);

   /**
    * Returns whether the periodic notification with the given identifier is delivered at fixed rate or not
    *
    * @param id The notification identifier
    */
   public Boolean getFixedRate(Integer id);

   /**
    * Returns the number of notifications added to this Timer
    */
   public int getNbNotifications();

   /**
    * Returns the number of times the notification with the given identifier is delivered.
    *
    * @param id The notification identifier
    */
   public Long getNbOccurences(Integer id);

   /**
    * Returns the identifiers of the notifications with the given type
    *
    * @param type The notification type
    */
   public Vector getNotificationIDs(String type);

   /**
    * Returns the message of the notification with the given identifier
    *
    * @param id The notification identifier
    */
   public String getNotificationMessage(Integer id);

   /**
    * Returns the type of the notification with the given identifier
    *
    * @param id The notification identifier
    */
   public String getNotificationType(Integer id);

   /**
    * Returns the user data of the notification with the given identifier
    *
    * @param id The notification identifier
    */
   public Object getNotificationUserData(Integer id);

   /**
    * Returns the period between the deliver of two notifications for the notification with the given identifier
    *
    * @param id The notification identifier
    */
   public Long getPeriod(Integer id);

   /**
    * Returns whether this Timer delivers notifications that occurred while it has been stopped
    *
    * @see #setSendPastNotifications
    */
   public boolean getSendPastNotifications();

   /**
    * Returns whether this Timer delivers notifications or not.
    *
    * @see #start
    * @see #stop
    */
   public boolean isActive();

   /**
    * Returns whether this Timer has notifications to deliver.
    */
   public boolean isEmpty();

   /**
    * Removes all notifications from this Timer
    */
   public void removeAllNotifications();

   /**
    * Removes all notifications with the given type
    *
    * @param type The type of the notifications to be removed
    * @throws InstanceNotFoundException If no notifications with the given type are present
    */
   public void removeNotifications(String type) throws InstanceNotFoundException;

   /**
    * Removes the notification with the given identifier
    *
    * @param id The identifier of the notification to be removed
    * @throws InstanceNotFoundException If no notification with the given identifier is present
    */
   public void removeNotification(Integer id) throws InstanceNotFoundException;

   /**
    * Sets whether this Timer delivers notifications that occurred while it has been stopped
    *
    * @see #getSendPastNotifications
    */
   public void setSendPastNotifications(boolean value);

   /**
    * Starts this Timer. <br>
    * Only when a Timer is started it delivers notifications.
    * If {@link #getSendPastNotifications} returns true, this Timer sends notifications that were waiting
    * to be delivered, otherwise it updates the notification times to the next deliver.
    *
    * @see #stop
    */
   public void start();

   /**
    * Stops this Timer. <br>
    *
    * @see #start
    */
   public void stop();
}
