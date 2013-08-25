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
public class BadBinaryOpValueExpException extends Exception
{
   private static final long serialVersionUID = 5068475589449021227L;

   /**
    * @serial The irregular expression
    */
   private final ValueExp exp;

   public BadBinaryOpValueExpException(ValueExp exp)
   {
      this.exp = exp;
   }

   public ValueExp getExp()
   {
      return exp;
   }
}
