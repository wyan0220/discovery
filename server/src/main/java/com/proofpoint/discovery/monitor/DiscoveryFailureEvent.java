package com.proofpoint.discovery.monitor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.proofpoint.event.client.EventField;
import com.proofpoint.event.client.EventType;

@EventType("platform:type=discovery,name=discoveryFailure")
public class DiscoveryFailureEvent
{
    private final DiscoveryEventType type;
    private final Exception exception;
    private final String requestUri;

    public DiscoveryFailureEvent(DiscoveryEventType type, Exception exception, String requestUri)
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(requestUri));

        this.type = Preconditions.checkNotNull(type);
        this.exception = Preconditions.checkNotNull(exception);
        this.requestUri = requestUri;
    }

    @EventField
    public String getType()
    {
        return type.name();
    }

    @EventField
    public String getException()
    {
        return exception.getMessage();
    }

    @EventField
    public String getRequestUri()
    {
        return requestUri;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiscoveryFailureEvent that = (DiscoveryFailureEvent) o;

        if (!exception.equals(that.exception)) {
            return false;
        }
        if (!requestUri.equals(that.requestUri)) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + exception.hashCode();
        result = 31 * result + requestUri.hashCode();
        return result;
    }
}
