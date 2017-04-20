package it.albertus.router.http.html;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.AbstractHttpHandler;
import it.albertus.httpserver.HttpException;
import it.albertus.httpserver.HttpMethod;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;

public abstract class AbstractHtmlHandler extends AbstractHttpHandler {

	public static class Defaults {
		public static final boolean COMPRESS_RESPONSE = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String DEFAULT_STYLE = "";

	private static final String MSG_KEY_LBL_ERROR = "lbl.error";

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	@Override
	protected void sendForbidden(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("msg.server.forbidden"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected void sendNotFound(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("msg.server.not.found"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected void sendInternalError(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("err.server.handler"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected void sendError(final HttpExchange exchange, final HttpException e) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : getHttpStatusCodes().get(e.getStatusCode())).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(e.getStatusCode(), response.length);
		exchange.getResponseBody().write(response);
	}

	protected void sendResponse(final HttpExchange exchange, final String html) throws IOException {
		sendResponse(exchange, html.getBytes(getCharset()));
	}

	protected void sendResponse(final HttpExchange exchange, final String html, final int statusCode) throws IOException {
		sendResponse(exchange, html.getBytes(getCharset()), statusCode);
	}

	/**
	 * Creates HTML5 doctype, {@code <html>} opening tag, full {@code <head>}
	 * with {@code <title>}, {@code <style>} and {@code <body>} opening tag.
	 * 
	 * @param title the title to be included in {@code <title>} tag, after the
	 *        application name. If null or empty, nothing but the application
	 *        name will be used.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHeader(final String title) {
		final StringBuilder html = new StringBuilder("<!DOCTYPE html>").append(NewLine.CRLF);
		html.append("<html lang=\"").append(Messages.getLanguage().getLocale().getLanguage()).append("\" xmlns=\"http://www.w3.org/1999/xhtml\">");
		html.append(buildHtmlHead(title));
		html.append("<body>").append(NewLine.CRLF);
		html.append("<h1>").append(HtmlUtils.escapeHtml(Messages.get("msg.application.name"))).append("</h1>").append(NewLine.CRLF);
		return html.toString();
	}

	/**
	 * Creates full {@code <head>} with {@code <title>}, and {@code <style>}
	 * elements.
	 * 
	 * @param title the title to be included in {@code <title>} tag, after the
	 *        application name. If null or empty, nothing but the application
	 *        name will be used.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHead(final String title) {
		final StringBuilder html = new StringBuilder("<head>");
		html.append(buildHtmlHeadMeta());
		html.append(buildHtmlHeadTitle(title));
		html.append(buildHtmlHeadStyle());
		html.append("</head>");
		return html.toString();
	}

	/**
	 * Creates {@code <title>} element.
	 * 
	 * @param title the title to be included after the application name. If null
	 *        or empty, nothing but the application name will be used.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHeadTitle(final String title) {
		final StringBuilder html = new StringBuilder("<title>");
		if (title != null && !title.trim().isEmpty()) {
			html.append(title.trim()).append(" - ");
		}
		return html.append(HtmlUtils.escapeHtml(Messages.get("msg.application.name"))).append("</title>").toString();
	}

	/**
	 * Override this method to create {@code <style>} element. The default
	 * implementation returns an empty string.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHeadStyle() {
		return DEFAULT_STYLE;
	}

	protected String buildHtmlHeadMeta() {
		final StringBuilder html = new StringBuilder();
		html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"); // responsive
		html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=").append(getCharset().name().toLowerCase()).append("\" />"); // XHTML
		return html.toString();
	}

	/**
	 * Closes {@code <body>} and {@code <html>} tags.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlFooter() {
		return "</body></html>";
	}

	protected String buildHtmlHomeButton() {
		if (configuration.getBoolean(RootHtmlHandler.CFG_KEY_ENABLED, RootHtmlHandler.Defaults.ENABLED)) {
			return new StringBuilder("<form action=\"").append(getAnnotatedPath(RootHtmlHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.home"))).append("\" /></div></form>").append(NewLine.CRLF).toString();
		}
		else {
			return "";
		}
	}

	protected String buildHtmlRefreshButton() {
		return new StringBuilder("<form action=\"").append(getPath()).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.refresh"))).append("\" /></div></form>").append(NewLine.CRLF).toString();
	}

	/**
	 * Adds {@code Content-Type: text/html} header to the provided
	 * {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected void addContentTypeHeader(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + getCharset().name().toLowerCase());
	}

	@Override
	protected boolean canCompressResponse(final HttpExchange exchange) {
		return configuration.getBoolean("server.compress.response.html", Defaults.COMPRESS_RESPONSE) && super.canCompressResponse(exchange);
	}

}
