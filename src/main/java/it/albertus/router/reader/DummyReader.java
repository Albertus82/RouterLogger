package it.albertus.router.reader;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.ThreadUtils;

public class DummyReader extends Reader {

	private static final Logger logger = LoggerFactory.getLogger(DummyReader.class);

	private static final byte CHARACTERS = 15;
	private static final byte COLUMNS = 30;

	private static final short LAG_IN_MILLIS = 100;
	private static final short CONNECTION_TIME_IN_MILLIS = 1000;
	private static final short AUTHENTICATION_TIME_IN_MILLIS = 1000;

	private static final double CONNECTION_ERROR_PERCENTAGE = 0.0;
	private static final double AUTHENTICATION_ERROR_PERCENTAGE = 0.0;
	private static final double READ_ERROR_PERCENTAGE = 0.0;

	@Override
	public boolean connect() {
		logger.info(Messages.get("msg.dummy.connect"), Destination.CONSOLE);
		if (CONNECTION_TIME_IN_MILLIS > 0) {
			ThreadUtils.sleep(CONNECTION_TIME_IN_MILLIS);
		}
		if (Math.random() > (100.0 - CONNECTION_ERROR_PERCENTAGE) / 100.0) {
			logger.error(new ConnectException(Messages.get("msg.dummy.connect.error", CONNECTION_ERROR_PERCENTAGE)));
			return false;
		}
		return true;
	}

	@Override
	public boolean login(final String username, final char[] password) {
		out.println("Username: " + username);
		out.println("Password: " + (password != null ? String.valueOf(password) : Arrays.toString(password)));
		if (AUTHENTICATION_TIME_IN_MILLIS > 0) {
			ThreadUtils.sleep(AUTHENTICATION_TIME_IN_MILLIS);
		}
		if (Math.random() > (100.0 - AUTHENTICATION_ERROR_PERCENTAGE) / 100.0) {
			throw new SecurityException(Messages.get("msg.dummy.authentication.error", AUTHENTICATION_ERROR_PERCENTAGE));
		}
		final String message = getClass().getSimpleName() + " - " + Messages.get("msg.test.purposes.only");
		final StringBuilder separator = new StringBuilder();
		for (int c = 0; c < message.length(); c++) {
			separator.append('-');
		}
		out.println(separator.toString(), true);
		out.println(message);
		out.println(separator.toString());
		return true;
	}

	@Override
	public LinkedHashMap<String, String> readInfo() throws IOException {
		final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (byte i = 1; i <= COLUMNS; i++) {
			StringBuilder field = new StringBuilder();
			for (byte j = 1; j <= CHARACTERS; j++) {
				field.append((char) (97 + Math.random() * 25));
			}
			map.put(Messages.get("lbl.column.number", i), field.toString());
		}
		if (LAG_IN_MILLIS != 0) {
			ThreadUtils.sleep(LAG_IN_MILLIS);
		}
		if (Math.random() > (100.0 - READ_ERROR_PERCENTAGE) / 100.0) {
			throw new IOException(Messages.get("msg.dummy.readinfo.error", READ_ERROR_PERCENTAGE));
		}
		return map;
	}

	@Override
	public void logout() {
		logger.info(Messages.get("msg.dummy.logout"), Destination.CONSOLE);
	}

	@Override
	public void disconnect() {
		logger.info(Messages.get("msg.dummy.disconnect"), Destination.CONSOLE);
	}

}
