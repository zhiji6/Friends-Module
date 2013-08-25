/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.8 $
 * @serial include
 */
class ClassAttributeValueExp extends AttributeValueExp
{
   private static final long serialVersionUID = -1081892073854801359L;

   /**
    * @serial Not used, here only for serialization compatibility
    */
   private String attr;

   private transient MBeanServer server;

   ClassAttributeValueExp()
   {
      super("classname");
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
         String className = server.getObjectInstance(name).getClassName();
         return new StringValueExp(className);
      }
      catch (InstanceNotFoundException ignored)
      {
      }
      throw new BadAttributeValueExpException(null);
   }
}
