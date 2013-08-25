/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import javax.security.auth.Subject;

/**
 * @version $Revision: 1.3 $
 */
public interface JMXAuthenticator
{
   public Subject authenticate(Object credentials) throws SecurityException;
}
