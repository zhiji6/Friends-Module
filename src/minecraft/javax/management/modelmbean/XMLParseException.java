/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

/**
 * Thrown when invalid XML formatted strings are used to specify Descriptor fields.
 *
 * @version $Revision: 1.3 $
 */
public class XMLParseException extends Exception
{
   private static final long serialVersionUID = 0x2c15c79a5801029dL;

   public XMLParseException()
   {
   }

   public XMLParseException(String message)
   {
      super(message);
   }

   public XMLParseException(Exception x, String message)
   {
      super(message + " - " + x);
   }
}
