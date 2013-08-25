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
public class JMException extends Exception
{
   private static final long serialVersionUID = 350520924977331825L;

   public JMException()
   {
   }

   public JMException(String message)
   {
      super(message);
   }
}
