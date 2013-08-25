/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.EventObject;

/**
 * Notifications are events emitted by {@link NotificationEmitter}s
 *
 * @version $Revision: 1.8 $
 */
public class Notification extends EventObject
{
   private static final long serialVersionUID = -7516092053498031989L;

   /**
    * @serial The notification type
    */
   private String type;
   /**
    * @serial The notification sequence number
    */
   private long sequenceNumber;
   /**
    * @serial The notification timestamp
    */
   private long timeStamp;
   /**
    * @serial The notification message
    */
   private String message;
   /**
    * @serial The notification user data
    */
   private Object userData;
   /**
    * A duplicate for the existing data member in EventObject: this one is not transient and should hold only
    * ObjectNames (so they are Serializable and can be sent along the wire for remote notifications), and it's
    * protected and not private for a mistake in JMX 1.0, but for binary compatibility we leave it protected.
    *
    * @serial The ObjectName of the emitter MBean
    */
   protected Object source;

   /**
    * Convenience constructor for <code>Notification(type, source, sequenceNumber, System.currentTimeMillis(), null)</code>
    *
    * @see #Notification(String, Object, long, long, String)
    */
   public Notification(String type, Object source, long sequenceNumber)
   {
      this(type, source, sequenceNumber, System.currentTimeMillis(), null);
   }

   /**
    * Convenience constructor for <code>Notification(type, source, sequenceNumber, timestamp, null)</code>
    *
    * @see #Notification(String, Object, long, long, String)
    */
   public Notification(String type, Object source, long sequenceNumber, long timeStamp)
   {
      this(type, source, sequenceNumber, timeStamp, null);
   }

   /**
    * Convenience constructor for <code>Notification(type, source, sequenceNumber, System.currentTimeMillis(), message)</code>
    *
    * @see #Notification(String, Object, long, long, String)
    */
   public Notification(String type, Object source, long sequenceNumber, String message)
   {
      this(type, source, sequenceNumber, System.currentTimeMillis(), message);
   }

   /**
    * Creates a new Notification
    *
    * @param type           The notification type
    * @param source         The ObjectName of the emitter MBean
    * @param sequenceNumber The Notification sequence number
    * @param timeStamp      The notification timestamp
    * @param message        The Notification message
    */
   public Notification(String type, Object source, long sequenceNumber, long timeStamp, String message)
   {
      // Data member source of EventObject is transient
      super(source);
      this.source = source;
      this.type = type;
      this.sequenceNumber = sequenceNumber;
      this.timeStamp = timeStamp;
      this.message = message;
   }

   /**
    * Returns the notification message
    */
   public String getMessage()
   {
      return message;
   }

   /**
    * Returns the notification type
    */
   public String getType()
   {
      return type;
   }

   /**
    * Returns the notification source
    *
    * @see #setSource
    */
   public Object getSource()
   {
      return this.source;
   }

   /**
    * Sets the notification source
    *
    * @see #getSource
    */
   public void setSource(Object source)
   {
      this.source = source;
   }

   /**
    * Returns the notification sequence number
    *
    * @see #setSequenceNumber
    */
   public long getSequenceNumber()
   {
      return sequenceNumber;
   }

   /**
    * Sets the notification sequence number
    *
    * @see #getSequenceNumber
    */
   public void setSequenceNumber(long sequenceNumber)
   {
      this.sequenceNumber = sequenceNumber;
   }

   /**
    * Returns the notification timestamp
    *
    * @see #setTimeStamp
    */
   public long getTimeStamp()
   {
      return timeStamp;
   }

   /**
    * Sets the notification timestamp
    *
    * @see #getTimeStamp
    */
   public void setTimeStamp(long timeStamp)
   {
      this.timeStamp = timeStamp;
   }

   /**
    * Returns the notification user data
    *
    * @see #setUserData
    */
   public Object getUserData()
   {
      return userData;
   }

   /**
    * Sets the notification user data
    *
    * @see #getUserData
    */
   public void setUserData(Object userData)
   {
      this.userData = userData;
   }

   public String toString()
   {
      StringBuffer b = new StringBuffer("[");
      b.append("source=").append(getSource()).append(", ");
      b.append("message=").append(getMessage()).append(", ");
      b.append("sequence=").append(getSequenceNumber()).append(", ");
      b.append("type=").append(getType()).append(", ");
      b.append("time=").append(getTimeStamp()).append(", ");
      b.append("data=").append(getUserData());
      b.append("]");
      return b.toString();
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      // EventObject data member is transient
      super.source = source;
   }
}
