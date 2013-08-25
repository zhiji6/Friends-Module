/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.io.IOException;
import java.util.Map;
import javax.management.remote.rmi.RMIConnection;

import mx4j.remote.AbstractHeartBeat;
import mx4j.remote.ConnectionNotificationEmitter;

/**
 * @version $Revision: 1.3 $
 */
public class RMIHeartBeat extends AbstractHeartBeat
{
   private final RMIConnection connection;

   public RMIHeartBeat(RMIConnection connection, ConnectionNotificationEmitter emitter, Map environment)
   {
      super(emitter, environment);
      this.connection = connection;
   }

   protected void pulse() throws IOException
   {
      connection.getDefaultDomain(null);
   }
}
