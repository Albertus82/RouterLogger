package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public class RestartHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean ENABLED = true;
	}
	
	public static final String PATH = "/restart";
	public static final String[] METHODS = { "POST" };

	protected RestartHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.server.restart")));
		html.append("<h3>").append(Resources.get("msg.server.accepted")).append("</h3>").append(NewLine.CRLF.toString());
		html.append("<form action=\"").append(RootHandler.PATH).append("\" method=\"").append(RootHandler.METHODS[0]).append("\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.home")).append("\" /></form>").append(NewLine.CRLF.toString());
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();

		engine.restart();
	}

	@Override
	public String getPath() {
		return PATH;
	}

	@Override
	public String[] getMethods() {
		return METHODS;
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean("server.handler.restart.enabled", Defaults.ENABLED);
	}

}
