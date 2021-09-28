package com.example.articledemo.core.converter.impl;

import com.example.articledemo.core.converter.CustomMessageConverter;
import com.example.articledemo.domain.EntityWithSmthInfo;
import com.example.articledemo.domain.SmthInfo;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.Assert;
import org.springframework.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MappingCustomJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter
    implements CustomMessageConverter {

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = getJsonEncoding(contentType);
        JsonGenerator generator = getObjectMapper().getFactory().createGenerator(outputMessage.getBody(), encoding);
        try {
            writePrefix(generator, object);

            Object value = object;
            Class<?> serializationView = null;
            FilterProvider filters = null;
            JavaType javaType = null;

            if (object instanceof MappingJacksonValue) {
                MappingJacksonValue container = (MappingJacksonValue) object;
                value = container.getValue();
                serializationView = container.getSerializationView();
                filters = container.getFilters();
            }

            if (value instanceof ResponseEntity) {
                value = ((ResponseEntity<?>) value).getBody();
            }

            if (value != null && type != null && (TypeUtils.isAssignable(type, value.getClass()))) {
                javaType = getJavaType(type, null);
            }

            ObjectWriter objectWriter = getObjectWriter(serializationView, filters, javaType);

            Assert.notNull(javaType, "Response type can not be null");

            // +++ Add Info in result json tree
            String responseJson = objectWriter.writeValueAsString(value);
            JsonNode rootNode = getJsonNodeWithInfo(object, javaType, getObjectMapper().readTree(responseJson));
            getObjectMapper().writeTree(generator, rootNode);

            writeSuffix(generator, object);
            generator.flush();
        }
        catch (InvalidDefinitionException ex) {
            throw new HttpMessageConversionException("Type definition error: " + ex.getType(), ex);
        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getOriginalMessage(), ex);
        }
    }


    /**
     * Here we process different types of response
     *
     * @param rootNode json tree with entity
     * @param javaType type of entity
     * @param object object of entity which returned
     */
    private JsonNode getJsonNodeWithInfo(Object object, JavaType javaType, JsonNode rootNode) throws JsonProcessingException {
        if (javaType.isCollectionLikeType()) {
            handleCollectionContent((ArrayNode) rootNode, javaType.getContentType());
        } else if (javaType.isTypeOrSubTypeOf(Page.class)) {
            handlePageContent(javaType, rootNode);
        } else {
            getJsonWithInfo(rootNode, javaType, object);
        }

        return rootNode;
    }

    // +++
    private void handlePageContent(JavaType javaType, JsonNode rootNode) throws JsonProcessingException {
        if (javaType.getBindings().size() == 0) {
            throw new IllegalArgumentException("Page must have bound type");
        }

        handleCollectionContent((ArrayNode) rootNode.get("content"), javaType.getBindings().getBoundType(0));
    }

    // +++
    private void handleCollectionContent(ArrayNode rootNode, JavaType contentType) throws JsonProcessingException {
        addToArrayNodeElementInfo(contentType, rootNode);
    }

    // +++
    private void addToArrayNodeElementInfo(JavaType boundType, ArrayNode arrayNode) throws JsonProcessingException {
        List<JsonNode> nodesWithInfo = new ArrayList<>();
        for (JsonNode elementNode : arrayNode) {
            Object element = getObjectMapper().readValue(elementNode.toPrettyString(), boundType);
            JsonNode elementWithInfo = getJsonWithInfo(elementNode, boundType, element);
            nodesWithInfo.add(elementWithInfo);
        }

        arrayNode.removeAll();
        arrayNode.addAll(nodesWithInfo);
    }

    /**
     * Here on base type of our response entity can find additional info and add it to json
     */
    private JsonNode getJsonWithInfo(JsonNode root, JavaType javaType, Object returnedObject) {
        // Get info by entity from DB...
        SmthInfo smthInfo = new SmthInfo("additional info for entity");

        ObjectNode node = (ObjectNode) root;
        node.putPOJO("info", smthInfo);

        return root;
    }

    private ObjectWriter getObjectWriter(Class<?> serializationView, FilterProvider filters, JavaType javaType) {
        ObjectWriter objectWriter = (serializationView != null ?
                getObjectMapper().writerWithView(serializationView) : getObjectMapper().writer());

        if (filters != null) {
            objectWriter = objectWriter.with(filters);
        }
        if (javaType != null && javaType.isContainerType()) {
            objectWriter = objectWriter.forType(javaType);
        }
        return objectWriter;
    }

    @Override
    public EntityWithSmthInfo readWithSmthInfo(Class<?> clazz, HttpInputMessage inputMessage) throws Exception {
        return readJavaTypeWithInfo(clazz, inputMessage);
    }

    private EntityWithSmthInfo readJavaTypeWithInfo(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException {

        JavaType javaType = getJavaType(clazz, null);

        ObjectNode rootNode = (ObjectNode) getObjectMapper().readTree(inputMessage.getBody());

        ObjectNode infoNode = (ObjectNode) rootNode.at("/info");
        SmthInfo info = getObjectMapper().readValue(infoNode.toPrettyString(), SmthInfo.class);

        // +++ remove info node
        rootNode.remove("/info");

        try {
            Object value = getEntity(inputMessage, javaType, rootNode);

            return new EntityWithSmthInfo(value, info);
        } catch (InvalidDefinitionException ex) {
            throw new HttpMessageConversionException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getOriginalMessage(), ex, inputMessage);
        }
    }

    private Object getEntity(HttpInputMessage inputMessage, JavaType javaType, ObjectNode rootNode) throws JsonProcessingException {
        Object value;

        Class<?> deserializationView;
        if (inputMessage instanceof MappingJacksonInputMessage
                && (deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView()) != null) {

            value = getObjectMapper().readerWithView(deserializationView).forType(javaType).
                    readValue(rootNode.toPrettyString());
        } else {
            value = getObjectMapper().readValue(rootNode.toPrettyString(), javaType);
        }

        return value;
    }

}
