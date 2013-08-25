/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.io.IOException;
import java.rmi.MarshalledObject;

/**
 * IMPORTANT: see {@link RMIMarshaller}
 *
 * @version $Revision: 1.3 $
 */
public class Marshaller
{
   public static Object unmarshal(MarshalledObject obj) throws IOException, ClassNotFoundException
   {
      return obj.get();
   }
}
