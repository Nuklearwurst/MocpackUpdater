package common.nw.updater.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.updater.Updater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Nuklearwurst
 */
public class UpdateWindow implements IProgressWatcher, WindowListener {


	private JProgressBar pbOverall;
	private JProgressBar pbDownload;
	private JButton btnCancel;
	private JLabel lblOverallProgress;
	private JLabel lblDownloadProgress;
	private JPanel contentPanel;
	private JFrame frmUpdater;


	private boolean isCancelled = false;
	private boolean isPaused = false;
	private boolean quitToLauncher = false;

	public UpdateWindow() {
		frmUpdater = new JFrame();
		frmUpdater.setResizable(false);
		frmUpdater.setTitle("Updater");
		frmUpdater.setBounds(100, 100, 378, 199);
		frmUpdater.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmUpdater.setContentPane(contentPanel);

		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
	}

	public UpdateWindow(Updater updater) {
		this();
		updater.setListener(this);
	}


	@SuppressWarnings("SameParameterValue")
	public void setVisible(boolean b) {
		frmUpdater.setVisible(b);
	}

	private void cancel() {
		isPaused = true;
		String[] options = new String[]{"Quit to launcher", "Continue without updating", "Cancel"};
		int r = JOptionPane.showOptionDialog(frmUpdater, "Are you sure you want to cancel the update process?", "Cancel", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (r == JOptionPane.YES_OPTION) {
			isCancelled = true;
			quitToLauncher = true;
		} else if (r == JOptionPane.NO_OPTION) {
			isCancelled = true;
		}
		isPaused = false;
	}

	public void close() {
		frmUpdater.dispose();
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public boolean isPaused() {
		return isPaused;
	}

	@Override
	public boolean quitToLauncher() {
		return quitToLauncher;
	}

	public int showErrorDialog(String title, String message) {
		String[] options = {"Retry", "Quit To Launcher",
				"Continue without updating"};
		return JOptionPane.showOptionDialog(frmUpdater, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
	}

	@Override
	public int showConfirmDialog(String message, String title, int optionType,
	                             int messageType) {
		return JOptionPane.showConfirmDialog(frmUpdater, message, title, optionType, messageType);
	}

	@Override
	public String showInputDialog(String message) {
		return JOptionPane.showInputDialog(frmUpdater, message);
	}

	@Override
	public int showOptionDialog(String msg, String title, int optionType,
	                            int messageType, Icon icon, String[] options, String defaultOption) {
		return JOptionPane.showOptionDialog(frmUpdater, msg, title, optionType, messageType, icon, options, defaultOption);
	}

	@Override
	public void setDownloadProgress(String msg) {
		Updater.logger.info("Secondary Progress: " + msg);
		lblDownloadProgress.setText(msg);
	}

	@Override
	public void setDownloadProgress(int progress) {
		pbDownload.setValue(progress);
	}

	@Override
	public void setDownloadProgress(String msg, int progress) {
		setDownloadProgress(msg);
		setDownloadProgress(progress);
	}

	@Override
	public void setDownloadProgress(String msg, int progress, int maxProgress) {
		pbDownload.setMaximum(maxProgress);
		setDownloadProgress(msg, progress);
	}

	@Override
	public void setOverallProgress(int progress) {
		pbOverall.setValue(progress);
	}

	@Override
	public void setOverallProgress(String msg, int progress) {
		Updater.logger.info("Primary Progress: " + msg + ": " + progress);
		lblOverallProgress.setText(msg);
		setOverallProgress(progress);
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		isCancelled = true;
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
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
		contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayoutManager(7, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
		lblOverallProgress = new JLabel();
		lblOverallProgress.setText("Initializing...");
		contentPanel.add(lblOverallProgress, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
		btnCancel = new JButton();
		btnCancel.setText("Cancel");
		contentPanel.add(btnCancel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblDownloadProgress = new JLabel();
		lblDownloadProgress.setText("Loading...");
		contentPanel.add(lblDownloadProgress, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
		pbOverall = new JProgressBar();
		pbOverall.setStringPainted(true);
		pbOverall.setToolTipText("Overall Progress");
		contentPanel.add(pbOverall, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
		pbDownload = new JProgressBar();
		pbDownload.setStringPainted(true);
		pbDownload.setToolTipText("Download Progress");
		contentPanel.add(pbDownload, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		contentPanel.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		contentPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPanel;
	}
}
