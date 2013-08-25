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
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import mx4j.util.Utils;

/**
 * Permission that control access to MBeanServer methods. <br>
 * The MBeanPermission contains a target name and a comma separated list of target actions.
 * The target name is composed by:
 * <ul>
 * <li> the class name of the MBean, as returned by
 * {@link javax.management.MBeanInfo#getClassName MBeanInfo.getClassName()} </li>
 * <li> the pound character '#' </li>
 * <li> the attribute name or the operation name </li>
 * <li> the object name of the MBean inclosed in squared brackets </li>
 * </ul>
 * When used in the target name, the wildcard '*' may be used to specify packages, classes or methods as a whole. <br>
 * When used in the actions, the wildcard '*' indicates all actions. <br>
 * An example of policy file is the following:
 * <pre>
 * grant codebase my-jmx-application.jar
 * {
 *    permission javax.management.MBeanPermission "mx4j.tools.naming.NamingService", "instantiate, registerMBean, unregisterMBean";
 *    permission javax.management.MBeanPermission "mx4j.tools.naming.NamingService#start", "invoke";
 *    permission javax.management.MBeanPermission "mx4j.tools.naming.NamingService#stop", "invoke";
 * }
 * </pre>
 *
 * @version $Revision: 1.11 $
 */
public class MBeanPermission extends Permission
{
   private static final long serialVersionUID = 0xde755825e2a117abL;

   private static final String WILDCARD = "*";
   private static final String NILCARD = "-";

   /**
    * @serial The permission actions
    */
   private String actions;

   private transient int hash;
   private transient String className;
   private transient String memberName;
   private transient ObjectName objectName;
   private transient ArrayList actionsList;

   /**
    * Creates a new MBeanPermission
    *
    * @param name    The target name
    * @param actions The comma separated list of actions
    */
   public MBeanPermission(String name, String actions)
   {
      super(name);
      this.actions = actions;

      parse(name, actions);
   }

   /**
    * Creates a new MBeanPermission. If the parts composing the target name are all specified as null,
    * the wildcard target name '*' is assumed.
    *
    * @param className  The className part of the target name, may be null
    * @param memberName The memberName part of the target name, may be null
    * @param objectName The ObjectName part of the target name, may be null
    * @param actions    The comma separated list of actions
    */
   public MBeanPermission(String className, String memberName, ObjectName objectName, String actions)
   {
      this(createTargetName(className, memberName, objectName), actions);
   }

   private static String createTargetName(String className, String memberName, ObjectName objectName)
   {
      StringBuffer target = new StringBuffer();

      target.append(className == null ? "-" : className);
      target.append('#');
      target.append(memberName == null ? "-" : memberName);
      target.append('[');
      target.append(objectName == null ? "-" : objectName.getCanonicalName());
      target.append(']');

      return target.toString();
   }

   private void parse(String name, String actions)
   {
      className = parseClassName(name);
      memberName = parseMemberName(name);
      objectName = parseObjectName(name);
      actionsList = parseActions(actions);
   }

   public int hashCode()
   {
      if (hash == 0) hash = computeHash();
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;
      if (getClass() != obj.getClass()) return false;

      // Must have the same target name, and the same action list, after parsing
      MBeanPermission other = (MBeanPermission)obj;

      // The parsed members can be null (means they're the nilcard)
      if (!areEqual(getClassName(), other.getClassName())) return false;
      if (!areEqual(getMemberName(), other.getMemberName())) return false;
      if (!areEqual(getObjectName(), other.getObjectName())) return false;
      return getActionsList().equals(other.getActionsList());
   }

   private boolean areEqual(Object obj1, Object obj2)
   {
      if (obj1 == null) return obj2 == null;
      return obj1.equals(obj2);
   }

   public String getActions()
   {
      return actions;
   }

   private String getClassName()
   {
      return className;
   }

   private String getMemberName()
   {
      return memberName;
   }

   private ObjectName getObjectName()
   {
      return objectName;
   }

   private ArrayList getActionsList()
   {
      return actionsList;
   }

   public boolean implies(Permission p)
   {
      if (p == null) return false;
      if (getClass() != p.getClass()) return false;

      MBeanPermission permission = (MBeanPermission)p;
      if (!impliesClassName(permission)) return false;
      if (!impliesMemberName(permission)) return false;
      if (!impliesObjectName(permission)) return false;
      if (!impliesActions(permission)) return false;
      return true;
   }

   private boolean impliesClassName(MBeanPermission p)
   {
      return impliesTarget(getClassName(), p.getClassName());
   }

   private boolean impliesMemberName(MBeanPermission p)
   {
      return impliesTarget(getMemberName(), p.getMemberName());
   }

   private boolean impliesTarget(String thisTarget, String otherTarget)
   {
      // Handle nilcards
      if (thisTarget == null) return otherTarget == null;
      if (otherTarget == null) return true;

      // Easy and fast check
      if (thisTarget.equals(otherTarget)) return true;

      // Now see the wildcard. While thisTarget can be wildcarded in several ways,
      // otherTarget is created only with the full wildcard, or with no wildcard
      boolean otherWildcard = otherTarget.indexOf(WILDCARD) >= 0;

      boolean thisWildcard = thisTarget.indexOf(WILDCARD) >= 0;

      if (thisWildcard)
      {
         if (otherWildcard)
         {
            // otherTarget can only be '*'
            return thisTarget.equals(WILDCARD);
         }
         else
         {
            // We support all types of wildcarding with '*'
            return Utils.wildcardMatch(thisTarget, otherTarget);
         }
      }
      else
      {
         if (otherWildcard)
         {
            return false;
         }
         else
         {
            return thisTarget.equals(otherTarget);
         }
      }
   }

   private boolean impliesObjectName(MBeanPermission p)
   {
      ObjectName name1 = getObjectName();
      ObjectName name2 = p.getObjectName();

      // Handle nilcards
      if (name1 == null) return name2 == null;
      if (name2 == null) return true;

      return name1.implies(name2);
   }

   private boolean impliesActions(MBeanPermission p)
   {
      ArrayList thisActions = getActionsList();
      boolean thisWild = thisActions.contains(WILDCARD);

      ArrayList otherActions = p.getActionsList();
      boolean otherWild = otherActions.contains(WILDCARD);

      // Access is granted for all actions
      if (thisWild) return true;
      // Access was not granted for all actions, but is requested for all actions
      if (otherWild) return false;

      if (thisActions.containsAll(otherActions)) return true;

      // Special check: queryMBeans implies queryNames
      if (otherActions.contains("queryNames") && thisActions.contains("queryMBeans"))
      {
         // Umpf, this is ugly, but not immediate: must care multithreading
         for (int i = 0; i < otherActions.size(); ++i)
         {
            Object perm = otherActions.get(i);
            if ("queryNames".equals(perm)) continue;
            if (!thisActions.contains(perm)) return false;
         }
         return true;
      }

      return false;
   }

   private String parseClassName(String name)
   {
      if (name == null) throw new IllegalArgumentException("Target name cannot be null");

      String target = name.trim();

      if (target.length() == 0) throw new IllegalArgumentException("Target name cannot be empty");

      // Try to find the ObjectName beginning
      int square = target.indexOf('[');
      if (square >= 0)
      {
         // There is an ObjectName, take only the classname and member
         target = target.substring(0, square).trim();
      }

      // There is only the ObjectName, means classname == wildcard
      if (target.length() == 0) return WILDCARD;

      // Try to find the member beginning
      int pound = target.indexOf('#');
      if (pound >= 0)
      {
         // There is a member, take only the classname
         target = target.substring(0, pound).trim();
      }

      // There is only the member, means classname == wildcard
      if (target.length() == 0) return WILDCARD;

      // Check for the nilcard
      if (target.equals(NILCARD)) return null;

      return target;
   }

   private String parseMemberName(String name)
   {
      // Checks already done in parseClassName

      String target = name.trim();

      // Try to find the ObjectName beginning
      int square = target.indexOf('[');
      if (square >= 0)
      {
         // There is an ObjectName, take only the classname and member
         target = target.substring(0, square).trim();
      }

      // There is only the ObjectName, means member == wildcard
      if (target.length() == 0) return WILDCARD;

      // Try to find the member beginning
      int pound = target.indexOf('#');
      if (pound >= 0)
      {
         // There is a member, take only it
         target = target.substring(pound + 1).trim();
      }
      else
      {
         // No member, means member == wildcard
         target = WILDCARD;
      }

      // Check for the nilcard
      if (target.equals(NILCARD)) return null;

      return target;
   }

   private ObjectName parseObjectName(String name)
   {
      // Checks already done in parseClassName

      String target = name.trim();
      String inside = "*:*";

      // Find beginning of ObjectName
      int open = target.indexOf('[');
      if (open >= 0)
      {
         int close = target.indexOf(']');
         if (close < 0) throw new IllegalArgumentException("Missing closing ObjectName bracket");

         // Find the ObjectName string inside the brackets
         inside = target.substring(open + 1, close).trim();
         if (inside.length() == 0)
         {
            inside = "*:*";
         }
         else if (inside.equals(NILCARD))
         {
            return null;
         }
      }

      // Create the ObjectName
      try
      {
         ObjectName objectName = new ObjectName(inside);
         return objectName;
      }
      catch (MalformedObjectNameException x)
      {
         throw new IllegalArgumentException("Invalid ObjectName: " + inside);
      }
   }

   private ArrayList parseActions(String actions)
   {
      if (actions == null) throw new IllegalArgumentException("Actions list cannot be null");

      actions = actions.trim();

      if (actions.length() == 0) throw new IllegalArgumentException("Actions list cannot be empty");

      // Split the comma separated list of actions
      ArrayList list = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer(actions, ",");
      while (tokenizer.hasMoreTokens())
      {
         String token = tokenizer.nextToken().trim();
         if (token.length() == 0) continue;
         if (token.equals(WILDCARD))
         {
            list.clear();
            list.add(WILDCARD);
            return list;
         }
         else
         {
            list.add(token);
         }
      }

      if (list.size() < 1) throw new IllegalArgumentException("No actions specified");

      // It is very important for hashCode() and equals() that the list is sorted
      Collections.sort(list);

      return list;
   }

   private int computeHash()
   {
      String cls = getClassName();
      int hash = cls == null ? NILCARD.hashCode() : cls.hashCode();
      String member = getMemberName();
      hash ^= member == null ? NILCARD.hashCode() : member.hashCode();
      ObjectName name = getObjectName();
      hash ^= name == null ? NILCARD.hashCode() : name.hashCode();
      hash ^= getActionsList().hashCode();
      return hash;
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      parse(getName(), getActions());
   }
}
