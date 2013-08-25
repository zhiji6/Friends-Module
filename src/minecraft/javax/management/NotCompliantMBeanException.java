/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Thrown when trying to register an MBean object that is not a compliant JMX MBean.
 *
 * @version $Revision: 1.7 $
 */
public class NotCompliantMBeanException extends OperationsException
{
   private static final long serialVersionUID = 5175579583207963577L;

   public NotCompliantMBeanException()
   {
   }

   public NotCompliantMBeanException(String message)
   {
      super(message);
   }
}
