/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.DomainCombiner;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.remote.SubjectDelegationPermission;
import javax.security.auth.AuthPermission;
import javax.security.auth.Policy;
import javax.security.auth.Subject;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.19 $
 */
public class MX4JRemoteUtils
{
   private static int connectionNumber;

   /**
    * Returns a copy of the given Map that does not contain non-serializable entries
    */
   public static Map removeNonSerializableEntries(Map map)
   {
      Map newMap = new HashMap(map.size());
      for (Iterator i = map.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry)i.next();
         if (isSerializable(entry)) newMap.put(entry.getKey(), entry.getValue());
      }
      return newMap;
   }

   private static boolean isSerializable(Object object)
   {
      if (object instanceof Map.Entry) return isSerializable(((Map.Entry)object).getKey()) && isSerializable(((Map.Entry)object).getValue());
      if (object == null) return true;
      if (object instanceof String) return true;
      if (object instanceof Number) return true;
      if (!(object instanceof Serializable)) return false;

      return isTrulySerializable(object);
   }

   public static boolean isTrulySerializable(Object object)
   {
      // Give up and serialize the object
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(object);
         oos.close();
         return true;
      }
      catch (IOException ignored)
      {
      }
      return false;
   }

   public static String createConnectionID(String protocol, String callerAddress, int callerPort, Subject subject)
   {
      // See JSR 160 specification at javax/management/remote/package-summary.html

      StringBuffer buffer = new StringBuffer(protocol);
      buffer.append(':');
      if (callerAddress != null) buffer.append("//").append(callerAddress);
      if (callerPort >= 0) buffer.append(':').append(callerPort);
      buffer.append(' ');

      if (subject != null)
      {
         Set principals = subject.getPrincipals();
         for (Iterator i = principals.iterator(); i.hasNext();)
         {
            Principal principal = (Principal)i.next();
            String name = principal.getName();
            name = name.replace(' ', '_');
            buffer.append(name);
            if (i.hasNext()) buffer.append(';');
         }
      }
      buffer.append(' ');

      buffer.append("0x").append(Integer.toHexString(getNextConnectionNumber()).toUpperCase());

      return buffer.toString();
   }

   private static synchronized int getNextConnectionNumber()
   {
      return ++connectionNumber;
   }

   private static Logger getLogger()
   {
      return Log.getLogger(MX4JRemoteUtils.class.getName());
   }

   public static Object subjectInvoke(Subject subject, Subject delegate, AccessControlContext context, Map environment, PrivilegedExceptionAction action) throws Exception
   {
      if (delegate != null)
      {
         if (subject == null) throw new SecurityException("There is no authenticated subject to delegate to");
         checkSubjectDelegationPermission(delegate, getSubjectContext(subject, null, context, environment));
      }

      Logger logger = getLogger();

      // If there is no authenticated subject, I leave the transport library to perform its job.
      // In the RMIConnectorServer, the context at start() time is used by the RMI runtime to
      // restrict permissions via a doPrivileged() call.
      // In HTTP JMXConnectorServer, it's the HTTP server responsibility to give such semantic,
      // if it wants to.
      // Here, I just execute the action and trust the transport library to do its job right.
      if (subject == null)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("No authenticated subject, invoking action without using Subject.doAs");
         return action.run();
      }

      // The precedent stack frames have normally AllPermission, since - for example in RMI - they
      // are JDK domains or JMX/MX4J domains. Below I take the context, and I
      // inject the JSR 160 domain with the authenticated Subject, then call Subject.doAsPrivileged()
      // with, eventually, the delegate Subject.
      // Must call Subject.doAs, since anyone down in the stack call can call Subject.getSubject()
      // and expect to get the Subject or the delegate, even in absence of the SecurityManager
      try
      {
         if (delegate == null)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Invoking Subject.doAs using authenticated subject " + subject);
            return Subject.doAsPrivileged(subject, action, getSubjectContext(subject, delegate, context, environment));
         }
         else
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Invoking Subject.doAs using delegate subject " + delegate);
            return Subject.doAsPrivileged(delegate, action, getSubjectContext(subject, delegate, context, environment));
         }
      }
      catch (PrivilegedActionException x)
      {
         throw x.getException();
      }
   }

   private static void checkSubjectDelegationPermission(final Subject delegate, AccessControlContext context) throws SecurityException
   {
      Logger logger = getLogger();

      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("No SecurityManager, skipping Subject delegation permission check");
         return;
      }

      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            StringBuffer buffer = new StringBuffer();
            Set principals = delegate.getPrincipals();
            for (Iterator i = principals.iterator(); i.hasNext();)
            {
               Principal principal = (Principal)i.next();
               buffer.setLength(0);
               String permission = buffer.append(principal.getClass().getName()).append(".").append(principal.getName()).toString();
               AccessController.checkPermission(new SubjectDelegationPermission(permission));
            }
            return null;
         }
      }, context);
   }

   /**
    * Returns a suitable AccessControlContext that restricts access in a {@link Subject#doAsPrivileged} call
    * based on the current JAAS authorization policy, and combined with the given context.
    * <br/>
    * This is needed because the server stack frames in a call to a JMXConnectorServer are,
    * for example for RMI, like this:
    * <pre>
    * java.lang.Thread.run()
    *   [rmi runtime classes]
    *     javax.management.remote.rmi.RMIConnectionImpl
    *       [mx4j JSR 160 implementation code]
    *         javax.security.auth.Subject.doAsPrivileged()
    *           [mx4j JSR 160 implementation code]
    *             [mx4j JSR 3 implementation code]
    *               java.lang.SecurityManager.checkPermission()
    * </pre>
    * All protection domains in this stack frames have AllPermission, normally, so that when the JMX implementation
    * checks for permissions, it will always pass the check.
    * <br/>
    * One solution would be to use a doPrivileged() call with a restricting context (normally created at the start()
    * of the connector server), but this forces to grant to the code that starts the connector server all the
    * permissions needed by clients, and furthermore, grants to clients the permissions needed to start the connector
    * server.
    * <br/>
    * Therefore, a "special" ProtectionDomain will be injected in the AccessControlContext returned by this method.
    * This special ProtectionDomain will have a CodeSource with null location and the principals specified by the
    * subject passed as argument.
    * <br/>
    * The "injection" of this synthetic ProtectionDomain allows to give AllPermission to the JSR 3 and 160 classes
    * and implementation, but still have the possibility to specify a JAAS policy with MBeanPermissions in this way:
    * <pre>
    * grant principal javax.management.remote.JMXPrincipal "mx4j"
    * {
    *    permission javax.management.MBeanPermission "*", "getAttribute";
    * };
    * </pre>
    * MX4J also offer an alternative implementation that checks if the given context has a
    * {@link SubjectDelegationPermission} for the given subject; if so, the policy configuration is much simpler
    * since does not require that the context has all the possible permissions needed by code down the stack.
    * This also allows to specify separately the permissions to start the connector server
    * and the permissions needed by clients.
    */
   private static AccessControlContext getSubjectContext(final Subject subject, Subject delegate, final AccessControlContext context, Map environment)
   {
      final Logger logger = getLogger();

      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("No security manager, injecting JSR 160 domain only");
         // Just return the injected domain, to allow Subject.getSubject() return correct values
         InjectingDomainCombiner combiner = new InjectingDomainCombiner(delegate != null ? delegate : subject);
         return new AccessControlContext(new ProtectionDomain[]{combiner.getInjectedProtectionDomain()});
      }

      // Check if the caller can delegate to a subject
      boolean combine = ((Boolean)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            try
            {
               // Here use the authenticated subject, not the delegate
               checkSubjectDelegationPermission(subject, context);
               if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Check for SubjectDelegationPermission passed, avoiding security domains combination");
               return Boolean.FALSE;
            }
            catch (AccessControlException x)
            {
               if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Check for SubjectDelegationPermission not passed, combining security domains");
               return Boolean.TRUE;
            }
         }
      }, context)).booleanValue();

      if (combine)
      {
         final InjectingDomainCombiner combiner = new InjectingDomainCombiner(delegate != null ? delegate : subject);
         AccessControlContext acc = (AccessControlContext)AccessController.doPrivileged(new PrivilegedAction()
         {
            public Object run()
            {
               return new AccessControlContext(context, combiner);
            }
         });
         AccessController.doPrivileged(new PrivilegedAction()
         {
            public Object run()
            {
               try
               {
                  // Check this permission, that is required anyway, to combine the domains
                  AccessController.checkPermission(new AuthPermission("doAsPrivileged"));
               }
               catch (AccessControlException ignored)
               {
               }
               return null;
            }
         }, acc);
         ProtectionDomain[] combined = combiner.getCombinedDomains();
         return new AccessControlContext(combined);
      }
      else
      {
         InjectingDomainCombiner combiner = new InjectingDomainCombiner(delegate != null ? delegate : subject);
         return new AccessControlContext(new ProtectionDomain[]{combiner.getInjectedProtectionDomain()});
      }
   }

   private static class InjectingDomainCombiner implements DomainCombiner
   {
      private static Constructor domainConstructor;

      static
      {
         try
         {
            domainConstructor = ProtectionDomain.class.getConstructor(new Class[]{CodeSource.class, PermissionCollection.class, ClassLoader.class, Principal[].class});
         }
         catch (Exception x)
         {
         }
      }

      private ProtectionDomain domain;
      private ProtectionDomain[] combined;

      public InjectingDomainCombiner(Subject subject)
      {
         if (domainConstructor != null)
         {
            Principal[] principals = (Principal[])subject.getPrincipals().toArray(new Principal[0]);
            try
            {
               Object[] args = new Object[]{new CodeSource((URL)null, (Certificate[])null), null, null, principals};
               domain = (ProtectionDomain)domainConstructor.newInstance(args);
            }
            catch (Exception x)
            {
            }
         }

         if (domain == null)
         {
            // This is done for JDK 1.3 compatibility.
            domain = new SubjectProtectionDomain(new CodeSource((URL)null, (Certificate[])null), subject);
         }
      }

      public ProtectionDomain getInjectedProtectionDomain()
      {
         return domain;
      }

      public ProtectionDomain[] combine(ProtectionDomain[] current, ProtectionDomain[] assigned)
      {
         ProtectionDomain[] result = null;

         if (current == null || current.length == 0)
         {
            if (assigned == null || assigned.length == 0)
            {
               result = new ProtectionDomain[1];
            }
            else
            {
               result = new ProtectionDomain[assigned.length + 1];
               System.arraycopy(assigned, 0, result, 1, assigned.length);
            }
         }
         else
         {
            if (assigned == null || assigned.length == 0)
            {
               result = new ProtectionDomain[current.length + 1];
               System.arraycopy(current, 0, result, 1, current.length);
            }
            else
            {
               result = new ProtectionDomain[current.length + assigned.length + 1];
               System.arraycopy(current, 0, result, 1, current.length);
               System.arraycopy(assigned, 0, result, current.length + 1, assigned.length);
            }
         }

         result[0] = domain;
         this.combined = result;

         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.TRACE))
         {
            logger.trace("Security domains combination");
            logger.trace("Current domains");
            logger.trace(dumpDomains(current));
            logger.trace("Assigned domains");
            logger.trace(dumpDomains(assigned));
            logger.trace("Combined domains");
            logger.trace(dumpDomains(result));
         }

         return result;
      }

      private String dumpDomains(ProtectionDomain[] domains)
      {
         if (domains == null) return "null";
         StringBuffer buffer = new StringBuffer();
         for (int i = domains.length - 1; i >= 0; --i)
         {
            int k = domains.length - 1 - i;
            while (k-- > 0) buffer.append("  ");
            buffer.append(domains[i].getCodeSource().getLocation());
            // Only work in JDK 1.4
//            buffer.append(" - ");
//            buffer.append(java.util.Arrays.asList(domains[i].getPrincipals()));
            buffer.append("\n");
         }
         return buffer.toString();
      }

      public ProtectionDomain[] getCombinedDomains()
      {
         return combined;
      }

      private static class SubjectProtectionDomain extends ProtectionDomain
      {
         private final Subject subject;

         public SubjectProtectionDomain(CodeSource codesource, Subject subject)
         {
            super(codesource, null);
            this.subject = subject;
         }

         public boolean implies(Permission permission)
         {
            Policy policy = (Policy)AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return Policy.getPolicy();
               }
            });
            PermissionCollection permissions = policy.getPermissions(subject, getCodeSource());
            return permissions.implies(permission);
         }
      }
   }
}
