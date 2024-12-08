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

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Bridge that handles dispatching {@link HttpServletRequest} and {@link HttpServletResponse} to a {@link BiConsumer}.
 */
final class JettyHttpServerHandler extends AbstractHandler {

    static JettyHttpServerHandler with(final HttpHandler handler) {
        return new JettyHttpServerHandler(handler);
    }

    private JettyHttpServerHandler(final HttpHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void handle(final String base,
                       final Request request,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) throws IOException, ServletException {
        if (false == request.isHandled()) {
            request.setHandled(true);

            final JettyHttpServerHandlerHttpResponse httpResponse = JettyHttpServerHandlerHttpResponse.create();

            final HttpRequest httpRequest = HttpRequests.httpServletRequest(httpServletRequest);
            this.handle(
                    httpRequest,
                    HttpResponses.headerScope(
                            HttpResponses.httpStatusCodeRequiredHeaders(httpResponse)
                    )
            );

            httpResponse.commit(httpServletResponse);
        }
    }

    private void handle(final HttpRequest request, final HttpResponse response) {
        this.handler.handle(
                request,
                response
        );
    }

    private final HttpHandler handler;

    @Override
    public String toString() {
        return this.handler.toString();
    }
}
