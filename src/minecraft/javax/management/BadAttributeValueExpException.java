/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.7 $
 */
public class BadAttributeValueExpException extends Exception
{
   private static final long serialVersionUID = -3105272988410493376L;

   /**
    * @serial The value of the MBean attribute
    */
   private final Object val;

   public BadAttributeValueExpException(Object value)
   {
      super(String.valueOf(value));
      this.val = value;
   }
}
