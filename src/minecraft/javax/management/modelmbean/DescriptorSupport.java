/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.modelmbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Revision: 1.31 $
 */
public class DescriptorSupport implements Descriptor
{
   private static final long serialVersionUID = -6292969195866300415L;

   private HashMap descriptor;
   private transient HashMap fields = new HashMap(20);

   public DescriptorSupport()
   {
   }

   public DescriptorSupport(int initNumFields) throws MBeanException, RuntimeOperationsException
   {
      if (initNumFields <= 0)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Number of Fields cannot be <= 0"));
      }
      fields = new HashMap(initNumFields);
   }

   public DescriptorSupport(DescriptorSupport inDescr)
   {
      if (inDescr != null)
      {
         setFields(inDescr.getFieldNames(), inDescr.getFieldValues(inDescr.getFieldNames()));
      }
   }

   public DescriptorSupport(String xml) throws MBeanException, RuntimeOperationsException, XMLParseException
   {
      if (xml == null)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor XML string is null"));
      }

      NodeList fields = documentFromXML(xml).getElementsByTagName("field");
      for (int i = 0; i < fields.getLength(); i++)
      {
         addFieldFromXML(fields.item(i));
      }
   }

   public DescriptorSupport(String[] pairs)
   {
      if (pairs != null && pairs.length != 0)
      {
         for (int i = 0; i < pairs.length; ++i)
         {
            String pair = pairs[i];
            // null or empty strings are to be ignored
            if (pair == null || pair.length() == 0) continue;

            int equal = pair.indexOf('=');
            if (equal < 1)
            {
               throw new RuntimeOperationsException(new IllegalArgumentException("Illegal pair: " + pair));
            }
            else
            {
               String name = pair.substring(0, equal);
               Object value = null;
               if (equal < pair.length() - 1)
               {
                  value = pair.substring(equal + 1);
               }
               setField(name, value);
            }
         }
      }
   }

   public DescriptorSupport(String[] names, Object[] values) throws RuntimeOperationsException
   {
      setFields(names, values);
   }

   public Object clone() throws RuntimeOperationsException
   {
      return new DescriptorSupport(this);
   }

   public Object getFieldValue(String name) throws RuntimeOperationsException
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid field name"));
      }
      // Field names are case insensitive, retrieve the value from the case-insensitive map
      ValueHolder holder = (ValueHolder)fields.get(name.toLowerCase());
      return holder == null ? null : holder.fieldValue;
   }

   public void setField(String name, Object value) throws RuntimeOperationsException
   {
      checkField(name, value);

      // update field but keep the original name if an entry already exists
      String lcase = name.toLowerCase();
      ValueHolder holder = (ValueHolder) fields.get(lcase);
      ValueHolder newHolder = new ValueHolder(holder == null ? name : holder.fieldName, value);
      fields.put(lcase, newHolder);
   }

   public void removeField(String name)
   {
      if (name != null)
      {
         fields.remove(name.toLowerCase());
      }
   }

   public String[] getFieldNames()
   {
      // Preserve the case of field names so use the ones from the values
      String[] names = new String[fields.size()];
      int x = 0;
      for (Iterator i = fields.values().iterator(); i.hasNext();)
      {
         ValueHolder holder = (ValueHolder) i.next();
         names[x++] = holder.fieldName;
      }
      return names;
   }

   public Object[] getFieldValues(String[] names)
   {
      // quick check for empty descriptor (which overrides all)
      if (fields.isEmpty()) return new Object[0];

      if (names == null)
      {
         // All values must be returned
         Object[] list = new Object[fields.size()];
         int x = 0;
         for (Iterator i = fields.values().iterator(); i.hasNext();)
         {
            ValueHolder holder = (ValueHolder) i.next();
            list[x++] = holder.fieldValue;
         }
         return list;
      }

      Object[] list = new Object[names.length];
      for (int i = 0; i < names.length; ++i)
      {
         try
         {
            list[i] = getFieldValue(names[i]);
         }
         catch (RuntimeOperationsException x)
         {
            list[i] = null;
         }
      }
      return list;
   }

   public String[] getFields()
   {
      String[] values = new String[fields.size()];
      StringBuffer buffer = new StringBuffer();
      // Preserve the case of field names
      int x = 0;
      for (Iterator i = fields.values().iterator(); i.hasNext();)
      {
         ValueHolder holder = (ValueHolder) i.next();
         String key = holder.fieldName;
         Object value = holder.fieldValue;
         buffer.setLength(0);
         buffer.append(key);
         buffer.append("=");
         if (value != null)
         {
            if (value instanceof String)
            {
               buffer.append(value.toString());
            }
            else
            {
               buffer.append("(");
               buffer.append(value.toString());
               buffer.append(")");
            }
         }
         values[x++] = buffer.toString();
      }
      return values;
   }

   public void setFields(String[] names, Object[] values) throws RuntimeOperationsException
   {
      if (names == null || values == null || names.length != values.length)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid arguments"));
      }

      for (int i = 0; i < names.length; ++i)
      {
         setField(names[i], values[i]);
      }
   }

   public boolean isValid() throws RuntimeOperationsException
   {
      if (getFieldValue("name") == null || getFieldValue("descriptorType") == null) return false;

      try
      {
         for (Iterator i = fields.values().iterator(); i.hasNext();)
         {
            ValueHolder holder = (ValueHolder) i.next();
            checkField(holder.fieldName, holder.fieldValue);
         }
         return true;
      }
      catch (RuntimeOperationsException x)
      {
         return false;
      }
   }

   public String toXMLString()
   {
      StringBuffer buf = new StringBuffer(32);
      buf.append("<Descriptor>");

      try
      {
         if (fields.size() != 0)
         {
            for (Iterator i = fields.values().iterator(); i.hasNext();
                    )
            {
               ValueHolder holder = (ValueHolder) i.next();
               Object value = holder.fieldValue;
               String valstr = toXMLValueString(value);
               buf.append("<field name=\"");
               buf.append(holder.fieldName);
               buf.append("\" value=\"");
               buf.append(valstr);
               buf.append("\"></field>");
            }
         }
         buf.append("</Descriptor>");
         return buf.toString();
      }
      catch (RuntimeException x)
      {
         throw new RuntimeOperationsException(x);
      }
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      try
      {
         if (fields.size() != 0)
         {
            for (Iterator i = fields.values().iterator(); i.hasNext();)
            {
               ValueHolder holder = (ValueHolder) i.next();
               buf.append(holder.fieldName).append(" ").append(holder.fieldValue);
               if (i.hasNext())
               {
                  buf.append(",");
               }
            }
         }
         return buf.toString();

      }
      catch (RuntimeOperationsException x)
      {
         return buf.toString();
      }
   }

   private void addFieldFromXML(Node n) throws XMLParseException, DOMException, RuntimeOperationsException
   {
      if (!(n instanceof Element))
      {
         throw new XMLParseException("Invalid XML descriptor entity");
      }
      else
      {
         NamedNodeMap attributes = n.getAttributes();
         if (attributes.getLength() != 2
             && (attributes.getNamedItem("name") == null
                 || attributes.getNamedItem("value") == null))
         {
            throw new XMLParseException("Invalid XML descriptor element");
         }
         else
         {
            String name =
                    attributes.getNamedItem("name").getNodeValue();
            String value =
                    attributes.getNamedItem("value").getNodeValue();
            setField(name, parseValueString(value));
         }
      }
   }

   private void checkField(String name, Object value) throws RuntimeOperationsException
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Illegal field name"));
      }

      boolean isValid = true;

      // checks relaxed to match JavaDoc
      if (name.equalsIgnoreCase("name") || name.equalsIgnoreCase("descriptorType"))
      {
         isValid = value instanceof String && ((String)value).length() != 0;
      }
      else if (name.equalsIgnoreCase("class") ||
              name.equalsIgnoreCase("role") ||
              name.equalsIgnoreCase("getMethod") ||
              name.equalsIgnoreCase("setMethod"))
      {
         isValid = value instanceof String;
      }
      else if (name.equalsIgnoreCase("persistPeriod") ||
            name.equalsIgnoreCase("currencyTimeLimit") ||
            name.equalsIgnoreCase("lastUpdatedTimeStamp") ||
            name.equalsIgnoreCase("lastReturnedTimeStamp"))
      {
         if (value instanceof Number)
         {
            isValid = ((Number)value).longValue() >= -1;
         }
         else if (value instanceof String)
         {
            try
            {
               isValid = Long.parseLong((String)value) >= -1;
            } catch (NumberFormatException e)
            {
               isValid = false;
            }
         }
         else
         {
            isValid = false;
         }
      }
      else if (name.equalsIgnoreCase("log"))
      {
         if (value instanceof String)
         {
            String s = (String) value;
            isValid = "t".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s) ||
                  "f".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
         }
         else
         {
            isValid = value instanceof Boolean;
         }
      }
      else if (name.equalsIgnoreCase("visibility"))
      {
         if (value instanceof Number)
         {
            long l = ((Number)value).longValue();
            isValid = l >= 1 && l <= 4;
         }
         else if (value instanceof String)
         {
            try
            {
               long l = Long.parseLong((String)value);
               isValid = l >= 1 && l <= 4;
            } catch (NumberFormatException e)
            {
               isValid = false;
            }
         }
         else
         {
            isValid = false;
         }
      }
      else if (name.equalsIgnoreCase("severity"))
      {
         if (value instanceof Number)
         {
            long l = ((Number)value).longValue();
            isValid = l >= 0 && l <= 6;
         }
         else if (value instanceof String)
         {
            try
            {
               long l = Long.parseLong((String)value);
               isValid = l >= 0 && l <= 6;
            } catch (NumberFormatException e)
            {
               isValid = false;
            }
         }
         else
         {
            isValid = false;
         }
      }
      else if (name.equalsIgnoreCase("persistPolicy"))
      {
         if (value instanceof String)
         {
            String s = (String) value;
            isValid =
                  "OnUpdate".equalsIgnoreCase(s) ||
                  "OnTimer".equalsIgnoreCase(s) ||
                  "NoMoreOftenThan".equalsIgnoreCase(s) ||
                  "Always".equalsIgnoreCase(s) ||
                  "Never".equalsIgnoreCase(s);
         }
         else
         {
            isValid = false;
         }
      }

      if (!isValid)
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid value '" + value + "' for field " + name));
      }
   }

   private Document documentFromXML(String xml) throws XMLParseException
   {
      try
      {
         DocumentBuilder db =
                 DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document d = db.parse(new ByteArrayInputStream(xml.getBytes()));
         return d;
      }
      catch (Exception x)
      {
         throw new XMLParseException(x.toString());
      }
   }

   private Class getObjectValueClass(String value) throws XMLParseException
   {
      int eoc = value.indexOf("/");
      if (eoc == -1)
      {
         throw new XMLParseException("Illegal XML descriptor class name");
      }
      String klass = value.substring(1, eoc);
      Class result = null;
      try
      {
         result = Thread.currentThread().getContextClassLoader().loadClass(klass);
      }
      catch (Exception x)
      {
         throw new XMLParseException(x.toString());
      }
      return result;
   }

   private String getObjectValueString(String value) throws XMLParseException
   {
      int bov = value.indexOf("/");
      if (bov == -1)
      {
         throw new XMLParseException("Illegal XML descriptor object value");
      }
      return value.substring(bov + 1, value.length() - 1);
   }

   private String objectClassToID(Class k)
   {
      StringBuffer result = new StringBuffer();
      result.append(k.getName());
      result.append("/");
      return result.toString();
   }

   private Object parseValueString(String value) throws XMLParseException
   {
      Object result = null;
      if (value.compareToIgnoreCase("(null)") == 0)
      {
         result = null;
      }
      else if (value.charAt(0) != '(')
      {
         result = value;
      }
      else
      {
         result = parseObjectValueString(value);
      }
      return result;
   }

   private Object parseObjectValueString(String value) throws XMLParseException
   {
      if (value.charAt(value.length() - 1) != ')')
      {
         throw new XMLParseException("Invalid XML descriptor value");
      }

      Object result = null;
      Class k = getObjectValueClass(value);
      String s = getObjectValueString(value);
      try
      {
         if (k != Character.class)
         {
            result =
            k.getConstructor(new Class[]{String.class}).newInstance(new Object[]{s});
         }
         else
         {
            result = new Character(s.charAt(0));
         }
      }
      catch (Exception x)
      {
         throw new XMLParseException(x.toString());
      }
      return result;
   }

   private String toXMLValueString(Object value)
   {
      String result;
      if (value == null)
      {
         result = "(null)";
      }
      else
      {
         Class k = value.getClass();
         if (k == String.class && ((String)value).charAt(0) != '(')
         {
            result = (String)value;
         }
         else
         {
            result = toObjectXMLValueString(k, value);
         }
      }
      return result;
   }

   private String toObjectXMLValueString(Class k, Object value)
   {
      StringBuffer result = new StringBuffer();
      result.append("(");
      result.append(objectClassToID(k));
      result.append(value.toString());
      result.append(")");
      return result.toString();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
      HashMap desc = (HashMap) stream.readFields().get("descriptor", null);
      fields = new HashMap(desc.size());
      for (Iterator i = desc.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry)i.next();
         String name = (String)entry.getKey();
         fields.put(name.toLowerCase(), new ValueHolder(name, entry.getValue()));
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      HashMap desc = new HashMap(fields.size());
      for (Iterator i = fields.values().iterator(); i.hasNext();)
      {
         ValueHolder holder = (ValueHolder) i.next();
         desc.put(holder.fieldName, holder.fieldValue);
      }
      ObjectOutputStream.PutField fields = stream.putFields();
      fields.put("descriptor", desc);
      stream.writeFields();
   }

   private static class ValueHolder
   {
      private final String fieldName;
      private final Object fieldValue;

      private ValueHolder(String fieldName, Object value)
      {
         this.fieldName = fieldName;
         this.fieldValue = value;
      }
   }
}
