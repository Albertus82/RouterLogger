package it.albertus.router.http.html;

import java.io.IOException;
import java.text.DateFormat;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.HttpMethod;
import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.http.HttpServer;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

@Path("/")
public class RootHtmlHandler extends AbstractHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_ENABLED = "server.handler.root.enabled";

	private static final String RESOURCE_BASE_PATH = '/' + HttpServer.class.getPackage().getName().toLowerCase().replace('.', '/') + '/';

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		if (!exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			sendStaticResource(exchange, RESOURCE_BASE_PATH + getPathInfo(exchange));
		}
		else {
			// Response...
			final Version version = Version.getInstance();
			final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get("lbl.server.home"))));
			html.append("<h3>").append('v').append(version.getNumber()).append(" (").append(DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())).append(")</h3>").append(NewLine.CRLF);

			if (configuration.getBoolean(StatusHtmlHandler.CFG_KEY_ENABLED, StatusHtmlHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(StatusHtmlHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.status"))).append("\" /></div></form>").append(NewLine.CRLF);
			}
			if (configuration.getBoolean(LogsHandler.CFG_KEY_ENABLED, LogsHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(LogsHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.logs"))).append("\" /></div></form>").append(NewLine.CRLF);
			}
			if (configuration.getBoolean(ConfigurationHandler.CFG_KEY_ENABLED, ConfigurationHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(ConfigurationHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.configuration"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.server.configuration.confirm.open"))).append("');\" /></div></form>").append(NewLine.CRLF);
			}
			if (configuration.getBoolean(RestartHandler.CFG_KEY_ENABLED, RestartHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(RestartHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.restart"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.restart.message"))).append("');\" /></div></form>").append(NewLine.CRLF);
			}
			if (configuration.getBoolean(ConnectHandler.CFG_KEY_ENABLED, ConnectHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(ConnectHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.connect"))).append("\" /></div></form>").append(NewLine.CRLF);
			}
			if (configuration.getBoolean(DisconnectHandler.CFG_KEY_ENABLED, DisconnectHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(DisconnectHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.disconnect"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.disconnect.message"))).append("');\" /></div></form>").append(NewLine.CRLF);
			}
			if (configuration.getBoolean(CloseHandler.CFG_KEY_ENABLED, CloseHandler.Defaults.ENABLED)) {
				html.append("<form action=\"").append(getAnnotatedPath(CloseHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.close"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.close.message"))).append("');\" /></div></form>").append(NewLine.CRLF);
			}

			html.append(buildHtmlFooter());

			sendResponse(exchange, html.toString());
		}
	}

	@Override
	protected void addContentTypeHeader(final HttpExchange exchange) {
		if (existsStaticResource(RESOURCE_BASE_PATH + getPathInfo(exchange)) && !exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			exchange.getResponseHeaders().add("Content-Type", getContentType(exchange.getRequestURI().getPath())); // extension based
		}
		else {
			super.addContentTypeHeader(exchange); // text/html
		}
	}

	@Override
	public boolean isEnabled(final HttpExchange exchange) {
		if (!exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			return true; // always serve static resources
		}
		else {
			return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED); // configuration based
		}
	}

}
