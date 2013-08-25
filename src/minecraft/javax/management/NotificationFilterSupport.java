/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Default implementation of a NotificationListener that filters out Notifications that
 * does not match the types enabled in this filter.
 *
 * @version $Revision: 1.11 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class NotificationFilterSupport implements NotificationFilter
public class NotificationFilterSupport implements NotificationFilter, Serializable
{
   private static final long serialVersionUID = 6579080007561786969L;

   private static final String serialName = "enabledTypes";
   /**
    * @serialField enabledTypes List The list of notification types that this filter will not filter out
    */
   private static final ObjectStreamField[] serialPersistentFields =
           {
              new ObjectStreamField(serialName, List.class)
           };

   private HashSet types = new HashSet();

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof NotificationFilterSupport)) return false;

      final NotificationFilterSupport support = (NotificationFilterSupport)o;

      if (!types.equals(support.types)) return false;

      return true;
   }

   public int hashCode()
   {
      return types.hashCode();
   }

   /**
    * Allows the given notification type to be received by listeners
    *
    * @param type The notification type to enable
    * @throws IllegalArgumentException If the notification type is null
    */
   public void enableType(String type) throws IllegalArgumentException
   {
      if (type == null) throw new IllegalArgumentException("Null notification type");
      synchronized (types)
      {
         types.add(type);
      }
   }

   /**
    * Forbids all notification types to be received by listeners
    */
   public void disableAllTypes()
   {
      synchronized (types)
      {
         types.clear();
      }
   }

   /**
    * Forbids the gven notification type to be received by listeners
    *
    * @param type The notification type to disable
    */
   public void disableType(String type)
   {
      synchronized (types)
      {
         types.remove(type);
      }
   }

   /**
    * Returns the notification type that are not filtered out by this filter
    */
   public Vector getEnabledTypes()
   {
      Vector v = new Vector();
      synchronized (types)
      {
         v.addAll(types);
      }
      return v;
   }

   /**
    * Filters out notifications whose type is not enabled in this filter.
    *
    * @param notification The notification to filter
    * @return True if the notification should be delivered to the listener, false otherwise
    */
   public boolean isNotificationEnabled(Notification notification)
   {
      String type = notification.getType();
      if (type != null)
      {
         for (Iterator i = getEnabledTypes().iterator(); i.hasNext();)
         {
            String t = (String)i.next();
            if (type.startsWith(t)) return true;
         }
      }
      return false;
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField fields = in.readFields();

      Vector vector = (Vector)fields.get(serialName, null);
      if (fields.defaulted(serialName))
      {
         throw new IOException("Serialized stream corrupted: expecting a non-null Vector");
      }

      if (types == null) types = new HashSet();
      types.clear();
      types.addAll(vector);
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      ObjectOutputStream.PutField fields = out.putFields();

      Vector vector = getEnabledTypes();
      fields.put(serialName, vector);
      out.writeFields();
   }
}
