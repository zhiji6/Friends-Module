/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * The ValueExp that represent the value of an attribute of a specific MBean.
 *
 * @version $Revision: 1.6 $
 * @serial include
 */
class QualifiedAttributeValueExp extends AttributeValueExp
{
   private static final long serialVersionUID = 8832517277410933254L;

   /**
    * The MBean class name
    */
   private String className;

   private transient MBeanServer server;

   QualifiedAttributeValueExp(String className, String attr)
   {
      super(attr);
      this.className = className;
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      this.server = server;
   }

   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      try
      {
         // getObjectInstance is called for compatibility with the RI;
         // in fact, when used under security manager, calling this method requires
         // the proper permission to call getObjectInstance.
         String cls = server.getObjectInstance(name).getClassName();
         if (cls.equals(className)) return super.apply(name);
      }
      catch (InstanceNotFoundException ignored)
      {
      }
      throw new InvalidApplicationException(className);
   }
}
