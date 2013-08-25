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
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.6 $
 */
public interface RelationServiceMBean
{
   public void isActive() throws RelationServiceNotRegisteredException;

   public boolean getPurgeFlag();

   public void setPurgeFlag(boolean purgeFlag);

   public void createRelationType(String relationTypeName, RoleInfo[] roleInfos) throws IllegalArgumentException,
                                                                                        InvalidRelationTypeException;

   public void addRelationType(RelationType relationTypeObject) throws IllegalArgumentException,
                                                                       InvalidRelationTypeException;

   public List getAllRelationTypeNames();

   public List getRoleInfos(String relationTypeName) throws IllegalArgumentException,
                                                            RelationTypeNotFoundException;

   public RoleInfo getRoleInfo(String relationTypeName, String roleInfoName) throws IllegalArgumentException,
                                                                                    RelationTypeNotFoundException, RoleInfoNotFoundException;

   public void removeRelationType(String relationTypeName) throws IllegalArgumentException,
                                                                  RelationServiceNotRegisteredException, RelationTypeNotFoundException;

   public void createRelation(String relationId, String relationTypeName, RoleList roleList) throws IllegalArgumentException,
                                                                                                    RelationServiceNotRegisteredException, RoleNotFoundException,
                                                                                                    InvalidRelationIdException, RelationTypeNotFoundException, InvalidRoleValueException;

   public void addRelation(ObjectName relationObjectName) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                                 NoSuchMethodException, InvalidRelationIdException, InstanceNotFoundException,
                                                                 InvalidRelationServiceException, RelationTypeNotFoundException,
                                                                 RoleNotFoundException, InvalidRoleValueException;

   public ObjectName isRelationMBean(String relationId) throws IllegalArgumentException, RelationNotFoundException;

   public String isRelation(ObjectName objectName) throws IllegalArgumentException;

   public Boolean hasRelation(String relationId) throws IllegalArgumentException;

   public List getAllRelationIds();

   public Integer checkRoleReading(String roleName, String relationTypeName) throws IllegalArgumentException,
                                                                                    RelationTypeNotFoundException;

   public Integer checkRoleWriting(Role role, String relationTypeName, Boolean initializeRoleFlag) throws
                                                                                                   IllegalArgumentException, RelationTypeNotFoundException;

   public void sendRelationCreationNotification(String relationId) throws IllegalArgumentException, RelationNotFoundException;

   public void sendRoleUpdateNotification(String relationId, Role newRole, List oldRoleValues) throws IllegalArgumentException,
                                                                                                      RelationNotFoundException;

   public void sendRelationRemovalNotification(String relationId, List unregisteredMBeanList) throws IllegalArgumentException,
                                                                                                     RelationNotFoundException;

   public void updateRoleMap(String relationId, Role newRole, List oldRoleValues) throws IllegalArgumentException,
                                                                                         RelationServiceNotRegisteredException, RelationNotFoundException;

   public void removeRelation(String relationId) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                        RelationNotFoundException;

   public void purgeRelations() throws RelationServiceNotRegisteredException;

   public Map findReferencingRelations(ObjectName mbeanObjectName, String relationTypeName, String roleName) throws
                                                                                                             IllegalArgumentException;

   public Map findAssociatedMBeans(ObjectName mbeanObjectName, String relationTypeName, String roleName) throws
                                                                                                         IllegalArgumentException;

   public List findRelationsOfType(String relationTypeName) throws IllegalArgumentException, RelationTypeNotFoundException;

   public List getRole(String relationId, String roleName) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                                  RelationNotFoundException, RoleNotFoundException;

   public RoleResult getRoles(String relationId, String[] roleNames) throws IllegalArgumentException, RelationNotFoundException,
                                                                            RelationServiceNotRegisteredException;

   public RoleResult getAllRoles(String relationId) throws IllegalArgumentException, RelationNotFoundException,
                                                           RelationServiceNotRegisteredException;

   public Integer getRoleCardinality(String relationId, String roleName) throws IllegalArgumentException,
                                                                                RelationNotFoundException, RoleNotFoundException;

   public void setRole(String relationId, Role role) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                            RelationNotFoundException, RoleNotFoundException, InvalidRoleValueException, RelationTypeNotFoundException;

   public RoleResult setRoles(String relationId, RoleList roleList) throws RelationServiceNotRegisteredException,
                                                                           IllegalArgumentException, RelationNotFoundException;

   public Map getReferencedMBeans(String relationId) throws IllegalArgumentException, RelationNotFoundException;

   public String getRelationTypeName(String relationId) throws IllegalArgumentException, RelationNotFoundException;
}