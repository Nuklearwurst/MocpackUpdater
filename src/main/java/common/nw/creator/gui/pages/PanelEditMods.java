package common.nw.creator.gui.pages;

import common.nw.creator.Creator;
import common.nw.creator.gui.FileTransferHandler;
import common.nw.creator.gui.IDropFileHandler;
import common.nw.creator.gui.Reference;
import common.nw.creator.gui.TableModelList;
import common.nw.creator.gui.pages.dialog.DialogEditMod;
import common.nw.creator.gui_legacy.pages.dialog.EditBlackListDialog;
import common.nw.creator.gui_legacy.pages.dialog.ITableHolder;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;
import common.nw.modpack.ModInfo;
import common.nw.modpack.ModpackValues;
import common.nw.modpack.RepoMod;
import common.nw.utils.DownloadHelper;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * @author Nuklearwurst
 */
public class PanelEditMods  implements IPageHandler, IDropFileHandler, ITableHolder {

	private JButton btnNew;
	private JButton btnEdit;
	private JButton btnRemove;
	private JButton btnEditBlacklist;

	private final JFrame parentFrame;

	private JPanel panel_editmods;
	private JTable tableMods;
	private JCheckBox chbxHideMods;
	private JCheckBox chbxHideConfig;

	private final Creator creator;

	private List<RepoMod> mods;
	private List<RepoMod> blacklist;

	private List<RepoMod> hiddenFiles;


	/**
	 * Create the panel
	 */
	public PanelEditMods(Creator creator, JFrame parentFrame) {

		this.parentFrame = parentFrame;
		this.creator = creator;

		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] index = tableMods.getSelectedRows();
				if (index.length < 0) {
					return;
				}
				if(index.length > 1) {
					if (JOptionPane.showConfirmDialog(panel_editmods,
							"Are you sure you want to remove these files from the list?",
							"Are you sure", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				for(int i = index.length - 1; i >= 0; i--) {
					if(i < mods.size()) {
						mods.remove(tableMods.getRowSorter().convertRowIndexToModel(index[i]));
					}
				}
				tableMods.clearSelection();
				updateTable();
			}
		});

		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditDialog(false);
			}
		});

		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showEditDialog(true);
			}
		});

		btnEditBlacklist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				editBlackList();
			}
		});

		//TODO: maybe use RowFilters
		chbxHideMods.setActionCommand("hideMods");
		chbxHideMods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable();
			}
		});

		chbxHideConfig.setActionCommand("hideConfig");
		chbxHideConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTable();
			}
		});

		tableMods.setModel(new TableModelList(new String[] { "Name", "Version",
				"URL" }, new String[] { "name", "version", "downloadUrl" },
				creator.modpack.files));
		tableMods.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		tableMods.setTransferHandler(new FileTransferHandler(this));
		tableMods.setAutoCreateRowSorter(true);

	}

	private void hideFiles() {
		if(hiddenFiles == null) {
			hiddenFiles = new ArrayList<RepoMod>();
		}
		//hide mods
		if(chbxHideMods.isSelected()) {
			for(int i = mods.size() - 1; i >= 0; i--) {
				if((mods.get(i).fileName.startsWith("mods/") || mods.get(i).fileName.startsWith("mods" + File.separator)) && !hiddenFiles.contains(mods.get(i))) {
					hiddenFiles.add(mods.get(i));
					mods.remove(i);
				}
			}
		} else {
			for(int i = hiddenFiles.size() - 1; i >= 0 ; i--) {
				if(hiddenFiles.get(i).fileName.startsWith("mods/") || hiddenFiles.get(i).fileName.startsWith("mods" + File.separator)) {
					if(!mods.contains(hiddenFiles.get(i))) {
						mods.add(hiddenFiles.get(i));
					}
					hiddenFiles.remove(i);
				}
			}
		}
		//hids config
		if(chbxHideConfig.isSelected()) {
			for(int i = mods.size() - 1; i >= 0; i--) {
				if((mods.get(i).fileName.startsWith("config/") || mods.get(i).fileName.startsWith("config" + File.separator)) && !hiddenFiles.contains(mods.get(i))) {
					hiddenFiles.add(mods.get(i));
					mods.remove(i);
				}
			}
		} else {
			for(int i = hiddenFiles.size() - 1; i >= 0 ; i--) {
				if(hiddenFiles.get(i).fileName.startsWith("config/") || hiddenFiles.get(i).fileName.startsWith("config" + File.separator)) {
					if(!mods.contains(hiddenFiles.get(i))) {
						mods.add(hiddenFiles.get(i));
					}
					hiddenFiles.remove(i);
				}
			}
		}
	}

	/**
	 *
	 * @param mode
	 *            true equals new entry, false equals edit entry
	 */
	private void showEditDialog(boolean mode) {
		int index = mode ? mods.size() : tableMods.getRowSorter().convertRowIndexToModel(tableMods.getSelectedRow());
		if (index == -1) {
			return;
		}
		Dialog d = new DialogEditMod(parentFrame, mode, index, this);
		d.pack();
		d.setVisible(true);
	}

	public void updateTable() {
		hideFiles();
		((TableModelList) tableMods.getModel()).updateData();
		tableMods.revalidate();
		tableMods.repaint();
	}

	@Override
	public boolean dropFile(File file) {
		if (!file.exists()) {
			return false;
		}
		String absolutePath = file.getAbsolutePath();

		/** path within the minecraft installation */
		String mcRelativePath = absolutePath;
		/** base dir */
		String baseDirPath = "";
		/** added base dir */
		String baseDirToAdd = "";

		//split absolutePath in two parts
		if(mcRelativePath.contains(File.separator + "mods" + File.separator)) {
			int index = absolutePath.indexOf("mods" + File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
		} else if(mcRelativePath.contains(File.separator + "config" + File.separator)) {
			int index = absolutePath.indexOf("config" + File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
		} else if(mcRelativePath.endsWith(".jar")) {
			int index = absolutePath.lastIndexOf(File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
			baseDirToAdd = File.separator + "mods";
		} else if(mcRelativePath.endsWith(".cfg")) {
			int index = absolutePath.lastIndexOf(File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
			baseDirToAdd = File.separator + "config";
		}

		ModInfo mod = new ModInfo(mcRelativePath);
		mod.loadInfo(new File(baseDirPath));
		//insert default folders if needed after info loaded
		mod.fileName = baseDirToAdd + mod.fileName;
		mod.name = mod.version = mod.fileName.replace(File.separator, "/");

		RepoMod repo = new RepoMod();
		repo.name = mod.name;
		repo.version = mod.version;
		if(repo.version == null) {
			repo.version = repo.name;
		}
		repo.downloadUrl = "EDIT_DOWNLOAD_URL_PLEASE";
		repo.downloadType = "PLEASE_EDIT";
		repo.fileName = mod.fileName;
		repo.md5 = DownloadHelper.getHash(file);

		if (mod.hasName) {
			repo.nameType = ModpackValues.nameTypeZipEntry;
		} else {
			repo.nameType = ModpackValues.nameTypeFileName;
		}
		if (mod.hasVersionFile) {
			repo.versionType = ModpackValues.versionTypeZipEntry;
		} else if(mod.fileName.startsWith("config" + File.separator)) {
			repo.version = DateFormat.getDateInstance().format(new Date(System.currentTimeMillis()));
			repo.versionType = ModpackValues.versionTypeTracked;
		} else {
			repo.versionType = ModpackValues.versionTypeFileName;
		}
		this.mods.add(repo);
		updateTable();
		return true;
	}

	private void editBlackList() {
		EditBlackListDialog dialog = new EditBlackListDialog(parentFrame, true, blacklist);
		dialog.setVisible(true);
		//TODO edit blacklist dialog (--> edit mod dialog)
	}

	@Override
	public void setValue(int index, Object o) {
		mods.set(index, (RepoMod) o);
	}

	@Override
	public void addValue(Object o) {
		mods.add((RepoMod) o);
	}

	@Override
	public void removeValue(int index) {
		mods.remove(index);
	}

	@Override
	public Object getValue(int index) {
		return mods.get(index);
	}

	@Override
	public Object getProperty(String s) {
		if(s.equals(Reference.KEY_NAME)) {
			return "Edit Mods";
		}
		if(s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		this.mods = creator.modpack.files;
		this.blacklist = creator.modpack.blacklist;
		((TableModelList)this.tableMods.getModel()).setValues(creator.modpack.files);
		updateTable();
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		//insert hidden files
		this.mods.addAll(hiddenFiles);
		hiddenFiles.clear();
		//probably not needed
		creator.modpack.files = this.mods;
		creator.modpack.blacklist = this.blacklist;
		return true;
	}

	public JPanel getPanel() {
		return panel_editmods;
	}
}
