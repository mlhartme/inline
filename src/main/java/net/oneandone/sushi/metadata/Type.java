/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.sushi.metadata;

import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public abstract class Type {
    public static final String SCHEMA_HEAD = 
        "<?xml version='1.0' encoding='UTF-8'?>\n" + 
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <xs:attributeGroup name='ids'>\n" +
        "    <xs:attribute name='id' type='xs:string'/>\n" +
        "    <xs:attribute name='idref' type='xs:string'/>\n" +
        "  </xs:attributeGroup>\n";
        
    protected final Schema schema;
    protected final java.lang.reflect.Type type;
    protected final Class<?> rawType;
    protected final String name;

    public Type(Schema schema, java.lang.reflect.Type type, String name) {
        this.schema = schema;
        this.type = type;
        if (type instanceof Class) {
            rawType = (Class) type;
        } else {
            try {
                rawType = (Class) ((ParameterizedType) type).getRawType();
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException();
            }
        }
        if (rawType.isPrimitive()) {
            throw new IllegalArgumentException(rawType.getName());
        }
        if (rawType.isArray()) {
            throw new IllegalArgumentException(rawType.getName());
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            throw new IllegalArgumentException(rawType.getName());
        }
        this.name = name;
    }

    public Schema getSchema() {
        return schema;
    }
    
    public Class<?> getRawType() {
        return rawType;
    }

    public java.lang.reflect.Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    
    public abstract Object newInstance();

    //--

    //-- xsd schema generation
    public String createSchema() {
        StringBuilder schema;
        Set<Type> types;

        schema = new StringBuilder();

        schema.append(SCHEMA_HEAD);
        schema.append("  <xs:element name='").append(getName()).append("' type='").append(getSchemaTypeName()).append("'/>\n");

        types = new HashSet<>();
        addSchemaType(types, schema);

        schema.append("</xs:schema>");
        return schema.toString();
    }

    public abstract String getSchemaTypeName();
    public abstract void addSchemaType(Set<Type> done, StringBuilder dest);
    
}
