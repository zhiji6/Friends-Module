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
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import javax.security.auth.Subject;

/**
 * @version $Revision: 1.10 $
 */
public class RMIJRMPServerImpl extends RMIServerImpl
{
   private final int port;
   private final RMIClientSocketFactory clientFactory;
   private final RMIServerSocketFactory serverFactory;

   public RMIJRMPServerImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map env) throws IOException
   {
      super(env);
      this.port = port;
      this.clientFactory = csf;
      this.serverFactory = ssf;
   }

   protected void export() throws IOException
   {
      UnicastRemoteObject.exportObject(this, port, clientFactory, serverFactory);
   }

   protected String getProtocol()
   {
      return "rmi";
   }

   public Remote toStub() throws IOException
   {
      return RemoteObject.toStub(this);
   }

   protected RMIConnection makeClient(String connectionId, Subject subject) throws IOException
   {
      RMIConnectionImpl client = new RMIConnectionImpl(this, connectionId, getDefaultClassLoader(), subject, getEnvironment());
      client.setContext(getContext());
      UnicastRemoteObject.exportObject(client, port, clientFactory, serverFactory);
      return client;
   }

   protected void closeClient(RMIConnection client) throws IOException
   {
      // The force parameter must be true, since a connector can be closed by the client code.
      // In this case there is a remote call pending (close() itself) and the object will not be exported.
      UnicastRemoteObject.unexportObject(client, true);
   }

   protected void closeServer() throws IOException
   {
      // The force parameter must be true, since a when I close a server I don't want that a pending call
      // to newClient() will avoid to unexport this server.
      UnicastRemoteObject.unexportObject(this, true);
   }
}
