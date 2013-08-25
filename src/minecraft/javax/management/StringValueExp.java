/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * The ValueExp that represents a string.
 *
 * @version $Revision: 1.6 $
 */
public class StringValueExp implements ValueExp
{
   private static final long serialVersionUID = -3256390509806284044L;

   /**
    * @serial The string
    */
   private String val;

   /**
    * Creates a new StringValueExp with a null string
    */
   public StringValueExp()
   {
      this(null);
   }

   /**
    * Creates a new StringValueExp with the given string
    */
   public StringValueExp(String value)
   {
      this.val = value;
   }

   /**
    * Returns the string represented by this instance
    */
   public String getValue()
   {
      return val;
   }

   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      return this;
   }

   public void setMBeanServer(MBeanServer server)
   {
      // Not needed
   }

   public String toString()
   {
      return val;
   }
}
