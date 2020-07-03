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

import org.eclipse.jetty.server.Server;
import walkingkooka.Binary;
import walkingkooka.Cast;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.HttpServerException;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A {@link HttpServer} that uses an embedded JETTY servlet container.
 */
public final class JettyHttpServer implements HttpServer {

    public static JettyHttpServer with(final HostAddress host,
                                       final IpPort port,
                                       final BiConsumer<HttpRequest, HttpResponse> handler) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(port, "port");
        Objects.requireNonNull(handler, "handler");

        return new JettyHttpServer(host, port, handler);
    }

    private JettyHttpServer(final HostAddress host,
                            final IpPort port,
                            final BiConsumer<HttpRequest, HttpResponse> handler) {
        final Server server = new Server(new InetSocketAddress(host.value(), port.value()));
        server.setHandler(JettyHttpServerHandler.with(handler));
        this.server = server;
    }

    @Override
    public void start() {
        try {
            this.server.start();
        } catch (final Exception cause) {
            throw new HttpServerException("Server start failed: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void stop() {
        try {
            this.server.stop();
        } catch (final Exception cause) {
            throw new HttpServerException("Server stop failed: " + cause.getMessage(), cause);
        }
    }

    private final Server server;

    @Override
    public String toString() {
        return this.server.toString();
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Required 2 arguments <host> <port> got " + Arrays.toString(args));
        }

        final HostAddress host = HostAddress.with(args[0]);
        final IpPort port = IpPort.with(Integer.parseInt(args[1]));

        final JettyHttpServer server = JettyHttpServer.with(host,
                port,
                JettyHttpServer::handle);

        try {
            server.start();
            server.server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }

    private static void handle(final HttpRequest req, final HttpResponse res) {
        if (UrlPath.parse("/dump.txt").equals(req.url().path())) {
            res.setStatus(HttpStatusCode.OK.status());

            final StringBuilder b = new StringBuilder();

            b.append("headers\n");

            req.headers()
                    .forEach((header, value) -> {
                        b.append("  ");
                        b.append(header);
                        b.append(": ");
                        b.append(header.headerText(Cast.to(value)));
                        b.append('\n');
                    });


            b.append("parameters\n");

            req.parameters()
                    .forEach((key, value) -> {
                        b.append("  ");
                        b.append(key);
                        b.append('=');
                        b.append(String.join(", ", value));
                        b.append('\n');
                    });


            final byte[] bytes = b.toString().getBytes(Charset.defaultCharset());
            final Map<HttpHeaderName<?>, Object> headers = Maps.of(
                    HttpHeaderName.SERVER, "JettyServer",
                    HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN.setCharset(CharsetName.UTF_8),
                    HttpHeaderName.CONTENT_LENGTH, (long)bytes.length);

            res.addEntity(HttpEntity.with(headers, Binary.with(bytes)));

        } else {
            res.setStatus(HttpStatusCode.NOT_FOUND.status());
        }
    }
}
