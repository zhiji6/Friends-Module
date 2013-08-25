/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;

/**
 * A set of name-value pairs that allow ModelMBean metadata to have additional information attached to.
 * Descriptor is cloneable since represent a snapshot of what the client passed in to the ModelMBean.
 * This ensures that if the client nulls out some value, the descriptor is still valid, since it has been cloned
 * prior the client modification. The client can always re-set the descriptor on the model mbean.
 *
 * @version $Revision: 1.6 $
 */
public interface Descriptor extends Cloneable, Serializable
{
   /**
    * Returns the value of a given field name.
    *
    * @param fieldName The field name
    * @return Object The value for the given field name. Returns null if not found.
    * @throws RuntimeOperationsException if the value for field name is illegal
    */
   public Object getFieldValue(String fieldName) throws RuntimeOperationsException;

   /**
    * Sets a value for the given field name. The field value will be checked
    * before being set. This will either add a new field or update it if it
    * already exists.
    *
    * @param fieldName  The name of the field
    * @param fieldValue The value for the given field name
    * @throws RuntimeOperationsException If values for fieldName or fieldValue
    *                                    are illegal or the description construction fails
    */
   public void setField(String fieldName, Object fieldValue) throws RuntimeOperationsException;

   /**
    * Removes the named field. If the field is not present, does nothing.
    *
    * @param fieldName The field to be removed.
    */
   public void removeField(String fieldName);

   /**
    * Returns the names of all existing fields. If no fields are present, an empty array is returned.
    */
   public String[] getFieldNames();

   /**
    * Return the values of the specified fields, in order.
    *
    * @param fieldNames The names of the fields
    * @return Object[] The values of the fields
    */
   public Object[] getFieldValues(String[] fieldNames);

   /**
    * Returns the names and values of all existing fields.
    *
    * @return String[] The String array in the format fieldName=fieldValue.
    *         An empty descriptor will result in an empty array returned.
    */
   public String[] getFields();

   /**
    * Sets the given fieldValues for the given fieldNames.
    * The size of both given array should match.
    *
    * @param fieldNames  The names of the fields.
    * @param fieldValues The values of the fields.
    * @throws RuntimeOperationsException if fieldNames or fieldValues contains illegal values.
    */
   public void setFields(String[] fieldNames, Object[] fieldValues) throws RuntimeOperationsException;

   /**
    * Returns a copy of this Descriptor
    */
   public Object clone() throws RuntimeOperationsException;

   /**
    * Returns true when the values for the fields of this Descriptor are valid values, false otherwise.
    *
    * @throws RuntimeOperationsException If the field names or values are illegal
    */
   public boolean isValid() throws RuntimeOperationsException;
}
