package org.apache.commons.fileupload2.core;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public abstract class AbstractRequestContext<T> implements RequestContext {
    /**
     * The Content-Type Pattern for multipart/related Requests.
     */
    private static final Pattern MULTIPART_RELATED =
            Pattern.compile("^\\s*multipart/related.*", Pattern.CASE_INSENSITIVE);

    /**
     * Supplies the content length default.
     */
    private final LongSupplier contentLengthDefault;

    /**
     * Supplies the content length string.
     */
    private final UnaryOperator<String> contentLengthString;

    /**
     * The request.
     */
    private final T request;

    /**
     * Constructs a new instance.
     *
     * @param contentLengthString  How to get the content length string.
     * @param contentLengthDefault How to get the content length default.
     * @param request              The request.
     */
    protected AbstractRequestContext(final UnaryOperator<String> contentLengthString, final LongSupplier contentLengthDefault, final T request) {
        this.contentLengthString = Objects.requireNonNull(contentLengthString, "contentLengthString");
        this.contentLengthDefault = Objects.requireNonNull(contentLengthDefault, "contentLengthDefault");
        this.request = Objects.requireNonNull(request, "request");
    }

    /**
     * Gets the content length of the request.
     *
     * @return The content length of the request.
     */
    @Override
    public long getContentLength() {
        try {
            return Long.parseLong(contentLengthString.apply(AbstractFileUpload.CONTENT_LENGTH));
        } catch (final NumberFormatException e) {
            return contentLengthDefault.getAsLong();
        }
    }

    public T getRequest() {
        return request;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return String.format("%s [ContentLength=%s, ContentType=%s]", getClass().getSimpleName(), getContentLength(), getContentType());
    }

    /**
     * Is the Request of type {@code multipart/related}?
     *
     * @return the Request is of type {@code multipart/related}
     * @since 2.0.0
     */
    @Override
    public boolean isMultipartRelated() {
        return MULTIPART_RELATED.matcher(getContentType()).matches();
    }
}
