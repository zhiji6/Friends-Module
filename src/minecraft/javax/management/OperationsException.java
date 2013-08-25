/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Thrown when an Exception is thrown by the MBeanServer when executing MBeanServer methods.
 *
 * @version $Revision: 1.8 $
 */
public class OperationsException extends JMException
{
   private static final long serialVersionUID = -4967597595580536216L;

   public OperationsException()
   {
   }

   public OperationsException(String message)
   {
      super(message);
   }
}
