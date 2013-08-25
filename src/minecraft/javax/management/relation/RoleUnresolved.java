/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.11 $
 */
public class RoleUnresolved implements Serializable
{
   private static final long serialVersionUID = -48350262537070138L;

   private String roleName;
   private List roleValue;
   private int problemType;

   public RoleUnresolved(String roleName, List roleValues, int problemType) throws IllegalArgumentException
   {
      setRoleName(roleName);
      setRoleValue(roleValues);
      setProblemType(problemType);
   }

   public int getProblemType()
   {
      return problemType;
   }

   public String getRoleName()
   {
      return roleName;
   }

   public List getRoleValue()
   {
      // During serialization I can get any type of List
      return roleValue == null ? null : new ArrayList(roleValue);
   }

   public void setRoleName(String name) throws IllegalArgumentException
   {
      if (name == null) throw new IllegalArgumentException("Role Name cannot be null");
      roleName = name;
   }

   public void setRoleValue(List list)
   {
      if (list != null)
      {
         if (roleValue == null)
         {
            roleValue = new ArrayList();
         }
         roleValue.clear();
         roleValue.addAll(list);
      }
      else
      {
         roleValue = null;
      }
   }

   public void setProblemType(int type) throws IllegalArgumentException
   {
      if (!(RoleStatus.isRoleStatus(type)))
      {
         throw new IllegalArgumentException("Problem Type unknown");
      }
      problemType = type;
   }

   public Object clone()
   {
      // NB we do not (and cannot) implement Cloneable so we do our own copy
      return new RoleUnresolved(roleName, roleValue, problemType);
   }

   public String toString()
   {
      StringBuffer roleBuff = new StringBuffer();
      roleBuff.append("Role Name: ").append(roleName);
      if (roleValue != null)
      {
         roleBuff.append("\nRole Values:");
         for (Iterator i = roleValue.iterator(); i.hasNext();)
         {
            ObjectName objectName = (ObjectName)i.next();
            roleBuff.append(objectName);
            if (i.hasNext())
            {
               roleBuff.append(", ");
            }
         }
      }
      roleBuff.append("\nProblem Type:");
      roleBuff.append(problemType);
      return roleBuff.toString();
   }
}