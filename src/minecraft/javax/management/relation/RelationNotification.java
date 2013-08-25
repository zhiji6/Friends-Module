/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.Notification;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.9 $
 */
public class RelationNotification extends Notification
{
   private static final long serialVersionUID = -6871117877523310399L;

   public static final String RELATION_BASIC_CREATION = "jmx.relation.creation.basic";
   public static final String RELATION_MBEAN_CREATION = "jmx.relation.creation.mbean";
   public static final String RELATION_BASIC_REMOVAL = "jmx.relation.removal.basic";
   public static final String RELATION_MBEAN_REMOVAL = "jmx.relation.removal.mbean";
   public static final String RELATION_BASIC_UPDATE = "jmx.relation.update.basic";
   public static final String RELATION_MBEAN_UPDATE = "jmx.relation.update.mbean";

   private String relationId;
   private String relationTypeName;
   private String roleName;
   private ObjectName relationObjName;
   private List unregisterMBeanList;
   private List oldRoleValue;
   private List newRoleValue;

   /**
    * Constructor used when creating or removing a relation
    */
   public RelationNotification(String createRemoveType, Object source, long sequenceNumber, long timestamp, String message,
                               String relationId, String relationTypeName, ObjectName relationObjectName, List unregisteredMBeanList) throws IllegalArgumentException
   {
      super(createRemoveType, source, sequenceNumber, timestamp, message);
      // checks if the type is one of Creation or removal of an MBean or a Relation
      checkCreateRemoveType(createRemoveType);
      this.relationId = relationId;
      this.relationTypeName = relationTypeName;
      this.relationObjName = relationObjectName;
      setUnregisterMBeanList(unregisteredMBeanList);
   }

   /**
    * Constructor used when updating a Relation
    */
   public RelationNotification(String updateType, Object source, long sequenceNumber, long timestamp, String message,
                               String relationId, String relationTypeName, ObjectName relationObjectName,
                               String roleName, List newRoleValues, List oldRoleValues) throws IllegalArgumentException
   {
      super(updateType, source, sequenceNumber, timestamp, message);
      // checks for an MBean or relation update
      checkUpdateType(updateType);
      this.relationId = relationId;
      this.relationTypeName = relationTypeName;
      this.relationObjName = relationObjectName;
      this.roleName = roleName;
      setOldRoleValues(oldRoleValues);
      setNewRoleValues(newRoleValues);
   }

   private void setOldRoleValues(List list)
   {
      if (list != null)
      {
         if (oldRoleValue == null)
         {
            oldRoleValue = new ArrayList();
         }
         oldRoleValue.clear();
         oldRoleValue.addAll(list);
      }
   }

   private void setNewRoleValues(List list)
   {
      if (list != null)
      {
         if (newRoleValue == null)
         {
            newRoleValue = new ArrayList();
         }
         newRoleValue.clear();
         newRoleValue.addAll(list);
      }
   }

   private void setUnregisterMBeanList(List list)
   {
      if (list != null)
      {
         if (unregisterMBeanList == null)
         {
            unregisterMBeanList = new ArrayList();
         }
         unregisterMBeanList.clear();
         unregisterMBeanList.addAll(list);
      }
   }

   public String getRelationId()
   {
      return relationId;
   }

   public String getRelationTypeName()
   {
      return relationTypeName;
   }

   public ObjectName getObjectName()
   {
      return relationObjName;
   }

   public List getMBeansToUnregister()
   {
      if (unregisterMBeanList != null)
      {
         // Cannot clone, since I'm not sure which type of list the data member is.
         return new ArrayList(unregisterMBeanList);
      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }

   public List getNewRoleValue()
   {
      if (newRoleValue != null)
      {
         // Cannot clone, since I'm not sure which type of list the data member is.
         return new ArrayList(newRoleValue);
      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }

   public List getOldRoleValue()
   {
      if (oldRoleValue != null)
      {
         // Cannot clone, since I'm not sure which type of list the data member is.
         return new ArrayList(oldRoleValue);
      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }

   public String getRoleName()
   {
      return roleName;
   }

   private void checkCreateRemoveType(String type) throws IllegalArgumentException
   {
      if (!(type.equals(RelationNotification.RELATION_BASIC_CREATION))
          && (!(type.equals(RelationNotification.RELATION_MBEAN_CREATION)))
          && (!(type.equals(RelationNotification.RELATION_BASIC_REMOVAL)))
          && (!(type.equals(RelationNotification.RELATION_MBEAN_REMOVAL))))
      {
         throw new IllegalArgumentException("Notification type is not recognized must be one of create or remove");
      }
   }

   private void checkUpdateType(String type) throws IllegalArgumentException
   {
      if (!(type.equals(RelationNotification.RELATION_BASIC_UPDATE)) && (!(type.equals(RelationNotification.RELATION_MBEAN_UPDATE))))
      {
         throw new IllegalArgumentException("Notification type is not recognized must be one of update");
      }
   }
}