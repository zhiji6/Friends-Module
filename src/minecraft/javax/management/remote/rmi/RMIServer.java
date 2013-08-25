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
import java.rmi.RemoteException;

/**
 * @version $Revision: 1.6 $
 */
public interface RMIServer extends Remote
{
   public String getVersion() throws RemoteException;

   public RMIConnection newClient(Object credentials) throws IOException, SecurityException;
}
