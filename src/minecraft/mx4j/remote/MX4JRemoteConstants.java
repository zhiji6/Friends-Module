/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;


/**
 * @version $Revision: 1.15 $
 */
public interface MX4JRemoteConstants
{
   /**
    * A vertical bar '|' as mandated by the spec
    */
   public static final String PROVIDER_PACKAGES_SEPARATOR = "|";
   /**
    * MX4J provider packages list for JMXConnector and JMXConnectorServer factories
    */
   public static final String PROVIDER_PACKAGES = "mx4j.remote.provider" + PROVIDER_PACKAGES_SEPARATOR + "mx4j.tools.remote.provider";
   /**
    * The string 'ClientProvider' as mandated by the spec
    */
   public static final String CLIENT_PROVIDER_CLASS = "ClientProvider";
   /**
    * The string 'ServerProvider' as mandated by the spec
    */
   public static final String SERVER_PROVIDER_CLASS = "ServerProvider";


   /**
    * The key that specifies resolver packages, very much like
    * {@link javax.management.remote.JMXConnectorFactory#PROTOCOL_PROVIDER_PACKAGES}
    */
   public static final String PROTOCOL_RESOLVER_PACKAGES = "mx4j.remote.resolver.pkgs";
   /**
    * A vertical bar '|'
    */
   public static final String RESOLVER_PACKAGES_SEPARATOR = PROVIDER_PACKAGES_SEPARATOR;
   /**
    * MX4J provider packages list for {@link mx4j.remote.ConnectionResolver} subclasses
    */
   public static final String RESOLVER_PACKAGES = "mx4j.remote.resolver" + RESOLVER_PACKAGES_SEPARATOR + "mx4j.tools.remote.resolver";
   /**
    * The string 'Resolver'
    */
   public static final String RESOLVER_CLASS = "Resolver";

   /**
    * The reference implementation uses this property to specify the notification fetch timeout (in ms).
    * MX4J will use the same for compatibility. DO NOT CHANGE IT unless the reference implementation changes it.
    */
   public static final String FETCH_NOTIFICATIONS_TIMEOUT = "jmx.remote.x.client.fetch.timeout";
   /**
    * The reference implementation uses this property to specify the maximum number of notification to fetch.
    * MX4J will use the same for compatibility. DO NOT CHANGE IT unless the reference implementation changes it.
    */
   public static final String FETCH_NOTIFICATIONS_MAX_NUMBER = "jmx.remote.x.client.max.notifications";
   /**
    * The reference implementation uses this property to specify the notification buffer size.
    * MX4J will use the same for compatibility. DO NOT CHANGE IT unless the reference implementation changes it.
    */
   public static final String NOTIFICATION_BUFFER_CAPACITY = "jmx.remote.x.buffer.size";
   /**
    * MX4J's implementation uses this property to specify the distance between the lowest expected notification
    * sequence number (sent by the client via fetchNotifications()) and the minimum sequence number of the
    * notification buffer. When this difference is greater than the value of this property, old notifications
    * are eliminated from the notification buffer
    */
   public static final String NOTIFICATION_PURGE_DISTANCE = "jmx.remote.x.notification.purge.distance";
   /**
    * MX4J's implementation uses this property to specify the amount of time (in ms) the client should sleep
    * between notification fetches. A value of 0 means there will be no sleep (fetches will be done one
    * after the other).
    */
   public static final String FETCH_NOTIFICATIONS_SLEEP = "jmx.remote.x.notification.fetch.sleep";

   /**
    * MX4J's implementation uses this property to specify the period (in ms) of the heartbeat pulse for
    * {@link javax.management.remote.JMXConnector JMXConnectors} that use heartbeat to check if the
    * connection with {@link javax.management.remote.JMXConnectorServer JMXConnectorServers} is still alive.
    *
    * @see #CONNECTION_HEARTBEAT_RETRIES
    */
   public static final String CONNECTION_HEARTBEAT_PERIOD = "jmx.remote.x.connection.heartbeat.period";

   /**
    * MX4J's implementation uses this property to specify the number of retries of heartbeat pulses before
    * declaring the connection between a {@link javax.management.remote.JMXConnector JMXConnector} and a
    * {@link javax.management.remote.JMXConnectorServer JMXConnectorServer} failed, at which a
    * {@link javax.management.remote.JMXConnectionNotification notification failed} is emitted.
    *
    * @see #CONNECTION_HEARTBEAT_PERIOD
    */
   public static final String CONNECTION_HEARTBEAT_RETRIES = "jmx.remote.x.connection.heartbeat.retries";

   /**
    * MX4J's implementation uses this property to specify the maximum notification queue size
    * on client size. If set to 0, or not present, the queue will have no limit.
    * Specify this property when the server side is generating notifications at a fast rate,
    * but clients can process them only at a slower rate. In this case notifications will queue
    * up on client side, and if no limit is given to the queue, there is a potential risk of
    * an OutOfMemoryError.
    */
   public static final String NOTIFICATION_QUEUE_CAPACITY = "jmx.remote.x.queue.size";

   /**
    * @deprecated Use {@link mx4j.tools.remote.http.HTTPConnectorServer#WEB_CONTAINER_CONFIGURATION} instead
    */
   public static final String HTTP_SERVER_CONFIGURATION = "jmx.remote.x.http.server.configuration";
}
