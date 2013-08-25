/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.security.BasicPermission;

/**
 * @version $Revision: 1.3 $
 */
public final class SubjectDelegationPermission extends BasicPermission
{
   public SubjectDelegationPermission(String name)
   {
      super(name);
   }

   public SubjectDelegationPermission(String name, String actions)
   {
      super(name, actions);
      if (actions != null) throw new IllegalArgumentException("The permission's actions must be null");
   }
}
