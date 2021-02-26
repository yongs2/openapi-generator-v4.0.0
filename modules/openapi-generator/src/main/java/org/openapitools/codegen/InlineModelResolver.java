/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
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

package org.openapitools.codegen;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.openapitools.codegen.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InlineModelResolver {
    private OpenAPI openapi;
    private Map<String, Schema> addedModels = new HashMap<String, Schema>();
    private Map<String, String> generatedSignature = new HashMap<String, String>();
    static Logger LOGGER = LoggerFactory.getLogger(InlineModelResolver.class);

    void flatten(OpenAPI openapi) {
        this.openapi = openapi;

        if (openapi.getComponents() == null) {
            openapi.setComponents(new Components());
        }

        if (openapi.getComponents().getSchemas() == null) {
            openapi.getComponents().setSchemas(new HashMap<String, Schema>());
        }

        flattenPaths(openapi);
        flattenComponents(openapi);
    }

    /**
     * Flatten inline models in Paths
     *
     * @param openAPI target spec
     */
    private void flattenPaths(OpenAPI openAPI) {
        Paths paths = openAPI.getPaths();
        if (paths == null) {
            return;
        }

        for (String pathname : paths.keySet()) {
            PathItem path = paths.get(pathname);
            for (Operation operation : path.readOperations()) {
                flattenRequestBody(openAPI, pathname, operation);
                flattenParameters(openAPI, pathname, operation);
                flattenResponses(openAPI, pathname, operation);
            }
        }
    }

    /**
     * Flatten inline models in RequestBody
     *
     * @param openAPI target spec
     * @param pathname target pathname
     * @param operation target operation
     */
    private void flattenRequestBody(OpenAPI openAPI, String pathname, Operation operation) {
        RequestBody requestBody = ModelUtils.getReferencedRequestBody(openAPI, operation.getRequestBody());
        if (requestBody == null) {
            return;
        }

        Content content = requestBody.getContent();
        content.forEach((contentType, mediaType) -> {
            Schema requestBodySchema = mediaType.getSchema();
            if (this.isNeedInlineSchema(requestBodySchema)) {
                String requestBodySchemaName = this.resolveModelName(requestBodySchema.getTitle(), operation.getOperationId() + "_request");
                this.flattenSchema(requestBodySchema, requestBodySchemaName);
                this.openapi.getComponents().addSchemas(requestBodySchemaName, requestBodySchema);
                mediaType.schema((new Schema()).$ref(requestBodySchemaName));
            }
        });
    }

    /**
     * Flatten inline models in parameters
     *
     * @param openAPI target spec
     * @param pathname target pathname
     * @param operation target operation
     */
    private void flattenParameters(OpenAPI openAPI, String pathname, Operation operation) {
        List<Parameter> parameters = operation.getParameters();
        if (parameters == null) {
            return;
        }
        for (final Parameter parameter : parameters) {
            final Parameter referencedParameter = ModelUtils.getReferencedParameter(this.openapi, parameter);
            Schema parameterModel = ModelUtils.getReferencedSchema(this.openapi, referencedParameter.getSchema());
            if (parameterModel == null) {
                parameterModel = ((MediaType)referencedParameter.getContent().values().iterator().next()).getSchema();
                referencedParameter.setContent(null);
                referencedParameter.setSchema(parameterModel);
            }
            String title = parameterModel.getTitle();
            String s = parameterModel.getName() + parameter.getName();
            String parameterModelName = this.resolveModelName(title, s);
            flattenSchema(parameterModel, parameterModelName);
        }
    }

    /**
     * Flatten inline models in ApiResponses
     *
     * @param openAPI target spec
     * @param pathname target pathname
     * @param operation target operation
     */
    private void flattenResponses(OpenAPI openAPI, String pathname, Operation operation) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            return;
        }

        for (String statusCode : responses.keySet()) {
            ApiResponse response = (ApiResponse)responses.get(statusCode);
            if (ModelUtils.getSchemaFromResponse(response) == null) {
                continue;
            }
            response.getContent().forEach((contentType, MediaType) -> {
                Schema responseModel = MediaType.getSchema();
                if (this.isNeedInlineSchema(responseModel)) {
                    String responseModelName = this.resolveModelName(responseModel.getTitle(), operation.getOperationId() + "_response_" + statusCode);
                    this.flattenSchema(responseModel, responseModelName);
                    this.openapi.getComponents().addSchemas(responseModelName, responseModel);
                    MediaType.schema(new Schema().$ref(responseModelName));
                }
            });
        }
    }
    
    private void flattenSchema(Schema schema, String path) {
        if (ModelUtils.isModel(schema)) {
            Map<String, Schema> properties = (Map<String, Schema>)schema.getProperties();
            if (properties == null) {   // FIXME??
                LOGGER.error("flattenSchema(schema{{}), path({}).properties({})", schema, path, properties);
                return;
            }
            for (String key : properties.keySet()) {
                Schema property = properties.get(key);
                if (ModelUtils.isModel(property)) {
                    String inlineModelName = this.resolveModelName(property.getTitle(), path + "_" + key);
                    this.flattenSchema(property, inlineModelName);
                    this.openapi.getComponents().addSchemas(inlineModelName, property);
                    Schema refSchema = new Schema().$ref(inlineModelName);
                    schema.addProperties(key, refSchema);
                }
            }
        }
        else if (ModelUtils.isArraySchema(schema)) {
            ArraySchema arraySchema = (ArraySchema)schema;
            Schema items = arraySchema.getItems();
            if (ModelUtils.isModel(items)) {
                String inlineModelName2 = this.resolveModelName(items.getTitle(), path + "_inner");
                this.flattenSchema(items, inlineModelName2);
                this.openapi.getComponents().addSchemas(inlineModelName2, items);
                Schema refSchema2 = new Schema().$ref(inlineModelName2);
                arraySchema.setItems(refSchema2);
            }
        }
        else if (ModelUtils.isComposedSchema(schema)) {
            ComposedSchema composedSchema = (ComposedSchema)schema;
            if (composedSchema.getAllOf() == null) {
                if (composedSchema.getAnyOf() != null) {
                    List<Schema> anyOf = composedSchema.getAnyOf();
                    for (Schema inner : anyOf) {
                        if (inner.getEnum() != null) {
                            this.openapi.schema(path, inner);
                        }
                    }
                }
                else if (composedSchema.getOneOf() != null) {}
            }
        }
        if (ModelUtils.isMapSchema(schema)) {
            Schema additionalProperties = ModelUtils.getAdditionalProperties(schema);
            this.flattenSchema(additionalProperties, path + "_additional");
        }
    }

    private void flattenComposedChildren(OpenAPI openAPI, String key, List<Schema> children) {
        if (children == null || children.isEmpty()) {
            return;
        }
        ListIterator<Schema> listIterator = children.listIterator();
        while (listIterator.hasNext()) {
            Schema component = listIterator.next();
            if (component instanceof ObjectSchema) {
                ObjectSchema op = (ObjectSchema) component;
                if (op.get$ref() == null && op.getProperties() != null && op.getProperties().size() > 0) {
                    String innerModelName = resolveModelName(op.getTitle(), key);
                    Schema innerModel = modelFromProperty(op, innerModelName);
                    String existing = matchGenerated(innerModel);
                    if (existing == null) {
                        openAPI.getComponents().addSchemas(innerModelName, innerModel);
                        addGenerated(innerModelName, innerModel);
                        Schema schema = new Schema().$ref(innerModelName);
                        schema.setRequired(op.getRequired());
                        listIterator.set(schema);
                    } else {
                        Schema schema = new Schema().$ref(existing);
                        schema.setRequired(op.getRequired());
                        listIterator.set(schema);
                    }
                }
            }
        }
    }

    private void flattenComponents(OpenAPI openAPI) {
        Map<String, Schema> models = this.openapi.getComponents().getSchemas();
        if (models == null) {
            return;
        }
        List<String> modelNames = new ArrayList<String>(models.keySet());
        for (String modelName : modelNames) {
            Schema schema = models.get(modelName);
            this.flattenSchema(schema, modelName);
        }
    }

    /**
     * This function fix models that are string (mostly enum). Before this fix, the
     * example would look something like that in the doc: "\"example from def\""
     *
     * @param m Schema implementation
     */
    private void fixStringModel(Schema m) {
        if (m.getType() != null && m.getType().equals("string") && m.getExample() != null) {
            String example = m.getExample().toString();
            if (example.substring(0, 1).equals("\"") && example.substring(example.length() - 1).equals("\"")) {
                m.setExample(example.substring(1, example.length() - 1));
            }
        }
    }

    private String resolveModelName(String title, String key) {
        if (title == null) {
            return uniqueName(key);
        } else {
            return uniqueName(title);
        }
    }

    private String matchGenerated(Schema model) {
        String json = Json.pretty(model);
        if (generatedSignature.containsKey(json)) {
            return generatedSignature.get(json);
        }
        return null;
    }

    private void addGenerated(String name, Schema model) {
        generatedSignature.put(Json.pretty(model), name);
    }

    private String uniqueName(String key) {
        if (key == null) {
            key = "NULL_UNIQUE_NAME";
            LOGGER.warn("null key found. Default to NULL_UNIQUE_NAME");
        }
        int count = 0;
        boolean done = false;
        key = key.replaceAll("/", "_"); // e.g. /me/videos => _me_videos
        key = key.replaceAll("[^a-z_\\.A-Z0-9 ]", ""); // FIXME: a parameter
        // should not be assigned. Also declare the methods parameters as 'final'.
        while (!done) {
            String name = key;
            if (count > 0) {
                name = key + "_" + count;
            }
            if (openapi.getComponents().getSchemas() == null) {
                return name;
            } else if (!openapi.getComponents().getSchemas().containsKey(name)) {
                return name;
            }
            count += 1;
        }
        return key;
    }

    private void flattenProperties(Map<String, Schema> properties, String path) {
        if (properties == null) {
            return;
        }
        Map<String, Schema> propsToUpdate = new HashMap<String, Schema>();
        Map<String, Schema> modelsToAdd = new HashMap<String, Schema>();
        for (String key : properties.keySet()) {
            Schema property = properties.get(key);
            if (property instanceof ObjectSchema && ((ObjectSchema) property).getProperties() != null
                    && ((ObjectSchema) property).getProperties().size() > 0) {
                ObjectSchema op = (ObjectSchema) property;
                String modelName = resolveModelName(op.getTitle(), path + "_" + key);
                Schema model = modelFromProperty(op, modelName);
                String existing = matchGenerated(model);
                if (existing != null) {
                    Schema schema = new Schema().$ref(existing);
                    schema.setRequired(op.getRequired());
                    propsToUpdate.put(key, schema);
                } else {
                    Schema schema = new Schema().$ref(modelName);
                    schema.setRequired(op.getRequired());
                    propsToUpdate.put(key, schema);
                    modelsToAdd.put(modelName, model);
                    addGenerated(modelName, model);
                    openapi.getComponents().addSchemas(modelName, model);
                }
            } else if (property instanceof ArraySchema) {
                ArraySchema ap = (ArraySchema) property;
                Schema inner = ap.getItems();
                if (inner instanceof ObjectSchema) {
                    ObjectSchema op = (ObjectSchema) inner;
                    if (op.getProperties() != null && op.getProperties().size() > 0) {
                        flattenProperties(op.getProperties(), path);
                        String modelName = resolveModelName(op.getTitle(), path + "_" + key);
                        Schema innerModel = modelFromProperty(op, modelName);
                        String existing = matchGenerated(innerModel);
                        if (existing != null) {
                            Schema schema = new Schema().$ref(existing);
                            schema.setRequired(op.getRequired());
                            ap.setItems(schema);
                        } else {
                            Schema schema = new Schema().$ref(modelName);
                            schema.setRequired(op.getRequired());
                            ap.setItems(schema);
                            addGenerated(modelName, innerModel);
                            openapi.getComponents().addSchemas(modelName, innerModel);
                        }
                    }
                }
            }
            if (ModelUtils.isMapSchema(property)) {
                Schema inner = ModelUtils.getAdditionalProperties(property);
                if (inner instanceof ObjectSchema) {
                    ObjectSchema op = (ObjectSchema) inner;
                    if (op.getProperties() != null && op.getProperties().size() > 0) {
                        flattenProperties(op.getProperties(), path);
                        String modelName = resolveModelName(op.getTitle(), path + "_" + key);
                        Schema innerModel = modelFromProperty(op, modelName);
                        String existing = matchGenerated(innerModel);
                        if (existing != null) {
                            Schema schema = new Schema().$ref(existing);
                            schema.setRequired(op.getRequired());
                            property.setAdditionalProperties(schema);
                        } else {
                            Schema schema = new Schema().$ref(modelName);
                            schema.setRequired(op.getRequired());
                            property.setAdditionalProperties(schema);
                            addGenerated(modelName, innerModel);
                            openapi.getComponents().addSchemas(modelName, innerModel);
                        }
                    }
                }
            }
        }
        if (propsToUpdate.size() > 0) {
            for (String key : propsToUpdate.keySet()) {
                properties.put(key, propsToUpdate.get(key));
            }
        }
        for (String key : modelsToAdd.keySet()) {
            openapi.getComponents().addSchemas(key, modelsToAdd.get(key));
            this.addedModels.put(key, modelsToAdd.get(key));
        }
    }

    private Schema modelFromProperty(ObjectSchema object, String path) {
        String description = object.getDescription();
        String example = null;
        Object obj = object.getExample();
        if (obj != null) {
            example = obj.toString();
        }
        XML xml = object.getXml();
        Map<String, Schema> properties = object.getProperties();
        Schema model = new Schema();
        model.setDescription(description);
        model.setExample(example);
        model.setName(object.getName());
        model.setXml(xml);
        model.setRequired(object.getRequired());
        model.setNullable(object.getNullable());
        if (properties != null) {
            flattenProperties(properties, path);
            model.setProperties(properties);
        }
        return model;
    }

    /**
     * Make a Schema
     *
     * @param ref      new property name
     * @param property Schema
     * @return {@link Schema} A constructed OpenAPI property
     */
    private Schema makeSchema(String ref, Schema property) {
        Schema newProperty = new Schema().$ref(ref);
        this.copyVendorExtensions(property, newProperty);
        return newProperty;
    }

    /**
     * Copy vendor extensions from Model to another Model
     *
     * @param source source property
     * @param target target property
     */

    private void copyVendorExtensions(Schema source, Schema target) {
        Map<String, Object> vendorExtensions = source.getExtensions();
        if (vendorExtensions == null) {
             return;
        }
        for (String extName : vendorExtensions.keySet()) {
            target.addExtension(extName, vendorExtensions.get(extName));
        }
    }

    private boolean isNeedInlineSchema(final Schema schema) {
        return schema != null && (ModelUtils.isComposedSchema(schema) || (schema.getProperties() != null && schema.getProperties().size() > 1));
    }
}