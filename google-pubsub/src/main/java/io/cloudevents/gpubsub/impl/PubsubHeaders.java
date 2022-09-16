package io.cloudevents.gpubsub.impl;

import io.cloudevents.core.v1.CloudEventV1;

public class PubsubHeaders {
    public static final String CE_EVENT_FORMAT =  "ce-event-format";
    protected static final String CE_PREFIX = "ce-";

    public static final String CONTENT_TYPE = "content-type";

    protected static String headerMapper(String header) {
        if (CloudEventV1.DATACONTENTTYPE.equals(header)) {
            return CONTENT_TYPE;
        }

        return CE_PREFIX + header;
    }

    protected static String headerUnmapper(String header) {
        if (CONTENT_TYPE.equals(header)) {
            return CloudEventV1.DATACONTENTTYPE;
        }

        return header.substring(CE_PREFIX.length());
    }


    public static final String SPEC_VERSION = headerMapper(CloudEventV1.SPECVERSION);

}
