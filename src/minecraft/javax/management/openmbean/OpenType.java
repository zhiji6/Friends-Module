/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import mx4j.util.Utils;

/**
 * @version $Revision: 1.12 $
 */
public abstract class OpenType implements Serializable
{
   private static final long serialVersionUID = -9195195325186646468L;

   public static final String[] ALLOWED_CLASSNAMES =
           {
              "java.lang.Void",
              "java.lang.Boolean",
              "java.lang.Byte",
              "java.lang.Character",
              "java.lang.Short",
              "java.lang.Integer",
              "java.lang.Long",
              "java.lang.Float",
              "java.lang.Double",
              "java.lang.String",
              "java.math.BigDecimal",
              "java.math.BigInteger",
              "java.util.Date",
              "javax.management.ObjectName",
              CompositeData.class.getName(),
              TabularData.class.getName()
           };

   private String className = null;
   private String typeName = null;
   private String description = null;

   protected OpenType(String className, String typeName, String description) throws OpenDataException
   {
      initialize(className, typeName, description);
   }

   /**
    * private convenience method for the readObject method to call so that code is not being duplicated in the constructor and the readObject methods
    */
   private void initialize(String className, String typeName, String description) throws OpenDataException
   {
      if (className == null) throw new IllegalArgumentException("null className is invalid");
      if (typeName == null) throw new IllegalArgumentException("null typeName is invalid");
      if (description == null) throw new IllegalArgumentException("null description is invalid");
      if (!(validateClass(className))) throw new OpenDataException("Class does not represent one of the allowed className types");
      this.className = className;
      this.typeName = typeName;
      this.description = description;
   }

   /**
    * private validate method just to do some type checking with regards to that the entered className matches the array of allowed types
    */
   private boolean validateClass(String className)
   {
      /** test we do not have an array if it is it is allowed but it is not listed in the allowed types (be too long) so remove the array
       *  indicator [[[[[L etc and the ; at the end to test if the array type is of the correct class listed above.
       */
      if (className.startsWith("[")) className = className.substring(className.indexOf("L") + 1, (className.length() - 1));

      for (int i = 0; i < ALLOWED_CLASSNAMES.length; i++)
      {
         if (className.equals(ALLOWED_CLASSNAMES[i])) return true;
      }
      return false;
   }

   /**
    * Retrieve the className
    *
    * @return the name of the class
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Retrieve the description
    *
    * @return description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Retrieve the typeName
    *
    * @return typeName
    */
   public String getTypeName()
   {
      return typeName;
   }

   /**
    * check if this instance represents an array or not
    *
    * @return true if the class represents an array, false otherwise
    */
   public boolean isArray()
   {
      Class c = null;
      try
      {
         c = Utils.loadClass(Thread.currentThread().getContextClassLoader(), className);
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
      return c.isArray();
   }

   /**
    * read out object, validate it using the same methods the constructor uses to validate its' invariants
    */
   private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException
   {
      inputStream.defaultReadObject();
      try
      {
         initialize(className, typeName, description);
      }
      catch (OpenDataException e)
      {
         throw new StreamCorruptedException("The object read from the ObjectInputStream during deserialization is not valid");
      }
   }

   public abstract boolean isValue(Object object);

   public abstract boolean equals(Object object);

   public abstract int hashCode();

   public abstract String toString();
}
