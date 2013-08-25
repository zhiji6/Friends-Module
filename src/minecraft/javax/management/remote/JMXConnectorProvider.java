/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.IOException;
import java.util.Map;

/**
 * @version $Revision: 1.5 $
 */
public interface JMXConnectorProvider
{
   public JMXConnector newJMXConnector(JMXServiceURL url, Map environment) throws IOException;
}
