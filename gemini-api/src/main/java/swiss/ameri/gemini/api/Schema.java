package swiss.ameri.gemini.api;


import java.util.List;
import java.util.Map;

/**
 * The Schema object allows the definition of input and output data types.
 * These types can be objects, but also primitives and arrays.
 * Represents a select subset of an OpenAPI 3.0 schema object.
 *
 * @param type             Required. Data type.
 * @param format           Optional. The format of the data. This is used only for primitive datatypes.
 *                         Supported formats:
 *                         for NUMBER type: float, double
 *                         for INTEGER type: int32, int64
 *                         for STRING type: enum
 * @param description      Optional. A brief description of the parameter. This could contain examples of use.
 *                         Parameter description may be formatted as Markdown.
 * @param nullable         Optional. Indicates if the value may be null.
 * @param ameri_swiss_enum Optional. <b>Note: the ameri_swiss prefix must be removed by the {@link swiss.ameri.gemini.spi.JsonParser}</b>.
 *                         Possible values of the element of Type.STRING with enum format.
 *                         For example we can define an Enum Direction as :
 *                         <code>{type:STRING, format:enum, enum:["EAST", NORTH", "SOUTH", "WEST"]}</code>
 * @param maxItems         Optional. Maximum number of the elements for Type.ARRAY.
 * @param minItems         Optional. Minimum number of the elements for Type.ARRAY.
 * @param properties       Optional. Properties of Type.OBJECT.
 *                         An object containing a list of "key": value pairs. Example:
 *                         <code>{ "name": "wrench", "mass": "1.3kg", "count": "3" }</code>.
 * @param required         Optional. Required properties of Type.OBJECT.
 * @param items            Optional. Schema of the elements of Type.ARRAY.
 * @see <a href="https://ai.google.dev/api/caching#Schema">Schema</a> for further information.
 */
public record Schema(
        Type type,
        String format,
        String description,
        Boolean nullable,
        List<String> ameri_swiss_enum,
        String maxItems,
        String minItems,
        Map<String, Schema> properties,
        List<String> required,
        Schema items
) {


    /**
     * Create a {@link SchemaBuilder}.
     *
     * @return an empty {@link SchemaBuilder}
     */
    public static SchemaBuilder builder() {
        return new SchemaBuilder();
    }

    /**
     * A builder for {@link Schema}. Currently, does not validate the fields when building the model. Not thread-safe.
     */
    public static class SchemaBuilder {
        private Type type;
        private String format;
        private String description;
        private Boolean nullable;
        private List<String> ameri_swiss_enum;
        private String maxItems;
        private String minItems;
        private Map<String, Schema> properties;
        private List<String> required;
        private Schema items;


        private SchemaBuilder() {
        }

        public Schema build() {
            return new Schema(
                    this.type,
                    this.format,
                    this.description,
                    this.nullable,
                    this.ameri_swiss_enum,
                    this.maxItems,
                    this.minItems,
                    this.properties,
                    this.required,
                    this.items
            );
        }

        public SchemaBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public SchemaBuilder format(String format) {
            this.format = format;
            return this;
        }

        public SchemaBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SchemaBuilder nullable(Boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public SchemaBuilder ameri_swiss_enum(List<String> ameri_swiss_enum) {
            this.ameri_swiss_enum = ameri_swiss_enum;
            return this;
        }

        public SchemaBuilder maxItems(String maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        public SchemaBuilder minItems(String minItems) {
            this.minItems = minItems;
            return this;
        }

        public SchemaBuilder properties(Map<String, Schema> properties) {
            this.properties = properties;
            return this;
        }

        public SchemaBuilder required(List<String> required) {
            this.required = required;
            return this;
        }

        public SchemaBuilder items(Schema items) {
            this.items = items;
            return this;
        }
    }

    /**
     * Type contains the list of OpenAPI data types.
     *
     * @see <a href="https://ai.google.dev/api/caching#Type">Data types</a>
     */
    public enum Type {
        /**
         * Not specified, should not be used.
         */
        TYPE_UNSPECIFIED,
        /**
         * String type.
         */
        STRING,
        /**
         * Number type.
         */
        NUMBER,
        /**
         * Integer type.
         */
        INTEGER,
        /**
         * Boolean type.
         */
        BOOLEAN,
        /**
         * Array type.
         */
        ARRAY,
        /**
         * Object type.
         */
        OBJECT
    }
}

