/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.relation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.8 $
 */
public class MBeanServerNotificationFilter extends NotificationFilterSupport
{
   private static final long serialVersionUID = 2605900539589789736L;
   private static final String[] serialNames = {"selectedNames", "deselectedNames"};
   private static final ObjectStreamField[] serialPersistentFields =
           {
              new ObjectStreamField(serialNames[0], List.class),
              new ObjectStreamField(serialNames[1], List.class)
           };

   private HashSet m_disabledObjectNames;
   private HashSet m_enabledObjectNames;

   public MBeanServerNotificationFilter()
   {
      enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
      enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);

      // By default all disabled
      disableAllObjectNames();
   }

   public int hashCode()
   {
      int result = (m_disabledObjectNames != null ? m_disabledObjectNames.hashCode() : 0);
      result = 29 * result + (m_enabledObjectNames != null ? m_enabledObjectNames.hashCode() : 0);
      return result;
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (!(obj instanceof MBeanServerNotificationFilter)) return false;

      MBeanServerNotificationFilter other = (MBeanServerNotificationFilter)obj;

      if (m_disabledObjectNames != null ? !m_disabledObjectNames.equals(other.m_disabledObjectNames) : other.m_disabledObjectNames != null) return false;
      if (m_enabledObjectNames != null ? !m_enabledObjectNames.equals(other.m_enabledObjectNames) : other.m_enabledObjectNames != null) return false;

      return true;
   }

   public void disableAllObjectNames()
   {
      // Clear the enabled ones, and...
      if (m_enabledObjectNames == null)
      {
         m_enabledObjectNames = new HashSet();
      }
      else
      {
         m_enabledObjectNames.clear();
      }
      // ...reset the disabled ones
      m_disabledObjectNames = null;
   }

   public void enableAllObjectNames()
   {
      // Clear the disabled ones, and...
      if (m_disabledObjectNames == null)
      {
         m_disabledObjectNames = new HashSet();
      }
      else
      {
         m_disabledObjectNames.clear();
      }
      // ...reset the enabled ones
      m_enabledObjectNames = null;
   }

   public void enableObjectName(ObjectName name) throws IllegalArgumentException
   {
      if (name == null)
      {
         throw new IllegalArgumentException("ObjectName cannot be null");
      }

      // Remove from disabled if present
      if (m_disabledObjectNames != null && m_disabledObjectNames.size() > 0)
      {
         m_disabledObjectNames.remove(name);
      }

      // If not enableAll, add it to the list
      if (m_enabledObjectNames != null)
      {
         m_enabledObjectNames.add(name);
      }
   }

   public void disableObjectName(ObjectName name) throws IllegalArgumentException
   {
      if (name == null)
      {
         throw new IllegalArgumentException("ObjectName cannot be null");
      }

      // Remove from enabled if present
      if (m_enabledObjectNames != null && m_enabledObjectNames.size() > 0)
      {
         m_enabledObjectNames.remove(name);
      }

      // If not disableAll, add it to the list
      if (m_disabledObjectNames != null)
      {
         m_disabledObjectNames.add(name);
      }
   }

   public Vector getEnabledObjectNames()
   {
      if (m_enabledObjectNames == null)
      {
         return null;
      }
      Vector v = new Vector();
      v.addAll(m_enabledObjectNames);
      return v;
   }

   public Vector getDisabledObjectNames()
   {
      if (m_disabledObjectNames == null)
      {
         return null;
      }
      Vector v = new Vector();
      v.addAll(m_disabledObjectNames);
      return v;
   }

   public boolean isNotificationEnabled(Notification notification) throws IllegalArgumentException
   {
      boolean goOn = super.isNotificationEnabled(notification);

      if (goOn)
      {
         if (notification instanceof MBeanServerNotification)
         {
            MBeanServerNotification n = (MBeanServerNotification)notification;
            ObjectName name = n.getMBeanName();

            if (m_enabledObjectNames == null)
            {
               // All enabled, check the disabled ones
               if (m_disabledObjectNames != null && m_disabledObjectNames.contains(name))
               {
                  // All enabled apart this one
                  return false;
               }
               else
               {
                  return true;
               }
            }
            else
            {
               // Only some is enabled
               if (m_enabledObjectNames.contains(name))
               {
                  // This one is enabled
                  return true;
               }
               else
               {
                  // This one is not enabled
                  return false;
               }
            }
         }
      }

      return false;
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField fields = in.readFields();

      Vector vector = (Vector)fields.get(serialNames[0], null);
      if (fields.defaulted(serialNames[0]))
      {
         throw new IOException("Serialized stream corrupted: expecting a non-null Vector");
      }
      if (vector != null)
      {
         if (m_enabledObjectNames == null)
         {
            m_enabledObjectNames = new HashSet();
         }
         m_enabledObjectNames.clear();
         m_enabledObjectNames.addAll(vector);
      }

      vector = (Vector)fields.get(serialNames[1], null);
      if (fields.defaulted(serialNames[1]))
      {
         throw new IOException("Serialized stream corrupted: expecting a non-null Vector");
      }
      if (vector != null)
      {
         if (m_disabledObjectNames == null)
         {
            m_disabledObjectNames = new HashSet();
         }
         m_disabledObjectNames.clear();
         m_disabledObjectNames.addAll(vector);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      ObjectOutputStream.PutField fields = out.putFields();

      Vector vector = getEnabledObjectNames();
      fields.put(serialNames[0], vector);

      vector = getDisabledObjectNames();
      fields.put(serialNames[1], vector);

      out.writeFields();
   }
}
