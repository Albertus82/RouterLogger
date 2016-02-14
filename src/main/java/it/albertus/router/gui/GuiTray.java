package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.NewLine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class GuiTray {

	private enum TrayIcon {
		OK(RouterLoggerStatus.OK, GuiImages.TRAY_ICON_ROUTER_OK),
		INFO(RouterLoggerStatus.WARNING, GuiImages.TRAY_ICON_ROUTER_WARNING),
		WARNING(RouterLoggerStatus.WARNING, GuiImages.TRAY_ICON_ROUTER_WARNING),
		ERROR(RouterLoggerStatus.ERROR, GuiImages.TRAY_ICON_ROUTER_WARNING);

		private final RouterLoggerStatus status;
		private final Image icon;

		private TrayIcon(final RouterLoggerStatus status, final Image icon) {
			this.status = status;
			this.icon = icon;
		}
	}

	private interface Defaults {
		boolean GUI_TRAY_DYNAMIC = true;
	}

	private static class Singleton {
		private static final GuiTray TRAY = new GuiTray();
	}

	public static GuiTray getInstance() {
		return Singleton.TRAY;
	}

	private GuiTray() {}

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private TrayItem trayItem = null;
	private Menu menu = null;
	private String toolTipText = Resources.get("lbl.tray.tooltip");
	private TrayIcon trayIcon = TrayIcon.OK;

	public void init(final Shell shell) {
		if (this.trayItem == null && menu == null) {
			shell.addShellListener(new ShellAdapter() {
				@Override
				public void shellIconified(ShellEvent e) {
					iconify(shell);
					shell.setMinimized(false);
				}
			});
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	private void iconify(final Shell shell) {
		Tray tray = shell.getDisplay().getSystemTray();
		if (tray != null) {
			shell.setVisible(false);
			boolean addListeners = false;
			if (trayItem == null) {
				trayItem = new TrayItem(tray, SWT.NONE);
				trayItem.setImage(GuiImages.TRAY_ICON_ROUTER_OK);
				trayItem.setToolTipText(toolTipText);
				addListeners = true;
			}
			else {
				trayItem.setVisible(true);
			}

			if (menu == null) {
				menu = new Menu(shell, SWT.POP_UP);
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(Resources.get("lbl.tray.show"));

				menuItem.addListener(SWT.Selection, new RestoreListener(shell));

				menuItem = new MenuItem(menu, SWT.SEPARATOR);

				// Tasto "Exit"...
				menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(Resources.get("lbl.tray.close"));
				menuItem.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (!GuiCloseMessageBox.show() || GuiCloseMessageBox.newInstance(shell).open() == SWT.YES) {
							shell.dispose();
						}
					}
				});
			}

			if (addListeners) {
				trayItem.addListener(SWT.MenuDetect, new Listener() {
					@Override
					public void handleEvent(Event event) {
						menu.setVisible(true);
					}
				});

				trayItem.addListener(SWT.DefaultSelection, new RestoreListener(shell));
			}
		}
	}

	public void updateTrayItem(final RouterData info, final RouterLoggerStatus status) {
		if (configuration.getBoolean("gui.tray.dynamic", Defaults.GUI_TRAY_DYNAMIC) && trayItem != null && !trayItem.isDisposed()) {
			final String updatedToolTipText;
			if (!configuration.getGuiImportantKeys().isEmpty() && info != null && info.getData() != null && !info.getData().isEmpty()) {
				final StringBuilder sb = new StringBuilder(Resources.get("lbl.tray.tooltip"));
				for (final String key : configuration.getGuiImportantKeys()) {
					if (info.getData().containsKey(key)) {
						sb.append(NewLine.SYSTEM_LINE_SEPARATOR).append(key).append(": ").append(info.getData().get(key));
					}
				}
				updatedToolTipText = sb.toString();
			}
			else {
				updatedToolTipText = toolTipText;
			}
			if (!updatedToolTipText.equals(toolTipText) || !trayIcon.status.equals(status)) {
				try {
					trayItem.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (!trayItem.isDisposed()) {
								if (!updatedToolTipText.equals(toolTipText)) {
									toolTipText = updatedToolTipText;
									trayItem.setToolTipText(toolTipText);
								}
								if (!trayIcon.status.equals(status)) {
									final TrayIcon newTrayIcon = TrayIcon.valueOf(status.name());
									if (newTrayIcon != null) {
										trayIcon = newTrayIcon;
										trayItem.setImage(trayIcon.icon);
									}
								}
							}
						}
					});
				}
				catch (SWTException se) {
					Logger.getInstance().log(se, Destination.CONSOLE);
				}
			}
		}
	}

	private final class RestoreListener implements Listener {
		private final Shell shell;

		private RestoreListener(Shell shell) {
			this.shell = shell;
		}

		@Override
		public void handleEvent(Event event) {
			shell.setVisible(true);
			trayItem.setVisible(false);
		}
	}

}
