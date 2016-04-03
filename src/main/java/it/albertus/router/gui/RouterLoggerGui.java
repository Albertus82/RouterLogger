package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ExceptionUtils;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RouterLoggerGui extends RouterLoggerEngine implements IShellProvider {

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	protected interface Defaults extends RouterLoggerEngine.Defaults {
		boolean GUI_START_MINIMIZED = false;
	}

	private Thread updateThread;

	private final Shell shell;
	private final DataTable dataTable;
	private final TrayIcon trayIcon;
	private final MenuBar menuBar;
	private final SashForm sashForm;

	/** Entry point for GUI version */
	public static void start() {
		try {
			// Creazione finestra applicazione...
			final Display display = new Display();
			final RouterLoggerGui routerLogger = newInstance(display);
			final Shell shell = routerLogger.getShell();
			shell.open();

			// Stampa del messaggio di benvenuto...
			routerLogger.beforeOuterLoop();

			// Avvio thread di interrogazione router...
			routerLogger.connect();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			// Segnala al thread che deve terminare il loop immediatamente.
			routerLogger.disconnect(true);

			// Distrugge la GUI...
			display.dispose();

			// Attende che il thread completi il rilascio risorse...
			routerLogger.updateThread.join();

			// Stampa del messaggio di commiato...
			routerLogger.afterOuterLoop();
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
	}

	private static RouterLoggerGui newInstance(final Display display) {
		RouterLoggerGui instance = null;
		try {
			instance = new RouterLoggerGui(display);
		}
		catch (final Exception exception) {
			fatalError(display, exception);
		}
		catch (final ExceptionInInitializerError exception) {
			fatalError(display, exception.getCause() != null ? exception.getCause() : exception);
		}
		return instance;
	}

	private static void fatalError(final Display display, final Throwable throwable) {
		final Shell shell = new Shell(display);
		final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
		messageBox.setText(Resources.get("lbl.window.title"));
		messageBox.setMessage(ExceptionUtils.getUIMessage(throwable));
		messageBox.open();
		shell.dispose();
		display.dispose();
		System.exit(1);
	}

	private RouterLoggerGui(final Display display) {
		shell = new Shell(display);
		shell.setMinimized(configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED));
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(Images.MAIN_ICONS);
		shell.setLayout(new GridLayout());

		trayIcon = new TrayIcon(this);

		menuBar = new MenuBar(this);

		sashForm = new SashForm(shell, SWT.VERTICAL);
		sashForm.setSashWidth((int) (sashForm.getSashWidth() * SASH_MAGNIFICATION_FACTOR));
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		dataTable = new DataTable(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), this);
		getConsole().init(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true));

		shell.addListener(SWT.Close, new CloseListener(this));
	}

	@Override
	protected void showInfo(final RouterData info, final Map<Threshold, String> thresholdsReached) {
		/* Aggiunta riga nella tabella a video */
		dataTable.addRow(info, thresholdsReached, iteration);

		/* Aggiornamento icona e tooltip nella barra di notifica (se necessario) */
		if (trayIcon != null) {
			trayIcon.updateTrayItem(getCurrentStatus(), info);
		}

		/* Stampa eventuali soglie raggiunte in console */
		printThresholdsReached(thresholdsReached);
	}

	private void printThresholdsReached(final Map<Threshold, String> thresholdsReached) {
		if (thresholdsReached != null && !thresholdsReached.isEmpty()) {
			final Map<String, String> message = new TreeMap<String, String>();
			boolean print = false;
			for (final Threshold threshold : thresholdsReached.keySet()) {
				message.put(threshold.getKey(), thresholdsReached.get(threshold));
				if (!threshold.isExcluded()) {
					print = true;
				}
			}
			if (print) {
				logger.log(Resources.get("msg.thresholds.reached", message), Destination.CONSOLE);
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(thresholdsReached);
				}
			}
		}
	}

	public boolean canCopyDataTable() {
		return this.getDataTable().getTable() != null && this.getDataTable().getTable().getSelectionCount() > 0;
	}

	public boolean canCopyConsole() {
		return this.getConsole().getText() != null && this.getConsole().getText().isFocusControl() && this.getConsole().getText().getSelectionCount() > 0;
	}

	public boolean canSelectAllDataTable() {
		return this.getDataTable().getTable() != null && this.getDataTable().getTable().getItemCount() > 0;
	}

	public boolean canSelectAllConsole() {
		return this.getConsole().getText() != null && this.getConsole().getText().isFocusControl();
	}

	public boolean canDeleteDataTable() {
		return canCopyDataTable();
	}

	public boolean canClearDataTable() {
		return canSelectAllDataTable();
	}

	public boolean canClearConsole() {
		return canSelectAllConsole() && !this.getConsole().getText().getText().isEmpty();
	}

	public boolean canConnect() {
		return (RouterLoggerStatus.STARTING.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTED.equals(getCurrentStatus()) || RouterLoggerStatus.ERROR.equals(getCurrentStatus())) && (configuration.getInt("logger.iterations", Defaults.ITERATIONS) <= 0 || iteration <= configuration.getInt("logger.iterations", Defaults.ITERATIONS));
	}

	public boolean canDisconnect() {
		return !(RouterLoggerStatus.STARTING.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTED.equals(getCurrentStatus()) || RouterLoggerStatus.ERROR.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTING.equals(getCurrentStatus()));
	}

	/** Avvia il ciclo. */
	public void connect() {
		if (canConnect()) {
			exit = false;
			if (dataTable != null && dataTable.getTable() != null && !dataTable.getTable().isDisposed()) {
				iteration = dataTable.getTable().getItemCount() + 1;
			}
			updateThread = new Thread("updateThread") {
				@Override
				public void run() {
					outerLoop();
				}
			};
			updateThread.start();
		}
		else {
			logger.log(Resources.get("err.operation.not.allowed", getCurrentStatus().toString()), Destination.CONSOLE);
		}
	}

	private void disconnect(final boolean force) {
		if (canDisconnect() || force) {
			setStatus(RouterLoggerStatus.DISCONNECTING);
			exit = true;
			updateThread.interrupt();
		}
		else {
			logger.log(Resources.get("err.operation.not.allowed", getCurrentStatus().toString()), Destination.CONSOLE);
		}
	}

	/** Interrompe il ciclo e forza la disconnessione. */
	public void disconnect() {
		disconnect(false);
	}

	public void reset() {
		disconnect(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					updateThread.join();
				}
				catch (InterruptedException e) {}
				afterOuterLoop();
				configuration.reload();
				iteration = 1;
				setStatus(RouterLoggerStatus.STARTING);
				if (shell != null && !shell.isDisposed()) {
					try {
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								getConsole().getText().setText("");
								dataTable.reset();
								beforeOuterLoop();
								connect();
							}
						});
					}
					catch (SWTException se) {}
				}
			}
		}, "resetThread").start();
	}

	@Override
	protected void setStatus(RouterLoggerStatus status) {
		super.setStatus(status);
		if (trayIcon != null) {
			trayIcon.updateTrayItem(status);
		}
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	@Override
	public TextConsole getConsole() {
		return TextConsole.getInstance();
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

}
