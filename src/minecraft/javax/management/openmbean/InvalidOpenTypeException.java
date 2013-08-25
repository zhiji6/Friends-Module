/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.openmbean;

import java.io.Serializable;

/**
 * @version $Revision: 1.5 $
 */
public class InvalidOpenTypeException extends IllegalArgumentException implements Serializable
{
   public InvalidOpenTypeException()
   {
   }

   public InvalidOpenTypeException(String s)
   {
      super(s);
   }
}
