/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

/**
 * @version $Revision: 1.7 $
 */
// In JMX 1.2 this class will extend JMException
public class InvalidTargetObjectTypeException extends /*JMException*/Exception
{
   private static final long serialVersionUID = 1190536278266811217L;

   private Exception exception;

   public InvalidTargetObjectTypeException()
   {
   }

   public InvalidTargetObjectTypeException(String message)
   {
      super(message);
   }

   public InvalidTargetObjectTypeException(Exception x, String message)
   {
      super(message);
      this.exception = x;
   }
}
