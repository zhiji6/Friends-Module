/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;

/**
 * A continuous pulse from client to server that gives the information that
 * the connection is alive and the server is up.
 *
 * @version $Revision: 1.4 $
 */
public interface HeartBeat
{
   /**
    * Starts the heart beat
    *
    * @throws IOException If there are problems contacting the server
    * @see #stop
    */
   public void start() throws IOException;

   /**
    * Stops the heart beat
    *
    * @throws IOException If there are problems contacting the server
    * @see #start
    */
   public void stop() throws IOException;

   /**
    * Returns the period of time in milliseconds between two heart beats
    *
    * @see MX4JRemoteConstants#CONNECTION_HEARTBEAT_PERIOD
    * @see #getMaxRetries
    */
   public long getPulsePeriod();

   /**
    * Returns the maximum number of retries this heart beat attempts after
    * a first connection failure before declaring the connection or the server
    * as dead.
    *
    * @see MX4JRemoteConstants#CONNECTION_HEARTBEAT_RETRIES
    * @see #getPulsePeriod
    */
   public int getMaxRetries();
}
