/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * <p>The SimpleType class is the open type class whose instances describe all open data values which are neither arrays,
 * nor {@link CompositeData} values, nor {@link TabularData} values. It predefines all its possible instances as static fields,
 * and has no public constructor.</p>
 * <p/>
 * <p>Given a SimpleType instance describing values whose Java class name is className, the internal fields corresponding to the
 * TypeName and description of this SimpleType instance are also set to className. In other words, its methods getClassName, getTypeName
 * and getDescription all return the same string value className.</p>
 *
 * @version $Revision: 1.13 $
 */
public final class SimpleType extends OpenType implements Serializable
{
   private static final long serialVersionUID = 2215577471957694503L;

   // No non-transient fields allowed

   public final static SimpleType BIGDECIMAL;
   public final static SimpleType BIGINTEGER;
   public final static SimpleType BOOLEAN;
   public final static SimpleType BYTE;
   public final static SimpleType CHARACTER;
   public final static SimpleType DOUBLE;
   public final static SimpleType FLOAT;
   public final static SimpleType INTEGER;
   public final static SimpleType LONG;
   public final static SimpleType OBJECTNAME;
   public final static SimpleType SHORT;
   public final static SimpleType STRING;
   public final static SimpleType DATE;
   public final static SimpleType VOID;


   static
   {
/*
         * We need to assign them to temp one by one, or the compiler
         * will complain. We need to assign temp=null even though
         * the exception will not happen.
         */
      SimpleType temp = null;
      try
      {
         temp = new SimpleType("java.math.BigDecimal");
      }
      catch (OpenDataException ignored)
      {
      }
      BIGDECIMAL = temp;
      temp = null;

// BIGINTEGER
      try
      {
         temp = new SimpleType("java.math.BigInteger");
      }
      catch (OpenDataException ignored)
      {
      }
      BIGINTEGER = temp;
      temp = null;

//BOOLEAN
      try
      {
         temp = new SimpleType("java.lang.Boolean");
      }
      catch (OpenDataException ignored)
      {
      }
      BOOLEAN = temp;
      temp = null;

//BYTE
      try
      {
         temp = new SimpleType("java.lang.Byte");
      }
      catch (OpenDataException ignored)
      {
      }
      BYTE = temp;
      temp = null;

//CHARACTER
      try
      {
         temp = new SimpleType("java.lang.Character");
      }
      catch (OpenDataException ignored)
      {
      }
      CHARACTER = temp;
      temp = null;

//DOUBLE
      try
      {
         temp = new SimpleType("java.lang.Double");
      }
      catch (OpenDataException ignored)
      {
      }
      DOUBLE = temp;
      temp = null;

//FLOAT
      try
      {
         temp = new SimpleType("java.lang.Float");
      }
      catch (OpenDataException ignored)
      {
      }
      FLOAT = temp;
      temp = null;

//INTEGER
      try
      {
         temp = new SimpleType("java.lang.Integer");
      }
      catch (OpenDataException ignored)
      {
      }
      INTEGER = temp;
      temp = null;

//LONG
      try
      {
         temp = new SimpleType("java.lang.Long");
      }
      catch (OpenDataException ignored)
      {
      }
      LONG = temp;
      temp = null;

//OBJECTNAME
      try
      {
         temp = new SimpleType("javax.management.ObjectName");
      }
      catch (OpenDataException ignored)
      {
      }
      OBJECTNAME = temp;
      temp = null;

//SHORT
      try
      {
         temp = new SimpleType("java.lang.Short");
      }
      catch (OpenDataException ignored)
      {
      }
      SHORT = temp;
      temp = null;

//STRING
      try
      {
         temp = new SimpleType("java.lang.String");
      }
      catch (OpenDataException ignored)
      {
      }
      STRING = temp;
      temp = null;

//DATE
      try
      {
         temp = new SimpleType("java.util.Date");
      }
      catch (OpenDataException ignored)
      {
      }
      DATE = temp;
      temp = null;

//VOID
      try
      {
         temp = new SimpleType("java.lang.Void");
      }
      catch (OpenDataException ignored)
      {
      }
      VOID = temp;
      temp = null;
   }

   private transient int m_hashCode = 0;

   private SimpleType(String className) throws OpenDataException
   {
      super(className, className, className);
   }


   /**
    * Checks if this <code>SimpleType</code> object is value of
    * the given object
    *
    * @param object The object to check
    * @return boolean
    */
   public boolean isValue(Object object)
   {
      if (object == null) return false;
      return getClassName().equals(object.getClass().getName());
   }

   public Object readResolve() throws ObjectStreamException
   {
      //TODO: need a better way of doing this
      if (getClassName().equals(String.class.getName()))
         return SimpleType.STRING;
      if (getClassName().equals(java.math.BigDecimal.class.getName()))
         return SimpleType.BIGDECIMAL;
      if (getClassName().equals(java.math.BigInteger.class.getName()))
         return SimpleType.BIGINTEGER;
      if (getClassName().equals(Boolean.class.getName()))
         return SimpleType.BOOLEAN;
      if (getClassName().equals(Byte.class.getName()))
         return SimpleType.BYTE;
      if (getClassName().equals(Character.class.getName()))
         return SimpleType.CHARACTER;
      if (getClassName().equals(Double.class.getName()))
         return SimpleType.DOUBLE;
      if (getClassName().equals(Float.class.getName()))
         return SimpleType.FLOAT;
      if (getClassName().equals(Integer.class.getName()))
         return SimpleType.INTEGER;
      if (getClassName().equals(Long.class.getName()))
         return SimpleType.LONG;
      if (getClassName().equals(javax.management.ObjectName.class.getName()))
         return SimpleType.OBJECTNAME;
      if (getClassName().equals(Short.class.getName()))
         return SimpleType.SHORT;
      if (getClassName().equals(Void.class.getName()))
         return SimpleType.VOID;
      if (getClassName().equals(java.util.Date.class.getName()))
         return SimpleType.DATE;

      return null;
   }

   /**
    * Check the given object for equality
    *
    * @return boolean if object is equal
    */
   public boolean equals(Object object)
   {
      if (!(object instanceof SimpleType)) return false;
      SimpleType otherType = (SimpleType)object;
      return (this.getClassName().equals(otherType.getClassName()));
   }

   /**
    * Retrieve the hashCode
    *
    * @return int The computed hasCode
    */
   public int hashCode()
   {
      if (m_hashCode == 0)
      {
         int result = getClassName().hashCode();
         m_hashCode = result;
      }
      return m_hashCode;
   }

   /**
    * Returns a human readable representation of this SimpleType object
    *
    * @return String the String representation
    */
   public String toString()
   {
      return (getClass().getName() + "(name = " + getTypeName() + ")");
   }
}
