/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of JEncConv.
 * 
 * JEncConv is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JEncConv is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JEncConv.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sf.jencconv;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static net.sf.jencconv.Converter.BUF_SIZE;
import static net.sf.jencconv.Converter.chainPlugins;
import static net.sf.jencconv.Converter.convert;
import static net.sf.simpleswing.GridBagContainerFiller.makeTopHorizFillHolder;
import static net.sf.simpleswing.LafUtils.applyStartupLookAndFeel;
import static net.sf.simpleswing.LafUtils.getAvailableLookAndFeels;
import static net.sf.simpleswing.LafUtils.getLafClassName;
import static net.sf.simpleswing.LafUtils.getLookAndFeelNames;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.management.timer.Timer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import net.sf.simpleswing.FormMaker;
import net.sf.simpleswing.RowListMaker;
import net.sf.simpleswing.RowMaker;

class GUI implements ActionListener, ListSelectionListener {

	private static final String PROGRAM_VERSION = "2.1.1";
	private static final Properties props = new Properties();
	private static final File propsFile;
	private static final Charset propsFileCharset = Charset.forName("UTF-8");
	private static final String lafKey = "LookAndFeel";
	private static final String inEncKey = "SrcEnc", outEncKey = "DestEnc";
	private static final String storeEncKey = "StoreEncodings";
	private static final String pluginsUsedKey = "Plugins";
	private static final String storePluginsKey = "StorePlugins";
	private static final String applyPlgPrevKey = "ApplyPluginsForPreview";
	private static final String applyPlgConvKey = "ApplyPluginsForConversion";
	private static final String updateChkIntervalKey = "UpdateCheckInterval";
	private static final String lastUpdateChkKey = "LastUpdateCheck";

	private static final String inpErrActKey = "InputErrAct";
	private static final String inpErrCustomReplaceKey = "InputErrCustomReplStr";
	private static final String outErrActKey = "OutputErrAct";
	private static final String outErrCustomReplaceKey = "OutputErrCustomReplStr";

	private static enum PreviewAreaLocation {
		RIGHT, BOTTOM, SEPARATE_TAB
	}

	private static final String previewAreaLocationKey = "PreviewAreaLocation";

	/** Date formatter. Not thread safe. Use only from the Event Dispatch Thread */
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
	private static final Map<String, Integer> updateDays;
	private static String startupLafClassName;

	private final static Comparator<ReaderFactory> readerFactoryCaseInsensComp = new Comparator<ReaderFactory>() {
		@Override
		public int compare(ReaderFactory o1, ReaderFactory o2) {
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	};

	static {
		String userHome = System.getProperty("user.home");
		String fileName = userHome + File.separator + ".jencconv";
		propsFile = new File(fileName);

		updateDays = new LinkedHashMap<String, Integer>();
		updateDays.put("Monthly", 30);
		updateDays.put("Weekly", 7);
		updateDays.put("Daily", 1);
	}

	private boolean storeEncodings = Boolean.parseBoolean(props.getProperty(
			storeEncKey, "true"));
	private boolean storePlugins = Boolean.parseBoolean(props.getProperty(
			storePluginsKey, "true"));
	private PreviewAreaLocation previewAreaLocation;

	{
		// instance initializer block

		String storedLoc = props.getProperty(previewAreaLocationKey,
				PreviewAreaLocation.RIGHT.toString());
		try {
			previewAreaLocation = PreviewAreaLocation.valueOf(storedLoc);
		} catch (Exception e) {
			System.err.println("Invalid previewAreaLocationKey stored: "
					+ storedLoc);
			previewAreaLocation = PreviewAreaLocation.RIGHT;
			props.setProperty(previewAreaLocationKey,
					previewAreaLocation.toString());
		}
	}

	// GUI main window
	private JFrame mainFrame;
	private final String mainFrameTitle = "JEncConv";
	private JTabbedPane mainTabbedPane;
	private final String mainTabTxt = "Convert", pluginsTabTxt = "Plugins",
			optionsTabTxt = "Options", previewTabTxt = "Preview";
	private JPanel mainPanel, pluginsPanel, optionsPanel;
	private final String prefsPanelTxt = "Preferences";

	// main tab
	private JPanel inputPanel, outputPanel;
	private final String inputPanelTxt = "Input File",
			outputPanelTxt = "Output File", displayPanelTxt = "Preview";
	private JFileChooser fileChooser;
	// input panel
	private JLabel inpFileLbl, inpEncLbl, inpErrLbl;
	private JTextField inpFileTxtF, inpAliasTxtF, inpErrReplaceTxtF;
	private JCheckBox inpApplyPluginsChk;
	private JButton inpBrowseBtn, inpDetectEncBtn, inpLoadBtn;
	private JComboBox<String> inpEncCmbBox, inpErrCmbBox;
	private final String inpFileLblTxt = "File:", inpEncLblTxt = "Encoding:",
			inpErrLblTxt = "On error:",
			inpAliasTxtFToolTip = "name and aliases of last used encoding",
			inpBrowseBtnTxt = "Browse...",
			inpDetectEncBtnTxt = "Detect possible encodings",
			inpApplyPluginsChkTxt = "Apply plugins for preview",
			inpLoadBtnTxt = "Load for preview",
			inpBrowseBtnAC = "inpBrowseBtn",
			inpDetectEncBtnAC = "inpDetectEncBtn",
			inpErrCmbBoxAC = "inpErrCmbBox", inpLoadBtnAC = "inpLoadBtn",
			encCmbBoxToolTip = "Encoding name or alias";
	private final String inpErrActReport = "fail",
			inpErrActReplDefault = "replace with \uFFFD (U+FFFD)",
			inpErrActReplCustom = "replace with string:",
			inpErrActIgnore = "ignore";
	private final String[] inpErrActions = { inpErrActReport,
			inpErrActReplDefault, inpErrActReplCustom, inpErrActIgnore };
	// plugins panel
	private final String plgInUseLblTxt = "Plugins in use:",
			plgAllLblTxt = "Available plugins:",
			plgDescrLblTxt = "Plugin Description:";
	private final String plgUseAC = "plgUse", plgRemAC = "plgRem",
			plgTopAC = "plgTop", plgUpAC = "plgUp", plgDownAC = "plgDown",
			plgBotAC = "plgBot";
	private final String plgUseTxt = "use plugin →",
			plgRemTxt = "← remove plugin", plgTopTxt = "move to top ⤒",
			plgUpTxt = "move up ↑", plgDownTxt = "move down ↓",
			plgBotTxt = "move to bottom ⤓";
	private DefaultListModel<ReaderFactory> plgInUseListModel;
	private JList<ReaderFactory> plgInUseList, plgAvailableList;
	private JLabel plgDescrLbl;
	private JTextArea plgDescrTA;
	// output panel
	private JLabel outEncLbl, outErrLbl;
	private JComboBox<String> outEncCmbBox, outErrCmbBox;
	private JRadioButton outOverwriteRadioBtn, outFileRadioBtn;
	private JTextField outFileTxtF, outAliasTxtF, outErrReplaceTxtF;
	private JButton outBrowseBtn, outWriteBtn;
	private JCheckBox outApplyPluginsChk;
	private final String outEncLblTxt = "Encoding:",
			outErrLblTxt = "On error:",
			outErrCmbBoxAC = "outErrCmbBox",
			outOverwriteRadioBtnTxt = "Replace original (after renaming it to .bak)",
			outOverwriteRadioBtnAC = "outOverwriteRadioBtn",
			outFileRadioBtnTxt = "File:",
			outFileRadioBtnAC = "outFileRadioBtn",
			outAliasTxtFToolTip = "name and aliases of last used encoding",
			outBrowseBtnTxt = "Browse...", outWriteBtnTxt = "Convert",
			outBrowseBtnAC = "outBrowseBtn", outWriteBtnAC = "outWriteBtn",
			outApplyPluginsChkTxt = "Apply plugins for conversion";
	private final String outErrActReport = "fail",
			outErrActReplDefault = "default replace (often '?')",
			outErrActReplCustom = "replace with string:",
			outErrActIgnore = "ignore";
	private final String[] outErrActions = { outErrActReport,
			outErrActReplDefault, outErrActReplCustom, outErrActIgnore };
	// display panel
	private JScrollPane displaySP;
	private JTextArea displayTA;

	// plugins tab

	// options tab
	private JLabel optThemeLbl, optPreviewAreaLbl, optChkUpdateLbl;
	private JComboBox<String> optThemeCmbBox, optChkUpdateCmbBox;
	private JComboBox<PreviewAreaLocation> optPreviewAreaCmbBox;
	private JCheckBox storeEncodingsChk, storePluginsChk;
	private JButton optChkUpdateBtn, optApplyBtn;
	private final String optThemeLblTxt = "Theme:",
			optPreviewAreaLblTxt = "Preview Area Location:",
			optChkUpdateLblTxt = "Check for updates:",
			storeEncodingsChkTxt = "Remember Encodings used",
			storePluginsChkTxt = "Remember Plugins used",
			optApplyBtnTxt = "Apply", optApplyBtnAC = "optApplyBtn",
			optChkUpdateBtnTxt = "Check now";

	// menu
	private JMenuBar menuBar;
	private final String menuHelpTxt = "Help",
			menuHelpShowEncAliasesTxt = "Show all aliases",
			menuHelpChkUpdatesTxt = "Check for updates",
			menuHelpAboutTxt = "About";
	private final String showEncAliasesAC = "showEncodingsAndAliases",
			chkUpdatesAC = "Check for updates", helpAboutAC = "About",
			browseSfPageAC = "browseSfPage";
	private final String sfPageAddr = "http://sourceforge.net/projects/jencconv/";

	// help-about panel
	private JPanel helpAboutPanel;
	private final String browseWebsiteAC = "browseWebsite";
	private final String websiteAddr = "http://jencconv.sourceforge.net/";

	/** Most recent checker, or null. Ignore results from other checkers. */
	private UpdateChecker crtUpdateChecker;

	private GUI() {
	}

	private void init() {
		makeMainFrame();
		fileChooser = new JFileChooser(".");
	}

	private void makeMainFrame() {
		makeMainPanel();
		makePluginsPanel();
		makeOptionsPanel();

		mainFrame = new JFrame(mainFrameTitle);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<Image> imgList = new ArrayList<Image>();
		Class<GUI> cls = GUI.class;
		for (String imgStr : new String[] { "100.png", "48.png", "32.png",
				"16.png" }) {
			URL imgUrl = cls.getResource("/icons/JEncConv-" + imgStr);
			if (imgUrl != null)
				imgList.add(new ImageIcon(imgUrl).getImage());
		}
		mainFrame.setIconImages(imgList);

		mainTabbedPane = new JTabbedPane();
		mainTabbedPane.addTab(mainTabTxt, mainPanel);
		mainTabbedPane.addTab(pluginsTabTxt, pluginsPanel);
		mainTabbedPane.addTab(optionsTabTxt, optionsPanel);

		addPreviewArea();

		RowListMaker rm = new RowListMaker(mainFrame.getContentPane());
		rm.addFillComponent(mainTabbedPane);

		makeMenu();
		mainFrame.setJMenuBar(menuBar);

		makeHelpAboutPanel();
	}

	private void makeMainPanel() {
		String[] charsets = Charset.availableCharsets().keySet()
				.toArray(new String[0]);
		String defCs = Charset.defaultCharset().name();
		String utf8Cs = Charset.forName("UTF-8").name();

		// input panel
		inpFileLbl = new JLabel(inpFileLblTxt);
		inpFileTxtF = new JTextField(10);
		inpBrowseBtn = new JButton(inpBrowseBtnTxt);
		inpDetectEncBtn = new JButton(inpDetectEncBtnTxt);
		inpEncLbl = new JLabel(inpEncLblTxt);
		String inEnc = storeEncodings ? props.getProperty(inEncKey, defCs)
				: defCs;
		if (inEnc == null || inEnc.isEmpty())
			inEnc = utf8Cs;
		inpEncCmbBox = makeEncodingCmbBox(charsets, inEnc);
		inpAliasTxtF = new JTextField();
		inpAliasTxtF.setToolTipText(inpAliasTxtFToolTip);
		inpAliasTxtF.setEditable(false);
		inpAliasTxtF.setEnabled(false);

		inpErrLbl = new JLabel(inpErrLblTxt);
		inpErrCmbBox = new JComboBox<>(inpErrActions);
		String storedInpErrAct = props.getProperty(inpErrActKey);
		for (String s : inpErrActions) {
			if (s.equals(storedInpErrAct)) {
				inpErrCmbBox.setSelectedItem(s);
				break;
			}
		}
		inpErrCmbBox.addActionListener(this);
		inpErrCmbBox.setActionCommand(inpErrCmbBoxAC);

		inpErrReplaceTxtF = new JTextField(
				props.getProperty(inpErrCustomReplaceKey), 3);
		inpErrReplaceTxtF.setEnabled(inpErrCmbBox.getSelectedItem().equals(
				inpErrActReplCustom));
		inpErrReplaceTxtF.setMinimumSize(inpErrReplaceTxtF.getPreferredSize());

		inpApplyPluginsChk = new JCheckBox(inpApplyPluginsChkTxt);
		inpApplyPluginsChk.setSelected(Boolean.parseBoolean(props.getProperty(
				applyPlgPrevKey, "true")));
		inpLoadBtn = new JButton(inpLoadBtnTxt);

		inputPanel = makeTitledPanel(inputPanelTxt);
		RowListMaker rm = new RowListMaker(inputPanel);
		RowMaker row = new RowMaker(new JPanel());
		row.addComponents(inpFileLbl);
		row.addHorizFillComponent(inpFileTxtF);
		row.addComponents(inpBrowseBtn);
		rm.addHorizFillComponentZeroInsets(row.container);
		rm.addLine(inpDetectEncBtn);
		row = new RowMaker(new JPanel());
		row.addComponents(inpEncLbl, inpEncCmbBox);
		row.addHorizFillComponent(inpAliasTxtF);
		rm.addHorizFillComponentZeroInsets(row.container);
		rm.addLine(inpErrLbl, inpErrCmbBox, inpErrReplaceTxtF);
		rm.addLine(inpLoadBtn, inpApplyPluginsChk);

		inpBrowseBtn.setActionCommand(inpBrowseBtnAC);
		inpDetectEncBtn.setActionCommand(inpDetectEncBtnAC);
		inpLoadBtn.setActionCommand(inpLoadBtnAC);
		inpBrowseBtn.addActionListener(this);
		inpDetectEncBtn.addActionListener(this);
		inpLoadBtn.addActionListener(this);

		// output panel
		outOverwriteRadioBtn = new JRadioButton(outOverwriteRadioBtnTxt);
		outOverwriteRadioBtn.setSelected(true);
		outFileRadioBtn = new JRadioButton(outFileRadioBtnTxt);
		outFileTxtF = new JTextField(10);
		outFileTxtF.setEnabled(false);
		outBrowseBtn = new JButton(outBrowseBtnTxt);
		outBrowseBtn.setEnabled(false);
		outWriteBtn = new JButton(outWriteBtnTxt);

		outEncLbl = new JLabel(outEncLblTxt);
		String outEnc = storeEncodings ? props.getProperty(outEncKey, utf8Cs)
				: utf8Cs;
		if (outEnc == null || outEnc.isEmpty())
			outEnc = utf8Cs;
		outEncCmbBox = makeEncodingCmbBox(charsets, outEnc);
		outAliasTxtF = new JTextField();
		outAliasTxtF.setToolTipText(outAliasTxtFToolTip);
		outAliasTxtF.setEditable(false);
		outAliasTxtF.setEnabled(false);

		outErrLbl = new JLabel(outErrLblTxt);
		outErrCmbBox = new JComboBox<>(outErrActions);
		String storedOutErrAct = props.getProperty(outErrActKey);
		for (String s : outErrActions) {
			if (s.equals(storedOutErrAct)) {
				outErrCmbBox.setSelectedItem(s);
				break;
			}
		}
		outErrCmbBox.addActionListener(this);
		outErrCmbBox.setActionCommand(outErrCmbBoxAC);

		outErrReplaceTxtF = new JTextField(
				props.getProperty(outErrCustomReplaceKey), 3);
		outErrReplaceTxtF.setEnabled(outErrCmbBox.getSelectedItem().equals(
				outErrActReplCustom));
		outErrReplaceTxtF.setMinimumSize(outErrReplaceTxtF.getPreferredSize());

		outApplyPluginsChk = new JCheckBox(outApplyPluginsChkTxt);
		outApplyPluginsChk.setSelected(Boolean.parseBoolean(props.getProperty(
				applyPlgConvKey, "true")));

		outputPanel = makeTitledPanel(outputPanelTxt);
		rm = new RowListMaker(outputPanel);
		rm.addLine(outOverwriteRadioBtn);
		row = new RowMaker(new JPanel());
		row.addComponents(outFileRadioBtn);
		row.addHorizFillComponent(outFileTxtF);
		row.addComponents(outBrowseBtn);
		rm.addHorizFillComponentZeroInsets(row.container);
		row = new RowMaker(new JPanel());
		row.addComponents(outEncLbl, outEncCmbBox);
		row.addHorizFillComponent(outAliasTxtF);
		rm.addHorizFillComponentZeroInsets(row.container);
		rm.addLine(outErrLbl, outErrCmbBox, outErrReplaceTxtF);
		rm.addLine(outWriteBtn, outApplyPluginsChk);

		ButtonGroup outRadioGroup = new ButtonGroup();
		outRadioGroup.add(outOverwriteRadioBtn);
		outRadioGroup.add(outFileRadioBtn);
		outOverwriteRadioBtn.setActionCommand(outOverwriteRadioBtnAC);
		outFileRadioBtn.setActionCommand(outFileRadioBtnAC);
		outOverwriteRadioBtn.addActionListener(this);
		outFileRadioBtn.addActionListener(this);

		outBrowseBtn.setActionCommand(outBrowseBtnAC);
		outWriteBtn.setActionCommand(outWriteBtnAC);
		outBrowseBtn.addActionListener(this);
		outWriteBtn.addActionListener(this);

		// display scroll pane
		displayTA = new JTextArea(10, 30);
		setNeverUpdateCaretPolicy(displayTA);
		displaySP = new JScrollPane(displayTA);

		// make the main panel
		mainPanel = new JPanel();
		assembleMainPanelWithoutPreviewArea();
	}

	private JComboBox<String> makeEncodingCmbBox(String[] sortedCharsets,
			String userCharset) {
		List<String> elements = new ArrayList<>();
		int index = Arrays.binarySearch(sortedCharsets, userCharset);
		if (index < 0) {
			if (userCharset != null)
				elements.add(userCharset);
			index = 0;
		}
		elements.addAll(Arrays.asList(sortedCharsets));

		JComboBox<String> cmbBox = new JComboBox<>(
				elements.toArray(new String[0]));
		cmbBox.setSelectedIndex(index);
		cmbBox.setEditable(true);
		cmbBox.setToolTipText(encCmbBoxToolTip);
		return cmbBox;
	}

	private void makePluginsPanel() {
		List<ReaderFactory> availablePlugins = new ArrayList<ReaderFactory>();
		ServiceLoader<ReaderFactory> sl = ServiceLoader
				.load(ReaderFactory.class);
		for (ReaderFactory plugin : sl)
			availablePlugins.add(plugin);
		Collections.sort(availablePlugins, readerFactoryCaseInsensComp);

		JLabel plgAllLbl = new JLabel(plgAllLblTxt);
		JLabel plgInUseLbl = new JLabel(plgInUseLblTxt);
		plgAvailableList = new JList<>(
				availablePlugins.toArray(new ReaderFactory[0]));
		plgAvailableList.addListSelectionListener(this);
		plgAvailableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		plgInUseListModel = new DefaultListModel<>();
		plgInUseList = new JList<>(plgInUseListModel);
		plgInUseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		retrieveStoredPlugins();

		// prevent resizing (depending on contents) and large size if empty
		plgInUseList
				.setFixedCellWidth(plgAvailableList.getPreferredSize().width);

		JButton topBtn = makeBtn("/icons/go-top.png", plgTopTxt, plgTopAC);
		JButton upBtn = makeBtn("/icons/go-up.png", plgUpTxt, plgUpAC);
		JButton downBtn = makeBtn("/icons/go-down.png", plgDownTxt, plgDownAC);
		JButton botBtn = makeBtn("/icons/go-bottom.png", plgBotTxt, plgBotAC);
		JButton useBtn = makeBtn("/icons/go-next.png", plgUseTxt, plgUseAC);
		JButton remBtn = makeBtn("/icons/go-previous.png", plgRemTxt, plgRemAC);

		// make top panel

		JPanel availListPanel = new JPanel();
		RowListMaker rm = new RowListMaker(availListPanel);
		rm.addLineCenter(plgAllLbl);
		rm.addFillComponentZeroInsets(new JScrollPane(plgAvailableList));

		JPanel useRemBtnPanel = new JPanel();
		rm = new RowListMaker(useRemBtnPanel);
		rm.addLine(useBtn);
		rm.addLine(remBtn);

		JPanel inUsePanel = new JPanel();
		rm = new RowListMaker(inUsePanel);
		rm.addLineCenter(plgInUseLbl);
		rm.addFillComponentZeroInsets(new JScrollPane(plgInUseList));

		JPanel orderBtnPanel = new JPanel();
		rm = new RowListMaker(orderBtnPanel);
		rm.addLine(topBtn);
		rm.addLine(upBtn);
		rm.addLine(downBtn);
		rm.addLine(botBtn);

		JPanel topPanel = new JPanel();
		RowMaker row = new RowMaker(topPanel);
		row.addFillComponentZeroInsets(availListPanel);
		row.addComponents(useRemBtnPanel);
		row.addFillComponentZeroInsets(inUsePanel);
		row.addComponents(orderBtnPanel);

		// make bottom panel

		plgDescrLbl = new JLabel(plgDescrLblTxt);
		plgDescrTA = new JTextArea(5, 10);
		plgDescrTA.setEditable(false);
		setNeverUpdateCaretPolicy(plgDescrTA);

		JPanel bottomPanel = new JPanel();
		rm = new RowListMaker(bottomPanel);
		rm.addLine(plgDescrLbl);
		JScrollPane plgDescrSP = new JScrollPane(plgDescrTA);
		plgDescrSP.setMinimumSize(plgDescrSP.getPreferredSize());
		rm.addFillComponentZeroInsets(plgDescrSP);

		// assemble plugins panel
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				topPanel, bottomPanel);
		splitPane.setResizeWeight(1.0);
		pluginsPanel = new JPanel();
		rm = new RowListMaker(pluginsPanel);
		rm.addFillComponentZeroInsets(splitPane);
	}

	private JButton makeBtn(String iconResStr, String text, String actCom) {
		JButton btn;
		URL url = null;
		if (iconResStr != null)
			url = GUI.class.getResource(iconResStr);
		if (url != null) {
			btn = new JButton(new ImageIcon(url));
			btn.setToolTipText(text);
		} else {
			btn = new JButton(text);
		}
		btn.setActionCommand(actCom);
		btn.addActionListener(this);
		return btn;
	}

	private void makeOptionsPanel() {
		optThemeLbl = new JLabel(optThemeLblTxt);
		optThemeCmbBox = new JComboBox<>(getLookAndFeelNames());
		int lafIndex = 0;
		for (LookAndFeelInfo lafInfo : getAvailableLookAndFeels()) {
			if (lafInfo.getClassName().equals(startupLafClassName)) {
				optThemeCmbBox.setSelectedIndex(lafIndex);
				break;
			}
			lafIndex++;
		}

		storeEncodingsChk = new JCheckBox(storeEncodingsChkTxt);
		storeEncodingsChk.setSelected(storeEncodings);
		storePluginsChk = new JCheckBox(storePluginsChkTxt);
		storePluginsChk.setSelected(storePlugins);

		optPreviewAreaLbl = new JLabel(optPreviewAreaLblTxt);
		optPreviewAreaCmbBox = new JComboBox<>(PreviewAreaLocation.values());
		optPreviewAreaCmbBox.setSelectedItem(previewAreaLocation);

		optChkUpdateLbl = new JLabel(optChkUpdateLblTxt);
		optChkUpdateBtn = new JButton(optChkUpdateBtnTxt);

		List<String> updateValues = new ArrayList<String>();
		updateValues.add("Never");
		updateValues.addAll(updateDays.keySet());
		optChkUpdateCmbBox = new JComboBox<>(
				updateValues.toArray(new String[0]));

		String interval = props.getProperty(updateChkIntervalKey);
		for (String s : updateValues)
			if (s.equals(interval)) {
				optChkUpdateCmbBox.setSelectedItem(s);
				break;
			}

		optApplyBtn = new JButton(optApplyBtnTxt);

		JPanel prefsPanel = makeTitledPanel(prefsPanelTxt);
		FormMaker fm = new FormMaker(prefsPanel);
		fm.addFormLine(optThemeLbl, optThemeCmbBox);
		fm.addFormLine(storeEncodingsChk);
		fm.addFormLine(storePluginsChk);
		fm.addFormLine(optPreviewAreaLbl, optPreviewAreaCmbBox);
		fm.addFormLine(optChkUpdateLbl, optChkUpdateCmbBox, optChkUpdateBtn);

		optionsPanel = new JPanel();
		RowListMaker rm = new RowListMaker(optionsPanel);
		rm.addLine(prefsPanel);
		rm.addLineCenter(optApplyBtn);
		optionsPanel = makeTopHorizFillHolder(optionsPanel);

		optChkUpdateBtn.setActionCommand(chkUpdatesAC);
		optChkUpdateBtn.addActionListener(this);
		optApplyBtn.setActionCommand(optApplyBtnAC);
		optApplyBtn.addActionListener(this);
	}

	private void assembleMainPanelWithoutPreviewArea() {
		mainPanel.removeAll();
		// make inner (invisible) container
		RowListMaker rm = new RowListMaker(new JPanel());
		rm.addHorizFillComponent(inputPanel);
		rm.addHorizFillComponent(outputPanel);
		// place inside (outer) mainPanel
		makeTopHorizFillHolder(rm.container, mainPanel);
	}

	private JPanel makeDisplayPanel() {
		JPanel displayPanel = makeTitledPanel(displayPanelTxt);
		RowListMaker rm = new RowListMaker(displayPanel);
		rm.addFillComponent(displaySP);
		return displayPanel;
	}

	/** Add the preview TextArea to the GUI (which MUST NOT already contain it). */
	private void addPreviewArea() {
		RowListMaker rm;
		JSplitPane sp;

		switch (previewAreaLocation) {
		default:
			System.err.println("Unknown previewAreaLocation "
					+ previewAreaLocation);
			// fallthrough
		case BOTTOM:
			mainPanel.removeAll();
			rm = new RowListMaker(mainPanel);
			rm.addHorizFillComponent(inputPanel);
			rm.addHorizFillComponent(outputPanel);
			rm.addFillComponent(makeDisplayPanel());
			break;

		case RIGHT:
			// make inner (invisible) container
			rm = new RowListMaker(new JPanel());
			rm.addHorizFillComponent(inputPanel);
			rm.addHorizFillComponent(outputPanel);

			// make SplitPane with outer(holder) panel and display panel
			sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					makeTopHorizFillHolder(rm.container), makeDisplayPanel());

			mainPanel.removeAll();
			rm = new RowListMaker(mainPanel);
			rm.addFillComponent(sp);
			break;

		case SEPARATE_TAB:
			mainTabbedPane.insertTab(previewTabTxt, null, displaySP, null, 0);
			break;
		}
	}

	private void removePreviewArea() {
		switch (previewAreaLocation) {
		case BOTTOM:
		case RIGHT:
			assembleMainPanelWithoutPreviewArea();
			break;
		case SEPARATE_TAB:
			mainTabbedPane.removeTabAt(0);
			break;
		default:
			System.err.println("Unknown previewAreaLocation "
					+ previewAreaLocation);
		}
	}

	private void makeMenu() {
		menuBar = new JMenuBar();

		JMenu menuHelp = new JMenu(menuHelpTxt);
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menuHelp);

		JMenuItem menuShowEncAliases = new JMenuItem(menuHelpShowEncAliasesTxt);
		menuShowEncAliases.setActionCommand(showEncAliasesAC);
		menuShowEncAliases.addActionListener(this);
		menuHelp.add(menuShowEncAliases);

		JMenuItem menuChkUpdates = new JMenuItem(menuHelpChkUpdatesTxt);
		menuChkUpdates.setActionCommand(chkUpdatesAC);
		menuChkUpdates.addActionListener(this);
		menuHelp.add(menuChkUpdates);

		JMenuItem menuAbout = new JMenuItem(menuHelpAboutTxt);
		menuAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuAbout.setActionCommand(helpAboutAC);
		menuAbout.addActionListener(this);
		menuHelp.add(menuAbout);
	}

	private void makeHelpAboutPanel() {
		helpAboutPanel = new JPanel();
		RowListMaker rm = new RowListMaker(helpAboutPanel);
		rm.addLine(new JLabel("JEncConv " + PROGRAM_VERSION));

		JButton btn = new JButton(websiteAddr);
		btn.setActionCommand(browseWebsiteAC);
		btn.addActionListener(this);
		rm.addLine(btn);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String actCom = event.getActionCommand();

		if (actCom.equals(optApplyBtnAC)) {
			storeEncodings = storeEncodingsChk.isSelected();
			props.setProperty(storeEncKey, String.valueOf(storeEncodings));
			if (storeEncodings == false) {
				props.remove(inEncKey);
				props.remove(outEncKey);

				props.remove(inpErrActKey);
				props.remove(inpErrCustomReplaceKey);
				props.remove(outErrActKey);
				props.remove(outErrCustomReplaceKey);
			}

			storePlugins = storePluginsChk.isSelected();
			props.setProperty(storePluginsKey, String.valueOf(storePlugins));
			if (storePlugins == false)
				props.remove(pluginsUsedKey);

			PreviewAreaLocation newPrevAreaLoc = (PreviewAreaLocation) optPreviewAreaCmbBox
					.getSelectedItem();
			if (newPrevAreaLoc != previewAreaLocation) {
				removePreviewArea();
				previewAreaLocation = newPrevAreaLoc;
				addPreviewArea();
				props.setProperty(previewAreaLocationKey,
						previewAreaLocation.toString());
			}

			String interval = (String) optChkUpdateCmbBox.getSelectedItem();
			props.setProperty(updateChkIntervalKey, interval);

			try {
				int selIndex = optThemeCmbBox.getSelectedIndex();
				if (selIndex == -1)
					return;

				// aliases can make the window very wide upon pack()
				inpAliasTxtF.setText(null);
				outAliasTxtF.setText(null);
				inpAliasTxtF.setEnabled(false);
				outAliasTxtF.setEnabled(false);

				UIManager.setLookAndFeel(getAvailableLookAndFeels()[selIndex]
						.getClassName());
				SwingUtilities.updateComponentTreeUI(mainFrame);
				mainFrame.pack();
				SwingUtilities.updateComponentTreeUI(fileChooser);
				SwingUtilities.updateComponentTreeUI(helpAboutPanel);
				setNeverUpdateCaretPolicy(displayTA);
				props.setProperty(lafKey, getLookAndFeelNames()[selIndex]);
				writePropertiesToFile();
			} catch (Exception e) {
				System.err.println(e);
			}
		} else if (actCom.equals(outOverwriteRadioBtnAC)) {
			outFileTxtF.setEnabled(false);
			outBrowseBtn.setEnabled(false);
		} else if (actCom.equals(outFileRadioBtnAC)) {
			outFileTxtF.setEnabled(true);
			outBrowseBtn.setEnabled(true);
		} else if (actCom.equals(inpBrowseBtnAC)) {
			int returnVal = fileChooser.showOpenDialog(mainFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				inpFileTxtF.setText(file.toString());
			}
		} else if (actCom.equals(outBrowseBtnAC)) {
			int returnVal = fileChooser.showSaveDialog(mainFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				outFileTxtF.setText(file.toString());
			}
		} else if (actCom.equals(inpDetectEncBtnAC)) {
			try {
				String fileName = inpFileTxtF.getText();
				Charset[] charsets = Converter.getPossibleCharsets(fileName);

				StringBuilder sb = new StringBuilder();
				sb.append(charsets.length);
				sb.append(" possible encodings found for " + fileName);
				for (Charset cs : charsets) {
					sb.append('\n');
					sb.append(cs.name());
				}

				displayTA.setText(sb.toString());
				if (previewAreaLocation == PreviewAreaLocation.SEPARATE_TAB)
					mainTabbedPane.setSelectedIndex(0);
			} catch (FileNotFoundException fnfe) {
				showExceptionMessage(fnfe);
			}
		} else if (actCom.equals(inpLoadBtnAC)) {
			try {
				inpAliasTxtF.setText(null);
				inpAliasTxtF.setEnabled(false);
				String csName = (String) inpEncCmbBox.getSelectedItem();
				Charset cs = getCharset(csName, inEncKey);
				inpAliasTxtF.setText(getNameAndAliases(cs));
				inpAliasTxtF.setCaretPosition(0);
				inpAliasTxtF.setEnabled(true);

				CharsetDecoder decoder = getCharsetDecoder(cs);

				boolean usePlugins = inpApplyPluginsChk.isSelected();
				props.setProperty(applyPlgPrevKey, String.valueOf(usePlugins));

				ReaderFactory[] plugins = storeAndReturnCurrentPlugins();
				if (!usePlugins)
					plugins = new ReaderFactory[0];

				writePropertiesToFile();

				String fileContents = getFileContents(inpFileTxtF.getText(),
						decoder, plugins);
				displayTA.setText(fileContents);

				if (previewAreaLocation == PreviewAreaLocation.SEPARATE_TAB)
					mainTabbedPane.setSelectedIndex(0);
			} catch (Exception e) {
				showExceptionMessage(e);
			}
		} else if (actCom.equals(outWriteBtnAC)) {
			try {
				inpAliasTxtF.setText(null);
				outAliasTxtF.setText(null);
				inpAliasTxtF.setEnabled(false);
				outAliasTxtF.setEnabled(false);

				String inCsName = (String) inpEncCmbBox.getSelectedItem();
				String outCsName = (String) outEncCmbBox.getSelectedItem();
				Charset inCharset = getCharset(inCsName, inEncKey);
				Charset outCharset = getCharset(outCsName, outEncKey);

				inpAliasTxtF.setText(getNameAndAliases(inCharset));
				outAliasTxtF.setText(getNameAndAliases(outCharset));
				inpAliasTxtF.setCaretPosition(0);
				outAliasTxtF.setCaretPosition(0);
				inpAliasTxtF.setEnabled(true);
				outAliasTxtF.setEnabled(true);

				CharsetDecoder decoder = getCharsetDecoder(inCharset);
				CharsetEncoder encoder = getCharsetEncoder(outCharset);

				boolean usePlugins = outApplyPluginsChk.isSelected();
				props.setProperty(applyPlgConvKey, String.valueOf(usePlugins));

				ReaderFactory[] plugins = storeAndReturnCurrentPlugins();
				if (!usePlugins)
					plugins = new ReaderFactory[0];

				writePropertiesToFile();

				String inFileName, outFileName;

				if (outOverwriteRadioBtn.isSelected()) {
					String origName = inpFileTxtF.getText();
					String bakName = origName + ".bak";

					File origFile = new File(origName);
					File bakFile = new File(bakName);
					if (!origFile.exists()) {
						JOptionPane.showMessageDialog(mainFrame, "File "
								+ origName + " not found");
						return;
					}

					if (bakFile.exists()) {
						// ask user for permission to overwrite .bak
						String msg = "File " + bakName + " exists.\nOverwrite?";
						String title = "Overwrite .bak file?";
						int option = JOptionPane.showConfirmDialog(mainFrame,
								msg, title, YES_NO_OPTION, WARNING_MESSAGE);
						if (option != YES_OPTION)
							return;
					}

					// try to move original to .bak
					boolean renamed = origFile.renameTo(bakFile);
					if (!renamed) {
						String msg = "Cannot move " + origName + "\n";
						msg += "to " + bakName + "\n";
						msg += "Conversion aborted.";
						String title = "Cannot move to .bak";
						JOptionPane.showMessageDialog(mainFrame, msg, title,
								ERROR_MESSAGE);
						return;
					}

					inFileName = bakName;
					outFileName = origName;
				} else if (outFileRadioBtn.isSelected()) {
					inFileName = inpFileTxtF.getText();
					outFileName = outFileTxtF.getText();

					if (new File(outFileName).exists()) {
						// ask user for permission to overwrite output file
						String msg = "File " + outFileName
								+ " exists.\nOverwrite?";
						String title = "Overwrite file?";
						int option = JOptionPane.showConfirmDialog(mainFrame,
								msg, title, YES_NO_OPTION, WARNING_MESSAGE);
						if (option != YES_OPTION)
							return;
					}
				} else {
					String msg = "Please select an output file option:";
					msg += "\n" + outOverwriteRadioBtnTxt;
					msg += "\n" + outFileRadioBtnTxt;

					JOptionPane.showMessageDialog(mainFrame, msg);
					return;
				}

				convert(inFileName, decoder, outFileName, encoder, plugins);
			} catch (Exception e) {
				showExceptionMessage(e);
			}
		} else if (actCom.equals(plgUseAC)) {
			ReaderFactory sel = plgAvailableList.getSelectedValue();
			if (sel != null)
				plgInUseListModel.addElement(sel);
		} else if (actCom.equals(plgRemAC)) {
			int index = plgInUseList.getSelectedIndex();
			if (index == -1)
				return;
			plgInUseListModel.remove(index);

			int size = plgInUseListModel.size();
			if (size > 0) {
				if (index >= size)
					index = size - 1;
				plgInUseList.setSelectedIndex(index);
			}
		} else if (actCom.equals(plgUpAC)) {
			int index = plgInUseList.getSelectedIndex();
			if (index == -1 || index == 0)
				return;
			ReaderFactory plg = plgInUseListModel.remove(index);
			plgInUseListModel.add(index - 1, plg);
			plgInUseList.setSelectedIndex(index - 1);
		} else if (actCom.equals(plgDownAC)) {
			int index = plgInUseList.getSelectedIndex();
			if (index == -1 || index == plgInUseListModel.size() - 1)
				return;
			ReaderFactory plg = plgInUseListModel.remove(index);
			plgInUseListModel.add(index + 1, plg);
			plgInUseList.setSelectedIndex(index + 1);
		} else if (actCom.equals(plgTopAC)) {
			int index = plgInUseList.getSelectedIndex();
			if (index == -1 || index == 0)
				return;
			ReaderFactory plg = plgInUseListModel.remove(index);
			plgInUseListModel.add(0, plg);
			plgInUseList.setSelectedIndex(0);
		} else if (actCom.equals(plgBotAC)) {
			int index = plgInUseList.getSelectedIndex();
			if (index == -1 || index == plgInUseListModel.size() - 1)
				return;
			ReaderFactory plg = plgInUseListModel.remove(index);
			plgInUseListModel.addElement(plg);
			plgInUseList.setSelectedIndex(plgInUseListModel.size() - 1);
		} else if (actCom.equals(showEncAliasesAC)) {
			StringBuilder sb = new StringBuilder("encoding\taliases\n");
			for (Charset cs : Charset.availableCharsets().values()) {
				sb.append(cs.name());
				sb.append('\t');

				boolean first = true;
				for (String alias : cs.aliases()) {
					if (first)
						first = false;
					else
						sb.append(' ');
					sb.append(alias);
				}

				sb.append('\n');
			}

			displayTA.setText(sb.toString());

			if (previewAreaLocation == PreviewAreaLocation.SEPARATE_TAB)
				mainTabbedPane.setSelectedIndex(0);
		} else if (actCom.equals(helpAboutAC)) {
			JOptionPane.showMessageDialog(mainFrame, helpAboutPanel, "About",
					INFORMATION_MESSAGE);
		} else if (actCom.equals(chkUpdatesAC)) {
			crtUpdateChecker = new UpdateChecker(this, true);
			crtUpdateChecker.execute();
		} else if (actCom.equals(browseWebsiteAC)) {
			try {
				Desktop.getDesktop().browse(new URI(websiteAddr));
			} catch (Exception e) {
				showExceptionMessage(e);
			}
		} else if (actCom.equals(browseSfPageAC)) {
			try {
				Desktop.getDesktop().browse(new URI(sfPageAddr));
			} catch (Exception e) {
				showExceptionMessage(e);
			}
		} else if (actCom.equals(inpErrCmbBoxAC)) {
			inpErrReplaceTxtF.setEnabled(inpErrCmbBox.getSelectedItem().equals(
					inpErrActReplCustom));
		} else if (actCom.equals(outErrCmbBoxAC)) {
			outErrReplaceTxtF.setEnabled(outErrCmbBox.getSelectedItem().equals(
					outErrActReplCustom));
		} else {
			// unknown event
			System.err.println("Unknown action command: " + actCom);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		Object src = e.getSource();
		if (src != plgAvailableList)
			return;
		ReaderFactory plg = plgAvailableList.getSelectedValue();
		if (plg != null) {
			plgDescrTA.setText(plg.getClass().getSimpleName());
			plgDescrTA.append("\n" + plg.getDescription());
		} else {
			plgDescrTA.setText(null);
		}
	}

	/** Called by UpdateChecker's done() method from the Event Dispatch Thread. */
	void updateCheckResult(UpdateChecker checker, String version) {
		if (checker != crtUpdateChecker)
			return;
		crtUpdateChecker = null;

		if (!version.equals(PROGRAM_VERSION)) {
			RowListMaker rm = new RowListMaker(new JPanel());
			rm.addLine(new JLabel("New version available: " + version));

			JButton btn = new JButton(sfPageAddr);
			btn.setActionCommand(browseSfPageAC);
			btn.addActionListener(this);
			rm.addLine(btn);

			JOptionPane.showMessageDialog(mainFrame, rm.container,
					"Update found", INFORMATION_MESSAGE);
		} else if (checker.manuallyStarted) {
			String msg = "You are running the latest version: " + version;
			JOptionPane.showMessageDialog(mainFrame, msg, "Up to date",
					INFORMATION_MESSAGE);
		}

		try {
			props.setProperty(lastUpdateChkKey, dateFmt.format(new Date()));
			writePropertiesToFile();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	/** Called by UpdateChecker's done() method from the Event Dispatch Thread. */
	void updateCheckException(UpdateChecker checker, Exception e) {
		if (checker != crtUpdateChecker)
			return;
		crtUpdateChecker = null;

		String msg = "Error while checking for update:\n";
		if (e.getCause() != null)
			msg += e.getCause();
		else
			msg += e;

		if (checker.manuallyStarted) {
			JOptionPane.showMessageDialog(mainFrame, msg);
		} else {
			System.err.println(msg);
		}
	}

	private static void createAndShowGUI() {
		// try to load Properties from user's home dir
		InputStreamReader isr = null;
		try {
			if (propsFile.exists()) {
				// load properties from file
				isr = new InputStreamReader(new FileInputStream(propsFile),
						propsFileCharset);
				props.load(isr);
			}
		} catch (IOException e) {
			System.err.print(e.toString());
		} finally {
			try {
				if (isr != null)
					isr.close();
			} catch (IOException e) {
				System.err.print(e.toString());
			}
		}

		String storedClassName = getLafClassName(props
				.getProperty(lafKey, null));
		startupLafClassName = applyStartupLookAndFeel(storedClassName);
		GUI gui = new GUI();
		gui.init();
		gui.mainFrame.pack();
		// strange: the second .pack() makes the JLists a bit smaller
		// otherwise it would happen when clicking Apply from the options tab
		gui.mainFrame.pack();
		gui.mainFrame.setVisible(true);

		gui.autoUpdateCheck();
	}

	/** Check for updates at startup, from the Event Dispatch (GUI) Thread */
	private void autoUpdateCheck() {
		String chkInterval = props.getProperty(updateChkIntervalKey);
		if (chkInterval == null || !updateDays.containsKey(chkInterval))
			return;

		// desired update interval in milliseconds
		long interval = updateDays.get(chkInterval) * Timer.ONE_DAY;

		try {
			String lastStr = props.getProperty(lastUpdateChkKey);
			if (lastStr != null) {
				Date last = dateFmt.parse(lastStr);
				Date now = new Date();
				if (now.getTime() - last.getTime() < interval)
					return;
			}
		} catch (ParseException e) {
			System.err.println("Error parsing last update check date: " + e);
		}

		crtUpdateChecker = new UpdateChecker(this, false);
		crtUpdateChecker.execute();
	}

	public static void start() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/*
	 * Utils
	 */

	private static JPanel makeTitledPanel(String panelTitle) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(panelTitle));
		return panel;
	}

	private void setNeverUpdateCaretPolicy(JTextComponent textComp) {
		Caret c = textComp.getCaret();
		if (c instanceof DefaultCaret)
			((DefaultCaret) c).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}

	/**
	 * @param charsetName
	 *            The name of the requested charset; may be either a canonical
	 *            name or an alias
	 * @param storageKey
	 *            the preferences key for storing the charsetName (if valid),
	 *            ignored if null
	 * @return A charset object for the named charset
	 * @throws IllegalCharsetNameException
	 *             If the given charset name is illegal
	 * @throws IllegalArgumentException
	 *             If the given charsetName is null
	 * @throws UnsupportedCharsetException
	 *             If no support for the named charset is available in this
	 *             instance of the Java virtual machine
	 */
	private Charset getCharset(String charsetName, String storageKey)
			throws IllegalCharsetNameException, IllegalArgumentException,
			UnsupportedCharsetException {
		Charset charset = Charset.forName(charsetName);
		if (storeEncodings && storageKey != null)
			props.setProperty(storageKey, charsetName);
		return charset;
	}

	private static String getNameAndAliases(Charset cs) {
		StringBuilder sb = new StringBuilder(cs.name());

		for (String alias : cs.aliases()) {
			sb.append(' ');
			sb.append(alias);
		}

		return sb.toString();
	}

	private CharsetDecoder getCharsetDecoder(Charset cs)
			throws IllegalArgumentException {
		CharsetDecoder decoder = cs.newDecoder();

		if (!(inpErrCmbBox.getSelectedItem() instanceof String)) {
			System.err.println("inpErrCmbBox selected item NOT String");
			return decoder;
		}

		String s = (String) inpErrCmbBox.getSelectedItem();
		CodingErrorAction inErrAct;

		if (s.equals(inpErrActReport)) {
			inErrAct = CodingErrorAction.REPORT;
		} else if (s.equals(inpErrActReplDefault)) {
			inErrAct = CodingErrorAction.REPLACE;
		} else if (s.equals(inpErrActReplCustom)) {
			inErrAct = CodingErrorAction.REPLACE;
			// throws IllegalArgumentException
			decoder.replaceWith(inpErrReplaceTxtF.getText());
		} else if (s.equals(inpErrActIgnore)) {
			inErrAct = CodingErrorAction.IGNORE;
		} else {
			System.err.println("Unknown InputCodingErrorAction: " + s);
			inErrAct = CodingErrorAction.REPORT;
		}

		if (storeEncodings) {
			props.setProperty(inpErrActKey, s);
			props.setProperty(inpErrCustomReplaceKey,
					inpErrReplaceTxtF.getText());
		}

		decoder.onMalformedInput(inErrAct);
		decoder.onUnmappableCharacter(inErrAct);

		return decoder;
	}

	private CharsetEncoder getCharsetEncoder(Charset cs)
			throws IllegalArgumentException {
		CharsetEncoder encoder = cs.newEncoder();

		if (!(outErrCmbBox.getSelectedItem() instanceof String)) {
			System.err.println("outErrCmbBox selected item NOT String");
			return encoder;
		}

		String s = (String) outErrCmbBox.getSelectedItem();
		CodingErrorAction outErrAct;

		if (s.equals(outErrActReport)) {
			outErrAct = CodingErrorAction.REPORT;
		} else if (s.equals(outErrActReplDefault)) {
			outErrAct = CodingErrorAction.REPLACE;
		} else if (s.equals(outErrActReplCustom)) {
			outErrAct = CodingErrorAction.REPLACE;
			// throws IllegalArgumentException
			encoder.replaceWith(outErrReplaceTxtF.getText().getBytes(cs));
		} else if (s.equals(outErrActIgnore)) {
			outErrAct = CodingErrorAction.IGNORE;
		} else {
			System.err.println("Unknown OutputCodingErrorAction: " + s);
			outErrAct = CodingErrorAction.REPORT;
		}

		if (storeEncodings) {
			props.setProperty(outErrActKey, s);
			props.setProperty(outErrCustomReplaceKey,
					outErrReplaceTxtF.getText());
		}

		encoder.onMalformedInput(outErrAct);
		encoder.onUnmappableCharacter(outErrAct);

		return encoder;
	}

	private void showExceptionMessage(Throwable e) {
		if (e == null)
			return;

		String displayMsg = e.getClass().getSimpleName();
		if (displayMsg == null || displayMsg.isEmpty())
			displayMsg = "Exception";

		String excMsg = e.getMessage();
		if (excMsg != null) {
			if (excMsg.trim().isEmpty())
				excMsg = "\"" + excMsg + "\"";
			displayMsg += ": " + excMsg;
		}

		JOptionPane.showMessageDialog(mainFrame, displayMsg);
	}

	private static String getFileContents(String fileName,
			CharsetDecoder decoder, ReaderFactory[] plugins)
			throws FileNotFoundException, IOException {
		Reader in = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			in = new BufferedReader(new InputStreamReader(fis, decoder));

			in = chainPlugins(in, plugins);

			StringBuilder sb = new StringBuilder();
			char[] buf = new char[BUF_SIZE];
			int len;
			while ((len = in.read(buf)) != -1) {
				sb.append(buf, 0, len);
			}

			return sb.toString();
		} finally {
			if (in != null)
				in.close();
		}
	}

	private ReaderFactory[] storeAndReturnCurrentPlugins() {
		StringBuilder sb = new StringBuilder();
		List<ReaderFactory> result = new ArrayList<ReaderFactory>();
		for (ReaderFactory plg : Collections.list(plgInUseListModel.elements())) {
			if (sb.length() != 0)
				sb.append("#");
			sb.append(plg.getClass().getName());
			result.add(plg);
		}
		if (storePlugins)
			props.setProperty(pluginsUsedKey, sb.toString());
		return result.toArray(new ReaderFactory[0]);
	}

	private void retrieveStoredPlugins() {
		Map<String, ReaderFactory> map = new HashMap<String, ReaderFactory>();
		ListModel<ReaderFactory> model = plgAvailableList.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			ReaderFactory plg = model.getElementAt(i);
			map.put(plg.getClass().getName(), plg);
		}

		String storedPlugins = props.getProperty(pluginsUsedKey, null);
		if (storedPlugins == null)
			return;
		for (String plgName : storedPlugins.split("#")) {
			ReaderFactory plg = map.get(plgName);
			if (plg != null)
				plgInUseListModel.addElement(plg);
		}
	}

	private void writePropertiesToFile() {
		OutputStreamWriter osw = null;
		try {
			// make sure properties file exists
			if (!propsFile.exists()) {
				if (!propsFile.createNewFile())
					throw new Exception("can't create file: " + propsFile);
			}

			osw = new OutputStreamWriter(new FileOutputStream(propsFile),
					propsFileCharset);
			props.store(osw, "JEncConv properties file");
		} catch (Exception e) {
			showExceptionMessage(e);
		} finally {
			try {
				if (osw != null)
					osw.close();
			} catch (Exception e) {
				showExceptionMessage(e);
			}
		}
	}

}
