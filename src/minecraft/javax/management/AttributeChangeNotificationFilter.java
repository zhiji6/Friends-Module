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
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Vector;

/**
 * @version $Revision: 1.14 $
 */
// Change not needed, workaround to a TCK bug only to achieve TCK compliance
// public class AttributeChangeNotificationFilter implements NotificationFilter
public class AttributeChangeNotificationFilter implements NotificationFilter, Serializable
{
   private static final long serialVersionUID = -6347317584796410029L;
   private static final String serialName = "enabledAttributes";

   /**
    * @serialField enabledAttributes Vector The names of the attributes for which
    * this filter will enable notification dispatching
    */
   private static final ObjectStreamField[] serialPersistentFields =
           {
              new ObjectStreamField(serialName, Vector.class)
           };

   private HashSet enabledAttributes = new HashSet();

   public int hashCode()
   {
      return enabledAttributes.hashCode();
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;

      try
      {
         AttributeChangeNotificationFilter other = (AttributeChangeNotificationFilter)obj;
         return getEnabledAttributes().equals(other.getEnabledAttributes());
      }
      catch (ClassCastException x)
      {
      }
      return false;
   }

   public void enableAttribute(String name) throws IllegalArgumentException
   {
      if (name == null) throw new IllegalArgumentException("Name cannot be null");
      synchronized (enabledAttributes)
      {
         enabledAttributes.add(name);
      }
   }

   public void disableAttribute(String name)
   {
      if (name != null)
      {
         synchronized (enabledAttributes)
         {
            enabledAttributes.remove(name);
         }
      }
   }

   public void disableAllAttributes()
   {
      synchronized (enabledAttributes)
      {
         enabledAttributes.clear();
      }
   }

   public Vector getEnabledAttributes()
   {
      synchronized (enabledAttributes)
      {
         return new Vector(enabledAttributes);
      }
   }


   public boolean isNotificationEnabled(Notification notification)
   {
      if (!(notification instanceof AttributeChangeNotification)) return false;
      AttributeChangeNotification n = (AttributeChangeNotification)notification;

      if (!AttributeChangeNotification.ATTRIBUTE_CHANGE.equals(n.getType())) return false;
      String attributeName = n.getAttributeName();

      if (attributeName != null)
      {
         synchronized (enabledAttributes)
         {
            if (enabledAttributes.contains(attributeName)) return true;
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
         throw new StreamCorruptedException("Serialized stream corrupted: expecting a non-null Vector");
      }

      if (enabledAttributes == null) enabledAttributes = new HashSet();
      enabledAttributes.clear();
      enabledAttributes.addAll(vector);
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      ObjectOutputStream.PutField fields = out.putFields();

      Vector vector = getEnabledAttributes();
      fields.put(serialName, vector);
      out.writeFields();
   }
}
