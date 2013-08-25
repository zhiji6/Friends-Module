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
 */
public class InvalidApplicationException extends Exception
{
   private static final long serialVersionUID = -3048022274675537269L;

   /**
    * @serial The invalid value
    */
   private final Object val;

   public InvalidApplicationException(Object value)
   {
      super(String.valueOf(value));
      this.val = value;
   }
}
