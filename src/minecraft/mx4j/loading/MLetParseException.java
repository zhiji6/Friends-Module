/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.loading;

/**
 * Thrown when a problem parsing MLet files is encountered
 *
 * @version $Revision: 1.5 $
 */
public class MLetParseException extends Exception
{
   public MLetParseException()
   {
   }

   public MLetParseException(String message)
   {
      super(message);
   }
}
