/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.17 $
 */
public class RelationSupport implements RelationSupportMBean, MBeanRegistration
{
   private String m_relationId;
   private String m_relationTypeName;
   private ObjectName m_relationServiceObjectName;
   private MBeanServer m_server;
   private RelationServiceMBean m_proxy;
   private Boolean m_isInRelationService = Boolean.FALSE;
   private Map m_roleNameToRole = new HashMap();

   public RelationSupport(String relationId,
                          ObjectName relationServiceObjectName,
                          MBeanServer server,
                          String relationTypeName,
                          RoleList roleList) throws InvalidRoleValueException,
                                                    IllegalArgumentException
   {
      init(relationId, relationServiceObjectName, relationTypeName, roleList);
      // note the server may at this stage be null;
      m_server = server;
      m_proxy = (RelationServiceMBean)MBeanServerInvocationHandler.newProxyInstance(m_server,
                                                                                    m_relationServiceObjectName,
                                                                                    RelationServiceMBean.class,
                                                                                    false);
   }

   private void init(String relationId, ObjectName relationServiceObjectName, String relationTypeName, RoleList roleList)
           throws InvalidRoleValueException
   {
      if (relationId == null) throw new IllegalArgumentException("Illegal Null RelationId");
      if (relationServiceObjectName == null) throw new IllegalArgumentException("Illegal Null RelationService ObjectName");
      if (relationTypeName == null) throw new IllegalArgumentException("Illegal Null RelationTypeName");
      if (roleList == null) roleList = new RoleList();

      m_relationId = relationId;
      m_relationServiceObjectName = relationServiceObjectName;
      m_relationTypeName = relationTypeName;
      //checks if role are not already in Map if not add them
      initializeRoleList(roleList);
   }

   public RelationSupport(String relationId,
                          ObjectName relationServiceObjectName,
                          String relationTypeName,
                          RoleList roleList) throws InvalidRoleValueException, IllegalArgumentException
   {
      init(relationId, relationServiceObjectName, relationTypeName, roleList);
   }

   public List getRole(String roleName) throws IllegalArgumentException, RoleNotFoundException,
                                               RelationServiceNotRegisteredException
   {
      Logger logger = getLogger();
      if (roleName == null) throw new IllegalArgumentException("Role name cannot be null");
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("getting roles whith RoleName: " + roleName + " from RelationSupport");
      Role role = getRoleFromRoleName(roleName);
      // check role reading
      int problemType = getReadingProblemType(role, roleName, m_relationTypeName);
      // no clone read only role found can return the list
      if (problemType == 0)
         return (role.getRoleValue());
      else
      {
         if (problemType == RoleStatus.NO_ROLE_WITH_NAME)
         {
            logger.warn("RoleName: " + roleName + " not found");
            throw new RoleNotFoundException("RoleName: " + roleName + " does not exist in the relation");
         }
         else if (problemType == RoleStatus.ROLE_NOT_READABLE)
         {
            logger.warn("Role with roleName: " + roleName + " cannot be read.");
            throw new RoleNotFoundException("RoleName: " + roleName + " is not readable");
         }
      }
      // need to return something to satisfy compiler, if we get here !!
      return null;
   }


   int getReadingProblemType(Role role,
                             String roleName,
                             String relationTypeName) throws RelationServiceNotRegisteredException,
                                                             IllegalArgumentException
   {
      if (roleName == null) throw new IllegalArgumentException("Null Role Name.");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("Checking the Role reading...");
      if (role == null) return (RoleStatus.NO_ROLE_WITH_NAME);
      try
      {
         return (m_proxy.checkRoleReading(roleName, relationTypeName)).intValue();
      }
      catch (RelationTypeNotFoundException ex)
      {
         logger.warn("Unable to find the Relation Type with name " + relationTypeName);
         throw new RuntimeOperationsException(null, "Relation Type with name: " + relationTypeName + " was not found");
      }
   }

   public RoleResult getRoles(String[] roleNames) throws IllegalArgumentException,
                                                         RelationServiceNotRegisteredException
   {
      if (roleNames == null) throw new IllegalArgumentException("Null RoleName Array.");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("Getting roles");
      RoleList roleList = new RoleList();
      RoleUnresolvedList unresolvedList = new RoleUnresolvedList();
      for (int i = 0; i < roleNames.length; i++)
      {
         String currentRoleName = roleNames[i];
         Role role = getRoleFromRoleName(currentRoleName);
         // check role reading for each role
         int problemType = getReadingProblemType(role, currentRoleName, m_relationTypeName);
         // no problems - the role can read add it to roleList
         if (problemType == 0)
            roleList.add((Role)(role.clone()));
         // we have a problem add the role to roleUnresolvedList with the problemType
         else
            unresolvedList.add((new RoleUnresolved(currentRoleName, null, problemType)));
      }
      return (new RoleResult(roleList, unresolvedList));
   }

   // can do here is get the list create a String[] of roleNames pass this to getRole(String[] roleNames)
   public RoleResult getAllRoles() throws RelationServiceNotRegisteredException
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("getting all roles");
      List roleNameList = getAllRoleNamesList();
      String[] roleNames = new String[roleNameList.size()];
      int index = 0;
      for (Iterator i = roleNameList.iterator(); i.hasNext();)
      {
         roleNames[index] = (String)i.next();
         index++;
      }
      return (getRoles(roleNames));
   }

   public RoleList retrieveAllRoles()
   {
      synchronized (m_roleNameToRole)
      {
         return (new RoleList(new ArrayList(m_roleNameToRole.values())));
      }
   }

   private ArrayList getAllRolesList()
   {
      synchronized (m_roleNameToRole)
      {
         return (new ArrayList(m_roleNameToRole.values()));
      }
   }

   public void setRole(Role role) throws IllegalArgumentException, RoleNotFoundException,
                                         RelationTypeNotFoundException, InvalidRoleValueException,
                                         RelationServiceNotRegisteredException, RelationNotFoundException
   {
      if (role == null) throw new IllegalArgumentException("RelationSupport setRole has recieved a null Role.");

      String roleName = role.getRoleName();
      Role oldRole = getRoleFromRoleName(roleName);
      List oldRoleValue;
      Boolean toBeInitialized;
      // no role found therefore we have a new role and a new List to hold it
      if (oldRole == null)
      {
         toBeInitialized = new Boolean(true);
         oldRoleValue = new ArrayList();
      }
      else
      {
         // role found we do not need to initialize the role
         toBeInitialized = new Boolean(false);
         oldRoleValue = oldRole.getRoleValue();
      }
      // check if the role is writable
      int problemType = getRoleWritingValue(role, m_relationTypeName, toBeInitialized);
      if (problemType == 0)
      {
         if (!(toBeInitialized.booleanValue()))
         {
            // new role send an update notification
            sendUpdateRoleNotification(m_relationId, role, oldRoleValue);
            updateRelationServiceMap(m_relationId, role, oldRoleValue);
         }
         addRolesToRoleMap(roleName, role);
      }
      else
      {
         RelationService.throwRoleProblemException(problemType, roleName);
      }
   }

   // checks if the role is writable
   int getRoleWritingValue(Role role,
                           String relationTypeName,
                           Boolean toBeInitialized) throws RelationTypeNotFoundException
   {
      if (m_proxy == null) throw new IllegalArgumentException("Please check the RelationService is running");
      return (m_proxy.checkRoleWriting(role, relationTypeName, toBeInitialized).intValue());
   }

   public RoleResult setRoles(RoleList roleList) throws IllegalArgumentException,
                                                        RelationServiceNotRegisteredException, RelationTypeNotFoundException,
                                                        RelationNotFoundException
   {
      Logger logger = getLogger();
      if (roleList == null) throw new IllegalArgumentException("RoleList cannot be null");
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("setting roles");
      RoleList newRoleList = new RoleList();
      RoleUnresolvedList roleUnresolvedList = new RoleUnresolvedList();
      List oldRoleValue;
      Boolean needsInitializing;
      for (Iterator i = roleList.iterator(); i.hasNext();)
      {
         Role currentRole = (Role)i.next();
         String roleName = currentRole.getRoleName();
         Role oldRole = getRoleFromRoleName(roleName);
         if (oldRole == null)
         {
            needsInitializing = new Boolean(true);
            oldRoleValue = new ArrayList();
         }
         else
         {
            needsInitializing = new Boolean(false);
            oldRoleValue = oldRole.getRoleValue();
         }
         int problemType = getRoleWritingValue(currentRole, m_relationTypeName, needsInitializing);
         if (problemType == 0)
         {
            if (!(needsInitializing.booleanValue()))
            {
               // send notification update RelationService map
               sendUpdateRoleNotification(m_relationId, currentRole, oldRoleValue);
               updateRelationServiceMap(m_relationId, currentRole, oldRoleValue);
            }
            // add the roles
            addRolesToRoleMap(roleName, currentRole);
            newRoleList.add(currentRole);
         }
         else
         {
            if (logger.isEnabledFor(Logger.WARN)) logger.warn("We have some unresolved roles adding them to RoleUnresolvedList");
            roleUnresolvedList.add(new RoleUnresolved(roleName, currentRole.getRoleValue(), problemType));
         }
      }
      return (new RoleResult(newRoleList, roleUnresolvedList));
   }

   // check role conforms to defined cardinality
   public Integer getRoleCardinality(String roleName) throws IllegalArgumentException, RoleNotFoundException
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("checking role cardinality with role named: " + roleName);
      if (roleName == null) throw new IllegalArgumentException("Role name should not be null.");
      Role role = getRoleFromRoleName(roleName);
      if (role == null)
      {
         int problemType = RoleStatus.NO_ROLE_WITH_NAME;
         try
         {
            // send the role cardinality error to the RelationService
            RelationService.throwRoleProblemException(problemType, roleName);
         }
         catch (InvalidRoleValueException ex)
         {
            new RuntimeOperationsException(null, "Invalid role value");
         }
      }
      List roleValue = role.getRoleValue();
      return new Integer(roleValue.size());
   }

   public void handleMBeanUnregistration(ObjectName objectName,
                                         String roleName) throws IllegalArgumentException,
                                                                 RoleNotFoundException, InvalidRoleValueException,
                                                                 RelationServiceNotRegisteredException, RelationTypeNotFoundException,
                                                                 RelationNotFoundException
   {
      Logger logger = getLogger();
      if (objectName == null) throw new IllegalArgumentException("ObjectName is null");
      if (roleName == null) throw new IllegalArgumentException("Null roleName");
      if (logger.isEnabledFor(Logger.WARN))
      {
         logger.warn("MBean with ObjectName: " +
                     objectName.getCanonicalName() +
                     " has been unregistered from the" +
                     " MBeanServer. Setting new Role values");
      }
      Role newRole = createNewRole(roleName, objectName);
      setRole(newRole);
   }

   private Role createNewRole(String roleName,
                              ObjectName objectName) throws RoleNotFoundException
   {
      // get role stored in the Map
      Role role = getRoleFromRoleName(roleName);
      if (role == null) throw new RoleNotFoundException("No role found for role name: " + roleName);

      // needs to be cloned as will be modified, need to cast role.getRoleValue as list does not implement Cloneable
      ArrayList newRoleValue = (ArrayList)((ArrayList)role.getRoleValue()).clone();
      newRoleValue.remove(objectName);
      Role newRole = new Role(roleName, newRoleValue);
      return newRole;
   }

   public Map getReferencedMBeans()
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("getting mbeanReferenced in RelationService");
      HashMap referencedMBeansMap = new HashMap();
      for (Iterator i = (getAllRolesList()).iterator(); i.hasNext();)
      {
         Role currentRole = (Role)i.next();
         String currentRoleName = currentRole.getRoleName();
         // get the roleValues for each Role
         List mbeanList = currentRole.getRoleValue();
         for (Iterator iter = mbeanList.iterator(); iter.hasNext();)
         {
            ObjectName currentObjectName = (ObjectName)iter.next();
            List mbeanRoleNameList = (List)(referencedMBeansMap.get(currentObjectName));
            boolean newReference = false;
            if (mbeanRoleNameList == null)
            {
               // we have new references
               newReference = true;
               mbeanRoleNameList = new ArrayList();
            }
            mbeanRoleNameList.add(currentRoleName);
            // we have a new reference add it to our map.
            if (newReference) referencedMBeansMap.put(currentObjectName, mbeanRoleNameList);
         }
      }
      return referencedMBeansMap;
   }

   private Role getRoleFromRoleName(String roleName)
   {
      synchronized (m_roleNameToRole)
      {
         return ((Role)m_roleNameToRole.get(roleName));
      }
   }

   public String getRelationTypeName()
   {
      return m_relationTypeName;
   }

   public ObjectName getRelationServiceName()
   {
      return m_relationServiceObjectName;
   }

   public String getRelationId()
   {
      return m_relationId;
   }

   public Boolean isInRelationService()
   {
      return m_isInRelationService;
   }

   public void setRelationServiceManagementFlag(Boolean isHandledByRelationService) throws IllegalArgumentException
   {
      if (isHandledByRelationService == null) throw new IllegalArgumentException("Null flag");
      m_isInRelationService = isHandledByRelationService;
   }

   public ObjectName preRegister(MBeanServer server,
                                 ObjectName name) throws Exception
   {
      if (server == null) throw new IllegalArgumentException("MBean Server is null cannot pre-register.");
      if (name == null) throw new IllegalArgumentException("Cannot register a null ObjectName");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.WARN)) logger.warn("pre Registering the RelationSupport");
      m_server = server;
      m_proxy = (RelationServiceMBean)MBeanServerInvocationHandler.newProxyInstance(m_server,
                                                                                    m_relationServiceObjectName,
                                                                                    RelationServiceMBean.class,
                                                                                    false);
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
      Logger logger = getLogger();
      boolean done = registrationDone.booleanValue();
      if (!done)
      {
         m_server = null;
         logger.warn("RelationSupport was NOT registered");
      }
      else
      {
         if (logger.isEnabledFor(Logger.TRACE))
         {
            logger.trace("RelationSupport postRegistered");
         }
      }
   }

   public void preDeregister() throws Exception
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE))
      {
         logger.debug("RelationSupport preDeregistered");
      }
   }

   public void postDeregister()
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE))
      {
         logger.debug("RelationSupport postDeregistered");
      }
   }

   private List getAllRoleNamesList()
   {
      synchronized (m_roleNameToRole)
      {
         return (new ArrayList(m_roleNameToRole.keySet()));
      }
   }

   private void initializeRoleList(RoleList roleList) throws InvalidRoleValueException
   {
      if (roleList == null) return;
      for (Iterator i = roleList.iterator(); i.hasNext();)
      {
         Role currentRole = (Role)i.next();
         String currentRoleName = currentRole.getRoleName();
         // our map already holds the current roleName cannot re-use it let the user know
         if (m_roleNameToRole.containsKey(currentRoleName))
         {
            throw new InvalidRoleValueException("RoleName already in use.");
         }
         // no existing roleName found add the values in the RoleList
         addRolesToRoleMap(currentRoleName, currentRole);
      }
   }

   private void addRolesToRoleMap(String roleName,
                                  Role role)
   {
      synchronized (m_roleNameToRole)
      {
         m_roleNameToRole.put(roleName, role.clone());
      }
   }

   void updateRelationServiceMap(String relationId,
                                 Role role,
                                 List oldRoleValue) throws IllegalArgumentException,
                                                           RelationServiceNotRegisteredException, RelationNotFoundException
   {
      Logger logger = getLogger();
      if (m_proxy != null)
         m_proxy.updateRoleMap(relationId, role, oldRoleValue);
      else
      {
         logger.warn("The RelationService cannot be registered.");
         throw new RelationServiceNotRegisteredException("Please check the RelationService is registered in the server");
      }
   }

   void sendUpdateRoleNotification(String relationId,
                                   Role role,
                                   List oldRoleValue) throws RelationServiceNotRegisteredException,
                                                             RelationNotFoundException
   {
      Logger logger = getLogger();
      if (relationId == null) throw new IllegalArgumentException("Null RelationId passed into sendUpdateRoleNotification");
      if (role == null) throw new IllegalArgumentException("Null role passed into sendUpdateRoleNotification");
      if (oldRoleValue == null) throw new IllegalArgumentException("Null list of role Values passed into sendUpdateRoleNotification");
      if (m_proxy != null)
         m_proxy.sendRoleUpdateNotification(relationId, role, oldRoleValue);
      else
      {
         logger.warn("cannot send an update notification as RelationService may not be registered, please check.");
         throw new RelationServiceNotRegisteredException("Please check the relation service has been registered in the MBeanServer");
      }
   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }
}