/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @version $Revision: 1.11 $
 */
public class AttributeValueExp implements ValueExp
{
   private static final long serialVersionUID = -7768025046539163385L;

   /**
    * @serial The name of the attribute
    */
   private String attr;

   private transient MBeanServer server;

   /**
    * @deprecated Must not be used
    */
   public AttributeValueExp()
   {
   }

   /**
    * Creates a new AttributeValueExp with the given attribute's name, that will be
    * used to retrieve the attribute's value used by this ValueExp
    *
    * @param attr The name of the MBean attribute
    */
   public AttributeValueExp(String attr)
   {
      this.attr = attr;
   }

   /**
    * Returns the name of the MBean attribute whose value is used by this ValueExp
    */
   public String getAttributeName()
   {
      return attr;
   }

   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      try
      {
         Object value = getAttribute(name);
         if (value == null)
         {
            // Must understand what to return
            return createValueExp(name);
         }
         else if (value instanceof Number)
         {
            return new NumericValueExp((Number)value);
         }
         else if (value instanceof Boolean)
         {
            return new BooleanValueExp(((Boolean)value).booleanValue());
         }
         else if (value instanceof String)
         {
            return new StringValueExp((String)value);
         }
         else
         {
            throw new BadAttributeValueExpException(value);
         }
      }
      catch (RuntimeOperationsException x)
      {
         throw new BadAttributeValueExpException(getAttributeName());
      }
   }

   public void setMBeanServer(MBeanServer server)
   {
      this.server = server;
   }

   protected Object getAttribute(ObjectName name)
   {
      try
      {
         return server.getAttribute(name, getAttributeName());
      }
      catch (MBeanException x)
      {
      }
      catch (AttributeNotFoundException x)
      {
      }
      catch (InstanceNotFoundException x)
      {
      }
      catch (ReflectionException x)
      {
      }
      throw new RuntimeOperationsException(null);
   }

   private ValueExp createValueExp(final ObjectName name) throws BadAttributeValueExpException
   {
      try
      {
         MBeanInfo info = (MBeanInfo)AccessController.doPrivileged(new PrivilegedExceptionAction()
         {
            public Object run() throws Exception
            {
               return server.getMBeanInfo(name);
            }
         });

         MBeanAttributeInfo[] attrs = info.getAttributes();
         for (int i = 0; i < attrs.length; ++i)
         {
            MBeanAttributeInfo attribute = attrs[i];
            if (attribute.getName().equals(getAttributeName()))
            {
               String type = attribute.getType();
               if (type.equals("java.lang.String"))
               {
                  return new StringValueExp(null);
               }
               else
               {
                  // If an attribute is meant to return a boolean or a number, and returns null,
                  // then the MBean programmer is dumb and we tell him so.
                  throw new BadAttributeValueExpException(null);
               }
            }
         }
      }
      catch (PrivilegedActionException x)
      {
      }
      throw new BadAttributeValueExpException(null);
   }
}
