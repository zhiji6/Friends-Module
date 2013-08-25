/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.util.List;
import java.util.Map;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.4 $
 */
public interface Relation
{
   public List getRole(String roleName) throws IllegalArgumentException, RoleNotFoundException,
                                               RelationServiceNotRegisteredException;

   public RoleResult getRoles(String[] roleNames) throws IllegalArgumentException,
                                                         RelationServiceNotRegisteredException;

   public Integer getRoleCardinality(String roleName) throws IllegalArgumentException, RoleNotFoundException;

   public RoleResult getAllRoles() throws RelationServiceNotRegisteredException;

   public RoleList retrieveAllRoles();

   public void setRole(Role role) throws IllegalArgumentException, RoleNotFoundException,
                                         RelationTypeNotFoundException, InvalidRoleValueException,
                                         RelationServiceNotRegisteredException, RelationNotFoundException;

   public RoleResult setRoles(RoleList roleList) throws IllegalArgumentException,
                                                        RelationServiceNotRegisteredException, RelationTypeNotFoundException,
                                                        RelationNotFoundException;

   public void handleMBeanUnregistration(ObjectName objectName, String roleName) throws IllegalArgumentException,
                                                                                        RoleNotFoundException, InvalidRoleValueException,
                                                                                        RelationServiceNotRegisteredException, RelationTypeNotFoundException,
                                                                                        RelationNotFoundException;

   public Map getReferencedMBeans();

   public String getRelationTypeName();

   public ObjectName getRelationServiceName();

   public String getRelationId();
}