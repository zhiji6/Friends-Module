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
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * The permission that guards access to MBeanServerFactory methods.
 * It has no actions, only target names can be provided.
 * The wildcard "*" means all names, and "createMBeanServer" implies "newMBeanServer", and the names
 * can be specified as a comma separated list.
 * The list of target names is the following:
 * <ul>
 * <li>newMBeanServer</li>
 * <li>createMBeanServer</li>
 * <li>findMBeanServer</li>
 * <li>releaseMBeanServer</li>
 * </ul>
 *
 * @version $Revision: 1.14 $
 */
public class MBeanServerPermission extends BasicPermission
{
   private static final long serialVersionUID = 0xb16c9a6bd5fae3d2L;

   private transient ArrayList targets;
   private transient boolean wildcard;

   /**
    * Creates a new MBeanServerPermission with the specified name and no actions
    *
    * @param name The comma separated list of target names
    */
   public MBeanServerPermission(String name)
   {
      this(name, null);
   }

   /**
    * Creates a new MBeanServerPermission with the specified name and actions, but the actions will be ignored
    *
    * @param name    The comma separated list of target names
    * @param actions Ignored
    */
   public MBeanServerPermission(String name, String actions)
   {
      super(name);
      parseName(name);
      if (actions != null && actions.length() != 0) throw new IllegalArgumentException("Actions must be null or an empty string");
   }

   /**
    * Returns null, as this permission does not have actions
    */
   public String getActions()
   {
      return null;
   }

   public int hashCode()
   {
      return targets.hashCode();
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;

      try
      {
         MBeanServerPermission other = (MBeanServerPermission)obj;
         // We don't check wildcard: if targets is empty, means we have a wildcard (see parseName)
         return targets.equals(other.targets);
      }
      catch (ClassCastException x)
      {
      }
      return false;
   }

   public boolean implies(Permission p)
   {
      if (p == null) return false;
      if (getClass() != p.getClass()) return false;

      MBeanServerPermission other = (MBeanServerPermission)p;
      if (wildcard) return true;
      if (other.wildcard) return false;

      if (targets.containsAll(other.targets)) return true;

      // We have to manage the case where this contains createMBeanServer and other contains newMBeanServer
      if (other.targets.contains("newMBeanServer") && targets.contains("createMBeanServer"))
      {
         // Beware the case where we have MBeanServerPermission "createMBeanServer" and
         // MBeanServerPermission "newMBeanServer, findMBeanServer": the first should not imply the second.
         for (int i = 0; i < other.targets.size(); ++i)
         {
            Object perm = other.targets.get(i);
            if ("newMBeanServer".equals(perm)) continue;
            if (!targets.contains(perm)) return false;
         }
         return true;
      }

      return false;
   }

   private void parseName(String name)
   {
      if (name == null) throw new IllegalArgumentException("Permission name cannot be null");
      name = name.trim();
      if (name.length() == 0) throw new IllegalArgumentException("Permission name cannot be empty");

      targets = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer(name, ",");
      while (tokenizer.hasMoreTokens())
      {
         String target = tokenizer.nextToken().trim();
         if (target.length() == 0) continue;
         if ("*".equals(target))
         {
            targets.clear();
            wildcard = true;
            return;
         }
         else if ("newMBeanServer".equals(target) || "createMBeanServer".equals(target) || "findMBeanServer".equals(target) || "releaseMBeanServer".equals(target))
         {
            targets.add(target);
         }
         else
         {
            throw new IllegalArgumentException("Invalid permission name: " + target);
         }
      }

      if (targets.size() < 1) throw new IllegalArgumentException("Permission name does not contain targets");

      // Important to provide same hashcode and equals to permission with names in different order
      Collections.sort(targets);
   }

   public PermissionCollection newPermissionCollection()
   {
      return null;
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      parseName(getName());
   }
}
