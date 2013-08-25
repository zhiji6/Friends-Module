/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.Remote;
import java.util.Map;
import javax.rmi.CORBA.Stub;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.Subject;

/**
 * @version $Revision: 1.8 $
 */
public class RMIIIOPServerImpl extends RMIServerImpl
{
   public RMIIIOPServerImpl(Map env) throws IOException
   {
      super(env);
   }

   protected void export() throws IOException
   {
      PortableRemoteObject.exportObject(this);
   }

   protected String getProtocol()
   {
      return "iiop";
   }

   public Remote toStub() throws IOException
   {
      Remote remote = PortableRemoteObject.toStub(this);
      if (!(remote instanceof Stub)) throw new IOException("Could not find IIOP stub");
      return remote;
   }

   protected RMIConnection makeClient(String connectionId, Subject subject) throws IOException
   {
      RMIConnectionImpl client = new RMIConnectionImpl(this, connectionId, getDefaultClassLoader(), subject, getEnvironment());
      client.setContext(getContext());
      PortableRemoteObject.exportObject(client);
      return client;
   }

   protected void closeClient(RMIConnection client) throws IOException
   {
      PortableRemoteObject.unexportObject(client);
   }

   protected void closeServer() throws IOException
   {
      PortableRemoteObject.unexportObject(this);
   }
}
