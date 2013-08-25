/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import java.io.Serializable;

/**
 * The ArrayType class is the open type class whose instances describe all open data values
 * which are n-dimensional arrays of open data values
 *
 * @version $Revision: 1.13 $
 */
public class ArrayType extends OpenType implements Serializable
{
   private static final long serialVersionUID = 720504429830309770L;

   /**
    * The dimension of the ArrayType
    */
   private int dimension = 0;
   /**
    * The OpenType elemement of this ArrayType
    */
   private OpenType elementType = null;

   //transient fields
   private transient int hashCode = 0;


   /**
    * Constructs an ArrayType instance describing open data values
    * which are arrays with dimension dimension of elements whose
    * open type is elementType.
    *
    * @param dimension   The dimension of this ArrayType and should be greater
    *                    than 0;
    * @param elementType The OpenType element of this ArrayType
    * @throws OpenDataException        if elementType is instance of ArrayType
    * @throws IllegalArgumentException if dimension is less than or zero
    */
   public ArrayType(int dimension, OpenType elementType) throws OpenDataException
   {
      super(createArrayName(elementType, dimension),
            createArrayName(elementType, dimension),
            createDescription(elementType, dimension));

//shouldn't it passed on the first test already?
      if (elementType instanceof ArrayType)
         throw new OpenDataException("elementType can't be instance of ArrayType");

      if (dimension <= 0) throw new IllegalArgumentException("int type dimension must be greater than or equal to 1");


      this.dimension = dimension;
      this.elementType = elementType;

   }

   /**
    * Returns the Dimension described by this ArrayType
    *
    * @return int The dimension
    */
   public int getDimension()
   {
      return dimension;
   }


   /**
    * Returns the OpenType of element values contained in
    * in the arrays described by this ArrayType instance
    *
    * @return OpenType The <code>OpenType</code> instance
    */
   public OpenType getElementOpenType()
   {
      return elementType;
   }


   /**
    * Test whether object is a value for this <code>ArrayType</code> instance.
    *
    * @return boolean True if object is a value
    */
   public boolean isValue(Object object)
   {
      if (object == null || !object.getClass().isArray())
      {
         return false;
      }

      if (elementType instanceof SimpleType)
      {
         return getClassName().equals(object.getClass().getName());
      }

      if (elementType instanceof TabularType || elementType instanceof CompositeType)
      {
         try
         {
            Class elementClass = Thread.currentThread().getContextClassLoader().loadClass(getClassName());
            if (elementClass.isAssignableFrom(object.getClass()))
            {
               return checkElements((Object[]) object, dimension);
            }
         }
         catch (ClassNotFoundException x)
         {
            return false;
         }
      }
      return false;
   }


   /**
    * Check if object is equal with this ArrayType
    *
    * @return true If Equal
    */
   public boolean equals(Object object)
   {
      if (object == null)
         return false;
      if (object == this) return true;

      if (object != null)
      {

         if (object instanceof ArrayType)
         {
            ArrayType checkedType = (ArrayType)object;

            if (checkedType.dimension != dimension)
               return false;
            if (getElementOpenType().equals(checkedType.getElementOpenType()))
               return true;
         }
      }
      return false;
   }


   /**
    * Compute the hashCode of this ArrayType
    *
    * @return int The computed hashCode
    */
   public int hashCode()
   {
      if (hashCode == 0)
      {
         computeHashCode();
      }
      return hashCode;
   }

   /**
    * Format this ArrayType is a String
    *
    * @return String The readable format
    */
   public String toString()
   {

      StringBuffer sb = new StringBuffer();
      sb.append(elementType.getClassName());
      sb.append("(typename=");
      sb.append(getTypeName());
      sb.append(",dimension=");
      sb.append("" + dimension);
      sb.append(",elementType=");
      sb.append(elementType.toString());
      sb.append(")");

      return sb.toString();

   }

   /**
    * Returns a Description
    */
   private static String createDescription(OpenType type, int size)
   {
      StringBuffer sb = new StringBuffer("" + size);
      sb.append("-dimension array of ");
      sb.append(type.getClassName());
      return sb.toString();

   }

   private static String createArrayName(OpenType type, int size)
   {
      if (size <= 0) throw new IllegalArgumentException("int type dimension must be greater than or equal to 1");
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < size; i++)
         sb.append("[");
      sb.append("L");
      sb.append(type.getClassName());
      sb.append(";");
      return sb.toString();
   }

   private void computeHashCode()
   {
      hashCode = (dimension + elementType.hashCode());
   }

   private boolean checkElements(Object[] array, int dim) {
      if (dim == 1)
      {
         OpenType arrayType = getElementOpenType();
         for (int i = 0; i < array.length; i++)
         {
            Object o = array[i];
            if (o != null && !arrayType.isValue(o))
            {
               return false;
            }
         }
         return true;
      } else {
         for (int i = 0; i < array.length; i++)
         {
            Object o = array[i];
            if (o != null && !checkElements((Object[])o, dim-1))
            {
               return false;
            }
         }
         return true;
      }
   }
}
