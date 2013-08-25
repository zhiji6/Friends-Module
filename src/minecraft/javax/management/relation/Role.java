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
public class Role implements Serializable
{
   private static final long serialVersionUID = -279985518429862552L;

   private String name;
   private List objectNameList;

   public Role(String roleName, List roleValueList) throws IllegalArgumentException
   {
      setRoleName(roleName);
      // loop and add the values to our global list
      setRoleValue(roleValueList);
   }

   public void setRoleName(String roleName) throws IllegalArgumentException
   {
      if (roleName == null) throw new IllegalArgumentException("Cannot have a null role name");
      this.name = roleName;
   }

   public void setRoleValue(List roleValues) throws IllegalArgumentException
   {
      if (roleValues == null) throw new IllegalArgumentException("List of role values cannot be null");
      if (objectNameList == null)
      {
         objectNameList = new ArrayList();
      }
      objectNameList.clear();
      objectNameList.addAll(roleValues);
   }

   public String getRoleName()
   {
      return name;
   }

   public List getRoleValue()
   {
      return new ArrayList(objectNameList);
   }

   public String toString()
   {
      StringBuffer roleToString = new StringBuffer("roleName: ");
      roleToString.append(name);
      roleToString.append("\nroleValue: ");
      String values = roleValueToString(objectNameList);
      roleToString.append(values);
      return roleToString.toString();
   }

   public Object clone()
   {
      try
      {
         return new Role(name, objectNameList);
      }
      catch (IllegalArgumentException ignored)
      {
         // it never happens, no need to rethrow !
         return null;
      }
   }

   public static String roleValueToString(List roleValues) throws IllegalArgumentException
   {
      StringBuffer valuesToString = new StringBuffer();
      for (Iterator roleValuesIterator = roleValues.iterator(); roleValuesIterator.hasNext();)
      {
         ObjectName currentObjName = (ObjectName)roleValuesIterator.next();
         valuesToString.append(currentObjName.toString());

         if (roleValuesIterator.hasNext())
         {
            valuesToString.append("\n");
         }
      }
      return valuesToString.toString();
   }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof Role)) return false;

      final Role role = (Role)o;

      if (name != null ? !name.equals(role.name) : role.name != null) return false;
      if (objectNameList != null ? !objectNameList.equals(role.objectNameList) : role.objectNameList != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (name != null ? name.hashCode() : 0);
      result = 29 * result + (objectNameList != null ? objectNameList.hashCode() : 0);
      return result;
   }
}
