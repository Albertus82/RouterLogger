package it.albertus.router.gui;

import it.albertus.router.gui.listener.AboutSelectionListener;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.gui.listener.CopySelectionListener;
import it.albertus.router.gui.listener.EditArmListener;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MenuBar {

	private final Menu bar;

	private final Menu fileMenu;
	private final MenuItem fileMenuHeader;
	private final MenuItem fileExitItem;

	private final Menu editMenu;
	private final MenuItem editMenuHeader;
	private final MenuItem editCopyMenuItem;

	private final Menu helpMenu;
	private final MenuItem helpMenuHeader;
	private final MenuItem helpAboutItem;

	public MenuBar(final RouterLoggerGui gui) {
		bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN); // File
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Resources.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText(Resources.get("lbl.menu.item.exit"));
		fileExitItem.addSelectionListener(new CloseListener(gui));

		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN); // Edit
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Resources.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		editMenuHeader.addArmListener(new EditArmListener(gui));

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Resources.get("lbl.menu.item.copy") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_COPY);

		helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN); // Help
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Resources.get("lbl.menu.header.help"));
		helpMenuHeader.setMenu(helpMenu);

		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText(Resources.get("lbl.menu.item.about"));
		helpAboutItem.addSelectionListener(new AboutSelectionListener(gui));

		gui.getShell().setMenuBar(bar);
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFileMenuHeader() {
		return fileMenuHeader;
	}

	public MenuItem getFileExitItem() {
		return fileExitItem;
	}

	public Menu getBar() {
		return bar;
	}

	public Menu getEditMenu() {
		return editMenu;
	}

	public MenuItem getEditMenuHeader() {
		return editMenuHeader;
	}

	public MenuItem getEditCopyMenuItem() {
		return editCopyMenuItem;
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public MenuItem getHelpMenuHeader() {
		return helpMenuHeader;
	}

	public MenuItem getHelpAboutItem() {
		return helpAboutItem;
	}

}
