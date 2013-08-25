/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.util;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.JXPathTypeConversionException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;

/**
 * The default implementation of TypeConverter.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 670727 $ $Date: 2008-06-23 15:10:38 -0500 (Mon, 23 Jun 2008) $
 */
public class BasicTypeConverter implements TypeConverter {

    /**
     * Returns true if it can convert the supplied
     * object to the specified class.
     * @param object to check
     * @param toType prospective destination class
     * @return boolean
     */
    @Override
    public boolean canConvert(Object object, final Class toType) {
        if (object == null) {
            return true;
        }
        final Class useType = TypeUtils.wrapPrimitive(toType);
        Class fromType = object.getClass();

        if (useType.isAssignableFrom(fromType)) {
            return true;
        }

        if (useType == String.class) {
            return true;
        }

        if (object instanceof Boolean && (Number.class.isAssignableFrom(useType)
                || "java.util.concurrent.atomic.AtomicBoolean"
                        .equals(useType.getName()))) {
            return true;
        }
        if (object instanceof Number
                && (Number.class.isAssignableFrom(useType) || useType == Boolean.class)) {
            return true;
        }
        if (object instanceof String
                && (useType == Boolean.class
                        || useType == Character.class
                        || useType == Byte.class
                        || useType == Short.class
                        || useType == Integer.class
                        || useType == Long.class
                        || useType == Float.class
                        || useType == Double.class)) {
                return true;
        }
        if (fromType.isArray()) {
            // Collection -> array
            if (useType.isArray()) {
                Class cType = useType.getComponentType();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    Object value = Array.get(object, i);
                    if (!this.canConvert(value, cType)) {
                        return false;
                    }
                }
                return true;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                return this.canCreateCollection(useType);
            }
            if (Array.getLength(object) > 0) {
                Object value = Array.get(object, 0);
                return this.canConvert(value, useType);
            }
            return this.canConvert("", useType);
        }
        if (object instanceof Collection) {
            // Collection -> array
            if (useType.isArray()) {
                Class cType = useType.getComponentType();
                Iterator it = ((Collection) object).iterator();
                while (it.hasNext()) {
                    Object value = it.next();
                    if (!this.canConvert(value, cType)) {
                        return false;
                    }
                }
                return true;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                return this.canCreateCollection(useType);
            }
            if (((Collection) object).size() > 0) {
                Object value;
                if (object instanceof List) {
                    value = ((List) object).get(0);
                }
                else {
                    Iterator it = ((Collection) object).iterator();
                    value = it.next();
                }
                return this.canConvert(value, useType);
            }
            return this.canConvert("", useType);
        }
        if (object instanceof NodeSet) {
            return this.canConvert(((NodeSet) object).getValues(), useType);
        }
        if (object instanceof Pointer) {
            return this.canConvert(((Pointer) object).getValue(), useType);
        }
        return false;
    }

    /**
     * Converts the supplied object to the specified
     * type. Throws a runtime exception if the conversion is
     * not possible.
     * @param object to convert
     * @param toType destination class
     * @return converted object
     */
    @Override
    public Object convert(Object object, final Class toType) {
        if (object == null) {
            return toType.isPrimitive() ? this.convertNullToPrimitive(toType) : null;
        }

        if (toType == Object.class) {
            if (object instanceof NodeSet) {
                return this.convert(((NodeSet) object).getValues(), toType);
            }
            if (object instanceof Pointer) {
                return this.convert(((Pointer) object).getValue(), toType);
            }
            return object;
        }
        final Class useType = TypeUtils.wrapPrimitive(toType);
        Class fromType = object.getClass();

        if (useType.isAssignableFrom(fromType)) {
            return object;
        }

        if (fromType.isArray()) {
            int length = Array.getLength(object);
            if (useType.isArray()) {
                Class cType = useType.getComponentType();

                Object array = Array.newInstance(cType, length);
                for (int i = 0; i < length; i++) {
                    Object value = Array.get(object, i);
                    Array.set(array, i, this.convert(value, cType));
                }
                return array;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                Collection collection = this.allocateCollection(useType);
                for (int i = 0; i < length; i++) {
                    collection.add(Array.get(object, i));
                }
                return this.unmodifiableCollection(collection);
            }
            if (length > 0) {
                Object value = Array.get(object, 0);
                return this.convert(value, useType);
            }
            return this.convert("", useType);
        }
        if (object instanceof Collection) {
            int length = ((Collection) object).size();
            if (useType.isArray()) {
                Class cType = useType.getComponentType();
                Object array = Array.newInstance(cType, length);
                Iterator it = ((Collection) object).iterator();
                for (int i = 0; i < length; i++) {
                    Object value = it.next();
                    Array.set(array, i, this.convert(value, cType));
                }
                return array;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                Collection collection = this.allocateCollection(useType);
                collection.addAll((Collection) object);
                return this.unmodifiableCollection(collection);
            }
            if (length > 0) {
                Object value;
                if (object instanceof List) {
                    value = ((List) object).get(0);
                }
                else {
                    Iterator it = ((Collection) object).iterator();
                    value = it.next();
                }
                return this.convert(value, useType);
            }
            return this.convert("", useType);
        }
        if (object instanceof NodeSet) {
            return this.convert(((NodeSet) object).getValues(), useType);
        }
        if (object instanceof Pointer) {
            return this.convert(((Pointer) object).getValue(), useType);
        }
        if (useType == String.class) {
            return object.toString();
        }
        if (object instanceof Boolean) {
            if (Number.class.isAssignableFrom(useType)) {
                return this.allocateNumber(useType, ((Boolean) object).booleanValue() ? 1 : 0);
            }
            if ("java.util.concurrent.atomic.AtomicBoolean".equals(useType.getName())) {
                try {
                    return useType.getConstructor(new Class[] { boolean.class })
                            .newInstance(new Object[] { object });
                }
                catch (Exception e) {
                    throw new JXPathTypeConversionException(useType.getName(), e);
                }
            }
        }
        if (object instanceof Number) {
            double value = ((Number) object).doubleValue();
            if (useType == Boolean.class) {
                return value == 0.0 ? Boolean.FALSE : Boolean.TRUE;
            }
            if (Number.class.isAssignableFrom(useType)) {
                return this.allocateNumber(useType, value);
            }
        }
        if (object instanceof String) {
            Object value = this.convertStringToPrimitive(object, useType);
            if (value != null) {
                return value;
            }
        }

        throw new JXPathTypeConversionException("Cannot convert "
                + object.getClass() + " to " + useType);
    }

    /**
     * Convert null to a primitive type.
     * @param toType destination class
     * @return a wrapper
     */
    protected Object convertNullToPrimitive(Class toType) {
        if (toType == boolean.class) {
            return Boolean.FALSE;
        }
        if (toType == char.class) {
            return new Character('\0');
        }
        if (toType == byte.class) {
            return new Byte((byte) 0);
        }
        if (toType == short.class) {
            return new Short((short) 0);
        }
        if (toType == int.class) {
            return new Integer(0);
        }
        if (toType == long.class) {
            return new Long(0L);
        }
        if (toType == float.class) {
            return new Float(0.0f);
        }
        if (toType == double.class) {
            return new Double(0.0);
        }
        return null;
    }

    /**
     * Convert a string to a primitive type.
     * @param object String
     * @param toType destination class
     * @return wrapper
     */
    protected Object convertStringToPrimitive(Object object, Class toType) {
        toType = TypeUtils.wrapPrimitive(toType);
        if (toType == Boolean.class) {
            return Boolean.valueOf((String) object);
        }
        if (toType == Character.class) {
            return new Character(((String) object).charAt(0));
        }
        if (toType == Byte.class) {
            return new Byte((String) object);
        }
        if (toType == Short.class) {
            return new Short((String) object);
        }
        if (toType == Integer.class) {
            return new Integer((String) object);
        }
        if (toType == Long.class) {
            return new Long((String) object);
        }
        if (toType == Float.class) {
            return new Float((String) object);
        }
        if (toType == Double.class) {
            return new Double((String) object);
        }
        return null;
    }

    /**
     * Allocate a number of a given type and value.
     * @param type destination class
     * @param value double
     * @return Number
     */
    protected Number allocateNumber(Class type, double value) {
        type = TypeUtils.wrapPrimitive(type);
        if (type == Byte.class) {
            return new Byte((byte) value);
        }
        if (type == Short.class) {
            return new Short((short) value);
        }
        if (type == Integer.class) {
            return new Integer((int) value);
        }
        if (type == Long.class) {
            return new Long((long) value);
        }
        if (type == Float.class) {
            return new Float((float) value);
        }
        if (type == Double.class) {
            return new Double(value);
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf((long) value);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(value);
        }
        String classname = type.getName();
        Class initialValueType = null;
        if ("java.util.concurrent.atomic.AtomicInteger".equals(classname)) {
            initialValueType = int.class;
        }
        if ("java.util.concurrent.atomic.AtomicLong".equals(classname)) {
            initialValueType = long.class;
        }
        if (initialValueType != null) {
            try {
                return (Number) type.getConstructor(
                        new Class[] { initialValueType })
                        .newInstance(
                                new Object[] { this.allocateNumber(initialValueType,
                                        value) });
            }
            catch (Exception e) {
                throw new JXPathTypeConversionException(classname, e);
            }
        }
        return null;
    }

    /**
     * Learn whether this BasicTypeConverter can create a collection of the specified type.
     * @param type prospective destination class
     * @return boolean
     */
    protected boolean canCreateCollection(Class type) {
        if (!type.isInterface()
                && ((type.getModifiers() & Modifier.ABSTRACT) == 0)) {
            try {
                type.getConstructor(new Class[0]);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
        return type == List.class || type == Collection.class || type == Set.class;
    }

    /**
     * Create a collection of a given type.
     * @param type destination class
     * @return Collection
     */
    protected Collection allocateCollection(Class type) {
        if (!type.isInterface()
                && ((type.getModifiers() & Modifier.ABSTRACT) == 0)) {
            try {
                return (Collection) type.newInstance();
            }
            catch (Exception ex) {
                throw new JXPathInvalidAccessException(
                        "Cannot create collection of type: " + type, ex);
            }
        }

        if (type == List.class || type == Collection.class) {
            return new ArrayList();
        }
        if (type == Set.class) {
            return new HashSet();
        }
        throw new JXPathInvalidAccessException(
                "Cannot create collection of type: " + type);
    }

    /**
     * Get an unmodifiable version of a collection.
     * @param collection to wrap
     * @return Collection
     */
    protected Collection unmodifiableCollection(Collection collection) {
        if (collection instanceof List) {
            return Collections.unmodifiableList((List) collection);
        }
        if (collection instanceof SortedSet) {
            return Collections.unmodifiableSortedSet((SortedSet) collection);
        }
        if (collection instanceof Set) {
            return Collections.unmodifiableSet((Set) collection);
        }
        return Collections.unmodifiableCollection(collection);
    }

    /**
     * NodeSet implementation
     */
    static final class ValueNodeSet implements NodeSet {
        private List values;
        private List pointers;

        /**
         * Create a new ValueNodeSet.
         * @param values to return
         */
        public ValueNodeSet(List values) {
           this.values = values;
        }

        @Override
        public List getValues() {
            return Collections.unmodifiableList(this.values);
        }

        @Override
        public List getNodes() {
            return Collections.unmodifiableList(this.values);
        }

        @Override
        public List getPointers() {
            if (this.pointers == null) {
                this.pointers = new ArrayList();
                for (int i = 0; i < this.values.size(); i++) {
                    this.pointers.add(new ValuePointer(this.values.get(i)));
                }
                this.pointers = Collections.unmodifiableList(this.pointers);
            }
            return this.pointers;
        }
    }

    /**
     * Value pointer
     */
    static final class ValuePointer implements Pointer {
        private static final long serialVersionUID = -4817239482392206188L;

        private Object bean;

        /**
         * Create a new ValuePointer.
         * @param object value
         */
        public ValuePointer(Object object) {
            this.bean = object;
        }

        @Override
        public Object getValue() {
            return this.bean;
        }

        @Override
        public Object getNode() {
            return this.bean;
        }

        @Override
        public Object getRootNode() {
            return this.bean;
        }

        @Override
        public void setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object clone() {
            return this;
        }

        @Override
        public int compareTo(Object object) {
            return 0;
        }

        @Override
        public String asPath() {
            if (this.bean == null) {
                return "null()";
            }
            if (this.bean instanceof Number) {
                String string = this.bean.toString();
                if (string.endsWith(".0")) {
                    string = string.substring(0, string.length() - 2);
                }
                return string;
            }
            if (this.bean instanceof Boolean) {
                return ((Boolean) this.bean).booleanValue() ? "true()" : "false()";
            }
            if (this.bean instanceof String) {
                return "'" + this.bean + "'";
            }
            return "{object of type " + this.bean.getClass().getName() + "}";
        }
    }
}
