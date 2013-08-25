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
 */
public class AttributeChangeNotification extends Notification
{
   private static final long serialVersionUID = 535176054565814134L;

   public static final String ATTRIBUTE_CHANGE = "jmx.attribute.change";

   /**
    * @serial The attribute's name
    */
   private final String attributeName;
   /**
    * @serial The attribute's type
    */
   private final String attributeType;
   /**
    * @serial The attribute's old value
    */
   private final Object oldValue;
   /**
    * @serial The attribute's new value
    */
   private final Object newValue;

   public AttributeChangeNotification(Object source, long sequenceNumber, long timestamp, String message, String attributeName, String attributeType, Object oldValue, Object newValue)
   {
      super(ATTRIBUTE_CHANGE, source, sequenceNumber, timestamp, message);
      this.attributeName = attributeName;
      this.attributeType = attributeType;
      this.oldValue = oldValue;
      this.newValue = newValue;
   }

   public String getAttributeName()
   {
      return attributeName;
   }

   public String getAttributeType()
   {
      return attributeType;
   }

   public Object getOldValue()
   {
      return oldValue;
   }

   public Object getNewValue()
   {
      return newValue;
   }
}
