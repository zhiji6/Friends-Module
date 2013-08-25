/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.security.BasicPermission;

/**
 * Permission that MBean class must have in order to be trusted. <p>
 * Only MBeans whose codesource has this permission can be registered in the MBeanServer.
 * This permission is composed by a target name, whose only valid value are
 * <code>register</code> and the wildcard <code>*</code>.
 * The actions are ignored.
 *
 * @version $Revision: 1.5 $
 */
public class MBeanTrustPermission extends BasicPermission
{
   private static final long serialVersionUID = 0xd707c1ae24fd55e4L;

   /**
    * Creates a new MBeanTrustPermission with the specified target name and no actions
    *
    * @param name Can only be "register" or "*"
    */
   public MBeanTrustPermission(String name)
   {
      this(name, null);
   }

   /**
    * Creates a new MBeanTrustPermission with the specified target name and actions, but the actions will be ignored
    *
    * @param name    Can only be "register" or "*"
    * @param actions Ignored
    */
   public MBeanTrustPermission(String name, String actions)
   {
      super(name, actions);
      if (!"register".equals(name) && !"*".equals(name)) throw new IllegalArgumentException("Target name must be 'register' or '*' not '" + name + "'");
   }
}
