package common.nw.installer.gui;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import common.nw.core.gui.IExtendedPageHandler;
import common.nw.core.gui.PageHolder;
import common.nw.core.utils.SwingUtils;
import common.nw.core.utils.Utils;
import common.nw.core.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nuklearwurst
 */
public class PanelSettings implements IExtendedPageHandler {
	private JTextPane txtpnInstallerSettings;
	protected JTextField txtVersionName;
	private JButton btnModpackName;
	protected JTextField txtMinecraft;
	private JButton btnOpen;
	protected JCheckBox chbxCreateProfile;
	protected JTextField txtProfile;
	private JButton btnProfileSettings;
	private JPanel panel_settings;
	private JLabel lblProfile;

	public PanelSettings(final InstallerWindow window) {
		txtpnInstallerSettings.setBackground(SystemColor.menu);
		chbxCreateProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateCreateProfileCheckBox(window);
			}
		});

		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File folder = new File(txtMinecraft.getText());
				if (!folder.exists()) {
					folder = new File(Utils.getMinecraftDir());
				}
				String s = SwingUtils.openFolder(panel_settings, folder);
				if (s != null) {
					txtMinecraft.setText(s);
				}
			}
		});

		btnModpackName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtVersionName.setText(window.modpack.modpackName);
			}
		});

		btnProfileSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.openProfileSettingsDialog();
			}
		});
	}

	private void updateCreateProfileCheckBox(final InstallerWindow window) {
		boolean b = chbxCreateProfile.isSelected();
		lblProfile.setEnabled(b);
		txtProfile.setEnabled(b);
		btnProfileSettings.setEnabled(b);

		if (txtProfile.getText() == null
				|| txtProfile.getText().isEmpty()) {
			txtProfile.setText(window.modpack.modpackName);

			if (!readExsistingProfile(window, window.modpack.modpackName)) {
				if (window.profile_gameDirectory == null
						|| window.profile_gameDirectory.isEmpty()) {
					//Insert default value

					window.profile_gameDirectory = Utils.getMinecraftDir();
					if (window.profile_gameDirectory == null) {
						//.minecraft dir not found
						window.openProfileSettingsDialog();
					} else {
						window.profile_gameDirectory += File.separator + "modpacks"
								+ File.separator + window.modpack.modpackName;
					}
				}
				if (window.profile_javaOptions == null
						|| window.profile_javaOptions.isEmpty()) {
					window.profile_javaOptions = DialogProfileSettings.DEFAULT_JAVA_OPTIONS;
				}
			}
		}
	}

	private boolean readExsistingProfile(final InstallerWindow installer, String profileName) {
		//open profileFile
		File launcherProfiles = new File(txtMinecraft.getText() + File.separator + "launcher_profiles.json");
		if (!launcherProfiles.exists()) {
			//No profiles
			return false;
		}
		//init parser
		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;

		//parse File
		try {
			jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
		} catch (Exception e) {
			//error parsing
			NwLogger.INSTALLER_LOGGER.warn("Error reading profile-data!");
			return false;
		}
		//getData
		HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
		//format values to String array
		ArrayList<String> options = new ArrayList<>();
		for (JsonStringNode node : profileCopy.keySet()) {
			options.add(node.getText());
		}
		//cancel when empty
		if (options.isEmpty()) {
			return false;
		}
		//update data
		for (Map.Entry<JsonStringNode, JsonNode> entry : profileCopy.entrySet()) {
			if (entry.getKey().getText().equals(profileName)) {
				//minecraft data
				installer.profile_gameDirectory = entry.getValue().getStringValue("gameDir");
				installer.profile_javaOptions = entry.getValue().getStringValue("javaArgs");
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getProperty(String s) {
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {

	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		return true;
	}

	@Override
	public JPanel getPanel() {
		return panel_settings;
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		panel_settings = new JPanel();
		panel_settings.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
		txtVersionName = new JTextField();
		txtVersionName.setToolTipText("The name of the version that gets created in the minecraft launcher.");
		panel_settings.add(txtVersionName, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		btnModpackName = new JButton();
		btnModpackName.setText("Reset");
		panel_settings.add(btnModpackName, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Version name:");
		label1.setToolTipText("The name of the version that gets created in the minecraft launcher.");
		panel_settings.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Minecraft:");
		label2.setToolTipText("The path to your .minecraft folder.\nShould be correct for most people.");
		panel_settings.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtMinecraft = new JTextField();
		txtMinecraft.setToolTipText("The path to your .minecraft folder.\nShould be correct for most people.");
		panel_settings.add(txtMinecraft, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		btnOpen = new JButton();
		btnOpen.setText("Open");
		panel_settings.add(btnOpen, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		chbxCreateProfile = new JCheckBox();
		chbxCreateProfile.setText("Create Profile");
		chbxCreateProfile.setToolTipText("Check this if you want to automatically create a profile using this modpack.");
		panel_settings.add(chbxCreateProfile, new GridConstraints(3, 0, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
		lblProfile = new JLabel();
		lblProfile.setEnabled(false);
		lblProfile.setText("Profile Name:");
		lblProfile.setToolTipText("The name of the profile.");
		panel_settings.add(lblProfile, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 2, false));
		txtProfile = new JTextField();
		txtProfile.setEnabled(false);
		txtProfile.setToolTipText("The name of the profile.");
		panel_settings.add(txtProfile, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		btnProfileSettings = new JButton();
		btnProfileSettings.setEnabled(false);
		btnProfileSettings.setText("Profile Settings");
		btnProfileSettings.setToolTipText("Modpack Profile settings, such as installation directory.");
		panel_settings.add(btnProfileSettings, new GridConstraints(4, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel_settings.add(scrollPane1, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		txtpnInstallerSettings = new JTextPane();
		txtpnInstallerSettings.setContentType("text/html");
		txtpnInstallerSettings.setEditable(false);
		txtpnInstallerSettings.setFont(new Font(txtpnInstallerSettings.getFont().getName(), txtpnInstallerSettings.getFont().getStyle(), txtpnInstallerSettings.getFont().getSize()));
		txtpnInstallerSettings.setText("<html>\r\n  <head>\r\n    \r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      <b>Installer Settings:<br>Now select your .minecraft-folder (usually \r\n      preset) and the version name (used inside the mc-launcher)<br>You can \r\n      also create a Profile. </b>\r\n    </p>\r\n  </body>\r\n</html>\r\n");
		scrollPane1.setViewportView(txtpnInstallerSettings);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel_settings;
	}
}
