/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import mx4j.MX4JSystemKeys;
import mx4j.util.Utils;

/**
 * @version $Revision: 1.26 $
 */
public class ObjectName implements QueryExp, Serializable
{
   private static final long serialVersionUID = 1081892073854801359L;

   private static final boolean cacheEnabled;
   private static final WeakObjectNameCache cache;

   static
   {
      String enableCache = (String)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty(MX4JSystemKeys.MX4J_OBJECTNAME_CACHING);
         }
      });
      if (enableCache != null)
      {
         cacheEnabled = Boolean.valueOf(enableCache).booleanValue();
      }
      else
      {
         // Cache is on by default
         cacheEnabled = true;
      }
      if (cacheEnabled)
      {
         cache = new WeakObjectNameCache();
      }
      else
      {
         cache = null;
      }
   }

   private transient String propertiesString;
   private transient boolean isPropertyPattern;
   private transient boolean isDomainPattern;
   private transient String canonicalName;

   public ObjectName(String name) throws NullPointerException, MalformedObjectNameException
   {
      if (name == null) throw new NullPointerException("ObjectName 'name' parameter can't be null");
      if (name.length() == 0) name = "*:*";
      parse(name);
   }

   public ObjectName(String domain, Hashtable table) throws NullPointerException, MalformedObjectNameException
   {
      if (domain == null) throw new NullPointerException("ObjectName 'domain' parameter can't be null");
      if (table == null) throw new NullPointerException("ObjectName 'table' parameter can't be null");
      if (!isDomainValid(domain)) throw new MalformedObjectNameException("Invalid domain: " + domain);
      if (table.isEmpty()) throw new MalformedObjectNameException("Properties table cannot be empty");

      for (Iterator i = table.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry)i.next();
         String key = entry.getKey().toString();
         if (!isKeyValid(key)) throw new MalformedObjectNameException("Invalid key: " + key);
         Object value = entry.getValue();
         if (!(value instanceof String)) throw new MalformedObjectNameException("Property values must be Strings");
         String strvalue = value.toString();
         if (!isValueValid(strvalue)) throw new MalformedObjectNameException("Invalid value: " + strvalue);
      }

      init(domain, convertPropertiesToString(new TreeMap(table)), table);
   }

   public ObjectName(String domain, String key, String value) throws NullPointerException, MalformedObjectNameException
   {
      if (domain == null) throw new NullPointerException("ObjectName 'domain' parameter can't be null");
      if (key == null) throw new NullPointerException("ObjectName 'key' parameter can't be null");
      if (value == null) throw new NullPointerException("ObjectName 'value' parameter can't be null");
      if (!isDomainValid(domain)) throw new MalformedObjectNameException("Invalid domain: " + domain);
      if (!isKeyValid(key)) throw new MalformedObjectNameException("Invalid key: " + key);
      if (!isValueValid(value)) throw new MalformedObjectNameException("Invalid value: " + value);

      Map table = new HashMap();
      table.put(key, value);
      init(domain, convertPropertiesToString(table), table);
   }

   public boolean apply(ObjectName name) throws NullPointerException
   {
      boolean result = false;

      if (name.isPattern())
         result = false;
      else if (isPattern())
         result = domainsMatch(this, name) && propertiesMatch(this, name);
      else
         result = equals(name);

      return result;
   }

   boolean implies(ObjectName name)
   {
      return domainsMatch(this, name) && propertiesMatch(this, name);
   }

   private boolean domainsMatch(ObjectName name1, ObjectName name2)
   {
      String thisDomain = name1.getDomain();
      boolean thisPattern = name1.isDomainPattern();
      String otherDomain = name2.getDomain();
      boolean otherPattern = name2.isDomainPattern();

      if (!thisPattern && otherPattern) return false;
      if (!thisPattern && !otherPattern && !thisDomain.equals(otherDomain)) return false;
      return Utils.wildcardMatch(thisDomain, otherDomain);
   }

   private boolean propertiesMatch(ObjectName name1, ObjectName name2)
   {
      Map thisProperties = name1.getPropertiesMap();
      boolean thisPattern = name1.isPropertyPattern();
      Map otherProperties = name2.getPropertiesMap();
      boolean otherPattern = name2.isPropertyPattern();

      if (!thisPattern && otherPattern) return false;
      if (!thisPattern && !otherPattern && !thisProperties.equals(otherProperties)) return false;
      if (thisPattern && !otherProperties.entrySet().containsAll(thisProperties.entrySet())) return false;

      return true;
   }

   public void setMBeanServer(MBeanServer server)
   {
   }

   public String getCanonicalKeyPropertyListString()
   {
      String canonical = getCanonicalName();
      int index = canonical.indexOf(':');
      String list = canonical.substring(index + 1);
      if (isPropertyPattern())
      {
         if (getKeyPropertyListString().length() == 0)
            return list.substring(0, list.length() - "*".length());
         else
            return list.substring(0, list.length() - ",*".length());
      }
      return list;
   }

   public String getCanonicalName()
   {
      return canonicalName;
   }

   public String getDomain()
   {
      String canonical = getCanonicalName();
      int index = canonical.indexOf(':');
      return canonical.substring(0, index);
   }

   public String getKeyProperty(String key) throws NullPointerException
   {
      Map props = getPropertiesMap();
      return (String)props.get(key);
   }

   public Hashtable getKeyPropertyList()
   {
      return new Hashtable(getPropertiesMap());
   }

   private Map getPropertiesMap()
   {
      // TODO: Consider to cache this Map
      try
      {
         return convertStringToProperties(getKeyPropertyListString(), null);
      }
      catch (MalformedObjectNameException x)
      {
         return null;
      }
   }

   public String getKeyPropertyListString()
   {
      return propertiesString;
   }

   public boolean isPattern()
   {
      return isDomainPattern() || isPropertyPattern();
   }

   public boolean isPropertyPattern()
   {
      return isPropertyPattern;
   }

   public boolean isDomainPattern()
   {
      return isDomainPattern;
   }

   public static ObjectName getInstance(ObjectName name) throws NullPointerException
   {
      if (name.getClass() == ObjectName.class) return name;

      try
      {
         return getInstance(name.getCanonicalName());
      }
      catch (MalformedObjectNameException x)
      {
         throw new IllegalArgumentException(x.toString());
      }
   }

   public static ObjectName getInstance(String name) throws NullPointerException, MalformedObjectNameException
   {
      if (cacheEnabled)
      {
         ObjectName cached = null;
         synchronized (cache)
         {
            cached = cache.get(name);
         }
         if (cached != null) return cached;
      }

      // Keep ObjectName creation, that takes time for parsing, outside the synchronized block.
      return new ObjectName(name);
   }

   public static ObjectName getInstance(String domain, Hashtable table) throws NullPointerException, MalformedObjectNameException
   {
      return new ObjectName(domain, table);
   }

   public static ObjectName getInstance(String domain, String key, String value) throws NullPointerException, MalformedObjectNameException
   {
      return new ObjectName(domain, key, value);
   }

   public static String quote(String value) throws NullPointerException
   {
      StringBuffer buffer = new StringBuffer("\"");
      for (int i = 0; i < value.length(); ++i)
      {
         char ch = value.charAt(i);
         switch (ch)
         {
            case '\n':
               buffer.append("\\n");
               break;
            case '\"':
               buffer.append("\\\"");
               break;
            case '\\':
               buffer.append("\\\\");
               break;
            case '*':
               buffer.append("\\*");
               break;
            case '?':
               buffer.append("\\?");
               break;
            default:
               buffer.append(ch);
               break;
         }
      }
      buffer.append("\"");
      return buffer.toString();
   }

   public static String unquote(String value) throws IllegalArgumentException, NullPointerException
   {
      int lastIndex = value.length() - 1;
      if (lastIndex < 1 || value.charAt(0) != '\"' || value.charAt(lastIndex) != '\"') throw new IllegalArgumentException("The given string is not quoted");

      StringBuffer buffer = new StringBuffer();
      for (int i = 1; i < lastIndex; ++i)
      {
         char ch = value.charAt(i);
         if (ch == '\\')
         {
            // Found a backslash, let's see if it marks an escape sequence
            ++i;
            if (i == lastIndex) throw new IllegalArgumentException("Invalid escape sequence at the end of quoted string");
            ch = value.charAt(i);
            switch (ch)
            {
               case 'n':
                  buffer.append("\n");
                  break;
               case '\"':
                  buffer.append("\"");
                  break;
               case '\\':
                  buffer.append("\\");
                  break;
               case '*':
                  buffer.append("*");
                  break;
               case '?':
                  buffer.append("?");
                  break;
               default:
                  throw new IllegalArgumentException("Invalid escape sequence: \\" + ch);
            }
         }
         else
         {
            switch (ch)
            {
               case '\n':
               case '\"':
               case '*':
               case '?':
                  throw new IllegalArgumentException("Invalid unescaped character: " + ch);
               default:
                  buffer.append(ch);
            }
         }
      }
      return buffer.toString();
   }

   private void parse(String name) throws MalformedObjectNameException
   {
      boolean isSubclass = getClass() != ObjectName.class;

      // It is important from the security point of view to not cache subclasses.
      // An EvilObjectName may return an allowed domain when security checks are made, and
      // a prohibited domain when performing operations. Here we make sure that subclasses
      // are not cached.
      if (cacheEnabled && !isSubclass)
      {
         ObjectName cached = null;
         synchronized (cache)
         {
            cached = cache.get(name);
         }
         if (cached != null)
         {
            // This ObjectName is already created, just copy it to avoid string parsing
            propertiesString = cached.getKeyPropertyListString();
            isDomainPattern = cached.isDomainPattern();
            isPropertyPattern = cached.isPropertyPattern();
            canonicalName = cached.getCanonicalName();
            return;
         }
      }

      String domain = parseDomain(name);
      if (!isDomainValid(domain)) throw new MalformedObjectNameException("Invalid domain: " + domain);

      // Properties must be handled carefully.
      // The main problem is to create the keyPropertiesListString for non trivial cases such as
      // 1. no properties
      // 2. presence of the '*' wildcard in the middle of the list
      // 3. quoted values that contain the '*' wildcard
      // while maintaining the properties' order
      String properties = parseProperties(name);
      // Preliminar, easy checks
      if (properties.trim().length() < 1) throw new MalformedObjectNameException("Missing properties");
      if (properties.trim().endsWith(",")) throw new MalformedObjectNameException("Missing property after trailing comma");
      StringBuffer propsString = new StringBuffer();
      Map table = convertStringToProperties(properties, propsString);

      init(domain, propsString.toString(), table);

      if (cacheEnabled && !isSubclass)
      {
         // Cache this ObjectName
         synchronized (cache)
         {
            // Overwrite if 2 threads computed the same ObjectName: we have been unlucky
            cache.put(name, this);
         }
      }
   }

   private String parseDomain(String objectName) throws MalformedObjectNameException
   {
      int colon = objectName.indexOf(':');
      if (colon < 0) throw new MalformedObjectNameException("Missing ':' character in ObjectName");

      String domain = objectName.substring(0, colon);
      return domain;
   }

   private boolean isDomainValid(String domain)
   {
      if (domain == null) return false;
      if (domain.indexOf('\n') >= 0) return false;
      if (domain.indexOf(":") >= 0) return false;
      return true;
   }

   private String parseProperties(String objectName) throws MalformedObjectNameException
   {
      int colon = objectName.indexOf(':');
      if (colon < 0) throw new MalformedObjectNameException("Missing ':' character in ObjectName");

      String list = objectName.substring(colon + 1);
      return list;
   }

   /**
    * Returns a Map containing the pairs (key,value) parsed from the given string.
    * If the given string contains the wildcard '*', then the returned Hashtable will contains the pair (*,*).
    * If the given StringBuffer is not null, it will be filled with the
    * {@link #getKeyPropertyListString keyPropertiesListString}.
    *
    * @see #initProperties
    */
   private Map convertStringToProperties(String properties, StringBuffer buffer) throws MalformedObjectNameException
   {
      if (buffer != null) buffer.setLength(0);
      Map table = new HashMap();

      StringBuffer toBeParsed = new StringBuffer(properties);
      while (toBeParsed.length() > 0)
      {
         String key = parsePropertyKey(toBeParsed);

         String value = null;
         if ("*".equals(key))
            value = "*";
         else
            value = parsePropertyValue(toBeParsed);

         Object duplicate = table.put(key, value);
         if (duplicate != null) throw new MalformedObjectNameException("Duplicate key not allowed: " + key);

         if (buffer != null && !"*".equals(key))
         {
            if (buffer.length() > 0) buffer.append(',');
            buffer.append(key).append('=').append(value);
         }
      }

      return table;
   }

   private String parsePropertyKey(StringBuffer buffer) throws MalformedObjectNameException
   {
      String toBeParsed = buffer.toString();
      int equal = toBeParsed.indexOf('=');
      int comma = toBeParsed.indexOf(',');

      if (equal < 0 && comma < 0)
      {
         // Then it can only be the asterisk
         String key = toBeParsed.trim();
         if (!"*".equals(key)) throw new MalformedObjectNameException("Invalid key: '" + key + "'");
         buffer.setLength(0);
         return key;
      }

      if (comma >= 0 && comma < equal)
      {
         // Then it can only be the asterisk
         String key = toBeParsed.substring(0, comma).trim();
         if (!"*".equals(key)) throw new MalformedObjectNameException("Invalid key: '" + key + "'");
         buffer.delete(0, comma + 1);
         return key;
      }

      // Normal key
      String key = toBeParsed.substring(0, equal);
      if (!isKeyValid(key)) throw new MalformedObjectNameException("Invalid key: '" + key + "'");
      buffer.delete(0, equal + 1);
      return key;
   }

   private boolean isKeyValid(String key)
   {
      if (key == null) return false;
      if (key.trim().length() < 1) return false;
      if (key.indexOf('\n') >= 0) return false;
      if (key.indexOf(',') >= 0) return false;
      if (key.indexOf('=') >= 0) return false;
      if (key.indexOf('*') >= 0) return false;
      if (key.indexOf('?') >= 0) return false;
      if (key.indexOf(':') >= 0) return false;
      return true;
   }

   private String parsePropertyValue(StringBuffer buffer) throws MalformedObjectNameException
   {
      String toBeParsed = buffer.toString();
      if (toBeParsed.trim().startsWith("\""))
      {
         // It's quoted, delimiter is the closing quote
         int start = toBeParsed.indexOf('"') + 1;
         int endQuote = -1;

         while ((endQuote = toBeParsed.indexOf('"', start)) >= 0)
         {
            int bslashes = countBackslashesBackwards(toBeParsed, endQuote);
            if (bslashes % 2 != 0)
            {
               start = endQuote + 1;
               continue;
            }

            // Found closing quote
            String value = toBeParsed.substring(0, endQuote + 1).trim();
            if (!isValueValid(value)) throw new MalformedObjectNameException("Invalid value: '" + value + "'");

            buffer.delete(0, endQuote + 1);
            // Remove also a possible trailing comma
            toBeParsed = buffer.toString();
            if (toBeParsed.trim().startsWith(","))
            {
               int comma = toBeParsed.indexOf(',');
               buffer.delete(0, comma + 1);
               return value;
            }
            else if (toBeParsed.trim().length() == 0)
            {
               buffer.setLength(0);
               return value;
            }
            else
            {
               throw new MalformedObjectNameException("Garbage after quoted value: " + toBeParsed);
            }
         }
         throw new MalformedObjectNameException("Missing closing quote: " + toBeParsed);
      }
      else
      {
         // Non quoted, delimiter is comma
         int comma = toBeParsed.indexOf(',');
         if (comma >= 0)
         {
            String value = toBeParsed.substring(0, comma);
            if (!isValueValid(value)) throw new MalformedObjectNameException("Invalid value: '" + value + "'");
            buffer.delete(0, comma + 1);
            return value;
         }
         else
         {
            String value = toBeParsed;
            if (!isValueValid(value)) throw new MalformedObjectNameException("Invalid value: '" + value + "'");
            buffer.setLength(0);
            return value;
         }
      }
   }

   private int indexOfLastConsecutiveBackslash(String value, int from)
   {
      int index = value.indexOf('\\', from);
      if (index < 0) return index;
      if (index == value.length() - 1) return index;
      // Probe next char
      int next = indexOfLastConsecutiveBackslash(value, from + 1);
      if (next < 0)
         return index;
      else
         return next;
   }

   private boolean isValueValid(String value)
   {
      if (value == null) return false;
      if (value.length() == 0) return false;
      if (value.indexOf('\n') >= 0) return false;

      if (value.trim().startsWith("\""))
      {
         // strip leading and trailing spaces
         value = value.trim();

         // check value has quotes at start and end
         if (value.length() < 2) return false;
         if (value.charAt(value.length()-1) != '"') return false;

         // check final quote is not escaped
         if (countBackslashesBackwards(value, value.length()-1) % 2 == 1) return false;

         // Unquote the value
         value = value.substring(1, value.length() - 1);

         // Be sure escaped values are interpreted correctly
         int start = 0;
         int index = -1;
         do
         {
            index = indexOfLastConsecutiveBackslash(value, start);
            if (index >= 0)
            {
               // Found a backslash sequence, see if it's an escape or a backslash
               int count = countBackslashesBackwards(value, index + 1);
               if (count % 2 != 0)
               {
                  // Odd number of backslashes, probe next character, should be either '\', 'n', '"', '?', '*'
                  if (index == value.length() - 1) return false;

                  char next = value.charAt(index + 1);
                  if (next != '\\' && next != 'n' && next != '"' && next != '?' && next != '*') return false;
               }
               start = index + 1;
            }
         }
         while (index >= 0);

         start = 0;
         index = -1;
         do
         {
            index = value.indexOf('"', start);
            if (index < 0) index = value.indexOf('*', start);
            if (index < 0) index = value.indexOf('?', start);
            if (index >= 0)
            {
               int bslashCount = countBackslashesBackwards(value, index);
               // There is a special character not preceded by an odd number of backslashes
               if (bslashCount % 2 == 0) return false;
               start = index + 1;
            }
         }
         while (index >= 0);
      }
      else
      {
         if (value.indexOf(',') >= 0) return false;
         if (value.indexOf('=') >= 0) return false;
         if (value.indexOf(':') >= 0) return false;
         if (value.indexOf('"') >= 0) return false;
         if (value.indexOf('*') >= 0) return false;
         if (value.indexOf('?') >= 0) return false;
      }
      return true;
   }

   private int countBackslashesBackwards(String string, int from)
   {
      int bslashCount = 0;
      while (--from >= 0)
      {
         if (string.charAt(from) == '\\')
            ++bslashCount;
         else
            break;
      }
      return bslashCount;
   }

   /**
    * Initializes this ObjectName with the given domain, propertiesString and properties.
    *
    * @see #convertStringToProperties
    */
   private void init(String domain, String propertiesString, Map properties)
   {
      initDomain(domain);
      initProperties(properties);
      this.propertiesString = propertiesString;
      StringBuffer buffer = new StringBuffer(domain).append(':').append(convertPropertiesToString(new TreeMap(properties)));
      if (isPropertyPattern())
      {
         if (getKeyPropertyListString().length() == 0)
            buffer.append('*');
         else
            buffer.append(",*");
      }
      canonicalName = buffer.toString();
   }

   /**
    * If the given domain contains the '*' or the '?' characters, sets this ObjectName as a domain pattern.
    */
   private void initDomain(String domain)
   {
      // Domain may contain '*' and '?' characters if it's a pattern
      if (domain.indexOf('*') >= 0 || domain.indexOf('?') >= 0)
      {
         isDomainPattern = true;
      }
   }

   /**
    * If present, it removes the pair (*,*) from the given Hashtable, and sets this ObjectName as a property pattern.
    *
    * @see #convertStringToProperties
    */
   private void initProperties(Map properties)
   {
      if (properties.containsKey("*"))
      {
         // The Hashtable will never contain the '*'
         properties.remove("*");
         isPropertyPattern = true;
      }
   }

   /**
    * Converts the pairs present in the given Map into a comma separated list of tokens
    * with the form 'key=value'
    */
   private String convertPropertiesToString(Map properties)
   {
      StringBuffer b = new StringBuffer();
      boolean firstTime = true;
      for (Iterator i = properties.entrySet().iterator(); i.hasNext();)
      {
         if (!firstTime)
            b.append(",");
         else
            firstTime = false;

         Map.Entry entry = (Map.Entry)i.next();
         b.append(entry.getKey());
         b.append("=");
         b.append(entry.getValue());
      }

      return b.toString();
   }

   public int hashCode()
   {
      return getCanonicalName().hashCode();
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;

      try
      {
         ObjectName other = (ObjectName)obj;
         return getCanonicalName().equals(other.getCanonicalName());
      }
      catch (ClassCastException ignored)
      {
      }
      return false;
   }

   public String toString()
   {
      return getName(false);
   }

   private String getName(boolean canonical)
   {
      // TODO: Remove the boolean argument, not used anymore
      StringBuffer buffer = new StringBuffer(getDomain()).append(':');
      String properties = canonical ? getCanonicalKeyPropertyListString() : getKeyPropertyListString();
      buffer.append(properties);
      if (isPropertyPattern())
      {
         if (properties.length() == 0)
            buffer.append("*");
         else
            buffer.append(",*");
      }
      return buffer.toString();
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
      String name = getName(false);
      out.writeObject(name);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      String objectName = (String)in.readObject();
      try
      {
         parse(objectName);
      }
      catch (MalformedObjectNameException x)
      {
         throw new InvalidObjectException("String representing the ObjectName is not a valid ObjectName: " + x.toString());
      }
   }

   private static class WeakObjectNameCache
   {
      private ReferenceQueue queue = new ReferenceQueue();
      private HashMap map = new HashMap();

      public void put(String key, ObjectName value)
      {
         cleanup();
         map.put(key, WeakValue.create(key, value, queue));
      }

      public ObjectName get(String key)
      {
         cleanup();
         WeakValue value = (WeakValue)map.get(key);
         if (value == null)
            return null;
         else
            return (ObjectName)value.get();
      }

      private void cleanup()
      {
         WeakValue ref = null;
         while ((ref = (WeakValue)queue.poll()) != null)
         {
            map.remove(ref.getKey());
         }
      }

      private static final class WeakValue extends WeakReference
      {
         private Object key;

         /**
          * Creates a new WeakValue
          *
          * @return null if the given value is null.
          */
         public static WeakValue create(Object key, Object value, ReferenceQueue queue)
         {
            if (value == null) return null;
            return new WeakValue(key, value, queue);
         }

         private WeakValue(Object key, Object value, ReferenceQueue queue)
         {
            super(value, queue);
            this.key = key;
         }

         public Object getKey()
         {
            return key;
         }
      }
   }
}
