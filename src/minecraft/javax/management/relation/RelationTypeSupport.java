/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.10 $
 */
public class RelationTypeSupport implements RelationType
{
   private static final long serialVersionUID = 4611072955724144607L;

   private String typeName;
   private Map roleName2InfoMap = new HashMap();
   private boolean isInRelationService;

   public RelationTypeSupport(String relationTypeName, RoleInfo[] roleInfo) throws IllegalArgumentException,
                                                                                   InvalidRelationTypeException
   {
      if (relationTypeName == null || roleInfo == null) throw new IllegalArgumentException("Illegal Null Value");
      this.typeName = relationTypeName;
      // determines if the roleInfo[] are valid
      checkRoleInfos(roleInfo);
      // no exceptions thrown in validate add the roleInfos to our HashMap
      addRoleInfos(roleInfo);
   }

   protected RelationTypeSupport(String relationTypeName)
   {
      if (relationTypeName == null) throw new IllegalArgumentException("Null RelationType Name");
      this.typeName = relationTypeName;
   }

   private void addRoleInfos(RoleInfo[] roleInfos) throws IllegalArgumentException
   {
      if (roleInfos == null) throw new IllegalArgumentException("Null RoleInfo[]");
      synchronized (roleName2InfoMap)
      {
         for (int i = 0; i < roleInfos.length; i++)
         {
            RoleInfo currentRoleInfo = roleInfos[i];
            addRoleNameToRoleInfo(currentRoleInfo.getName(), currentRoleInfo);
         }
      }
   }

   public String getRelationTypeName()
   {
      return typeName;
   }

   public RoleInfo getRoleInfo(String roleInfoName) throws RoleInfoNotFoundException, IllegalArgumentException
   {
      if (roleInfoName == null) throw new IllegalArgumentException("roleInfo Name cannot have a null value");
      RoleInfo roleInfo = (RoleInfo)(roleName2InfoMap.get(roleInfoName));
      if (roleInfo == null)
      {
         throw new RoleInfoNotFoundException("No role info for role named " + roleInfoName);
      }
      return roleInfo;
   }

   public List getRoleInfos()
   {
      return new ArrayList(roleName2InfoMap.values());
   }

   protected void addRoleInfo(RoleInfo roleInfo) throws IllegalArgumentException, InvalidRelationTypeException
   {
      if (roleInfo == null) throw new IllegalArgumentException("Cannot add a null roleInfo in the relation service");
      if (isInRelationService) throw new RuntimeOperationsException(null, "RoleInfo cannot be added as the relation type is already declared in the relation service");
      String roleName = roleInfo.getName();
      // roleName already present cannot have conflicting names
      if (roleName2InfoMap.containsKey(roleName))
      {
         throw new InvalidRelationTypeException("Already a roleInfo declared for roleName " + roleName);
      }

      // no problems add the roleName as the key in our Map and the roleInfo as the value
      addRoleNameToRoleInfo(roleName, roleInfo);
   }

   private void addRoleNameToRoleInfo(String roleName, RoleInfo roleInfo)
   {
      synchronized (roleName2InfoMap)
      {
         roleName2InfoMap.put(roleName, roleInfo);
      }
   }

   // validates the the RoleInfo[] conforms
   static void checkRoleInfos(RoleInfo[] roleInfo) throws IllegalArgumentException, InvalidRelationTypeException
   {
      if (roleInfo == null) throw new IllegalArgumentException("RoleInfo is null.");
      if (roleInfo.length == 0) throw new InvalidRelationTypeException("RoleInfo is empty");
      ArrayList roleNameList = new ArrayList();
      for (int i = 0; i < roleInfo.length; ++i)
      {
         RoleInfo currentRoleInfo = roleInfo[i];
         if (currentRoleInfo == null) throw new InvalidRelationTypeException("Null roleInfo");
         String roleName = currentRoleInfo.getName();
         if (roleNameList.contains(roleName))
         {
            throw new InvalidRelationTypeException("Two RoleInfos provided for role " + roleName);
         }
         roleNameList.add(roleName);
      }
   }

   void setRelationServiceFlag(boolean value)
   {
      isInRelationService = value;
   }
}