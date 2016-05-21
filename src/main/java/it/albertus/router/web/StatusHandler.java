package it.albertus.router.web;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.HttpExchange;

public class StatusHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean REFRESH = false;
	}

	public static final String PATH = "/status";

	protected static final String KEY_VALUE_SEPARATOR = ": ";
	protected static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public StatusHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		// Charset...
		final Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());

		// Refresh...
		if (configuration.getBoolean("server.status.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.status.refresh.secs", 0);
			if (refresh == 0) {
				// Division by 2 for sampling theorem
				refresh = Math.max(1, Long.valueOf(configuration.getLong("logger.interval.normal.ms", RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS) / 1000 / 2).intValue());
			}
			exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
		}

		// Response...
		byte[] response;
		try {
			// Status...
			final StringBuilder status = new StringBuilder(Resources.get("lbl.status")).append(KEY_VALUE_SEPARATOR);
			final String currentStatus = engine.getCurrentStatus().toString();
			status.append(currentStatus);
			if (!currentStatus.endsWith(".") && !currentStatus.endsWith("!")) {
				status.append('.');
			}

			// Current data...
			final RouterData currentData = engine.getCurrentData();
			if (currentData != null) {
				status.append(NewLine.CRLF);
				status.append(Resources.get("lbl.column.timestamp.text")).append(KEY_VALUE_SEPARATOR).append(dateFormat.format(currentData.getTimestamp())).append(NewLine.CRLF);
				status.append(Resources.get("lbl.column.response.time.text")).append(KEY_VALUE_SEPARATOR).append(currentData.getResponseTime()).append(NewLine.CRLF);
				for (final String key : currentData.getData().keySet()) {
					status.append(key).append(KEY_VALUE_SEPARATOR).append(currentData.getData().get(key)).append(NewLine.CRLF);
				}
			}

			response = status.toString().trim().getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		}
		catch (final Exception e) {
			response = Resources.get("err.server.status").getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
		}
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	@Override
	public String getPath() {
		return PATH;
	}

}