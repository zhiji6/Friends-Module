/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.8 $
 * @see MBeanRegistration
 */
public class MBeanRegistrationException extends MBeanException
{
   private static final long serialVersionUID = 4482382455277067805L;

   public MBeanRegistrationException(Exception x)
   {
      super(x);
   }

   public MBeanRegistrationException(Exception x, String message)
   {
      super(x, message);
   }
}
