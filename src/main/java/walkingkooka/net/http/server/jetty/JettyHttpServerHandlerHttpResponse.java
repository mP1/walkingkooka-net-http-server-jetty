/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
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
 *
 */

package walkingkooka.net.http.server.jetty;

import walkingkooka.Cast;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.server.HttpResponse;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

final class JettyHttpServerHandlerHttpResponse implements HttpResponse {

    static JettyHttpServerHandlerHttpResponse create() {
        return new JettyHttpServerHandlerHttpResponse();
    }

    private JettyHttpServerHandlerHttpResponse() {
        super();
        this.entity = HttpEntity.EMPTY;
    }

    @Override
    public void setVersion(final HttpProtocolVersion version) {
        Objects.requireNonNull(version, "version");
        this.version = version;
    }

    @Override
    public Optional<HttpProtocolVersion> version() {
        return Optional.ofNullable(this.version);
    }

    private HttpProtocolVersion version;

    @Override
    public void setStatus(final HttpStatus status) {
        Objects.requireNonNull(status, "status");
        this.status = status;
    }

    @Override
    public Optional<HttpStatus> status() {
        return Optional.ofNullable(this.status);
    }

    private HttpStatus status;

    @Override
    public void setEntity(final HttpEntity entity) {
        Objects.requireNonNull(entity, "entity");
        this.entity = entity;
    }

    @Override
    public HttpEntity entity() {
        return this.entity;
    }

    private HttpEntity entity;

    /**
     * Copies the status and entities to the given {@link HttpServletResponse}
     */
    void commit(final HttpServletResponse response) throws IOException, ServletException {
        final HttpStatus status = this.status;
        if (null == status) {
            throw new ServletException("Request not handled");
        }
        response.setStatus(status.value().code(), status.message());

        try (final ServletOutputStream output = response.getOutputStream()) {
            final HttpEntity entity = this.entity;

            copyHeaders(entity, response);
            copyBody(entity, output);

            output.flush();
        }
    }

    private static void copyHeaders(final HttpEntity entity, final HttpServletResponse response) {
        for (final Entry<HttpHeaderName<?>, List<?>> headerAndValues : entity.headers().entrySet()) {
            final HttpHeaderName<?> headerName = headerAndValues.getKey();

            final String headerNameString = headerName.value();
            final List<?> headerValues = headerAndValues.getValue();

            for (final Object headerValue : headerValues) {
                response.addHeader(headerNameString,
                    headerName.headerText(Cast.to(headerValue)));
            }
        }
    }

    private static void copyBody(final HttpEntity entity, final ServletOutputStream output) throws IOException {
        output.write(entity.body().value());
    }
}
