/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

/**
 * @version $Revision: 1.4 $
 */
public class RoleStatus
{
   public static final int NO_ROLE_WITH_NAME = 1;
   public static final int ROLE_NOT_READABLE = 2;
   public static final int ROLE_NOT_WRITABLE = 3;
   public static final int LESS_THAN_MIN_ROLE_DEGREE = 4;
   public static final int MORE_THAN_MAX_ROLE_DEGREE = 5;
   public static final int REF_MBEAN_OF_INCORRECT_CLASS = 6;
   public static final int REF_MBEAN_NOT_REGISTERED = 7;

   public static boolean isRoleStatus(int roleStatusType)
   {
      if (roleStatusType != NO_ROLE_WITH_NAME && roleStatusType != ROLE_NOT_READABLE
          && roleStatusType != ROLE_NOT_WRITABLE && roleStatusType != LESS_THAN_MIN_ROLE_DEGREE
          && roleStatusType != MORE_THAN_MAX_ROLE_DEGREE
          && roleStatusType != REF_MBEAN_OF_INCORRECT_CLASS
          && roleStatusType != REF_MBEAN_NOT_REGISTERED)
      {
         return false;
      }
      return true;
   }
}