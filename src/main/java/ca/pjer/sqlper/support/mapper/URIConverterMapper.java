package ca.pjer.sqlper.support.mapper;

import java.net.URI;

public class URIConverterMapper extends ConverterMapper<URI, String> {

    public URIConverterMapper() {
        super(String.class);
    }

    @Override
    protected String convertToNative(Class<URI> fromType, URI object, Class<String> toType) throws Exception {
        return object.toString();
    }

    @Override
    protected URI convertFromNative(Class<String> fromType, String object, Class<URI> toType) throws Exception {
        return URI.create(object);
    }
}
