/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.io.Serializable;
import javax.management.NotCompliantMBeanException;

/**
 * @version $Revision: 1.7 $
 */
public class RoleInfo implements Serializable
{
   private static final long serialVersionUID = 2504952983494636987L;

   public static int ROLE_CARDINALITY_INFINITY = -1;

   private String name;
   private String description;
   //name of the class of MBean(s) expected to be referenced in corresponding role
   private String referencedMBeanClassName;
   private boolean isWritable;
   private boolean isReadable;
   private int minDegree;
   private int maxDegree;

   public RoleInfo(String roleName, String mbeanClassName, boolean isReadable, boolean isWritable, int minNumber,
                   int maxNumber, String description) throws IllegalArgumentException, InvalidRoleInfoException,
                                                             ClassNotFoundException, NotCompliantMBeanException
   {
      initialize(roleName, mbeanClassName, isReadable, isWritable, minNumber, maxNumber, description);
   }

   public RoleInfo(String roleName, String mbeanClassName, boolean isReadable, boolean isWritable)
           throws IllegalArgumentException, ClassNotFoundException, NotCompliantMBeanException
   {
      try
      {
         // set cardinality to default 1, 1
         initialize(roleName, mbeanClassName, isReadable, isWritable, 1, 1, null);
      }
      catch (InvalidRoleInfoException ignored)
      {
      }
   }

   public RoleInfo(String roleName, String mbeanClassName) throws IllegalArgumentException,
                                                                  ClassNotFoundException, NotCompliantMBeanException
   {
      try
      {
         // read, write and cardinality set default values
         initialize(roleName, mbeanClassName, true, true, 1, 1, null);
      }
      catch (InvalidRoleInfoException ignored)
      {
      }
   }

   public RoleInfo(RoleInfo info) throws IllegalArgumentException
   {
      if (info == null)
      {
         throw new IllegalArgumentException("RoleInfo cannot be null");
      }

      try
      {
         initialize(info.getName(), info.getRefMBeanClassName(), info.isReadable(), info.isWritable(), info.getMinDegree(), info.getMaxDegree(), info.getDescription());
      }
      catch (Exception ignored)
      {
         // The provided role info is valid, no reason why this one would fail
      }
   }

   private void initialize(String roleName, String mbeanClassName, boolean isReadable, boolean isWritable, int minNumber, int maxNumber, String description) throws IllegalArgumentException, InvalidRoleInfoException, ClassNotFoundException, NotCompliantMBeanException
   {
      if (roleName == null) throw new IllegalArgumentException("Null Role name");
      if (mbeanClassName == null) throw new IllegalArgumentException("Null MBean class Name");
      this.name = roleName;
      this.isReadable = isReadable;
      this.isWritable = isWritable;
      this.description = description;
      // check our cardinality is valid ie) Max is not less than Min
      checkValidCardinality(maxNumber, minNumber);
      this.maxDegree = maxNumber;
      this.minDegree = minNumber;
      this.referencedMBeanClassName = mbeanClassName;
   }

   public String getName()
   {
      return name;
   }

   public boolean isReadable()
   {
      return isReadable;
   }

   public boolean isWritable()
   {
      return isWritable;
   }

   public String getDescription()
   {
      return description;
   }

   public int getMinDegree()
   {
      return minDegree;
   }

   public int getMaxDegree()
   {
      return maxDegree;
   }

   public String getRefMBeanClassName()
   {
      return referencedMBeanClassName;
   }

   // return true if the given value is greater than or equal to the expected maximum
   public boolean checkMaxDegree(int maxNumber)
   {
      if (maxNumber >= ROLE_CARDINALITY_INFINITY && (maxDegree == ROLE_CARDINALITY_INFINITY
                                                     || (maxNumber != ROLE_CARDINALITY_INFINITY && maxNumber <= maxDegree)))
      {
         return true;
      }
      return false;
   }

   // returns true if the given value is greater or equal to the expected minimum value
   public boolean checkMinDegree(int minNumber)
   {
      if (minNumber >= ROLE_CARDINALITY_INFINITY &&
          (minDegree == ROLE_CARDINALITY_INFINITY || minNumber >= minDegree))
      {
         return true;
      }
      return false;
   }

   public String toString()
   {
      StringBuffer result = new StringBuffer("Name: ");
      result.append(name);
      result.append("; isReadable: ").append(isReadable);
      result.append("; isWritable: ").append(isWritable);
      result.append("; description: ").append(description);
      result.append("; minimum degree: ").append(minDegree);
      result.append("; maximum degree: ").append(maxDegree);
      result.append("; MBean class: ").append(referencedMBeanClassName);
      return result.toString();
   }

   private void checkValidCardinality(int maxNumber, int minNumber) throws InvalidRoleInfoException
   {
      if (maxNumber != ROLE_CARDINALITY_INFINITY && (minNumber == ROLE_CARDINALITY_INFINITY || minNumber > maxNumber))
      {
         throw new InvalidRoleInfoException("Role cardinality is invalid");
      }
      else if (minNumber < ROLE_CARDINALITY_INFINITY || maxNumber < ROLE_CARDINALITY_INFINITY)
      {
         throw new InvalidRoleInfoException("Role cardinality is invalid");
      }
   }
}