/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @version $Revision: 1.6 $
 */
public class RoleResult implements Serializable
{
   private static final long serialVersionUID = -6304063118040985512L;

   private RoleList roleList;
   private RoleUnresolvedList unresolvedRoleList;

   public RoleResult(RoleList roleList, RoleUnresolvedList unresolvedList)
   {
      setRoles(roleList);
      setRolesUnresolved(unresolvedList);
   }

   public RoleList getRoles()
   {
      return roleList == null ? null : (RoleList)roleList.clone();
   }

   public RoleUnresolvedList getRolesUnresolved()
   {
      return unresolvedRoleList == null ? null : (RoleUnresolvedList)unresolvedRoleList.clone();
   }

   public void setRoles(RoleList list)
   {
      if (list != null)
      {
         if (roleList == null)
         {
            roleList = new RoleList();
         }
         for (Iterator i = list.iterator(); i.hasNext();)
         {
            Role currentRole = (Role)i.next();
            roleList.add(currentRole.clone());
         }
      }
      else
      {
         roleList = null;
      }
   }

   public void setRolesUnresolved(RoleUnresolvedList list)
   {
      if (list != null)
      {
         if (unresolvedRoleList == null)
         {
            unresolvedRoleList = new RoleUnresolvedList();
         }
         for (Iterator i = list.iterator(); i.hasNext();)
         {
            RoleUnresolved currentUnresolvedRole = (RoleUnresolved)i.next();
            unresolvedRoleList.add(currentUnresolvedRole.clone());
         }
      }
      else
      {
         unresolvedRoleList = null;
      }
   }
}