/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.relation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.35 $
 */

/**
 * An MBean that maintains the consistency of all relation types and all relation instances within a JMX agent.
 * It provides query operations to fins related and associated mbeans and their roles in the relation.
 */
public class RelationService extends NotificationBroadcasterSupport implements RelationServiceMBean,
                                                                               MBeanRegistration, NotificationListener
{
   private boolean m_purgeFlag;
   private Long m_notificationCounter = new Long(0);

   private MBeanServer m_server = null;
   private RelationSupportMBean m_proxy = null;
   private ObjectName m_relationServiceObjectName = null;

   private MBeanServerNotificationFilter m_notificationFilter = null;

   private Map m_relationIdToRelationObject = new HashMap();
   private Map m_relationIdToRelationTypeName = new HashMap();
   private Map m_relationMBeanObjectNameToRelationId = new HashMap();
   private Map m_relationTypeNameToRelationTypeObject = new HashMap();
   private Map m_relationTypeNameToRelationIds = new HashMap();
   private Map m_referencedMBeanObjectNameToRelationIds = new HashMap();

   private List m_deregisteredNotificationList = new ArrayList();

   /**
    * constructor
    *
    * @param purgeFlag - this is a flag, if true indicates an immediate update of relations is to be done when a
    *                  notification is recieved for the unregistration of an MBean referenced in a relation
    *                  - if false update of relations must be performed explicitly by calling purgeRelations()
    * @see #purgeRelations
    */
   public RelationService(boolean purgeFlag)
   {
      m_purgeFlag = purgeFlag;
   }

   /**
    * @throws RelationServiceNotRegisteredException
    *          - thrown if the RelationService is not registered in the MBeanServer
    *          <p>Currently this class must be registered in the MBeanServer before any relations can be created or added</p>
    */
   public void isActive() throws RelationServiceNotRegisteredException
   {
      Logger logger = getLogger();
      if (m_server == null)
      {
         logger.error("RelationService has not been registered in the MBeanServer");
         throw new RelationServiceNotRegisteredException("Relation Service is not registered");
      }
   }

   /**
    * @return true - if the purgeFlag has been set, false if updates of a relation must be called explicitly
    * @see #purgeRelations
    */
   public boolean getPurgeFlag()
   {
      return m_purgeFlag;
   }

   /**
    * @param purgeFlag - a flag that when set to true indicates to the <code>RelationService</code> that it must update all relations
    *                  when it recieves a unregistration notification
    *                  if false this will not occur and purgeRelations must be called explicitly
    */
   public void setPurgeFlag(boolean purgeFlag)
   {
      m_purgeFlag = purgeFlag;
   }

   /**
    * @param relationTypeName - a string giving relations a type name this must be a unique name
    * @param roleInfos        - an array of RoleInfo objects.
    *                         Which are used to define the roles a relation plays a part in. It defines attributes
    *                         such as cardinality, role reading and writing...
    *                         The RelationService will then use these RoleInfo to maintain the relation
    * @throws IllegalArgumentException     - thrown if any of the parameters are null
    * @throws InvalidRelationTypeException - thrown if the role name, contained in the RoleInfo, already exists.
    *                                      <p/>
    *                                      <p>This method creates a relationType (a RelationTypeSupport Object) from the parameters passed in.</p>
    *                                      <p>The RelationTypeSupport represents an internal relation</p>
    */
   public void createRelationType(String relationTypeName, RoleInfo[] roleInfos) throws IllegalArgumentException,
                                                                                        InvalidRelationTypeException
   {
      if (relationTypeName == null) throw new IllegalArgumentException("Illegal Null Relation Type Name value");
      if (roleInfos == null) throw new IllegalArgumentException("Illegal Null RoleInfo");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Creating Relation Type with relationTypeName: " + relationTypeName);

      RelationTypeSupport relationType = new RelationTypeSupport(relationTypeName, roleInfos);
      // created a new RelationType add it to our map
      addRelationTypeToMap(relationTypeName, relationType);
   }

   /* Adds a relationTypeName as the key to a Map and the RelationType as the value */
   private void addRelationTypeToMap(String relationTypeName, RelationType relationType) throws InvalidRelationTypeException
   {
      Logger logger = getLogger();
      // synchronize all activities to map.
      synchronized (m_relationTypeNameToRelationTypeObject)
      {
         if ((m_relationTypeNameToRelationTypeObject.get(relationTypeName)) != null)
         {
            logger.warn("Cannot addRelationType as a relationType of the same name: " + relationTypeName + " already exists in the RelationService");
            throw new InvalidRelationTypeException("RelationType with name: " + relationTypeName + " already exists in the RelationService");
         }
         // set the RelationTypeSupport internal flag to true indicating that the relationType has been declared in the relation service
         if (relationType instanceof RelationTypeSupport)
         {
            ((RelationTypeSupport)relationType).setRelationServiceFlag(true);
         }
         // store the instance in the map
         m_relationTypeNameToRelationTypeObject.put(relationTypeName, relationType);
      }
   }

   /* returns the RelationType stored in the Map given the relationTypeName */
   private RelationType getRelationType(String relationTypeName)
           throws IllegalArgumentException, RelationTypeNotFoundException
   {
      RelationType relationType;
      synchronized (m_relationTypeNameToRelationTypeObject)
      {
         relationType = ((RelationType)(m_relationTypeNameToRelationTypeObject.get(relationTypeName)));
         // exception is caught by calling classes testing for RelationTypeNotFound.
         if (relationType == null)
         {
            throw new RelationTypeNotFoundException("No RelationType found for relationTypeName: " + relationTypeName);
         }
         return relationType;
      }
   }

   /**
    * @param relationType - an Object implementing the RelationType interface a utility implementation is provided by the
    *                     <code>RelationTypeSupport</code> class
    * @throws IllegalArgumentException     if a null RelationType is passed in as a parameter or if that RelationType has no RoleInfo defined
    * @throws InvalidRelationTypeException if the RoleInfo obtained from the RelationType is
    *                                      - empty
    *                                      - null
    *                                      - the RoleName is already in use
    *                                      <p>This method makes an externally defined relation type available through the relationService</p>
    *                                      <p>The RelationType is immutable, hence the returned values should never change while the relationType is registered
    *                                      with the realtion service</p>
    */
   public void addRelationType(RelationType relationType) throws IllegalArgumentException,
                                                                 InvalidRelationTypeException
   {
      if (relationType == null) throw new IllegalArgumentException("Relation Type should not be null.");

      // check type name
      String relationTypeName = relationType.getRelationTypeName();
      if (relationTypeName == null) throw new IllegalArgumentException("RelationTypeName must not be null");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Adding a RelationType");

      List roleInfoList = relationType.getRoleInfos();
      if (roleInfoList == null)
      {
         logger.warn("Cannot add RelationType: " + relationType.getClass().getName() + " RoleInfo information was not provided with the RelationType.");
         throw new InvalidRelationTypeException("No RoleInfo provided with Relation Type");
      }
      // build the roleInfo[] to validate the RoleInfo
      RoleInfo[] roleInfos = new RoleInfo[roleInfoList.size()];
      int index = 0;
      for (Iterator i = roleInfoList.iterator(); i.hasNext();)
      {
         RoleInfo currentRoleInfo = (RoleInfo)i.next();
         roleInfos[index] = currentRoleInfo;
         index++;
      }
      // need to validateRoleInfos before adding
      RelationTypeSupport.checkRoleInfos(roleInfos);

      // validated add the RelationType
      addRelationTypeToMap(relationTypeName, relationType);
   }

   /**
    * @return a list containing all the relationTypeNames registered with the relation service
    */
   public List getAllRelationTypeNames()
   {
      List result;
      synchronized (m_relationTypeNameToRelationTypeObject)
      {
         result = new ArrayList(m_relationTypeNameToRelationTypeObject.keySet());
      }
      // return all relationTypeNames or an empty list if none
      return result;
   }

   /**
    * @param relationTypeName - the string name representation of this RelationType
    * @return - List containing the RoleInfos for this RelationType Object
    * @throws IllegalArgumentException      - if the relationTypeName is null
    * @throws RelationTypeNotFoundException - if the Relationtype for the given relationTypeName is not found
    */
   public List getRoleInfos(String relationTypeName) throws IllegalArgumentException,
                                                            RelationTypeNotFoundException
   {
      if (relationTypeName == null) throw new IllegalArgumentException("Illegal relationType name is null.");
      RelationType relationType = getRelationType(relationTypeName);
      // returns a List of RoleInfo objects
      return relationType.getRoleInfos();
   }

   /**
    * @param relationTypeName - string name representing the RelationType
    * @param roleInfoName     - string name representing the RoleInfo object
    * @return - the corresponding RoleInfo Object for the given parameters
    * @throws IllegalArgumentException      - if either the relationtypeName or the roleInfoName is null
    * @throws RelationTypeNotFoundException - if the RelationType is not in the realtion service
    * @throws RoleInfoNotFoundException     - if the RoleInfo has not been found
    */
   public RoleInfo getRoleInfo(String relationTypeName, String roleInfoName) throws IllegalArgumentException,
                                                                                    RelationTypeNotFoundException, RoleInfoNotFoundException
   {
      if (relationTypeName == null) throw new IllegalArgumentException("Null relation type name");
      if (roleInfoName == null) throw new IllegalArgumentException("Null RoleInfo name");
      // gets the RelationType then gets the RoleInfo object
      return (getRelationType(relationTypeName).getRoleInfo(roleInfoName));
   }

   /**
    * @param relationTypeName - a string name representing the Relationtype Object
    * @throws IllegalArgumentException      - if the relationTypeName is null
    * @throws RelationServiceNotRegisteredException
    *                                       - if the RelationService has not been registered in the MBeanServer
    * @throws RelationTypeNotFoundException - if the RelationType has not been found
    *                                       <p/>
    *                                       <p>This method removes a RelationType, it's name(represented by the relationTypeName) and any relationIds associated with it,
    *                                       and all MBeans referenced in it's roles</p>
    *                                       <p>Note: this will not remove any mbeans registered with the MBeanServer this must be done if required via the MBeanServer.
    *                                       Any Mbeans registered with the MBean server will continue to be accessed via the MBeanServer, they will no longer be able to be
    *                                       referenced, queried via the relation service though.</p>
    */
   public void removeRelationType(String relationTypeName) throws IllegalArgumentException,
                                                                  RelationServiceNotRegisteredException, RelationTypeNotFoundException
   {
      Logger logger = getLogger();
      isActive();
      if (relationTypeName == null) throw new IllegalArgumentException("Illegal: relationType name cannot be null.");
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Removing RelationType with relationTypeName: " + relationTypeName);

      // will throw RelationTypeNotFoundException if not found
      getRelationType(relationTypeName);

      // no need to clone as relationIdList is internal and get its values from a private method.
      List relationIdList = getRelationIds(relationTypeName);

      removeRelationTypeObject(relationTypeName);
      removeRelationTypeName(relationTypeName);

      if (relationIdList != null)
      {
         for (Iterator i = relationIdList.iterator(); i.hasNext();)
         {
            String currentRelationId = (String)i.next();
            try
            {
               // removed the relationType now remove the relation
               removeRelation(currentRelationId);
            }
            catch (RelationNotFoundException ex)
            {
               throw new RuntimeOperationsException(null, ex.toString());
            }
         }
      }
   }

   private void removeRelationTypeObject(String relationTypeName)
   {
      synchronized (m_relationTypeNameToRelationTypeObject)
      {
         m_relationTypeNameToRelationTypeObject.remove(relationTypeName);
      }
   }

   private List getRelationIds(String relationTypeName)
   {
      synchronized (m_relationTypeNameToRelationIds)
      {
         return ((List)(m_relationTypeNameToRelationIds.get(relationTypeName)));
      }
   }

   /**
    * @param relationId       - the id through which this relation is referenced
    * @param relationTypeName - a unique name for the RelationType
    * @param roleList         - a list of roles to be associated with this relation
    * @throws IllegalArgumentException      - if the relationId, or relationTypeName is null
    * @throws RelationServiceNotRegisteredException
    *                                       - if the relationService has not been registered in the MBeanServer
    * @throws RoleNotFoundException         - if a role defined in the RoleList is null or empty
    * @throws InvalidRelationIdException    - if the relationId is already in use.
    * @throws RelationTypeNotFoundException - if the relationType is not found
    * @throws InvalidRoleValueException     - if cardinality is not correct i.e min cardinality is greater than max cardinality
    *                                       <p/>
    *                                       <p>According to the RI spec this method is used only to create internal relations - hence creates an InternalRelation</p>
    *                                       <p>This creates a relation represented by a RelationSupport Object, and a RelationNotification,
    *                                       with type RELATION_BASIC_CREATION, is sent</p>
    */
   public void createRelation(String relationId, String relationTypeName, RoleList roleList) throws IllegalArgumentException,
                                                                                                    RelationServiceNotRegisteredException, RoleNotFoundException,
                                                                                                    InvalidRelationIdException, RelationTypeNotFoundException, InvalidRoleValueException
   {
      isActive();
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      if (relationTypeName == null) throw new IllegalArgumentException("Null Relation Type Name");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Creating an InternalRelation with ID: " + relationId + " and relationType name: " + relationTypeName);
      }
      // creating InternalRelation to represent the internal relations
      InternalRelation internalRelation = new InternalRelation(relationId, m_relationServiceObjectName,
                                                               relationTypeName, roleList);
      try
      {
         if (getRelationObject(relationId) != null)
         {
            logger.warn("There is a Relation already registered in the RelationServcie with ID: " + relationId);
            throw new InvalidRelationIdException("There is already a relation with id: " + relationId);
         }
      }
      catch (RelationNotFoundException ex)
      {/*Do nothing as should not be found*/
      }

      RelationType relationType = getRelationType(relationTypeName);
      ArrayList roleInfoList = (ArrayList)(buildRoleInfoList(relationType, roleList));
      if (!(roleInfoList.isEmpty()))
      {
         initializeMissingCreateRoles(roleInfoList, internalRelation, relationId, relationTypeName);
      }

      synchronized (m_relationIdToRelationObject)
      {
         m_relationIdToRelationObject.put(relationId, internalRelation);
      }
      addRelationId(relationId, relationTypeName);
      addRelationTypeName(relationId, relationTypeName);
      updateRoles(roleList, relationId);
      try
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("sending RelationCreation notification to all listeners");
         sendRelationCreationNotification(relationId);
      }
      catch (RelationNotFoundException ex)
      {
         throw new RuntimeOperationsException(null, "Unable to send notification as Relation not found");
      }
   }

   /* updates roles given the roleList and the relationId */
   private void updateRoles(RoleList roleList, String relationId) throws RelationServiceNotRegisteredException,
                                                                         IllegalArgumentException
   {
      if (roleList == null) throw new IllegalArgumentException("Null RoleList");
      if (relationId == null) throw new IllegalArgumentException("Null relationId");
      for (Iterator i = roleList.iterator(); i.hasNext();)
      {
         Role currentRole = (Role)i.next();
         ArrayList tempList = new ArrayList();
         try
         {
            updateRoleMap(relationId, currentRole, tempList);
         }
         catch (RelationNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, "Cannot update the roleMap as Relation not found");
         }
      }
   }

   private List buildRoleInfoList(RelationType relationType, List roleList) throws InvalidRoleValueException,
                                                                                   RoleNotFoundException
   {
      List roleInfoList = relationType.getRoleInfos();
      if (roleList != null)
      {
         for (Iterator i = roleList.iterator(); i.hasNext();)
         {
            Role currentRole = (Role)i.next();
            String currentRoleName = currentRole.getRoleName();
            List currentRoleValue = currentRole.getRoleValue();
            RoleInfo roleInfo;
            try
            {
               roleInfo = relationType.getRoleInfo(currentRoleName);
            }
            catch (RoleInfoNotFoundException ex)
            {
               throw new RoleNotFoundException(ex.getMessage());
            }
            int problemType = (checkRoleCardinality(currentRoleName, currentRoleValue, roleInfo)).intValue();
            if (problemType != 0)
            {
               throwRoleProblemException(problemType, currentRoleName);
            }
            roleInfoList.remove(roleInfoList.indexOf(roleInfo));
         }
      }
      return roleInfoList;
   }

   private void addRelationTypeName(String relationId, String relationTypeName)
   {
      synchronized (m_relationTypeNameToRelationIds)
      {
         ArrayList idList = (ArrayList)m_relationTypeNameToRelationIds.get(relationTypeName);
         boolean isNewRelation = false;
         if (idList == null)
         {
            isNewRelation = true;
            idList = new ArrayList();
         }
         idList.add(relationId);
         if (isNewRelation) m_relationTypeNameToRelationIds.put(relationTypeName, idList);
      }
   }

   private void addRelationObjectName(String relationId, ObjectName relationObjectName)
   {
      synchronized (m_relationIdToRelationObject)
      {
         m_relationIdToRelationObject.put(relationId, relationObjectName);
      }
   }

   private void addRelationId(String relationId, String relationTypeName)
   {
      synchronized (m_relationIdToRelationTypeName)
      {
         m_relationIdToRelationTypeName.put(relationId, relationTypeName);
      }
   }

   // method gets called only for internal relations
   private void initializeMissingCreateRoles(List roleInfoList, InternalRelation internalRelation,
                                             String relationId, String relationTypeName)
           throws RelationTypeNotFoundException, RelationServiceNotRegisteredException,
                  InvalidRoleValueException, RoleNotFoundException, IllegalArgumentException
   {
      isActive();
      if (roleInfoList == null) throw new IllegalArgumentException("RoleInfoList is Null");
      if (relationId == null) throw new IllegalArgumentException("RelationId is Null.");
      if (relationTypeName == null) throw new IllegalArgumentException("Relation Type Name is Null.");

      for (Iterator i = roleInfoList.iterator(); i.hasNext();)
      {
         RoleInfo currentRoleInfo = (RoleInfo)i.next();
         String roleName = currentRoleInfo.getName();

         ArrayList temp = new ArrayList();
         Role role = new Role(roleName, temp);
         try
         {
            internalRelation.setRole(role);
         }
         catch (RelationNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
      }
   }

   private Integer checkRoleCardinality(String roleName, List roleValue, RoleInfo roleInfo)
   {
      if (roleName == null) throw new IllegalArgumentException("Null Role Name");
      if (roleValue == null) throw new IllegalArgumentException("Null roleValue List");
      if (roleInfo == null) throw new IllegalArgumentException("Null RoleInfo");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("checking role cardinality");

      if (!(roleName.equals(roleInfo.getName())))
      {
         logger.warn("Role does not have a valid roleName");
         return new Integer(RoleStatus.NO_ROLE_WITH_NAME);
      }
      if (!(roleInfo.checkMinDegree(roleValue.size())))
      {
         logger.warn("Minimum number of references defined in the RoleInfo has fallen below minimum");
         return (new Integer(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE));
      }
      if (!(roleInfo.checkMaxDegree(roleValue.size())))
      {
         logger.warn("Maximum number of references defined in the RoleInfo has gone above the maximum");
         return new Integer(RoleStatus.MORE_THAN_MAX_ROLE_DEGREE);
      }

      String referencedClassName = roleInfo.getRefMBeanClassName();
      for (Iterator i = roleValue.iterator(); i.hasNext();)
      {
         ObjectName currentObjectName = (ObjectName)i.next();
         if (currentObjectName == null)
         {
            logger.warn("The mbean with RoleName: " + roleName + " is not registered in the MBeanServer");
            return new Integer(RoleStatus.REF_MBEAN_NOT_REGISTERED);
         }
         if (!(m_server.isRegistered(currentObjectName)))
         {
            logger.warn("The mbean with ObjectName: " + currentObjectName.getCanonicalName() + " is not registered in the MBeanServer");
            return new Integer(RoleStatus.REF_MBEAN_NOT_REGISTERED);
         }
         try
         {
            if (!(m_server.isInstanceOf(currentObjectName, referencedClassName)))
            {
               logger.warn("The class referenced: " + currentObjectName.toString() + " does not match the class expected: " +
                           referencedClassName + " in RoleInfo: " + roleInfo.toString());
               return new Integer(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS);
            }
         }
         catch (InstanceNotFoundException ex)
         {
            return null;
         }
      }
      return new Integer(0);
   }

   /**
    * @param relationMBeanObjectName - ObjectName of the relation MBean to be added
    * @throws IllegalArgumentException      - if parameter is null
    * @throws RelationServiceNotRegisteredException
    *                                       - if the Relation Service is not registered in the MBean Server
    * @throws NoSuchMethodException         - If the MBean does not implement the Relation interface
    * @throws InvalidRelationIdException    - if there is no relation identifier (ID) in the MBean
    *                                       - if relation identifier (ID) is already used in the Relation Service
    * @throws InstanceNotFoundException     - if the MBean for given ObjectName has not been registered
    * @throws InvalidRelationServiceException
    *                                       - if no Relation Service name in MBean
    *                                       - if the Relation Service name in the MBean is not the one of the current Relation Service
    * @throws RelationTypeNotFoundException - if no relation type name in MBean
    *                                       - if the relation type name in MBean does not correspond to a relation type created in the Relation Service
    * @throws RoleNotFoundException         - if a value is provided for a role that does not exist in the relation type
    * @throws InvalidRoleValueException     - if the number of referenced MBeans in a role is less than expected minimum degree
    *                                       - if the number of referenced MBeans in a role exceeds expected maximum degree
    *                                       - if one referenced MBean in the value is not an Object of the MBean class expected for that role
    *                                       - if an MBean provided for a role does not exist
    *                                       <p/>
    *                                       <p>Adds an MBean created by the user (and registered by him in the MBean Server) as a relation in the Relation Service </p>
    *                                       <p>To be added as a relation, the MBean must conform to the following:
    *                                       <ul>
    *                                       <li>implement the Relation interface</li>
    *                                       <li>have for RelationService ObjectName the ObjectName of current Relation Service </li>
    *                                       <li>have a relation id unique and unused in current Relation Service </li>
    *                                       <li>have for relation type a relation type created in the Relation Service </li>
    *                                       <li>have roles conforming to the role info provided in the relation type</li>
    *                                       </ul>
    */
   public void addRelation(ObjectName relationMBeanObjectName) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                                      NoSuchMethodException, InvalidRelationIdException, InstanceNotFoundException,
                                                                      InvalidRelationServiceException, RelationTypeNotFoundException,
                                                                      RoleNotFoundException, InvalidRoleValueException
   {
      isActive();
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("adding a Relation with ObjectName: " + relationMBeanObjectName.toString());
      // checks through the MBeanServer that the class is an instance of the Relation interface which
      // RelationSupport implements
      checkValidRelation(relationMBeanObjectName);
      //create the proxy

      m_proxy = (RelationSupportMBean)MBeanServerInvocationHandler.newProxyInstance(m_server, relationMBeanObjectName, RelationSupportMBean.class, false);
      // get the relationId of the class through the MBeanServer
      String relationId = m_proxy.getRelationId();    //getRelationIdAttribute(relationMBeanObjectName);
      if (relationId == null) throw new InvalidRelationIdException("No RelationId provided");
      // obtains the objectname of the RelationService defined by the user

      ObjectName relationServiceObjectName = m_proxy.getRelationServiceName();
      // checks that the RelationService objectName is running in the server
      if (!(checkRelationServiceIsCurrent(relationServiceObjectName)))
      {
         throw new InvalidRelationServiceException("The Relation Service referenced in the MBean is not the current one.");
      }
      // get the relationTypeName through the server for the user defined RelationSupport subclass
      String relationTypeName = m_proxy.getRelationTypeName();
      if (relationTypeName == null) throw new RelationTypeNotFoundException("RelationType not found");
      // get the roles
      RoleList roleList = m_proxy.retrieveAllRoles();
      try
      {
         // already have defined relation registered cannot add another with the same id
         if (getRelationObject(relationId) != null) throw new InvalidRelationIdException("Relation with ID " + relationId + " already exists");
      }
      catch (RelationNotFoundException ex)
      {/*Do nothing should not be found*/
      }

      RelationType relationType = getRelationType(relationTypeName);
      ArrayList roleInfoList = (ArrayList)buildRoleInfoList(relationType, roleList);
      if (!(roleInfoList.isEmpty()))
      {
         for (Iterator i = roleInfoList.iterator(); i.hasNext();)
         {
            RoleInfo currentRoleInfo = (RoleInfo)i.next();
            String currentRoleName = currentRoleInfo.getName();
            ArrayList emptyValueList = new ArrayList();
            Role role = new Role(currentRoleName, emptyValueList);
            try
            {
               m_proxy.setRole(role);
            }
            catch (RelationNotFoundException ex)
            {
               throw new RuntimeOperationsException(null, ex.getMessage());
            }
         }
      }
      // add all info to our corresponding maps
      updateAllInternals(relationId, relationMBeanObjectName, relationTypeName, roleList);
   }

   private void updateAllInternals(String relationId, ObjectName relationMBeanObjectName, String relationTypeName,
                                   RoleList roleList) throws RelationServiceNotRegisteredException
   {
      // adds key -> relationId value -> relationMBeanObjectName to HashMap
      addRelationObjectName(relationId, relationMBeanObjectName);
      addRelationId(relationId, relationTypeName);
      addRelationTypeName(relationId, relationTypeName);
      updateRoles(roleList, relationId);
      try
      {
         sendRelationCreationNotification(relationId);
      }
      catch (RelationNotFoundException ex)
      {
         throw new RuntimeOperationsException(null, "Cannot send a notification for relationId " + relationId + " as relation not found.");
      }

      synchronized (m_relationMBeanObjectNameToRelationId)
      {
         m_relationMBeanObjectNameToRelationId.put(relationMBeanObjectName, relationId);
      }
      m_proxy.setRelationServiceManagementFlag(new Boolean(true));
      List newReferenceList = new ArrayList();
      newReferenceList.add(relationMBeanObjectName);
      updateUnregistrationListener(newReferenceList, null);
   }

   private boolean checkRelationServiceIsCurrent(ObjectName relationServiceObjectName)
   {
      if (relationServiceObjectName == null) return false;
      if (!(relationServiceObjectName.equals(m_relationServiceObjectName))) return false;
      return true;
   }

   private void checkValidRelation(ObjectName relationMBeanObjectName) throws IllegalArgumentException, InstanceNotFoundException, NoSuchMethodException
   {
      if (relationMBeanObjectName == null) throw new IllegalArgumentException("Cannot have a null Relation ObjectName");
      Logger logger = getLogger();
      if (!(m_server.isInstanceOf(relationMBeanObjectName, "javax.management.relation.Relation")))
      {
         logger.warn("An MBean which is to be added as a Relation must implement the Relation interface");
         throw new NoSuchMethodException("MBean does implement the Relation interface");
      }
   }

   /**
    * @param relationId - relation id identifying the relation
    * @return - the ObjectName corresponding to the relationId given or null if it is not found
    * @throws IllegalArgumentException  - if a null parameter
    * @throws RelationNotFoundException - if there is no relation associated to that id
    *                                   <p/>
    *                                   <p>If the relation is represented by an MBean (created by the user and added as a relation in the Relation Service),
    *                                   returns the ObjectName of the MBean</p>
    */
   public ObjectName isRelationMBean(String relationId) throws IllegalArgumentException, RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id.");
      Object result = getRelationObject(relationId);
      if (result instanceof ObjectName)
      {
         return ((ObjectName)result);
      }
      return null;
   }

   /**
    * <p>Returns the relation id associated to the given ObjectName if the MBean has been added as a relation in the Relation Service</p>
    *
    * @param objectName - the ObjectName of supposed relation
    * @return - the relation id (String) or null (if the ObjectName is not a relation handled by the Relation Service)
    * @throws IllegalArgumentException - if the parameter is null
    */
   public String isRelation(ObjectName objectName) throws IllegalArgumentException
   {
      if (objectName == null) throw new IllegalArgumentException("Null ObjectName");
      return (getMBeanObjectName(objectName));
   }

   /**
    * <p>Checks if there is a relation identified in Relation Service with given relation id.</p>
    *
    * @param relationId - the relation id identifying the relation
    * @return boolean: true if there is a relation, false otherwise
    * @throws IllegalArgumentException - if parameter is null
    */
   public Boolean hasRelation(String relationId) throws IllegalArgumentException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      Boolean hasRelation = null;
      try
      {
         Object result = getRelationObject(relationId);
         if (result != null) hasRelation = Boolean.TRUE;
      }
      catch (RelationNotFoundException ex)
      {
         hasRelation = Boolean.FALSE;
      }
      return hasRelation;
   }

   /**
    * <p>Returns all the relation ids for all the relations handled by the Relation Service</p>
    *
    * @return an arrayList containing the relation ids
    */
   public List getAllRelationIds()
   {
      synchronized (m_relationIdToRelationObject)
      {
         return (new ArrayList(m_relationIdToRelationObject.keySet()));
      }
   }

   /**
    * <p>Checks if given Role can be read in a relation of the given type</p>
    *
    * @param roleName         - name of role to be checked
    * @param relationTypeName - name of the relation type
    * @return - an Integer wrapping an integer corresponding to possible problems represented as constants in RoleUnresolved:
    *         <ul>
    *         <li>0 if role can be read </li>
    *         <li>integer corresponding to RoleStatus.NO_ROLE_WITH_NAME </li>
    *         <li>integer corresponding to RoleStatus.ROLE_NOT_READABLE </li>
    *         </ul>
    * @throws IllegalArgumentException      - if null parameter
    * @throws RelationTypeNotFoundException - if the relation type is not known in the Relation Service
    */
   public Integer checkRoleReading(String roleName, String relationTypeName) throws IllegalArgumentException,
                                                                                    RelationTypeNotFoundException
   {
      if (roleName == null) throw new IllegalArgumentException("Null RoleName");
      if (relationTypeName == null) throw new IllegalArgumentException("Null RelationType name.");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("checking if Role with RoleName: " + roleName + " is readable");
      RelationType relationType = getRelationType(relationTypeName);
      try
      {
         RoleInfo roleInfo = relationType.getRoleInfo(roleName);
         if (!(roleName.equals(roleInfo.getName()))) return (new Integer(RoleStatus.NO_ROLE_WITH_NAME));
         if (!(roleInfo.isReadable()))
         {
            logger.warn("RoleInfo: " + roleInfo.toString() + " cannot be read");
            return (new Integer(RoleStatus.ROLE_NOT_READABLE));
         }
      }
      catch (RoleInfoNotFoundException ex)
      {
         logger.warn("roleInfo for roleName: " + roleName + " has not been found.");
         return (new Integer(RoleStatus.NO_ROLE_WITH_NAME));
      }
      return new Integer(0);
   }

   /**
    * <p>Checks if given Role can be set in a relation of given type</p>
    *
    * @param role             - role to be checked
    * @param relationTypeName - name of relation type
    * @param isInitialized    - flag to specify that the checking is done for the initialization of a role, write access shall not be verified
    * @return - an Integer wrapping an integer corresponding to possible problems represented as constants in RoleUnresolved:
    *         <ul>
    *         <li>0 if role can be set </li>
    *         <li>integer corresponding to RoleStatus.NO_ROLE_WITH_NAME </li>
    *         <li>integer for RoleStatus.ROLE_NOT_WRITABLE </li>
    *         <li>integer for RoleStatus.LESS_THAN_MIN_ROLE_DEGREE </li>
    *         <li>integer for RoleStatus.MORE_THAN_MAX_ROLE_DEGREE </li>
    *         <li>integer for RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS </li>
    *         <li>integer for RoleStatus.REF_MBEAN_NOT_REGISTERED </li>
    *         </ul>
    * @throws IllegalArgumentException      - if null parameter
    * @throws RelationTypeNotFoundException - if unknown relation type
    */
   public Integer checkRoleWriting(Role role, String relationTypeName, Boolean isInitialized) throws
                                                                                              IllegalArgumentException, RelationTypeNotFoundException
   {
      if (role == null) throw new IllegalArgumentException("checkRoleWriting was given a null Role");
      if (relationTypeName == null) throw new IllegalArgumentException("checkRoleWriting was given a null RelationTypeName");
      if (isInitialized == null) throw new IllegalArgumentException("checkRoleWriting was given a null Boolean");
      Logger logger = getLogger();
      RelationType relationType = getRelationType(relationTypeName);
      String roleName = role.getRoleName();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("checking if Role with RoleName: " + roleName + " is readable");
      ArrayList roleValue = (ArrayList)role.getRoleValue();
      boolean canWrite = true;
      if (isInitialized.booleanValue()) canWrite = false;
      RoleInfo roleInfo;
      try
      {
         roleInfo = relationType.getRoleInfo(roleName);
      }
      catch (RoleInfoNotFoundException ex)
      {
         logger.warn("roleInfo for roleName: " + roleName + " has not been found.");
         return new Integer(RoleStatus.NO_ROLE_WITH_NAME);
      }
      if (canWrite)
      {
         if (!(roleInfo.isWritable()))
         {
            logger.warn("RoleInfo: " + roleInfo.toString() + " cannot be written to.");
            return new Integer(RoleStatus.ROLE_NOT_WRITABLE);
         }
      }
      return (checkRoleCardinality(roleName, roleValue, roleInfo));
   }

   /**
    * <p>Sends a notification (RelationNotification) for a relation creation. The notification type is:
    * <ul>
    * <li>RelationNotification.RELATION_BASIC_CREATION if the relation is an object internal to the Relation Service </li>
    * <li>RelationNotification.RELATION_MBEAN_CREATION if the relation is a MBean added as a relation</li>
    * </ul>
    * The source object is the Relation Service itself<br/>
    * It is called in Relation Service createRelation() and addRelation() methods
    * </p>
    *
    * @param relationId - relation identifier of the updated relation
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationNotFoundException - if there is no relation for given relation id
    */
   public void sendRelationCreationNotification(String relationId) throws IllegalArgumentException,
                                                                          RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id.");
      Logger logger = getLogger();
      String message = "Creation of relation " + relationId;
      String relationTypeName = getRelationTypeNameFromMap(relationId);

      if (logger.isEnabledFor(Logger.DEBUG))
         logger.debug("A relation has been created with ID: " + relationId +
                      " and relationTypeName: " + relationTypeName + " ..sending notification");

      ObjectName relationObjectName = isRelationMBean(relationId);
      String notificationType = getCreationNotificationType(relationObjectName);
      long sequenceNumber = getNotificationSequenceNumber().longValue();
      Date currentDate = new Date();
      long timestamp = currentDate.getTime();
      RelationNotification relationNotification = new RelationNotification(notificationType, this, sequenceNumber,
                                                                           timestamp, message, relationId, relationTypeName, relationObjectName, null);
      sendNotification(relationNotification);
   }

   private Long getNotificationSequenceNumber()
   {
      Long result;
      synchronized (m_notificationCounter)
      {
         result = new Long(m_notificationCounter.longValue() + 1);
         m_notificationCounter = new Long(result.longValue());
      }
      return result;
   }

   private String getCreationNotificationType(ObjectName relationObjectName)
   {
      if (relationObjectName != null)
      {
         return RelationNotification.RELATION_MBEAN_CREATION;
      }
      return RelationNotification.RELATION_BASIC_CREATION;
   }

   /**
    * <p>Sends a notification (RelationNotification) for a role update in the given relation. The notification type is:
    * <ul>
    * <li>RelationNotification.RELATION_BASIC_UPDATE if the relation is an object internal to the Relation Service </li>
    * <li>RelationNotification.RELATION_MBEAN_UPDATE if the relation is a MBean added as a relation</li>
    * </ul></p>
    * <p>The source object is the Relation Service itself.</p>
    * <p>This method is called in relation MBean setRole() (for given role) and setRoles() (for each role) methods
    * (implementation provided in RelationSupport class)</p>
    * <p>It is also called in Relation Service setRole() (for given role) and setRoles() (for each role) methods</p>
    *
    * @param relationId    - the relation identifier of the updated relation
    * @param newRole       - new role (name and new value)
    * @param oldRoleValues - old role value (ArrayList of ObjectName objects)
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationNotFoundException - if there is no relation for given relation id
    */
   public void sendRoleUpdateNotification(String relationId, Role newRole, List oldRoleValues) throws IllegalArgumentException,
                                                                                                      RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null RelationId");
      if (newRole == null) throw new IllegalArgumentException("Null Role");
      if (oldRoleValues == null) throw new IllegalArgumentException("Null List of role values");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending a roleUpdateNotification of Relation with ID: " + relationId);
      String roleName = newRole.getRoleName();
      List newRoleValues = newRole.getRoleValue();
      String newRoleValueMessage = Role.roleValueToString(newRoleValues);
      String oldRoleValueMessage = Role.roleValueToString(oldRoleValues);
      StringBuffer message = new StringBuffer("Value of the role ");
      message.append(roleName);
      message.append(" has changed\nOld value:\n");
      message.append(oldRoleValueMessage);
      message.append("\nNew value:\n");
      message.append(newRoleValueMessage);
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Notification message: " + message.toString());
      String relationTypeName = getRelationTypeNameFromMap(relationId);

      // determine if this is a relation update or a relation mbean update
      ObjectName relationObjectName = isRelationMBean(relationId);
      String notificationType;
      if (relationObjectName != null)
         notificationType = RelationNotification.RELATION_MBEAN_UPDATE;
      else
         notificationType = RelationNotification.RELATION_BASIC_UPDATE;

      long sequenceNumber = getNotificationSequenceNumber().longValue();
      Date currentDate = new Date();
      long timeStamp = currentDate.getTime();

      RelationNotification notification = new RelationNotification(notificationType, this, sequenceNumber, timeStamp,
                                                                   message.toString(), relationId, relationTypeName, relationObjectName, roleName, newRoleValues,
                                                                   oldRoleValues);
      sendNotification(notification);
   }

   /**
    * <p>Sends a notification (RelationNotification) for a relation removal. The notification type is:
    * <ul>
    * <li>RelationNotification.RELATION_BASIC_REMOVAL if the relation is an object internal to the Relation Service </li>
    * <li>RelationNotification.RELATION_MBEAN_REMOVAL if the relation is a MBean added as a relation</li>
    * </ul>
    * The source object is the Relation Service itself</p>
    * <p>It is called in Relation Service removeRelation() method</p>
    *
    * @param relationId            - relation identifier of the updated relation
    * @param unregisteredMBeanList - ArrayList of ObjectNames of MBeans expected to be unregistered due to relation removal (can be null)
    * @throws IllegalArgumentException  - if relationId is null
    * @throws RelationNotFoundException - if there is no relation for given relation id
    */
   public void sendRelationRemovalNotification(String relationId, List unregisteredMBeanList) throws IllegalArgumentException,
                                                                                                     RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null RelationId");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("sending relationRemovalNotification of ID: " + relationId);
      StringBuffer message = new StringBuffer("Removal of relation ");
      message.append(relationId);
      String relationTypeName = getRelationTypeNameFromMap(relationId);
      ObjectName relationObjectName = isRelationMBean(relationId);
      String notificationType;
      if (relationObjectName != null)
         notificationType = RelationNotification.RELATION_MBEAN_REMOVAL;
      else
         notificationType = RelationNotification.RELATION_BASIC_REMOVAL;
      long sequenceNumber = getNotificationSequenceNumber().longValue();
      Date currentDate = new Date();
      long timeStamp = currentDate.getTime();
      RelationNotification notification = new RelationNotification(notificationType, this, sequenceNumber, timeStamp,
                                                                   message.toString(), relationId, relationTypeName, relationObjectName, unregisteredMBeanList);

      sendNotification(notification);
   }

   /**
    * <p>Handles update of the Relation Service role map for the update of given role in given relation</p>
    * <p>It is called in relation MBean setRole() (for given role) and setRoles()(for each role) methods
    * (implementation provided in RelationSupport class).</p>
    * <p>It is also called in Relation Service setRole() (for given role) and setRoles() (for each role) methods.</p>
    * <p>To allow the Relation Service to maintain the consistency (in case of MBean unregistration) and to be able to
    * perform queries, this method must be called when a role is updated. </p>
    *
    * @param relationId    - relation identifier of the updated relation
    * @param role          - new role (name and new value)
    * @param oldRoleValues - old role value (ArrayList of ObjectName objects)
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationServiceNotRegisteredException
    *                                   - if the Relation Service is not registered in the MBean Server
    * @throws RelationNotFoundException - if no relation for given id
    */
   public void updateRoleMap(String relationId, Role role, List oldRoleValues) throws IllegalArgumentException,
                                                                                      RelationServiceNotRegisteredException, RelationNotFoundException
   {
      isActive();
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      if (role == null) throw new IllegalArgumentException("Null Role");
      if (oldRoleValues == null) throw new IllegalArgumentException("Null Role value list.");

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Updating the RelationService RoleMap");

      String roleName = role.getRoleName();
      List newRoleValue = role.getRoleValue();

      // clone as the list is to be modified later, cast to ArrayList as List does not define clone()
      List oldValues = (ArrayList)(((ArrayList)oldRoleValues).clone());

      // List of ObjectNames of new referenced MBeans
      List newReferenceList = new ArrayList();
      for (Iterator i = newRoleValue.iterator(); i.hasNext();)
      {
         ObjectName currentObjectName = (ObjectName)i.next();
         // Check if this ObjectName was already present in oldValueList
         int currentObjectNamePosition = oldValues.indexOf(currentObjectName);
         // we have a new Reference
         if (currentObjectNamePosition == -1)
         {
            // returns true if we have a new reference, false if the MBean is already referenced
            if (addNewMBeanReference(currentObjectName, relationId, roleName))
            {
               // add to new references list
               newReferenceList.add(currentObjectName);
            }
         }
         else
         {
            // MBean referenced in an old value remove
            oldValues.remove(currentObjectNamePosition);
         }
      }
      List obsoleteReferenceList = getObsoleteReferenceList(oldValues, relationId, roleName);
      // update listeners as to the new references
      updateUnregistrationListener(newReferenceList, obsoleteReferenceList);
   }

   private List getObsoleteReferenceList(List oldValues, String relationId, String roleName)
           throws IllegalArgumentException
   {
      List obsoleteReferenceList = new ArrayList();
      for (Iterator i = oldValues.iterator(); i.hasNext();)
      {
         ObjectName currentObjectName = (ObjectName)i.next();
         if (removeMBeanReference(currentObjectName, relationId, roleName))
         {
            obsoleteReferenceList.add(currentObjectName);
         }
      }
      return obsoleteReferenceList;
   }

   private boolean removeMBeanReference(ObjectName objectName, String relationId, String roleName)
           throws IllegalArgumentException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      if (objectName == null) throw new IllegalArgumentException("Null ObjectName");
      if (roleName == null) throw new IllegalArgumentException("Null Role Name.");
      // first check if we have any references for MBean
      HashMap mbeanReferenceMap = (HashMap)getReferencedMBeansFromMap(objectName);

      // no references nothing to remove
      if (mbeanReferenceMap == null) return true;
      // we have references get the roleNames for the relationId
      ArrayList roleNames = (ArrayList)(mbeanReferenceMap.get(relationId));

      //check the roleNames is not null before removing
      if (roleNames != null)
      {
         // roleName found remove it
         if (roleNames.indexOf(roleName) != -1) roleNames.remove(roleNames.indexOf(roleName));
         // no more references remove the relationId key
         if (roleNames.isEmpty()) mbeanReferenceMap.remove(relationId);
      }
      if (mbeanReferenceMap.isEmpty())
      {
         // now we can remove the MBean ObejctName with all references removed
         removeObjectName(objectName);
         return true;
      }
      return false;
   }

   private Map getReferencedMBeansFromMap(ObjectName objectName)
   {
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         return ((HashMap)m_referencedMBeanObjectNameToRelationIds.get(objectName));
      }
   }

   private boolean addNewMBeanReference(ObjectName objectName, String relationId, String roleName)
           throws IllegalArgumentException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      if (roleName == null) throw new IllegalArgumentException("Null Role Name");
      if (objectName == null) throw new IllegalArgumentException("Null ObjectName.");

      // get the ObjectNames of the referenced ObjectName
      HashMap mbeanReferenceMap;
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         mbeanReferenceMap = ((HashMap)m_referencedMBeanObjectNameToRelationIds.get(objectName));
      }
      // it is null, add it to our map and return true - we have a new reference
      if (mbeanReferenceMap == null)
      {
         mbeanReferenceMap = new HashMap();
      }
      List roleNames = (List) mbeanReferenceMap.get(relationId);
      if (roleNames == null)
      {
         roleNames = new ArrayList();
         roleNames.add(roleName);

         mbeanReferenceMap.put(relationId, roleNames);
         addObjectNameToMBeanReference(objectName, mbeanReferenceMap);
         return true;
      }
      else
      {
         roleNames.add(roleName);
         addObjectNameToMBeanReference(objectName, mbeanReferenceMap);
         // not a new MBeanReference return false
         return false;
      }
   }

   private void addObjectNameToMBeanReference(ObjectName objectName, HashMap mbeanReferenceMap)
   {
      // get the mapfirst, then update it
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         Map temp = (Map)m_referencedMBeanObjectNameToRelationIds.get(objectName);
         if (temp != null)
         {
            mbeanReferenceMap.putAll(temp);
         }
         m_referencedMBeanObjectNameToRelationIds.put(objectName, mbeanReferenceMap);
      }
   }

   /**
    * <p>Removes given relation from the Relation Service.</p>
    * <p>A RelationNotification notification is sent, its type being:
    * <ul>
    * <li>RelationNotification.RELATION_BASIC_REMOVAL if the relation was only internal to the Relation Service </li>
    * <li>RelationNotification.RELATION_MBEAN_REMOVAL if the relation is registered as an MBean</li>
    * </ul></p>
    * <p>For MBeans referenced in such relation, nothing will be done</p>
    *
    * @param relationId - relation id of the relation to be removed
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationServiceNotRegisteredException
    *                                   - if the Relation Service is not registered in the MBean Server
    * @throws RelationNotFoundException - if no relation corresponding to given relation id
    */
   public void removeRelation(String relationId) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                        RelationNotFoundException
   {
      isActive();
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Removing a Relation from the RelationService.");
      Object result = getRelationObject(relationId);
      if (result instanceof ObjectName)
      {
         // add the objectName to List
         List obsoleteReferences = new ArrayList();
         obsoleteReferences.add(result);
         // update Listener with the list of ObjectNames to be deregistered
         updateUnregistrationListener(null, obsoleteReferences);
      }
      // notify all listeners a relation has been removed
      sendRelationRemovalNotification(relationId, null);

      List nonReferencedObjectNameList = getNonReferencedMBeans(relationId);
      // remove ObjectNames from global Map
      removeNonReferencedMBeans(nonReferencedObjectNameList);
      // remove relationId for our nonReferencedMBeans
      removeRelationId(relationId);
      // we have an MBean remove it from our Map
      if (result instanceof ObjectName) removeRelationMBean((ObjectName)result);
      // get the corresponding relationTypeName for the relationId
      String relationTypeName = getRelationTypeNameFromMap(relationId);
      // remove relationId from our Map
      removeRelationIdToRelationTypeName(relationId);
      // get all the corresponding relationIds
      List relationIdsList = getRelationIds(relationTypeName);

      // we have relationIds for the removed relationTypeName remove them from our Map
      if (relationIdsList != null)
      {
         relationIdsList.remove(relationId);
         if (relationIdsList.isEmpty())
         {
            // now we can remove the relationTypeName
            removeRelationTypeName(relationTypeName);
         }
      }
   }

   private void removeRelationMBean(ObjectName objectName)
   {
      synchronized (m_relationMBeanObjectNameToRelationId)
      {
         m_relationMBeanObjectNameToRelationId.remove(objectName);
      }
   }

   private void removeNonReferencedMBeans(List nonReferencedMBeansList)
   {
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         for (Iterator i = nonReferencedMBeansList.iterator(); i.hasNext();)
         {
            ObjectName currentObjectName = (ObjectName)i.next();
            m_referencedMBeanObjectNameToRelationIds.remove(currentObjectName);
         }
      }
   }

   private List getNonReferencedMBeans(String relationId)
   {
      List referencedMBeanList = new ArrayList();
      List nonReferencedObjectNameList = new ArrayList();
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         for (Iterator i = (m_referencedMBeanObjectNameToRelationIds.keySet()).iterator(); i.hasNext();)
         {
            ObjectName currentObjectName = (ObjectName)i.next();
            HashMap relationIdMap = (HashMap)(m_referencedMBeanObjectNameToRelationIds.get(currentObjectName));
            if (relationIdMap.containsKey(relationId))
            {
               relationIdMap.remove(relationId);
               referencedMBeanList.add(currentObjectName);
            }
            if (relationIdMap.isEmpty()) nonReferencedObjectNameList.add(currentObjectName);
         }
      }
      return nonReferencedObjectNameList;
   }

   private void removeRelationTypeName(String relationTypeName)
   {
      synchronized (m_relationTypeNameToRelationIds)
      {
         m_relationTypeNameToRelationIds.remove(relationTypeName);
      }
   }

   private String getRelationTypeNameFromMap(String relationId)
   {
      synchronized (m_relationIdToRelationTypeName)
      {
         return ((String)m_relationIdToRelationTypeName.get(relationId));
      }
   }

   private void removeRelationIdToRelationTypeName(String relationId)
   {
      synchronized (m_relationIdToRelationTypeName)
      {
         m_relationIdToRelationTypeName.remove(relationId);
      }
   }

   private String getMBeanObjectName(ObjectName objectName)
   {
      synchronized (m_relationMBeanObjectNameToRelationId)
      {
         return ((String)m_relationMBeanObjectNameToRelationId.get(objectName));
      }
   }

   private void removeRelationId(String relationId)
   {
      synchronized (m_relationIdToRelationObject)
      {
         m_relationIdToRelationObject.remove(relationId);
      }
   }

   private void updateUnregistrationListener(List newReferenceList, List obsoleteReferences) throws RelationServiceNotRegisteredException
   {
      if (newReferenceList != null && obsoleteReferences != null)
      {
         // nothing to do
         if (newReferenceList.isEmpty() && obsoleteReferences.isEmpty()) return;
      }
      isActive();
      if (newReferenceList != null || obsoleteReferences != null)
      {
         boolean isNewListener = false;
         if (m_notificationFilter == null)
         {
            m_notificationFilter = new MBeanServerNotificationFilter();
            isNewListener = true;
         }
         synchronized (m_notificationFilter)
         {
            // we have new references - update (Enable in NotificationFilter)
            if (newReferenceList != null) updateNewReferences(newReferenceList);

            // we have obsolete references - update (disable from notificationFilter)
            if (obsoleteReferences != null) updateObsoleteReferences(obsoleteReferences);

            ObjectName mbeanServerDelegateName = null;
            try
            {
               mbeanServerDelegateName = new ObjectName("JMImplementation:type=MBeanServerDelegate");
            }
            catch (MalformedObjectNameException ignored)
            {
            }

            if (isNewListener)
            {
               try
               {
                  m_server.addNotificationListener(mbeanServerDelegateName, this, m_notificationFilter, null);
               }
               catch (InstanceNotFoundException ex)
               {
                  throw new RelationServiceNotRegisteredException(ex.getMessage());
               }
            }
         }
      }
   }

   private void updateObsoleteReferences(List obsoleteReferences)
   {
      for (Iterator i = obsoleteReferences.iterator(); i.hasNext();)
      {
         ObjectName name = (ObjectName)i.next();
         m_notificationFilter.disableObjectName(name);
      }
   }

   private void updateNewReferences(List newReferencesList)
   {
      for (Iterator i = newReferencesList.iterator(); i.hasNext();)
      {
         ObjectName name = (ObjectName)i.next();
         m_notificationFilter.enableObjectName(name);
      }
   }

   // if null we have an instance of the internalRelation otherwise we have an MBean and can use the proxy
   private Relation getRelation(String relationId) throws RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null relation id passed into getRelation.");
      Relation relation;
      // if this is null we have a Relation not an ObjectName so we can return the Relation
      if (isRelationMBean(relationId) == null)
      {
         synchronized (m_relationIdToRelationObject)
         {
            relation = (Relation)m_relationIdToRelationObject.get(relationId);
            return relation;
         }
      }

      final ObjectName relationObjectName = (ObjectName)m_relationIdToRelationObject.get(relationId);
// oops no relation at all
      if (relationObjectName == null)
      {
         throw new RelationNotFoundException("Relation not found with ID: " + relationId);
      }
// good we have a relation lets return the proxy
      m_proxy = (RelationSupportMBean)MBeanServerInvocationHandler.newProxyInstance(m_server, relationObjectName, RelationSupportMBean.class, false);
      return m_proxy;
   }

   // will return an ObjectName or a Relation object
   private Object getRelationObject(String relationId) throws IllegalArgumentException, RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      Object relationObject;
      synchronized (m_relationIdToRelationObject)
      {
         relationObject = m_relationIdToRelationObject.get(relationId);
         if (relationObject == null)
         {
            // if this happens is caught by method looking for a null.
            throw new RelationNotFoundException("Null Relation");
         }
         // we return either an ObjectName or an InternalRelation
         return relationObject;
      }
   }

   /**
    * <p>Purges the relations</p>
    * <p/>
    * <p>Depending on the purgeFlag value, this method is either called automatically when a notification is received for the
    * unregistration of an MBean referenced in a relation (if the flag is set to true), or not (if the flag is set to false).</p>
    * <p/>
    * <p>In that case it is up to the user to call it to maintain the consistency of the relations. To be kept in mind that if an MBean is
    * unregistered and the purge not done immediately, if the ObjectName is reused and assigned to another MBean referenced in a relation,
    * calling manually this purgeRelations() method will cause trouble, as will consider the ObjectName as corresponding to the unregistered MBean,
    * not seeing the new one.</p>
    * <p/>
    * <p/>
    * <ul>
    * <li>if removing one MBean reference in the role makes its number of references less than the minimum degree, the relation has to be removed.</li>
    * <li>if the remaining number of references after removing the MBean reference is still in the cardinality range, keep the relation and update
    * it calling its handleMBeanUnregistration() callback.</li>
    * </ul></p>
    *
    * @throws RelationServiceNotRegisteredException
    *          - if the Relation Service is not registered in the MBean Server.
    */
   public void purgeRelations() throws RelationServiceNotRegisteredException
   {
      isActive();
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("purging relations");

      ArrayList localDeregisteredNotificationList;
      synchronized (m_deregisteredNotificationList)
      {
         // cone the list of notifications to be able to recieve notification while processing current ones
         localDeregisteredNotificationList = ((ArrayList)((ArrayList)m_deregisteredNotificationList).clone());
         // now reset
         m_deregisteredNotificationList = new ArrayList();
      }

      List obsoleteReferenceList = new ArrayList();
      Map localMBeanToRelationId = new HashMap();
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         for (Iterator i = localDeregisteredNotificationList.iterator(); i.hasNext();)
         {
            MBeanServerNotification serverNotification = (MBeanServerNotification)i.next();
            ObjectName deregisteredMBeanName = serverNotification.getMBeanName();
            obsoleteReferenceList.add(deregisteredMBeanName);

            HashMap relationIdMap = (HashMap)m_referencedMBeanObjectNameToRelationIds.get(deregisteredMBeanName);
            localMBeanToRelationId.put(deregisteredMBeanName, relationIdMap);
            m_referencedMBeanObjectNameToRelationIds.remove(deregisteredMBeanName);
         }
      }

      // update listener filter to avoid recieving notifications for same MBeans
      updateUnregistrationListener(null, obsoleteReferenceList);
      for (Iterator i = localDeregisteredNotificationList.iterator(); i.hasNext();)
      {
         MBeanServerNotification currentNotification = (MBeanServerNotification)i.next();
         ObjectName unregisteredMBeanObjectName = currentNotification.getMBeanName();
         HashMap localRelationIdMap = (HashMap)(localMBeanToRelationId.get(unregisteredMBeanObjectName));

         Set localRelationIdSet = localRelationIdMap.keySet();
         // handles the unregistration of mbeans
         unregisterReferences(localRelationIdSet, localRelationIdMap, unregisteredMBeanObjectName);
      }
   }

   private void unregisterReferences(Set relationIdSet, Map relationIdMap, ObjectName objectName)
           throws RelationServiceNotRegisteredException
   {
      for (Iterator iter = relationIdSet.iterator(); iter.hasNext();)
      {
         String currentRelationId = (String)iter.next();
         ArrayList localRoleNamesList = (ArrayList)(relationIdMap.get(currentRelationId));
         try
         {
            handleReferenceUnregistration(currentRelationId, objectName, localRoleNamesList);
         }
         catch (RelationTypeNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
         catch (RelationNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
         catch (RoleNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
      }
   }

   private void handleReferenceUnregistration(String relationId, ObjectName unregisteredObjectName, List roleNames)
           throws IllegalArgumentException, RelationServiceNotRegisteredException, RelationNotFoundException,
                  RoleNotFoundException, RelationTypeNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null relationId");
      if (unregisteredObjectName == null) throw new IllegalArgumentException("Null ObjectName");
      if (roleNames == null) throw new IllegalArgumentException("Null roleName list");

      isActive();
      String relationTypeName = getRelationTypeName(relationId);

      boolean canDeleteRelation = false;
      for (Iterator i = roleNames.iterator(); i.hasNext();)
      {
         String currentRoleName = (String)i.next();
         int currentRoleCardinality = (getRoleCardinality(relationId, currentRoleName)).intValue();
         int newRoleCardinality = currentRoleCardinality - 1;
         RoleInfo currentRoleInfo;
         try
         {
            currentRoleInfo = getRoleInfo(relationTypeName, currentRoleName);
         }
         catch (RelationTypeNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
         catch (RoleInfoNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
         // check that the role cardinality is maintained by the removal
         if (!(currentRoleInfo.checkMinDegree(newRoleCardinality)))
         {
            canDeleteRelation = true;
         }
      }
      // roleMinValue been checked everything ok, can now remove the relation
      if (canDeleteRelation)
      {
         removeRelation(relationId);
      }
      else
      {
         for (Iterator i = roleNames.iterator(); i.hasNext();)
         {
            String currentRoleName = (String)i.next();
            try
            {
               Relation relation = getRelation(relationId);
               relation.handleMBeanUnregistration(unregisteredObjectName, currentRoleName);
            }
            catch (InvalidRoleValueException ex)
            {
               throw new RuntimeOperationsException(null, ex.getMessage());
            }
         }
      }
   }

   private void removeObjectName(ObjectName objectName)
   {
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         m_referencedMBeanObjectNameToRelationIds.remove(objectName);
      }
   }

   /**
    * <p>Retrieves the relations where a given MBean is referenced.</p>
    *
    * @param mbeanObjectName  - ObjectName of MBean
    * @param relationTypeName - can be null; if specified, only the relations of that type will be considered in the search. Else all relation types are considered.
    * @param roleName         - can be null; if specified, only the relations where the MBean is referenced in that role will be returned. Else all roles are considered.
    * @return - HashMap, where the keys are the relation ids of the relations where the MBean is referenced, and the value is, for each key,
    *         an ArrayList of role names (as an MBean can be referenced in several roles in the same relation).
    * @throws IllegalArgumentException - if mbeanObjectName is null
    */
   public Map findReferencingRelations(ObjectName mbeanObjectName, String relationTypeName, String roleName) throws
                                                                                                             IllegalArgumentException
   {
      if (mbeanObjectName == null) throw new IllegalArgumentException("Cannot find references for a null ObjectName");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG))
         logger.debug("finding referencing relations for MBean with ObjectName: "
                      + mbeanObjectName.getCanonicalName() + " and relationTypeName: " + relationTypeName + " and roleName: " + roleName);
      HashMap result = new HashMap();
      HashMap relationIdMap = (HashMap)getReferencedMBeansFromMap(mbeanObjectName);
      if (relationIdMap != null)
      {
         Set allRelationIds = relationIdMap.keySet();
         List relationIdList;
         if (relationTypeName == null)
         {
            relationIdList = new ArrayList(allRelationIds);
         }
         else
         {
            relationIdList = findReferencesFromIds(allRelationIds, relationTypeName);
         }

         for (Iterator i = relationIdList.iterator(); i.hasNext();)
         {
            String currentRelationId = (String)i.next();
            ArrayList currentRoleNameList = (ArrayList)relationIdMap.get(currentRelationId);
            if (roleName == null)
            {
               result.put(currentRelationId, currentRoleNameList.clone());
            }
            else if (currentRoleNameList.contains(roleName))
            {
               ArrayList roleNameList = new ArrayList();
               roleNameList.add(roleName);
               result.put(currentRelationId, roleNameList);
            }
         }
      }
      return result;
   }

   private ArrayList findReferencesFromIds(Set allRelationIds, String relationTypeName)
   {
      ArrayList relationIdList = new ArrayList();
      for (Iterator i = allRelationIds.iterator(); i.hasNext();)
      {
         String currentRelationId = (String)i.next();
         String currentRelationTypeName = getRelationTypeNameFromMap(currentRelationId);
         if (currentRelationTypeName.equals(relationTypeName))
         {
            relationIdList.add(currentRelationId);
         }
      }
      return relationIdList;
   }

   /**
    * <p>Retrieves the MBeans associated to given one in a relation.</p>
    *
    * @param mbeanObjectName  - ObjectName of MBean
    * @param relationTypeName - can be null; if specified, only the relations of that type will be considered in the search. Else all relation types are considered
    * @param roleName         - can be null; if specified, only the relations where the MBean is referenced in that role will be considered. Else all roles are considered.
    * @return - HashMap, where the keys are the ObjectNames of the MBeans associated to given MBean, and the value is, for each key, an ArrayList of the
    *         relation ids of the relations where the key MBean is associated to given one (as they can be associated in
    *         several different relations).
    * @throws IllegalArgumentException - if mbeanObjectName is null
    */
   public Map findAssociatedMBeans(ObjectName mbeanObjectName, String relationTypeName, String roleName) throws
                                                                                                         IllegalArgumentException
   {
      if (mbeanObjectName == null) throw new IllegalArgumentException("mbean ObjectName cannot be null.");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG))
         logger.debug("finding associated relations for MBean with ObjectName: "
                      + mbeanObjectName.getCanonicalName() + " and relationTypeName: " + relationTypeName + " and roleName: " + roleName);
      Map relationIdsToRoleNames = findReferencingRelations(mbeanObjectName, relationTypeName, roleName);
      Map result = new HashMap();
      for (Iterator i = (relationIdsToRoleNames.keySet()).iterator(); i.hasNext();)
      {
         String currentRelationId = (String)i.next();
         HashMap objectNamesToRoleMap;
         try
         {
            objectNamesToRoleMap = (HashMap)(getReferencedMBeans(currentRelationId));
         }
         catch (RelationNotFoundException ex)
         {
            logger.warn("Relation with ID: " + currentRelationId + " not found.");
            throw new RuntimeOperationsException(null, "Relation Not Found");
         }
         for (Iterator iter = (objectNamesToRoleMap.keySet()).iterator(); iter.hasNext();)
         {
            ObjectName objectName = (ObjectName)iter.next();
            if (!(objectName.equals(mbeanObjectName)))
            {
               ArrayList currentRelationIdList = (ArrayList) result.get(objectName);
               if (currentRelationIdList == null)
               {
                  currentRelationIdList = new ArrayList();
                  result.put(objectName, currentRelationIdList);
               }
               currentRelationIdList.add(currentRelationId);
            }
         }
      }
      return result;
   }

   /**
    * <p>Returns the relation ids for relations of the given type. </p>
    *
    * @param relationTypeName - relation type name
    * @return - arrayList of relationIds
    * @throws IllegalArgumentException      - if relationTypeName is null
    * @throws RelationTypeNotFoundException - if there is no relation type with that name
    */
   public List findRelationsOfType(String relationTypeName) throws IllegalArgumentException, RelationTypeNotFoundException
   {
      if (relationTypeName == null) throw new IllegalArgumentException("relation type name cannot be null.");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("finding relations matching relationTypeName: " + relationTypeName);

      // throws RelationTypeNotFoundException if not found
      getRelationType(relationTypeName);

      List relationIdList = getRelationIds(relationTypeName);
      if (relationIdList == null) return new ArrayList();

      return new ArrayList(relationIdList);
   }

   /**
    * <P>Retrieves role value for given role name in given relation</p>
    *
    * @param relationId - the relation identifier
    * @param roleName   - the name of the role
    * @return - an ArrayList of ObjectName objects being the role value
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationServiceNotRegisteredException
    *                                   - if the Relation Service is not registered
    * @throws RelationNotFoundException - if no relation with given id
    * @throws RoleNotFoundException     - if there is no role with given name or
    *                                   the role is not readable
    */
   public List getRole(String relationId, String roleName) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                                  RelationNotFoundException, RoleNotFoundException
   {
      isActive();
      if (relationId == null) throw new IllegalArgumentException("RelationId cannot have a null value.");
      if (roleName == null) throw new IllegalArgumentException("Role Name cannot have a null value.");
      Relation relationObject = getRelation(relationId);
      return relationObject.getRole(roleName);
   }

   /**
    * <p>Retrieves values of roles with given names in given relation</p>
    *
    * @param relationId - the relation identifier
    * @param roleNames  - array of names of roles to be retrieved
    * @return - a RoleResult object, including a RoleList (for roles succcessfully retrieved) and a RoleUnresolvedList (for roles not retrieved).
    * @throws IllegalArgumentException  - if either parameter is null
    * @throws RelationNotFoundException - if no relation with given id
    * @throws RelationServiceNotRegisteredException
    *                                   - if the Relation Service is not registered in the MBean Server
    */
   public RoleResult getRoles(String relationId, String[] roleNames) throws IllegalArgumentException, RelationNotFoundException, RelationServiceNotRegisteredException
   {
      if (relationId == null) throw new IllegalArgumentException("Illegal Argument relationId is null.");
      if (roleNames == null) throw new IllegalArgumentException("Array of Roles Names should not be null");
      isActive();
      Relation relation = getRelation(relationId);
      return relation.getRoles(roleNames);
   }

   /**
    * <p>Returns all roles present in the relation</p>
    *
    * @param relationId - the relation identifier
    * @return - a  RoleResult object, including a RoleList (for roles succcessfully retrieved) and a RoleUnresolvedList (for roles not readable).
    * @throws IllegalArgumentException  - if the relation id is null
    * @throws RelationNotFoundException - if no relation for given id
    * @throws RelationServiceNotRegisteredException
    *                                   - if the Relation Service is not registered in the MBean Server
    */
   public RoleResult getAllRoles(String relationId) throws IllegalArgumentException, RelationNotFoundException,
                                                           RelationServiceNotRegisteredException
   {
      if (relationId == null) throw new IllegalArgumentException("RelationId cannot be null");
      //Object relationObject = getRelationObject(relationId);
      Relation relation = getRelation(relationId);
      return relation.getAllRoles();
   }

   /**
    * <p>Retrieves the number of MBeans currently referenced in the given role.</p>
    *
    * @param relationId - relation id
    * @param roleName   - name of role
    * @return - the number of currently referenced MBeans in that role
    * @throws IllegalArgumentException  - if a null parameter
    * @throws RelationNotFoundException - if no relation with given id
    * @throws RoleNotFoundException     - if there is no role with given name
    */
   public Integer getRoleCardinality(String relationId, String roleName) throws IllegalArgumentException,
                                                                                RelationNotFoundException, RoleNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Relation Id is null.");
      if (roleName == null) throw new IllegalArgumentException("Role Name is null.");
      Object relationObject = getRelationObject(relationId);
      if (relationObject instanceof InternalRelation)
         return (((InternalRelation)relationObject).getRoleCardinality(roleName));
      else
         return m_proxy.getRoleCardinality(roleName);
   }

   /**
    * <p>Sets the given role in given relation</p>
    * <p>Will check the role according to its corresponding role definition provided in relation's relation type </p>
    * <p>The Relation Service will keep track of the change to keep the consistency of relations by handling referenced MBean unregistrations</p>
    *
    * @param relationId - the relation identifier
    * @param role       - role to be set (name and new value)
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationServiceNotRegisteredException
    *                                   - if relation service is not registered in the MBean Server
    * @throws RelationNotFoundException - if no relation with given id
    * @throws RoleNotFoundException     - if the role does not exist or is not writable
    * @throws InvalidRoleValueException - if value provided for role is not valid i.e.
    *                                   <ul>
    *                                   <li>the number of referenced MBeans in given value is less than expected minimum degree</li>
    *                                   <li>the number of referenced MBeans in provided value exceeds expected maximum degree </li>
    *                                   <li>one referenced MBean in the value is not an Object of the MBean class expected for that role </li>
    *                                   <li>an MBean provided for that role does not exist</li>
    *                                   </ul>
    */
   public void setRole(String relationId, Role role) throws IllegalArgumentException, RelationServiceNotRegisteredException,
                                                            RelationNotFoundException, RoleNotFoundException, InvalidRoleValueException
   {
      if (relationId == null) throw new IllegalArgumentException("Illegal Null Relation Id.");
      if (role == null) throw new IllegalArgumentException("Illegal Null Role.");
      isActive();
      Relation relation = getRelation(relationId);
      try
      {
         relation.setRole(role);
      }
      catch (RelationTypeNotFoundException e)
      {
         throw new RelationNotFoundException("RelationType not found error: " + e.getMessage());
      }
   }

   /**
    * <p>Sets the given roles in given relation</p>
    * <p>Will check the role according to its corresponding role definition provided in relation's relation type </p>
    * <p>The Relation Service keeps track of the changes to keep the consistency of relations by handling referenced MBean unregistrations</p>
    *
    * @param relationId - the relation identifier
    * @param roleList   - the list of roles to be set
    * @return -  RoleResult object, including a RoleList(for roles succcessfully set) and a RoleUnresolvedList(for roles not set).
    * @throws RelationServiceNotRegisteredException
    *                                   - if the realtionService has not been registered in the mbean server
    * @throws IllegalArgumentException  - if any of the parameters are null
    * @throws RelationNotFoundException - if no relation for the given id has been found
    */
   public RoleResult setRoles(String relationId, RoleList roleList) throws RelationServiceNotRegisteredException,
                                                                           IllegalArgumentException, RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Relation Id is null");
      if (roleList == null) throw new IllegalArgumentException("RoleList is null");
      isActive();
      Relation relation = getRelation(relationId);
      try
      {
         return relation.setRoles(roleList);
      }
      catch (RelationTypeNotFoundException ex)
      {
         throw new RuntimeOperationsException(null, "Unable to find a RelationTypeName for relation ID: " + relationId);
      }
   }

   /**
    * <p>Retrieves MBeans referenced in the various roles of the relation</p>
    *
    * @param relationId - the relation identifier
    * @return - a hashMap mapping as key the MBean ObjectName and an arrayList of String roleNames.
    * @throws IllegalArgumentException  - if relation id is null
    * @throws RelationNotFoundException - if no relation for given relation id
    */
   public Map getReferencedMBeans(String relationId) throws IllegalArgumentException, RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("getting MBeans referenced for ID: " + relationId);
      Relation relation = getRelation(relationId);
      return relation.getReferencedMBeans();
   }

   /**
    * <p>Returns name of associated relation type for given relation</p>
    *
    * @param relationId - the relation identifier
    * @return - the name of the associated relation type
    * @throws IllegalArgumentException  - if null parameter
    * @throws RelationNotFoundException - if no relation for given relation id
    */
   public String getRelationTypeName(String relationId) throws IllegalArgumentException, RelationNotFoundException
   {
      if (relationId == null) throw new IllegalArgumentException("Null Relation Id");
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("getting the relationType for ID: " + relationId);
      Relation relation = getRelation(relationId);
      return relation.getRelationTypeName();
   }

   /**
    * <p>Invoked when a JMX notification occurs. Currently handles notifications for unregistration of MBeans,
    * either referenced in a relation role or being a relation itself.</p>
    *
    * @param notification - the notification needs to be of type MBeanServerNotification
    * @param handback     - An opaque object which helps the listener to associate information regarding the MBean emitter (can be null).
    */
   public void handleNotification(Notification notification, Object handback)
   {
      if (notification == null) throw new IllegalArgumentException("Null Notification");
      if (notification instanceof MBeanServerNotification)
      {
         String notificationType = notification.getType();
         if (notificationType.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
         {
            ObjectName mbeanName = ((MBeanServerNotification)notification).getMBeanName();
            // handle unregistration by purging relations
            handleUnregistration(notification, mbeanName);
            // remove mbeans now
            handleMBeanRemoval(mbeanName);
         }
      }
   }

   private void handleMBeanRemoval(ObjectName mbeanName)
   {
      String relationId = getMBeanObjectName(mbeanName);
      if (relationId != null)
      {
         try
         {
            // we have a relationId now removeRelation
            removeRelation(relationId);
         }
         catch (Exception ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
      }
   }

   private void handleUnregistration(Notification notification, ObjectName objectName)
   {
      boolean isReferenced = false;
      synchronized (m_referencedMBeanObjectNameToRelationIds)
      {
         // check the mbeanObjectName can be found
         if (m_referencedMBeanObjectNameToRelationIds.containsKey(objectName))
         {
            // it does so add it to our list of deregistrations
            synchronized (m_deregisteredNotificationList)
            {
               m_deregisteredNotificationList.add(notification);
            }
            isReferenced = true;
         }
         if (isReferenced && m_purgeFlag)
         {
            try
            {
               // purge the RelationService
               purgeRelations();
            }
            catch (Exception ex)
            {
               throw new RuntimeOperationsException(null, ex.getMessage());
            }
         }
      }
   }

   /**
    * <p>Returns a NotificationInfo object containing the name of the Java class of the notification and the notification types sent</p>
    *
    * @return - the array of possible notifications
    */
   public MBeanNotificationInfo[] getNotificationInfo()
   {
      MBeanNotificationInfo[] notificationInfo = new MBeanNotificationInfo[1];
      String[] notificationTypes = new String[6];
      notificationTypes[0] = RelationNotification.RELATION_BASIC_CREATION;
      notificationTypes[1] = RelationNotification.RELATION_MBEAN_CREATION;
      notificationTypes[2] = RelationNotification.RELATION_BASIC_UPDATE;
      notificationTypes[3] = RelationNotification.RELATION_MBEAN_UPDATE;
      notificationTypes[4] = RelationNotification.RELATION_BASIC_REMOVAL;
      notificationTypes[5] = RelationNotification.RELATION_MBEAN_REMOVAL;

      String notificationDescription = "Sent when a relation is created, updated or deleted.";
      notificationInfo[0] = new MBeanNotificationInfo(notificationTypes, "RelationNotification", notificationDescription);
      return notificationInfo;
   }

   /**
    * <p>Implementation of interface MBeanRegistration @see MBeanRegistration</p>
    * <p>Allows the MBean to perform any operations it needs before being registered in the MBean server. If the name of the MBean is
    * not specified, the MBean can provide a name for its registration. If any exception is raised, the MBean will not be registered
    * in the MBean server.</p>
    *
    * @param server - The MBean server in which the MBean will be registered
    * @param name   - The object name of the MBean. This name is null if the name parameter to one of the createMBean or registerMBean
    *               methods in the @see MBeanServer interface is null. In that case, this method must return a non-null ObjectName for the new MBean.
    * @return - The name under which the MBean is to be registered. This value must not be null. If the name parameter is
    *         not null, it will usually but not necessarily be the returned value
    * @throws Exception - This exception will be caught by the MBean server and re-thrown as an
    * @see javax.management.MBeanRegistrationException or a @see RuntimeMBeanException.
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      m_server = server;
      m_relationServiceObjectName = name == null ? new ObjectName(m_server.getDefaultDomain(), "service", "Relation") : name;
      return m_relationServiceObjectName;
   }

   /**
    * <p>Allows the MBean to perform any operations needed after having been registered in the MBean server or after the registration has failed</p>
    *
    * @param registrationDone - Indicates whether or not the MBean has been successfully registered in the MBean server.
    *                         The value false means that the registration phase has failed
    * @see MBeanRegistration
    */
   public void postRegister(Boolean registrationDone)
   {
      Logger logger = getLogger();
      boolean done = registrationDone.booleanValue();
      if (!done)
      {
         m_server = null;
         logger.warn("Relation service was NOT registered");
      }
      else
      {
         if (logger.isEnabledFor(Logger.DEBUG))
         {
            logger.debug("Relation service postRegistered");
         }
      }
   }

   /**
    * <p>Allows the MBean to perform any operations it needs before being unregistered by the MBean server.
    *
    * @throws Exception - This exception will be caught by the MBean server and re-thrown as an
    * @see MBeanRegistration </p>
    *      <p>The impmentation does nothing but log registration of the relation service</p>
    * @see javax.management.MBeanRegistrationException or a @see RuntimeMBeanException
    */
   public void preDeregister() throws Exception
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Relation service preDeregistered");
      }
   }

   /**
    * <p> logs nothing but log postRegisration</p>
    * <p> Implementation of MBeanRegistration</p>
    */
   public void postDeregister()
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Relation service postDeregistered");
      }
   }

   static void throwRoleProblemException(int problemType, String roleName) throws IllegalArgumentException,
                                                                                  RoleNotFoundException, InvalidRoleValueException
   {
      switch (problemType)
      {
         case RoleStatus.NO_ROLE_WITH_NAME:
            throw new RoleNotFoundException("RoleName: " + roleName + " does not exist in the relation");
         case RoleStatus.ROLE_NOT_READABLE:
            throw new RoleNotFoundException("RoleName: " + roleName + " is not readable");
         case RoleStatus.ROLE_NOT_WRITABLE:
            throw new RoleNotFoundException("RoleName: " + roleName + " is not writable");
         case RoleStatus.LESS_THAN_MIN_ROLE_DEGREE:
            throw new InvalidRoleValueException("RoleName: " + roleName +
                                                " has references less than the expected minimum.");
         case RoleStatus.MORE_THAN_MAX_ROLE_DEGREE:
            throw new InvalidRoleValueException("RoleName: " + roleName +
                                                " has references more than the expected maximum.");
         case RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS:
            throw new InvalidRoleValueException("RoleName: " + roleName +
                                                " has a MBean reference to a MBean not of the expected class of references for that role.");
         case RoleStatus.REF_MBEAN_NOT_REGISTERED:
            throw new InvalidRoleValueException("RoleName: " + roleName +
                                                "  has a reference to null MBean or to a MBean not registered.");
      }
   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   // inner class to represent internal relations defined in the relationService
   final class InternalRelation extends RelationSupport
   {
      InternalRelation(String relationId, ObjectName relationServiceObjectName, String relationTypeName,
                       RoleList roleList) throws InvalidRoleValueException, IllegalArgumentException
      {
         super(relationId, relationServiceObjectName, relationTypeName, roleList);
      }

      // checks role can be read
      int getReadingProblemType(Role role, String roleName, String relationTypeName)
      {
         if (roleName == null) throw new IllegalArgumentException("Null RoleName");
         if (role == null)
            return (RoleStatus.NO_ROLE_WITH_NAME);
         else
         {
            try
            {
               return ((checkRoleReading(roleName, relationTypeName)).intValue());
            }
            catch (RelationTypeNotFoundException ex)
            {
               throw new RuntimeOperationsException(null, ex.getMessage());
            }
         }
      }

      // overridden from RelationSupport
      int getRoleWritingValue(Role role, String relationTypeName, Boolean toBeInitialized) throws RelationTypeNotFoundException
      {
         try
         {   // check role is writable
            return (checkRoleWriting(role, relationTypeName, toBeInitialized)).intValue();
         }
         catch (RelationTypeNotFoundException ex)
         {
            throw new RuntimeOperationsException(null, ex.getMessage());
         }
      }

      // sends role update notification
      void sendUpdateRoleNotification(String relationId, Role role, List oldRoleValue) throws RelationServiceNotRegisteredException,
                                                                                              RelationNotFoundException
      {
         if (relationId == null) throw new IllegalArgumentException("Null RelationId passed into sendUpdateRoleNotification");
         if (role == null) throw new IllegalArgumentException("Null role passed into sendUpdateRoleNotification");
         if (oldRoleValue == null) throw new IllegalArgumentException("Null list of role Values passed into sendUpdateRoleNotification");
         sendRoleUpdateNotification(relationId, role, oldRoleValue);
      }

      void updateRelationServiceMap(String relationId, Role role, List oldRoleValue) throws IllegalArgumentException,
                                                                                            RelationServiceNotRegisteredException, RelationNotFoundException
      {
         if (relationId == null) throw new IllegalArgumentException("Null RelationId passed into updateRelationServiceMap");
         if (role == null) throw new IllegalArgumentException("Null role passed into updateRelationServiceMap");
         if (oldRoleValue == null) throw new IllegalArgumentException("Null list of role Values passed into updateRelationServiceMap");
         updateRoleMap(relationId, role, oldRoleValue);
      }
   }
}