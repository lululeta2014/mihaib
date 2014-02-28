/*
 * Copyright © Mihai Borobocea 2009, 2010, 2012
 * 
 * This file is part of DiceLottery.
 * 
 * DiceLottery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DiceLottery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DiceLottery.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sf.dicelottery;

import static net.sf.simpleswing.GridBagContainerFiller.makeTopHorizFillHolder;
import static net.sf.simpleswing.GridBagContainerFiller.makeTopLeftHolder;
import static net.sf.simpleswing.LafUtils.applyStartupLookAndFeel;
import static net.sf.simpleswing.LafUtils.getAvailableLookAndFeels;
import static net.sf.simpleswing.LafUtils.getLafClassName;
import static net.sf.simpleswing.LafUtils.getLookAndFeelNames;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import net.sf.dicelottery.EventMapper.RepeatsError;
import net.sf.dicelottery.element.ElementRepresConverter;
import net.sf.dicelottery.element.ElementRepresentation;
import net.sf.dicelottery.event.EventRepresConverter;
import net.sf.dicelottery.event.EventRepresentation;
import net.sf.dicelottery.input.InputSourceType;
import net.sf.dicelottery.worker.BackgroundTask;
import net.sf.dicelottery.worker.InterimHandler;
import net.sf.dicelottery.worker.ResultHandler;
import net.sf.dicelottery.worker.Worker;
import net.sf.simpleswing.FormMaker;
import net.sf.simpleswing.RowListMaker;
import net.sf.simpleswing.RowMaker;

public class DiceLottery implements ActionListener {

	private static final String lafKey = "LookAndFeel";
	private static String startupLafClassName;
	private static final Properties props = new Properties();
	private static final File propsFile = Paths.get(
			System.getProperty("user.home"), ".dicelottery").toFile();
	public static final Charset charset = Charset.forName("UTF-8");

	/*
	 * Main window
	 */

	// data elements
	private EventRepresConverter mainSrcUniv, mainDestUniv;
	private EventMapper mainMapper;
	private SwingWorker<Mapping, String> crtMappingWorker;
	private SwingWorker<ReverseMapping, String> crtReverseMappingWorker;
	private static final int statusTxtfCols = 20;
	/**
	 * Reader used by <code>crtMappingWorker</code> and
	 * <code>crtReverseMappingWorker</code> to read from the input JTextArea.
	 * Only the worker references indicate which (if any) is the current worker,
	 * even though the <code>crtMappingInputReader</code> is set to
	 * <code>null</code> together with them.
	 */
	private Reader crtMappingInputReader;

	// GUI elements
	private JFrame mainFrame;
	private JPanel mainLeftPanel, mainRightPanel;
	private JTextField mappingStatusTxtF;
	private final String mainFrameTitle = "DiceLottery",
			inputPanelTxt = "Input", outputPanelTxt = "Output",
			dataSrcPanelTxt = "Input Source", mappingPanelTxt = "Mapping",
			reverseMappingPanelTxt = "Reverse Mapping", infoPanelTxt = "Info",
			mappingStatusTxtFTxt = "Status: Idle";
	private JTextArea inputTA, outputTA;
	// `data source type' panel
	private JRadioButton dataSrcTaRadio, dataSrcRandomOrgRadio,
			dataSrcSysRandRadio;
	private final String dataSrcTaRadioTxt = "Input Text Area",
			dataSrcRandomOrgRadioTxt = "Random.org",
			dataSrcSysRandRadioTxt = "System Random Number Generator";
	private ButtonGroup dataSrcRadioGroup;
	// `mapping' and `reverse mapping' panels
	private JButton computeMappingBtn, cancelMappingBtn,
			computeReverseMappingBtn, cancelReverseMappingBtn;
	private final String computeMappingBtnTxt = "Compute Mapping",
			cancelMappingBtnTxt = "Cancel",
			computeReverseMappingBtnTxt = "Compute Reverse Mapping",
			cancelReverseMappingBtnTxt = "Cancel";
	private final String computeMappingBtnActCom = "computeMappingBtnActCom",
			cancelMappingBtnActCom = "cancelMappingBtnActCom",
			computeReverseMappingBtnActCom = "computeReverseMappingBtnActCom",
			cancelReverseMappingBtnActCom = "cancelReverseMappingBtnActCom";
	private JTextField reverseMappingSolTxtF, reverseMappingSolDescrTxtF;
	private final String reverseMappingSolLblTxt = "Reverse mapping results:",
			reverseMappingSolTxtFtxt = "1";
	// info panel
	private JTextArea mainMapperDescTA;
	private JButton configBtn;
	private final String configBtnTxt = "Configure...",
			configBtnActCom = "CONFIG";

	/*
	 * Configuration window
	 */

	// data elements
	private EventRepresConverter configWindowSrcUniv, configWindowDestUniv;
	private EventMapper configWindowMapper;
	private JFileChooser fileChooser;
	private SwingWorker<EventRepresConverter, Void> crtSrcUnivWorker,
			crtDestUnivWorker;
	// TODO rename this worker (and its `helpers')
	private SwingWorker<EventMapper, Void> crtRepeatsCheckWorker;
	private SwingWorker<RepeatsError, Void> crtSearchMinRepWorker;
	private final ResultHandler<RepeatsError, Void> searchMinRepResultHandler;

	// TODO: make this worker provide intermediate results
	private SwingWorker<Integer, Void> crtSearchTargetErrWorker;

	// GUI elements
	private JDialog configDialog;
	private JTabbedPane configTabbedPane;
	private JPanel configDialogPanel;
	private JScrollPane configDialogSP;
	private JPanel srcUnivPanel, destUnivPanel, repeatsPanel, prefsPanel;
	private final String configDialogTitle = "Configuration",
			srcUnivPanelTxt = "Source of Events (input)",
			destUnivPanelTxt = "Destination Event Universe (output)",
			repeatsPanelTxt = "Repeats (how many source events to map)",
			srcTabTxt = "Source", destTabTxt = "Destination",
			repTabTxt = "Mapping", prefsTabTxt = "Preferences";
	private final Object configOkTxt = "OK", configCancelTxt = "Cancel";

	// source universe panel
	private JButton srcUnivSetBtn, srcUnivCancelBtn;
	private final String srcUnivSetBtnTxt = "Set",
			srcUnivCancelBtnTxt = "Cancel";
	private final String srcUnivSetBtnActCom = "srcUnivSetBtnActCom",
			srcUnivCancelBtnActCom = "srcUnivCancelBtnActCom";
	private JTextField srcUnivStatusTxtF;
	private final String srcPanelDescrLblTxt = "<html>These are the events available to you.<br/>Use the "
			+ srcUnivSetBtnTxt + " button below when you are done.</html>",
			srcUnivStatusTxtFTxt = "Status: Idle";

	// panels
	private final String srcUnivElementsPanelTxt = "Elements you generate",
			srcUnivSeparatorsPanelTxt = "Separator characters",
			srcUnivOutcomesPanelTxt = "Events (Outcomes)";
	// Element Representation panel
	private final String srcElemDescrLblTxt = "<html>These may be numbers (e.g. the faces of a die)<br/>or custom strings from a (UTF-8) text file.</html>";
	private JRadioButton srcUnivElemNrRadio, srcUnivElemStringsRadio;
	private ButtonGroup srcUnivElemReprRadioGroup;
	private final String srcUnivElemNrRadioTxt = "Numbers: 1 to",
			srcUnivElemNrRadioAC = "srcUnivElemNrRadio",
			srcUnivElemStringsRadioTxt = "Words from file:",
			srcUnivElemStringsRadioAC = "srcUnivElemStringsRadio";
	private JTextField srcUnivElemNrTxtF, srcUnivStringsFileTxtF;
	private JButton srcUnivElemFileBrowseBtn;
	private final String srcUnivElemFileBrowseBtnTxt = "Browse...";
	private final String srcUnivElemFileBrowseBtnActCom = "srcUnivElemFileBrowseBtnActCom";
	private JCheckBox srcUnivCaseSensitiveChk;
	private final String srcUnivCaseSensitiveChkTxt = "case sensitive";
	// Separators panel
	private final String srcSepDescrLblTxt = "<html>These characters are ignored when reading your input.<br/>To ignore custom characters (e.g. punctuation)<br/>put them in a (UTF-8) text file and use it below.</html>";
	private JRadioButton srcUnivSepWsRadio, srcUnivSepFileRadio;
	private ButtonGroup srcUnivSepRadioGroup;
	private final String srcUnivSepWsRadioTxt = "Whitespace only",
			srcUnivSepWsRadioAC = "srcUnivSepWsRadio",
			srcUnivSepFileRadioTxt = "All characters in file:",
			srcUnivSepFileRadioAC = "srcUnivSepFileRadio";
	private JTextField srcUnivSepFileTxtF;
	private JButton srcUnivSepFileBrowseBtn;
	private final String srcUnivSepFileBrowseBtnTxt = "Browse...";
	private final String srcUnivSepFileBrowseBtnActCom = "srcUnivSepFileBrowseBtnActCom";
	private JCheckBox srcUnivWsSepChk;
	private final String srcUnivWsSepChkTxt = "and all whitespace characters";
	// Outcomes panel
	private final String srcOutcomesDescrLblTxt = "<html>An Event is a selection of the Elements defined above.<br/>You may select a single element (e.g. a face of a die)<br/>or multiple ones (e.g. several balls from a bin).</html>";
	private JRadioButton srcUnivSingleElemRadio, srcUnivLotteryRadio;
	private ButtonGroup srcUnivEventReprRadioGroup;
	private final String srcUnivSingleElemRadioTxt = "Single Element",
			srcUnivSingleElemRadioAC = "srcUnivSingleElemRadio",
			srcUnivLotteryRadioTxt = "Lottery",
			srcUnivLotteryRadioAC = "srcUnivLotteryRadio";
	private JLabel srcUnivSelectedElemsLbl;
	private final String srcUnivSelectedElemsLblTxt = "Selected elements:";
	private JTextField srcUnivSelectedElemsTxtF;
	private JCheckBox srcUnivOrderMattersChk, srcUnivMultipleOccurrChk;
	private final String srcUnivOrderMattersChkTxt = "order matters",
			srcUnivMultipleOccurrChkTxt = "allow multiple occurrences";

	// destination universe panel
	private JButton destUnivSetBtn, destUnivCancelBtn;
	private final String destUnivSetBtnTxt = "Set",
			destUnivCancelBtnTxt = "Cancel";
	private final String destUnivSetBtnActCom = "destUnivSetBtnActCom",
			destUnivCancelBtnActCom = "destUnivCancelBtnActCom";
	private JTextField destUnivStatusTxtF;
	private final String destPanelDescrLblTxt = "<html>These are the events you wish to obtain.<br/>Use the "
			+ destUnivSetBtnTxt + " button below when you are done.</html>",
			destUnivStatusTxtFTxt = "Status: Idle";

	// panels
	private final String destUnivElementsPanelTxt = "Elements you wish to obtain",
			destUnivSeparatorsPanelTxt = "Separator characters",
			destUnivOutcomesPanelTxt = "Events (Outcomes)";
	// Element Representation panel
	private final String destElemDescrLblTxt = "<html>These may be numbers (e.g. lottery balls)<br/>or custom strings from a (UTF-8) text file.</html>";
	private JRadioButton destUnivElemNrRadio, destUnivElemStringsRadio;
	private ButtonGroup destUnivElemReprRadioGroup;
	private final String destUnivElemNrRadioTxt = "Numbers: 1 to",
			destUnivElemNrRadioAC = "destUnivElemNrRadio",
			destUnivElemStringsRadioTxt = "Words from file:",
			destUnivElemStringsRadioAC = "destUnivElemStringsRadio";
	private JTextField destUnivElemNrTxtF, destUnivStringsFileTxtF;
	private JButton destUnivElemFileBrowseBtn;
	private final String destUnivElemFileBrowseBtnTxt = "Browse...";
	private final String destUnivElemFileBrowseBtnActCom = "destUnivElemFileBrowseBtnActCom";
	private JCheckBox destUnivCaseSensitiveChk;
	private final String destUnivCaseSensitiveChkTxt = "case sensitive";
	// Separators panel
	private final String destSepDescrLblTxt = "<html>These characters are ignored when reading your input.<br/>To ignore custom characters (e.g. punctuation)<br/>put them in a (UTF-8) text file and use it below.</html>";
	private JRadioButton destUnivSepWsRadio, destUnivSepFileRadio;
	private ButtonGroup destUnivSepRadioGroup;
	private final String destUnivSepWsRadioTxt = "Whitespace only",
			destUnivSepWsRadioAC = "destUnivSepWsRadio",
			destUnivSepFileRadioTxt = "All characters in file:",
			destUnivSepFileRadioAC = "destUnivSepFileRadio";
	private JTextField destUnivSepFileTxtF;
	private JButton destUnivSepFileBrowseBtn;
	private final String destUnivSepFileBrowseBtnTxt = "Browse...";
	private final String destUnivSepFileBrowseBtnActCom = "destUnivSepFileBrowseBtnActCom";
	private JCheckBox destUnivWsSepChk;
	private final String destUnivWsSepChkTxt = "and all whitespace characters";
	// Outcomes panel
	private final String destOutcomesDescrLblTxt = "<html>An Event is a selection of the Elements defined above.<br/>You may select a single element (e.g. a face of a die)<br/>or multiple ones (e.g. several balls from a bin).</html>";
	private JRadioButton destUnivSingleElemRadio, destUnivLotteryRadio;
	private ButtonGroup destUnivEventReprRadioGroup;
	private final String destUnivSingleElemRadioTxt = "Single Element",
			destUnivSingleElemRadioAC = "destUnivSingleElemRadio",
			destUnivLotteryRadioTxt = "Lottery",
			destUnivLotteryRadioAC = "destUnivLotteryRadio";
	private JLabel destUnivSelectedElemsLbl;
	private final String destUnivSelectedElemsLblTxt = "Selected elements:";
	private JTextField destUnivSelectedElemsTxtF;
	private JCheckBox destUnivOrderMattersChk, destUnivMultipleOccurrChk;
	private final String destUnivOrderMattersChkTxt = "order matters",
			destUnivMultipleOccurrChkTxt = "allow multiple occurrences";

	// repeats panel
	private final String repPanelDescrLblTxt = "<html>If there are fewer Source Events than Destination Events,<br/>several Source Events are needed for each mapping.<br/>Set this number below.</html>";
	private JTextField repStatusTxtF, repMinRepStatusTxtF,
			repTargetErrStatusTxtF;
	private final String repStatusTxtFTxt = "Status: Idle",
			repMinRepStatusTxtFTxt = "Status: Idle",
			repTargetErrStatusTxtFTxt = "Status: Idle";
	private final String repNewDataPanelTxt = "New Data",
			repMinRepeatsPanelTxt = "Minimum Repeats",
			repTargetErrPanelTxt = "Target Error", repFoundPanelTxt = "Found";
	private final String repRepDescrLblTxt = "<html>Choose the number of source events<br/>required for each mapping operation</html>",
			repMinrDescrLblTxt = "<html>Compute the minumim number of Source Events<br/>required for a mapping. The Error represents<br/>the fraction of inputs which can't be mapped.</html>",
			repTargetErrDescrLblTxt = "Not yet implemented";
	private final String repRepeatsLblTxt = "Repeats:",
			repMinrMinRepeatsLblTxt = "Min. Repeats:",
			repMinrErrLblTxt = "Error:", repTargetErrLblTxt = "Target Error:",
			repFoundRepLblTxt = "Repeats:", repFoundErrLblTxt = "Error:";
	private JTextField repRepeatsTxtF, repMinrMinRepeatsTxtF, repMinrErrTxtF,
			repTargetErrTxtF, repFoundRepTxtF, repFoundErrTxtF;
	private final String repRepeatsTxtFTxt = "1";
	private JButton repSetBtn, repCancelBtn, repSearchMinRepBtn,
			repCancelMinRepBtn, repSearchTargetErrBtn, repCancelTargetErrBtn;
	private final String repSetBtnTxt = "Set", repCancelBtnTxt = "Cancel",
			repSearchMinRepBtnTxt = "Search Min. Repeats",
			repCancelMinRepBtnTxt = "Cancel",
			repSearchTargetErrBtnTxt = "Search Repeats for target error",
			repCancelTargetErrBtnTxt = "Cancel";
	private final String repSetBtnActCom = "repSetBtnActCom",
			repCancelBtnActCom = "repCancelBtnActCom",
			repSearchMinRepBtnActCom = "repSearchMinRepBtnActCom",
			repCancelMinRepBtnActCom = "repCancelMinRepBtnActCom",
			repSearchTargetErrBtnActCom = "repSearchTargetErrBtnActCom",
			repCancelTargetErrBtnActCom = "repCancelTargetErrBtnActCom";

	// preferences panel
	private JComboBox<String> themeCmbBox;
	private JButton applyPrefsBtn;
	private final String prefsPanelTxt = "Preferences", themeLblTxt = "Theme:",
			applyPrefsBtnTxt = "Apply",
			applyPrefsBtnActCom = "applyPrefsActCom";

	// right panel (description)
	private JPanel descrPanel;
	private JTextArea srcDescrTA, destDescrTA, repDescrTA;
	private final String srcDescrLblTxt = "<html>Source events<br/>Use the "
			+ srcTabTxt + " tab to set them</html>",
			destDescrLblTxt = "<html>Destination events<br/>Use the "
					+ destTabTxt + " tab to set them</html>",
			repDescrLblTxt = "<html>Event Mapper<br/>Use the " + repTabTxt
					+ " tab to set it</html>";
	// TODO: default text for TAs; do it when writing the description; if the
	// descr is null, write custom text for each

	// messages
	private final String msgInvalidNr = "Invalid Number",
			msgSelElemRepr = "Please select one type of element representation:\n"
					+ "Number (and specify count) or Strings loaded from a file",
			msgSelEventRepr = "Please select one type of event representation:\n"
					+ "Single Element or Lottery",
			msgSelSepRepr = "Please select separator characters:\n"
					+ "whitespace or characters from a file",
			msgWaitForUnivWorkersToFinish = "Must wait for Source and Destination SET operations to finish (or cancel them)",
			msgSrcAndDestUnivMustBeSet = "Source and Destination Universes must be set!",
			msgWaitForRepWorkersToFinish = "Wait for repeat workers to finish (or cancel them)",
			msgWorkerRunning = "Running", msgWorkerSuccess = "Success",
			msgWorkerCancelled = "Cancelled",
			msgMapperMustBeSet = "Please set the mapper (using the "
					+ configBtnTxt + " button)",
			msgSelectInputSrc = "Please select the data input source\n"
					+ "using the " + dataSrcPanelTxt + " radio buttons",
			msgUnivNotSet = "Event Universe not set",
			msgMapperNotSet = "Event Mapper not set";

	// TODO decide about an empty constructor and about using the `this'
	// keyword inside a constructor.
	// Do we need to prevent the `escaping' of a reference to a partially
	// constructed object from the constructor (e.g. when calling
	// addActionListener(this))?
	private DiceLottery() {
		// initialize a worker parameter which doesn't change
		searchMinRepResultHandler = new ResultHandler<RepeatsError, Void>() {
			@Override
			public void result(Worker<RepeatsError, Void> worker,
					RepeatsError result) {
				configSearchMinRepeatsWrkDone(worker, result);
			}

			@Override
			public void exception(Worker<RepeatsError, Void> worker,
					ExecutionException e) {
				configSearchMinRepeatsWrkException(worker, e);
			}
		};
	}

	private void initialize() {
		makeMainFrame();
		makeConfigFrame();
		setLafRelatedProperties();
	}

	/*
	 * Construct Main window
	 */
	private void makeMainFrame() {
		makeMainLeftPanel();
		makeMainRightPanel();

		mainFrame = new JFrame(mainFrameTitle);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<Image> imgList = new ArrayList<Image>();
		for (String imgStr : new String[] { "100.png", "48.png", "32.png",
				"16.png" }) {
			URL imgUrl = this.getClass().getResource("/icons/" + imgStr);
			if (imgUrl != null)
				imgList.add(new ImageIcon(imgUrl).getImage());
		}
		mainFrame.setIconImages(imgList);

		mappingStatusTxtF = makeStatusTextField(mappingStatusTxtFTxt);
		RowMaker row = new RowMaker(new JPanel());
		row.addFillComponentZeroInsets(mainLeftPanel);
		row.addVertFillComponentZeroInsets(mainRightPanel);
		RowListMaker rm = new RowListMaker(mainFrame.getContentPane());
		rm.addFillComponentZeroInsets(row.container);
		rm.addHorizFillComponentZeroInsets(mappingStatusTxtF);
	}

	private void makeMainLeftPanel() {
		// input panel
		JPanel inputPanel = makeTitledPanel(inputPanelTxt);
		inputTA = new JTextArea(5, 20);
		JScrollPane inputSP = new JScrollPane(inputTA);
		RowListMaker rm = new RowListMaker(inputPanel);
		rm.addFillComponent(inputSP);

		// output panel
		JPanel outputPanel = makeTitledPanel(outputPanelTxt);
		outputTA = new JTextArea(5, 20);
		JScrollPane outputSP = new JScrollPane(outputTA);
		rm = new RowListMaker(outputPanel);
		rm.addFillComponent(outputSP);

		mainLeftPanel = new JPanel();
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				inputPanel, outputPanel);
		rm = new RowListMaker(mainLeftPanel);
		rm.addFillComponent(mainSplitPane);
	}

	private void makeMainRightPanel() {
		// data source type panel
		dataSrcTaRadio = new JRadioButton(dataSrcTaRadioTxt);
		dataSrcRandomOrgRadio = new JRadioButton(dataSrcRandomOrgRadioTxt);
		dataSrcSysRandRadio = new JRadioButton(dataSrcSysRandRadioTxt);
		dataSrcRadioGroup = new ButtonGroup();
		dataSrcRadioGroup.add(dataSrcTaRadio);
		dataSrcRadioGroup.add(dataSrcRandomOrgRadio);
		dataSrcRadioGroup.add(dataSrcSysRandRadio);
		dataSrcTaRadio.setSelected(true);

		// make the inner (invisible) panel
		JPanel dataSrcPanel = new JPanel();
		RowListMaker rm = new RowListMaker(dataSrcPanel);
		rm.addLine(dataSrcTaRadio);
		rm.addLine(dataSrcRandomOrgRadio);
		rm.addLine(dataSrcSysRandRadio);
		// make the outer (holder) titled panel
		Component inner = dataSrcPanel;
		dataSrcPanel = makeTitledPanel(dataSrcPanelTxt);
		makeTopLeftHolder(inner, dataSrcPanel);

		// mapping panel
		computeMappingBtn = new JButton(computeMappingBtnTxt);
		cancelMappingBtn = new JButton(cancelMappingBtnTxt);
		cancelMappingBtn.setEnabled(false);

		// make the inner (invisible) panel
		JPanel mappingPanel = new JPanel();
		rm = new RowListMaker(mappingPanel);
		rm.addLine(computeMappingBtn, cancelMappingBtn);
		// make the outer (holder) titled panel
		inner = mappingPanel;
		mappingPanel = makeTitledPanel(mappingPanelTxt);
		makeTopLeftHolder(inner, mappingPanel);

		// reverse mapping panel
		JPanel reverseMappingPanel = makeTitledPanel(reverseMappingPanelTxt);
		JLabel reverseMappingSolLbl = new JLabel(reverseMappingSolLblTxt);
		reverseMappingSolTxtF = new JTextField(reverseMappingSolTxtFtxt, 3);
		reverseMappingSolDescrTxtF = new JTextField(3);
		reverseMappingSolDescrTxtF.setText(getReverseMappingDescr(mainMapper));
		reverseMappingSolDescrTxtF.setEditable(false);
		computeReverseMappingBtn = new JButton(computeReverseMappingBtnTxt);
		cancelReverseMappingBtn = new JButton(cancelReverseMappingBtnTxt);
		cancelReverseMappingBtn.setEnabled(false);

		rm = new RowListMaker(reverseMappingPanel);
		rm.addLine(reverseMappingSolLbl, reverseMappingSolTxtF);
		rm.addFillComponent(reverseMappingSolDescrTxtF);
		rm.addLine(computeReverseMappingBtn, cancelReverseMappingBtn);

		computeMappingBtn.setActionCommand(computeMappingBtnActCom);
		cancelMappingBtn.setActionCommand(cancelMappingBtnActCom);
		computeReverseMappingBtn
				.setActionCommand(computeReverseMappingBtnActCom);
		cancelReverseMappingBtn.setActionCommand(cancelReverseMappingBtnActCom);
		computeMappingBtn.addActionListener(this);
		cancelMappingBtn.addActionListener(this);
		computeReverseMappingBtn.addActionListener(this);
		cancelReverseMappingBtn.addActionListener(this);

		// info panel
		JPanel infoPanel = makeTitledPanel(infoPanelTxt);

		mainMapperDescTA = new JTextArea(6, 20);
		mainMapperDescTA.setEditable(false);
		setMapperDescription(mainMapperDescTA, mainMapper, msgMapperNotSet);

		configBtn = new JButton(configBtnTxt);
		configBtn.setMnemonic(KeyEvent.VK_C);
		configBtn.setActionCommand(configBtnActCom);
		configBtn.addActionListener(this);

		rm = new RowListMaker(infoPanel);
		rm.addFillComponent(new JScrollPane(mainMapperDescTA));
		rm.addLine(configBtn);

		// main right panel
		mainRightPanel = new JPanel();
		rm = new RowListMaker(mainRightPanel);
		rm.addHorizFillComponent(dataSrcPanel);
		rm.addHorizFillComponent(mappingPanel);
		rm.addHorizFillComponent(reverseMappingPanel);
		rm.addFillComponent(infoPanel);
	}

	/*
	 * Construct Configuration window
	 */
	private void makeConfigFrame() {
		makeSrcUnivPanel();
		makeDestUnivPanel();
		makeRepeatsPanel();
		makePreferencesPanel();
		makeDescrPanel();

		configTabbedPane = new JTabbedPane();
		configTabbedPane.addTab(srcTabTxt, srcUnivPanel);
		configTabbedPane.addTab(destTabTxt, destUnivPanel);
		configTabbedPane.addTab(repTabTxt, repeatsPanel);
		configTabbedPane.addTab(prefsTabTxt, prefsPanel);

		configDialogPanel = new JPanel();
		RowMaker rowMaker = new RowMaker(configDialogPanel);
		rowMaker.addComponents(GridBagConstraints.PAGE_START, configTabbedPane);
		rowMaker.addFillComponent(descrPanel);
		configDialogSP = new JScrollPane(configDialogPanel);

		fileChooser = new JFileChooser(".");
	}

	private void makeSrcUnivPanel() {
		JPanel srcUnivElementsPanel, srcUnivSeparatorsPanel, srcUnivOutcomesPanel;

		// make Element Representation panel
		JLabel srcElemDescrLbl = new JLabel(srcElemDescrLblTxt);
		srcUnivElemNrRadio = new JRadioButton(srcUnivElemNrRadioTxt);
		srcUnivElemStringsRadio = new JRadioButton(srcUnivElemStringsRadioTxt);
		srcUnivElemNrTxtF = new JTextField(5);
		srcUnivStringsFileTxtF = new JTextField(10);
		srcUnivStringsFileTxtF.setEnabled(false);
		srcUnivElemFileBrowseBtn = new JButton(srcUnivElemFileBrowseBtnTxt);
		srcUnivElemFileBrowseBtn.setEnabled(false);
		srcUnivCaseSensitiveChk = new JCheckBox(srcUnivCaseSensitiveChkTxt);
		srcUnivCaseSensitiveChk.setEnabled(false);

		// make the inner (invisible) panel
		srcUnivElementsPanel = new JPanel();
		RowListMaker rm = new RowListMaker(srcUnivElementsPanel);
		rm.addLine(srcElemDescrLbl);
		rm.addLine(srcUnivElemNrRadio, srcUnivElemNrTxtF);
		RowMaker rowMaker = new RowMaker(new JPanel());
		rowMaker.addComponents(srcUnivElemStringsRadio);
		rowMaker.addHorizFillComponent(srcUnivStringsFileTxtF);
		rowMaker.addComponents(srcUnivElemFileBrowseBtn);
		rm.addHorizFillComponentZeroInsets(rowMaker.container);
		rm.addIndentedLine(srcUnivCaseSensitiveChk);
		// make the outer (holder) titled panel
		Component inner = srcUnivElementsPanel;
		srcUnivElementsPanel = makeTitledPanel(srcUnivElementsPanelTxt);
		makeTopHorizFillHolder(inner, srcUnivElementsPanel);

		// make Separators panel
		JLabel srcSepDescrLbl = new JLabel(srcSepDescrLblTxt);
		srcUnivSepWsRadio = new JRadioButton(srcUnivSepWsRadioTxt);
		srcUnivSepFileRadio = new JRadioButton(srcUnivSepFileRadioTxt);
		srcUnivSepFileTxtF = new JTextField(10);
		srcUnivSepFileTxtF.setEnabled(false);
		srcUnivSepFileBrowseBtn = new JButton(srcUnivSepFileBrowseBtnTxt);
		srcUnivSepFileBrowseBtn.setEnabled(false);
		srcUnivWsSepChk = new JCheckBox(srcUnivWsSepChkTxt);
		srcUnivWsSepChk.setSelected(true);
		srcUnivWsSepChk.setEnabled(false);

		// make the inner (invisible) panel
		srcUnivSeparatorsPanel = new JPanel();
		rm = new RowListMaker(srcUnivSeparatorsPanel);
		rm.addLine(srcSepDescrLbl);
		rm.addLine(srcUnivSepWsRadio);
		rowMaker = new RowMaker(new JPanel());
		rowMaker.addComponents(srcUnivSepFileRadio);
		rowMaker.addHorizFillComponent(srcUnivSepFileTxtF);
		rowMaker.addComponents(srcUnivSepFileBrowseBtn);
		rm.addHorizFillComponentZeroInsets(rowMaker.container);
		rm.addIndentedLine(srcUnivWsSepChk);
		// make the outer (holder) titled panel
		inner = srcUnivSeparatorsPanel;
		srcUnivSeparatorsPanel = makeTitledPanel(srcUnivSeparatorsPanelTxt);
		makeTopHorizFillHolder(inner, srcUnivSeparatorsPanel);

		// make Event Representation panel
		JLabel srcOutcomesDescrLbl = new JLabel(srcOutcomesDescrLblTxt);
		srcUnivSingleElemRadio = new JRadioButton(srcUnivSingleElemRadioTxt);
		srcUnivLotteryRadio = new JRadioButton(srcUnivLotteryRadioTxt);
		JPanel srcUnivLotteryPanel = makeLineBorderedPanel();
		srcUnivSelectedElemsLbl = new JLabel(srcUnivSelectedElemsLblTxt);
		srcUnivSelectedElemsLbl.setEnabled(false);
		srcUnivSelectedElemsTxtF = new JTextField(5);
		srcUnivSelectedElemsTxtF.setEnabled(false);
		srcUnivOrderMattersChk = new JCheckBox(srcUnivOrderMattersChkTxt);
		srcUnivOrderMattersChk.setEnabled(false);
		srcUnivMultipleOccurrChk = new JCheckBox(srcUnivMultipleOccurrChkTxt);
		srcUnivMultipleOccurrChk.setEnabled(false);

		rm = new RowListMaker(srcUnivLotteryPanel);
		rm.addLine(srcUnivSelectedElemsLbl, srcUnivSelectedElemsTxtF);
		rm.addLine(srcUnivOrderMattersChk);
		rm.addLine(srcUnivMultipleOccurrChk);

		// make the inner (invisible) panel
		srcUnivOutcomesPanel = new JPanel();
		rm = new RowListMaker(srcUnivOutcomesPanel);
		rm.addLine(srcOutcomesDescrLbl);
		rm.addLine(srcUnivSingleElemRadio);
		rm.addLine(srcUnivLotteryRadio, srcUnivLotteryPanel);
		// make the outer (holder) titled panel
		inner = srcUnivOutcomesPanel;
		srcUnivOutcomesPanel = makeTitledPanel(srcUnivOutcomesPanelTxt);
		makeTopLeftHolder(inner, srcUnivOutcomesPanel);

		// make the master srcUniv panel
		JLabel srcPanelDescrLbl = new JLabel(srcPanelDescrLblTxt);
		srcUnivSetBtn = new JButton(srcUnivSetBtnTxt);
		srcUnivCancelBtn = new JButton(srcUnivCancelBtnTxt);
		srcUnivCancelBtn.setEnabled(false);
		srcUnivStatusTxtF = makeStatusTextField(srcUnivStatusTxtFTxt);
		// make the inner (titled) panel
		srcUnivPanel = makeTitledPanel(srcUnivPanelTxt);
		rm = new RowListMaker(srcUnivPanel);
		rm.addLine(srcPanelDescrLbl);
		rm.addHorizFillComponent(srcUnivElementsPanel);
		rm.addHorizFillComponent(srcUnivSeparatorsPanel);
		rm.addHorizFillComponent(srcUnivOutcomesPanel);
		rm.addLineCenter(srcUnivSetBtn, srcUnivCancelBtn);
		rm.addHorizFillComponent(srcUnivStatusTxtF);
		// make the outer (holder) invisible panel
		srcUnivPanel = makeTopHorizFillHolder(srcUnivPanel);

		// Add Radio Buttons to group
		srcUnivElemReprRadioGroup = new ButtonGroup();
		srcUnivElemReprRadioGroup.add(srcUnivElemNrRadio);
		srcUnivElemReprRadioGroup.add(srcUnivElemStringsRadio);
		srcUnivSepRadioGroup = new ButtonGroup();
		srcUnivSepRadioGroup.add(srcUnivSepWsRadio);
		srcUnivSepRadioGroup.add(srcUnivSepFileRadio);
		srcUnivEventReprRadioGroup = new ButtonGroup();
		srcUnivEventReprRadioGroup.add(srcUnivSingleElemRadio);
		srcUnivEventReprRadioGroup.add(srcUnivLotteryRadio);

		// select default Radio buttons
		srcUnivElemNrRadio.setSelected(true);
		srcUnivSepWsRadio.setSelected(true);
		srcUnivSingleElemRadio.setSelected(true);

		// Set action commands and register event listener
		srcUnivElemNrRadio.setActionCommand(srcUnivElemNrRadioAC);
		srcUnivElemStringsRadio.setActionCommand(srcUnivElemStringsRadioAC);
		srcUnivSepWsRadio.setActionCommand(srcUnivSepWsRadioAC);
		srcUnivSepFileRadio.setActionCommand(srcUnivSepFileRadioAC);
		srcUnivSingleElemRadio.setActionCommand(srcUnivSingleElemRadioAC);
		srcUnivLotteryRadio.setActionCommand(srcUnivLotteryRadioAC);
		srcUnivElemNrRadio.addActionListener(this);
		srcUnivElemStringsRadio.addActionListener(this);
		srcUnivSepWsRadio.addActionListener(this);
		srcUnivSepFileRadio.addActionListener(this);
		srcUnivSingleElemRadio.addActionListener(this);
		srcUnivLotteryRadio.addActionListener(this);

		srcUnivSetBtn.setActionCommand(srcUnivSetBtnActCom);
		srcUnivCancelBtn.setActionCommand(srcUnivCancelBtnActCom);
		srcUnivElemFileBrowseBtn
				.setActionCommand(srcUnivElemFileBrowseBtnActCom);
		srcUnivSepFileBrowseBtn.setActionCommand(srcUnivSepFileBrowseBtnActCom);
		srcUnivSetBtn.addActionListener(this);
		srcUnivCancelBtn.addActionListener(this);
		srcUnivElemFileBrowseBtn.addActionListener(this);
		srcUnivSepFileBrowseBtn.addActionListener(this);
	}

	private void makeDestUnivPanel() {
		JPanel destUnivElementsPanel, destUnivSeparatorsPanel, destUnivOutcomesPanel;

		// make Element Representation panel
		JLabel destElemDescrLbl = new JLabel(destElemDescrLblTxt);
		destUnivElemNrRadio = new JRadioButton(destUnivElemNrRadioTxt);
		destUnivElemStringsRadio = new JRadioButton(destUnivElemStringsRadioTxt);
		destUnivElemNrTxtF = new JTextField(5);
		destUnivStringsFileTxtF = new JTextField(10);
		destUnivStringsFileTxtF.setEnabled(false);
		destUnivElemFileBrowseBtn = new JButton(destUnivElemFileBrowseBtnTxt);
		destUnivElemFileBrowseBtn.setEnabled(false);
		destUnivCaseSensitiveChk = new JCheckBox(destUnivCaseSensitiveChkTxt);
		destUnivCaseSensitiveChk.setEnabled(false);

		// make the inner (invisible) panel
		destUnivElementsPanel = new JPanel();
		RowListMaker rm = new RowListMaker(destUnivElementsPanel);
		rm.addLine(destElemDescrLbl);
		rm.addLine(destUnivElemNrRadio, destUnivElemNrTxtF);
		RowMaker rowMaker = new RowMaker(new JPanel());
		rowMaker.addComponents(destUnivElemStringsRadio);
		rowMaker.addHorizFillComponent(destUnivStringsFileTxtF);
		rowMaker.addComponents(destUnivElemFileBrowseBtn);
		rm.addHorizFillComponentZeroInsets(rowMaker.container);
		rm.addIndentedLine(destUnivCaseSensitiveChk);
		// make the outer (holder) titled panel
		Component inner = destUnivElementsPanel;
		destUnivElementsPanel = makeTitledPanel(destUnivElementsPanelTxt);
		makeTopHorizFillHolder(inner, destUnivElementsPanel);

		// make Separators panel
		JLabel destSepDescrLbl = new JLabel(destSepDescrLblTxt);
		destUnivSepWsRadio = new JRadioButton(destUnivSepWsRadioTxt);
		destUnivSepFileRadio = new JRadioButton(destUnivSepFileRadioTxt);
		destUnivSepFileTxtF = new JTextField(10);
		destUnivSepFileTxtF.setEnabled(false);
		destUnivSepFileBrowseBtn = new JButton(destUnivSepFileBrowseBtnTxt);
		destUnivSepFileBrowseBtn.setEnabled(false);
		destUnivWsSepChk = new JCheckBox(destUnivWsSepChkTxt);
		destUnivWsSepChk.setSelected(true);
		destUnivWsSepChk.setEnabled(false);

		// make the inner (invisible) panel
		destUnivSeparatorsPanel = new JPanel();
		rm = new RowListMaker(destUnivSeparatorsPanel);
		rm.addLine(destSepDescrLbl);
		rm.addLine(destUnivSepWsRadio);
		rowMaker = new RowMaker(new JPanel());
		rowMaker.addComponents(destUnivSepFileRadio);
		rowMaker.addHorizFillComponent(destUnivSepFileTxtF);
		rowMaker.addComponents(destUnivSepFileBrowseBtn);
		rm.addHorizFillComponentZeroInsets(rowMaker.container);
		rm.addIndentedLine(destUnivWsSepChk);
		// make the outer (holder) titled panel
		inner = destUnivSeparatorsPanel;
		destUnivSeparatorsPanel = makeTitledPanel(destUnivSeparatorsPanelTxt);
		makeTopHorizFillHolder(inner, destUnivSeparatorsPanel);

		// make Event Representation panel
		JLabel destOutcomesDescrLbl = new JLabel(destOutcomesDescrLblTxt);
		destUnivSingleElemRadio = new JRadioButton(destUnivSingleElemRadioTxt);
		destUnivLotteryRadio = new JRadioButton(destUnivLotteryRadioTxt);
		JPanel destUnivLotteryPanel = makeLineBorderedPanel();
		destUnivSelectedElemsLbl = new JLabel(destUnivSelectedElemsLblTxt);
		destUnivSelectedElemsLbl.setEnabled(false);
		destUnivSelectedElemsTxtF = new JTextField(5);
		destUnivSelectedElemsTxtF.setEnabled(false);
		destUnivOrderMattersChk = new JCheckBox(destUnivOrderMattersChkTxt);
		destUnivOrderMattersChk.setEnabled(false);
		destUnivMultipleOccurrChk = new JCheckBox(destUnivMultipleOccurrChkTxt);
		destUnivMultipleOccurrChk.setEnabled(false);

		rm = new RowListMaker(destUnivLotteryPanel);
		rm.addLine(destUnivSelectedElemsLbl, destUnivSelectedElemsTxtF);
		rm.addLine(destUnivOrderMattersChk);
		rm.addLine(destUnivMultipleOccurrChk);

		// make the inner (invisible) panel
		destUnivOutcomesPanel = new JPanel();
		rm = new RowListMaker(destUnivOutcomesPanel);
		rm.addLine(destOutcomesDescrLbl);
		rm.addLine(destUnivSingleElemRadio);
		rm.addLine(destUnivLotteryRadio, destUnivLotteryPanel);
		// make the outer (holder) titled panel
		inner = destUnivOutcomesPanel;
		destUnivOutcomesPanel = makeTitledPanel(destUnivOutcomesPanelTxt);
		makeTopLeftHolder(inner, destUnivOutcomesPanel);

		// make the master destUniv panel
		JLabel destPanelDescrLbl = new JLabel(destPanelDescrLblTxt);
		destUnivSetBtn = new JButton(destUnivSetBtnTxt);
		destUnivCancelBtn = new JButton(destUnivCancelBtnTxt);
		destUnivCancelBtn.setEnabled(false);
		destUnivStatusTxtF = makeStatusTextField(destUnivStatusTxtFTxt);
		// make the inner (titled) panel
		destUnivPanel = makeTitledPanel(destUnivPanelTxt);
		rm = new RowListMaker(destUnivPanel);
		rm.addLine(destPanelDescrLbl);
		rm.addHorizFillComponent(destUnivElementsPanel);
		rm.addHorizFillComponent(destUnivSeparatorsPanel);
		rm.addHorizFillComponent(destUnivOutcomesPanel);
		rm.addLineCenter(destUnivSetBtn, destUnivCancelBtn);
		rm.addHorizFillComponent(destUnivStatusTxtF);
		// make the outer (holder) invisible panel
		destUnivPanel = makeTopHorizFillHolder(destUnivPanel);

		// Add Radio Buttons to group
		destUnivElemReprRadioGroup = new ButtonGroup();
		destUnivElemReprRadioGroup.add(destUnivElemNrRadio);
		destUnivElemReprRadioGroup.add(destUnivElemStringsRadio);
		destUnivSepRadioGroup = new ButtonGroup();
		destUnivSepRadioGroup.add(destUnivSepWsRadio);
		destUnivSepRadioGroup.add(destUnivSepFileRadio);
		destUnivEventReprRadioGroup = new ButtonGroup();
		destUnivEventReprRadioGroup.add(destUnivSingleElemRadio);
		destUnivEventReprRadioGroup.add(destUnivLotteryRadio);

		// select default Radio buttons
		destUnivElemNrRadio.setSelected(true);
		destUnivSepWsRadio.setSelected(true);
		destUnivSingleElemRadio.setSelected(true);

		// Set action commands and register event listener
		destUnivElemNrRadio.setActionCommand(destUnivElemNrRadioAC);
		destUnivElemStringsRadio.setActionCommand(destUnivElemStringsRadioAC);
		destUnivSepWsRadio.setActionCommand(destUnivSepWsRadioAC);
		destUnivSepFileRadio.setActionCommand(destUnivSepFileRadioAC);
		destUnivSingleElemRadio.setActionCommand(destUnivSingleElemRadioAC);
		destUnivLotteryRadio.setActionCommand(destUnivLotteryRadioAC);
		destUnivElemNrRadio.addActionListener(this);
		destUnivElemStringsRadio.addActionListener(this);
		destUnivSepWsRadio.addActionListener(this);
		destUnivSepFileRadio.addActionListener(this);
		destUnivSingleElemRadio.addActionListener(this);
		destUnivLotteryRadio.addActionListener(this);

		destUnivSetBtn.setActionCommand(destUnivSetBtnActCom);
		destUnivCancelBtn.setActionCommand(destUnivCancelBtnActCom);
		destUnivElemFileBrowseBtn
				.setActionCommand(destUnivElemFileBrowseBtnActCom);
		destUnivSepFileBrowseBtn
				.setActionCommand(destUnivSepFileBrowseBtnActCom);
		destUnivSetBtn.addActionListener(this);
		destUnivCancelBtn.addActionListener(this);
		destUnivElemFileBrowseBtn.addActionListener(this);
		destUnivSepFileBrowseBtn.addActionListener(this);
	}

	private void makeRepeatsPanel() {
		// Make the New Data Panel
		JPanel repNewDataPanel = makeTitledPanel(repNewDataPanelTxt);
		JLabel repRepDescrLbl = new JLabel(repRepDescrLblTxt);
		JLabel repRepeatsLbl = new JLabel(repRepeatsLblTxt);
		repRepeatsTxtF = new JTextField(3);
		repRepeatsTxtF.setText(repRepeatsTxtFTxt);
		repSetBtn = new JButton(repSetBtnTxt);
		repCancelBtn = new JButton(repCancelBtnTxt);
		repCancelBtn.setEnabled(false);
		repStatusTxtF = makeStatusTextField(repStatusTxtFTxt);

		RowListMaker rm = new RowListMaker(repNewDataPanel);
		rm.addLine(repRepDescrLbl);
		rm.addLine(repRepeatsLbl, repRepeatsTxtF, repSetBtn, repCancelBtn);
		rm.addHorizFillComponent(repStatusTxtF);

		// Make the Min Repeats Panel
		JPanel repMinRepeatsPanel = makeTitledPanel(repMinRepeatsPanelTxt);
		JLabel repMinrDescrLbl = new JLabel(repMinrDescrLblTxt);
		JLabel repMinrMinRepeatsLbl = new JLabel(repMinrMinRepeatsLblTxt);
		JLabel repMinrErrLbl = new JLabel(repMinrErrLblTxt);
		repMinrMinRepeatsTxtF = new JTextField(3);
		repMinrErrTxtF = new JTextField(5);
		repMinrMinRepeatsTxtF.setEnabled(false);
		repMinrErrTxtF.setEnabled(false);
		repMinrErrTxtF.setHorizontalAlignment(JTextField.RIGHT);
		repSearchMinRepBtn = new JButton(repSearchMinRepBtnTxt);
		repCancelMinRepBtn = new JButton(repCancelMinRepBtnTxt);
		repCancelMinRepBtn.setEnabled(false);
		repMinRepStatusTxtF = makeStatusTextField(repMinRepStatusTxtFTxt);

		rm = new RowListMaker(repMinRepeatsPanel);
		rm.addLine(repMinrDescrLbl);
		rm.addLine(repMinrMinRepeatsLbl, repMinrMinRepeatsTxtF, repMinrErrLbl,
				repMinrErrTxtF, new JLabel("%"));
		rm.addLine(repSearchMinRepBtn, repCancelMinRepBtn);
		rm.addHorizFillComponent(repMinRepStatusTxtF);

		// Make the Found Panel
		JPanel repFoundPanel = makeTitledPanel(repFoundPanelTxt);
		JLabel repFoundRepLbl = new JLabel(repFoundRepLblTxt);
		JLabel repFoundErrLbl = new JLabel(repFoundErrLblTxt);
		repFoundRepTxtF = new JTextField(3);
		repFoundErrTxtF = new JTextField(5);
		repFoundRepTxtF.setEnabled(false);
		repFoundErrTxtF.setEnabled(false);
		repFoundErrTxtF.setHorizontalAlignment(JTextField.RIGHT);

		rm = new RowListMaker(repFoundPanel);
		rm.addLine(repFoundRepLbl, repFoundRepTxtF, repFoundErrLbl,
				repFoundErrTxtF, new JLabel("%"));

		// Make the Target Err Panel
		JPanel repTargetErrPanel = makeTitledPanel(repTargetErrPanelTxt);
		JLabel repTargetErrDescrLbl = new JLabel(repTargetErrDescrLblTxt);
		JLabel repTargetErrLbl = new JLabel(repTargetErrLblTxt);
		repTargetErrTxtF = new JTextField(3);
		repTargetErrTxtF.setEnabled(false);
		repSearchTargetErrBtn = new JButton(repSearchTargetErrBtnTxt);
		repCancelTargetErrBtn = new JButton(repCancelTargetErrBtnTxt);
		repSearchTargetErrBtn.setEnabled(false);
		repCancelTargetErrBtn.setEnabled(false);
		repTargetErrStatusTxtF = makeStatusTextField(repTargetErrStatusTxtFTxt);

		rm = new RowListMaker(repTargetErrPanel);
		rm.addLine(repTargetErrDescrLbl);
		rm.addLine(repTargetErrLbl, repTargetErrTxtF, new JLabel("%"));
		rm.addLine(repSearchTargetErrBtn, repCancelTargetErrBtn);
		rm.addLine(repFoundPanel);
		rm.addHorizFillComponent(repTargetErrStatusTxtF);

		// Make the Repeats Panel
		// make the inner (titled) panel
		repeatsPanel = makeTitledPanel(repeatsPanelTxt);
		JLabel repPanelDescrLbl = new JLabel(repPanelDescrLblTxt);
		rm = new RowListMaker(repeatsPanel);
		rm.addLine(repPanelDescrLbl);
		rm.addHorizFillComponent(repNewDataPanel);
		rm.addHorizFillComponent(repMinRepeatsPanel);
		rm.addHorizFillComponent(repTargetErrPanel);
		// make the outer (holder) invisible panel
		repeatsPanel = makeTopHorizFillHolder(repeatsPanel);

		// Set action commands and register event listener
		repSetBtn.setActionCommand(repSetBtnActCom);
		repCancelBtn.setActionCommand(repCancelBtnActCom);
		repSearchMinRepBtn.setActionCommand(repSearchMinRepBtnActCom);
		repCancelMinRepBtn.setActionCommand(repCancelMinRepBtnActCom);
		repSearchTargetErrBtn.setActionCommand(repSearchTargetErrBtnActCom);
		repCancelTargetErrBtn.setActionCommand(repCancelTargetErrBtnActCom);

		repSetBtn.addActionListener(this);
		repCancelBtn.addActionListener(this);
		repSearchMinRepBtn.addActionListener(this);
		repCancelMinRepBtn.addActionListener(this);
		repSearchTargetErrBtn.addActionListener(this);
		repCancelTargetErrBtn.addActionListener(this);
	}

	private void makePreferencesPanel() {
		// make the inner (titled) panel
		prefsPanel = makeTitledPanel(prefsPanelTxt);
		JLabel themeLbl = new JLabel(themeLblTxt);
		themeCmbBox = new JComboBox<>(getLookAndFeelNames());

		int lafIndex = 0;
		for (LookAndFeelInfo lafInfo : getAvailableLookAndFeels()) {
			if (lafInfo.getClassName().equals(startupLafClassName)) {
				themeCmbBox.setSelectedIndex(lafIndex);
				break;
			}
			lafIndex++;
		}

		applyPrefsBtn = new JButton(applyPrefsBtnTxt);

		FormMaker fm = new FormMaker(prefsPanel);
		fm.addFormLine(themeLbl, themeCmbBox);
		fm.addSingleCompCenter(applyPrefsBtn);
		// make the outer (invisible) panel
		prefsPanel = makeTopHorizFillHolder(prefsPanel);

		applyPrefsBtn.setActionCommand(applyPrefsBtnActCom);
		applyPrefsBtn.addActionListener(this);
	}

	private void makeDescrPanel() {
		JLabel srcDescrLbl = new JLabel(srcDescrLblTxt);
		srcDescrTA = new JTextArea(4, 20);
		srcDescrTA.setEditable(false);
		JScrollPane srcDescrSP = new JScrollPane(srcDescrTA);

		JLabel destDescrLbl = new JLabel(destDescrLblTxt);
		destDescrTA = new JTextArea(4, 20);
		destDescrTA.setEditable(false);

		JScrollPane destDescrSP = new JScrollPane(destDescrTA);

		JLabel repDescrLbl = new JLabel(repDescrLblTxt);
		repDescrTA = new JTextArea(4, 20);
		repDescrTA.setEditable(false);
		JScrollPane repDescrSP = new JScrollPane(repDescrTA);

		descrPanel = new JPanel();
		RowListMaker rm = new RowListMaker(descrPanel);
		rm.addLine(srcDescrLbl);
		rm.addFillComponent(srcDescrSP);
		rm.addLine(destDescrLbl);
		rm.addFillComponent(destDescrSP);
		rm.addLine(repDescrLbl);
		rm.addFillComponent(repDescrSP);
	}

	private void clearRepeatsPanel() {
		// Min Repeats Panel
		repMinrMinRepeatsTxtF.setText("");
		repMinrErrTxtF.setText("");

		// Found Panel
		repFoundRepTxtF.setText("");
		repFoundErrTxtF.setText("");
	}

	private static String getReverseMappingDescr(EventMapper mapper) {
		if (mapper == null)
			return "Mapper not set";
		return "Solutions for a reverse mapping: " + mapper.Q;
	}

	void displayExecError(JTextComponent txtC, ExecutionException e) {
		String msg = null;

		if (e != null) {
			Throwable cause;
			cause = e.getCause();
			if (cause != null)
				msg = cause.getMessage();
			if (msg == null || msg.isEmpty())
				msg = e.getClass().getName();
		}

		txtC.setText("Exception: " + msg);
	}

	private void displayExecRunning(JTextComponent txtC) {
		txtC.setText(msgWorkerRunning);
	}

	private void displayExecSuccess(JTextComponent txtC) {
		txtC.setText(msgWorkerSuccess);
	}

	private void displayExecCancelled(JTextComponent txtC) {
		txtC.setText(msgWorkerCancelled);
	}

	/*
	 * done() methods for the workers in the config dialog
	 */
	// TODO: document that these methods must only be called from the done()
	// method of the respective swingworkers running on the event dispatch
	// thread

	void configSrcWrkDone(
			SwingWorker<EventRepresConverter, Void> finishedWorker,
			EventRepresConverter newUniv) {
		// if canceled => crt*Worker == null
		// if canceled and other(s) started, finishedWorker != crt*Worker
		// in both cases, abort
		if (finishedWorker != crtSrcUnivWorker)
			return;

		crtSrcUnivWorker = null;
		configWindowSrcUniv = newUniv;
		setUniverseDescription(srcDescrTA, configWindowSrcUniv, msgUnivNotSet);

		// throw away old (now invalid) mapper
		configWindowMapper = null;
		setMapperDescription(repDescrTA, configWindowMapper, msgMapperNotSet);
		clearRepeatsPanel();

		displayExecSuccess(srcUnivStatusTxtF);
		srcUnivCancelBtn.setEnabled(false);
		srcUnivSetBtn.setEnabled(true);
	}

	void configDestWrkDone(
			SwingWorker<EventRepresConverter, Void> finishedWorker,
			EventRepresConverter newUniv) {
		// if canceled (and possibly other worker(s) started) abort
		if (finishedWorker != crtDestUnivWorker)
			return;

		crtDestUnivWorker = null;
		configWindowDestUniv = newUniv;
		setUniverseDescription(destDescrTA, configWindowDestUniv, msgUnivNotSet);

		// throw away old (now invalid) mapper
		configWindowMapper = null;
		setMapperDescription(repDescrTA, configWindowMapper, msgMapperNotSet);
		clearRepeatsPanel();

		displayExecSuccess(destUnivStatusTxtF);
		destUnivCancelBtn.setEnabled(false);
		destUnivSetBtn.setEnabled(true);
	}

	void configSrcWrkException(
			SwingWorker<EventRepresConverter, Void> finishedWorker,
			ExecutionException e) {
		if (finishedWorker != crtSrcUnivWorker)
			return;

		crtSrcUnivWorker = null;

		// show descriptive text and change nothing
		displayExecError(srcUnivStatusTxtF, e);
		srcUnivCancelBtn.setEnabled(false);
		srcUnivSetBtn.setEnabled(true);
	}

	void configDestWrkException(
			SwingWorker<EventRepresConverter, Void> finishedWorker,
			ExecutionException e) {
		if (finishedWorker != crtDestUnivWorker)
			return;

		crtDestUnivWorker = null;

		// show descriptive text and change nothing
		displayExecError(destUnivStatusTxtF, e);
		destUnivCancelBtn.setEnabled(false);
		destUnivSetBtn.setEnabled(true);
	}

	void configRepeatsCheckWrkDone(
			SwingWorker<EventMapper, Void> finishedWorker, EventMapper newMapper) {
		if (finishedWorker != crtRepeatsCheckWorker)
			return;

		crtRepeatsCheckWorker = null;
		configWindowMapper = newMapper;
		setMapperDescription(repDescrTA, configWindowMapper, msgMapperMustBeSet);

		displayExecSuccess(repStatusTxtF);
		repCancelBtn.setEnabled(false);
		repSetBtn.setEnabled(true);
	}

	void configRepeatsCheckWrkException(
			SwingWorker<EventMapper, Void> finishedWorker, ExecutionException e) {
		if (finishedWorker != crtRepeatsCheckWorker)
			return;

		crtRepeatsCheckWorker = null;

		// show descriptive text and change nothing
		displayExecError(repStatusTxtF, e);
		repCancelBtn.setEnabled(false);
		repSetBtn.setEnabled(true);
	}

	void configSearchMinRepeatsWrkDone(
			SwingWorker<RepeatsError, Void> finishedWorker, RepeatsError result) {
		if (finishedWorker != crtSearchMinRepWorker)
			return;

		crtSearchMinRepWorker = null;

		repMinrMinRepeatsTxtF.setText(Integer.toString(result.repeats));
		repMinrErrTxtF.setText(result.error);

		displayExecSuccess(repMinRepStatusTxtF);
		repCancelMinRepBtn.setEnabled(false);
		repSearchMinRepBtn.setEnabled(true);
	}

	void configSearchMinRepeatsWrkException(
			SwingWorker<RepeatsError, Void> finishedWorker, ExecutionException e) {
		if (finishedWorker != crtSearchMinRepWorker)
			return;

		crtSearchMinRepWorker = null;

		displayExecError(repMinRepStatusTxtF, e);
		repCancelMinRepBtn.setEnabled(false);
		repSearchMinRepBtn.setEnabled(true);
	}

	void mappingWrkDone(SwingWorker<Mapping, String> finishedWorker,
			Mapping mapping, InputSourceType inpSrcType) {
		if (finishedWorker != crtMappingWorker)
			return;

		crtMappingWorker = null;

		appendToOutputTA(mapping.destEvent.userRepres);
		removeConsumedData(mainSrcUniv.elementRepresConverter, inpSrcType);
		crtMappingInputReader = null;

		displayExecSuccess(mappingStatusTxtF);
		cancelMappingBtn.setEnabled(false);
		computeMappingBtn.setEnabled(true);
		computeReverseMappingBtn.setEnabled(true);
	}

	void mappingWrkException(SwingWorker<Mapping, String> finishedWorker,
			ExecutionException e) {
		if (finishedWorker != crtMappingWorker)
			return;

		crtMappingWorker = null;
		crtMappingInputReader = null;

		// show descriptive text and change nothing
		displayExecError(mappingStatusTxtF, e);
		cancelMappingBtn.setEnabled(false);
		computeMappingBtn.setEnabled(true);
		computeReverseMappingBtn.setEnabled(true);
	}

	void reverseMappingWrkDone(
			SwingWorker<ReverseMapping, String> finishedWorker,
			ReverseMapping reverseMapping, InputSourceType inpSrcType) {
		if (finishedWorker != crtReverseMappingWorker)
			return;

		crtReverseMappingWorker = null;

		appendToOutputTA(reverseMapping.getSrcEventsUserRepres());
		removeConsumedData(mainDestUniv.elementRepresConverter, inpSrcType);
		crtMappingInputReader = null;

		displayExecSuccess(mappingStatusTxtF);
		cancelReverseMappingBtn.setEnabled(false);
		computeReverseMappingBtn.setEnabled(true);
		computeMappingBtn.setEnabled(true);
	}

	void reverseMappingWrkException(
			SwingWorker<ReverseMapping, String> finishedWorker,
			ExecutionException e) {
		if (finishedWorker != crtReverseMappingWorker)
			return;

		crtReverseMappingWorker = null;
		crtMappingInputReader = null;

		displayExecError(mappingStatusTxtF, e);
		cancelReverseMappingBtn.setEnabled(false);
		computeReverseMappingBtn.setEnabled(true);
		computeMappingBtn.setEnabled(true);
	}

	void processDirectMappingWrkStatus(SwingWorker<Mapping, String> worker,
			String statusMsg) {
		if (worker != crtMappingWorker)
			return;

		mappingStatusTxtF.setText(statusMsg);
	}

	void processReverseMappingWrkStatus(
			SwingWorker<ReverseMapping, String> worker, String statusMsg) {
		if (worker != crtReverseMappingWorker)
			return;

		mappingStatusTxtF.setText(statusMsg);
	}

	/*
	 * Utilities
	 */

	private static JPanel makeTitledPanel(String panelTitle) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(panelTitle));
		return panel;
	}

	private static JPanel makeLineBorderedPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		return panel;
	}

	private static JTextField makeStatusTextField(String statusText) {
		JTextField statusTxtF = new JTextField(statusTxtfCols);
		statusTxtF.setText(statusText);
		statusTxtF.setEditable(false);
		return statusTxtF;
	}

	private void showMsgInvalidNr(String nr) {
		JOptionPane.showMessageDialog(mainFrame, msgInvalidNr + "\n" + nr);
	}

	private void setUniverseDescription(JTextArea ta,
			EventRepresConverter univ, String defaultMsg) {
		if (univ == null)
			ta.setText(defaultMsg);
		else
			ta.setText(univ.toString());
	}

	private void setMapperDescription(JTextArea ta, EventMapper mapper,
			String defaultMsg) {
		if (mapper == null)
			ta.setText(defaultMsg);
		else
			ta.setText(mapper.getDescription());
	}

	private static BigInteger getBigInteger(JTextField txtF)
			throws NumberFormatException {
		return new BigInteger(txtF.getText().trim());
	}

	/**
	 * Remove consumed data (and additional lines containing only separators)
	 * from the input JTextArea. If <code>'\n'</code> isn't separator, nothing
	 * will be removed. If <code>'\n'</code> is separator, the first lines
	 * containing only separators will be removed. This operation is performed
	 * only for the {@link InputSourceType.USER_READER} {@link InputSourceType}.
	 * 
	 * @param conv
	 *            used to check if characters in JTextArea are separators
	 * @param inpSrcType
	 *            used to disable the operation if
	 *            {@link InputSourceType.USER_READER} is not supplied.
	 */
	private void removeConsumedData(ElementRepresConverter conv,
			InputSourceType inpSrcType) {
		if (inpSrcType != InputSourceType.USER_READER)
			return;
		try {
			StringBuilder sb = new StringBuilder();
			int i;
			boolean onlySep = true;
			while ((i = crtMappingInputReader.read()) != -1) {
				char c = (char) i;
				sb.append(c);
				if (onlySep) {
					// while only separators have been found, remove empty lines
					if (conv.isSeparator(c)) {
						if (c == '\r' || c == '\n')
							sb = new StringBuilder();
					} else {
						onlySep = false;
					}
				}
			}
			// if only separators, delete the last line as well
			if (onlySep)
				inputTA.setText(null);
			else
				inputTA.setText(sb.toString());
		} catch (IOException e) {
			// TODO: change nothing; log
		}
	}

	private void appendToOutputTA(String userRepres) {
		String crtOutputTxt = outputTA.getText();
		if (!crtOutputTxt.isEmpty())
			crtOutputTxt += "\n";
		outputTA.setText(crtOutputTxt + userRepres);
	}

	// TODO: rename this method
	private void setLafRelatedProperties() {
		setNeverUpdateCaretPolicy(mainMapperDescTA);
		setNeverUpdateCaretPolicy(srcDescrTA);
		setNeverUpdateCaretPolicy(destDescrTA);
		setNeverUpdateCaretPolicy(repDescrTA);
	}

	private void setNeverUpdateCaretPolicy(JTextComponent textComp) {
		Caret c = textComp.getCaret();
		if (c instanceof DefaultCaret)
			((DefaultCaret) c).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}

	/*
	 * ActionListener
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		String actComm = event.getActionCommand();

		if (actComm.equals(configBtnActCom)) {
			// prepare config window data elements
			configWindowSrcUniv = mainSrcUniv;
			configWindowDestUniv = mainDestUniv;
			configWindowMapper = mainMapper;

			clearRepeatsPanel();
			setUniverseDescription(srcDescrTA, configWindowSrcUniv,
					msgUnivNotSet);
			setUniverseDescription(destDescrTA, configWindowDestUniv,
					msgUnivNotSet);
			setMapperDescription(repDescrTA, configWindowMapper,
					msgMapperNotSet);

			// TODO: reuse the JOptionPane and the JDialog
			// show config dialog window

			JOptionPane configOptionPane = new JOptionPane(configDialogSP,
					JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
					null, new Object[] { configOkTxt, configCancelTxt });
			configDialog = configOptionPane.createDialog(mainFrame,
					configDialogTitle);

			configDialog.setResizable(true);
			configDialog.pack();
			configDialog.setVisible(true);

			// blocked until dialog window is closed

			Object dialogResult = configOptionPane.getValue();

			cancelAllConfigDialogWorkers();

			if (dialogResult == null) {
				// config window was ended by closing it, do nothing
			} else if (dialogResult.equals(configCancelTxt)) {
				// changes (if any) canceled, do nothing
			} else if (dialogResult.equals(configOkTxt)) {
				mainSrcUniv = configWindowSrcUniv;
				mainDestUniv = configWindowDestUniv;
				mainMapper = configWindowMapper;
				setMapperDescription(mainMapperDescTA, mainMapper,
						msgMapperNotSet);
				reverseMappingSolDescrTxtF
						.setText(getReverseMappingDescr(mainMapper));
			} else {
				// other non-null value returned (e.g. closed with ESC key)
			}
		}
		// config window radio buttons
		// source universe
		else if (actComm.equals(srcUnivElemNrRadioAC)) {
			srcUnivStringsFileTxtF.setEnabled(false);
			srcUnivCaseSensitiveChk.setEnabled(false);
			srcUnivElemFileBrowseBtn.setEnabled(false);
			srcUnivElemNrTxtF.setEnabled(true);
		} else if (actComm.equals(srcUnivElemStringsRadioAC)) {
			srcUnivElemNrTxtF.setEnabled(false);
			srcUnivStringsFileTxtF.setEnabled(true);
			srcUnivCaseSensitiveChk.setEnabled(true);
			srcUnivElemFileBrowseBtn.setEnabled(true);
		} else if (actComm.equals(srcUnivSepWsRadioAC)) {
			srcUnivSepFileTxtF.setEnabled(false);
			srcUnivSepFileBrowseBtn.setEnabled(false);
			srcUnivWsSepChk.setEnabled(false);
		} else if (actComm.equals(srcUnivSepFileRadioAC)) {
			srcUnivSepFileTxtF.setEnabled(true);
			srcUnivSepFileBrowseBtn.setEnabled(true);
			srcUnivWsSepChk.setEnabled(true);
		} else if (actComm.equals(srcUnivSingleElemRadioAC)) {
			srcUnivSelectedElemsLbl.setEnabled(false);
			srcUnivSelectedElemsTxtF.setEnabled(false);
			srcUnivOrderMattersChk.setEnabled(false);
			srcUnivMultipleOccurrChk.setEnabled(false);
		} else if (actComm.equals(srcUnivLotteryRadioAC)) {
			srcUnivSelectedElemsLbl.setEnabled(true);
			srcUnivSelectedElemsTxtF.setEnabled(true);
			srcUnivOrderMattersChk.setEnabled(true);
			srcUnivMultipleOccurrChk.setEnabled(true);
		}
		// destination universe
		else if (actComm.equals(destUnivElemNrRadioAC)) {
			destUnivStringsFileTxtF.setEnabled(false);
			destUnivCaseSensitiveChk.setEnabled(false);
			destUnivElemFileBrowseBtn.setEnabled(false);
			destUnivElemNrTxtF.setEnabled(true);
		} else if (actComm.equals(destUnivElemStringsRadioAC)) {
			destUnivElemNrTxtF.setEnabled(false);
			destUnivStringsFileTxtF.setEnabled(true);
			destUnivCaseSensitiveChk.setEnabled(true);
			destUnivElemFileBrowseBtn.setEnabled(true);
		} else if (actComm.equals(destUnivSepWsRadioAC)) {
			destUnivSepFileTxtF.setEnabled(false);
			destUnivSepFileBrowseBtn.setEnabled(false);
			destUnivWsSepChk.setEnabled(false);
		} else if (actComm.equals(destUnivSepFileRadioAC)) {
			destUnivSepFileTxtF.setEnabled(true);
			destUnivSepFileBrowseBtn.setEnabled(true);
			destUnivWsSepChk.setEnabled(true);
		} else if (actComm.equals(destUnivSingleElemRadioAC)) {
			destUnivSelectedElemsLbl.setEnabled(false);
			destUnivSelectedElemsTxtF.setEnabled(false);
			destUnivOrderMattersChk.setEnabled(false);
			destUnivMultipleOccurrChk.setEnabled(false);
		} else if (actComm.equals(destUnivLotteryRadioAC)) {
			destUnivSelectedElemsLbl.setEnabled(true);
			destUnivSelectedElemsTxtF.setEnabled(true);
			destUnivOrderMattersChk.setEnabled(true);
			destUnivMultipleOccurrChk.setEnabled(true);
		}
		// set/cancel for src/dest universes
		else if (actComm.equals(srcUnivSetBtnActCom)) {
			performSrcUnivSetBtnFunction();
		} else if (actComm.equals(srcUnivCancelBtnActCom)) {
			performSrcUnivCancelBtnFunction();
		} else if (actComm.equals(destUnivSetBtnActCom)) {
			performDestUnivSetBtnFunction();
		} else if (actComm.equals(destUnivCancelBtnActCom)) {
			performDestUnivCancelBtnFunction();
		}
		// browse buttons
		else if (actComm.equals(srcUnivElemFileBrowseBtnActCom)) {
			performSrcUnivElemFileBrowseBtnFunction();
		} else if (actComm.equals(destUnivElemFileBrowseBtnActCom)) {
			performDestUnivElemFileBrowseBtnFunction();
		} else if (actComm.equals(srcUnivSepFileBrowseBtnActCom)) {
			performSrcUnivSepFileBrowseBtnFunction();
		} else if (actComm.equals(destUnivSepFileBrowseBtnActCom)) {
			performDestUnivSepFileBrowseBtnFunction();
		}
		// buttons for repeats tab
		else if (actComm.equals(repSetBtnActCom)) {
			performRepSetBtnFunction();
		} else if (actComm.equals(repCancelBtnActCom)) {
			performRepCancelBtnFunction();
		} else if (actComm.equals(repSearchMinRepBtnActCom)) {
			performRepSearchMinRepBtnFunction();
		} else if (actComm.equals(repCancelMinRepBtnActCom)) {
			performRepCancelMinRepBtnFunction();
		} else if (actComm.equals(repSearchTargetErrBtnActCom)) {
			performRepSearchTargetErrBtnFunction();
		} else if (actComm.equals(repCancelTargetErrBtnActCom)) {
			performRepCancelTargetErrBtnFunction();
		}
		// preferences tab
		else if (actComm.equals(applyPrefsBtnActCom)) {
			performapplyPrefsBtnFunction();
		}
		// compute direct/reverse mapping buttons
		else if (actComm.equals(computeMappingBtnActCom)) {
			performComputeMappingBtnFunction();
		} else if (actComm.equals(cancelMappingBtnActCom)) {
			performCancelMappingBtnFunction();
		} else if (actComm.equals(computeReverseMappingBtnActCom)) {
			performComputeReverseMappingBtnFunction();
		} else if (actComm.equals(cancelReverseMappingBtnActCom)) {
			performCancelReverseMappingBtnFunction();
		} else {
			// TODO: unknown action event, log
		}
	}

	private void performSrcUnivSetBtnFunction() {
		final ElementRepresentation elemRepr;
		final EventRepresentation eventRepr;
		final String customStringsFileName, separatorsFileName;
		final BigInteger nr, extractedElems;
		final boolean caseSensitive, whitespaceAlwaysSep;
		final boolean orderMatters, multipleOccurrences;

		// get data from config window
		try {
			if (srcUnivElemNrRadio.isSelected()) {
				elemRepr = ElementRepresentation.NUMBER;
				nr = getBigInteger(srcUnivElemNrTxtF);
				customStringsFileName = null; // dummy
				caseSensitive = false; // dummy
			} else if (srcUnivElemStringsRadio.isSelected()) {
				elemRepr = ElementRepresentation.CUSTOM_STRINGS;
				nr = null; // dummy
				customStringsFileName = srcUnivStringsFileTxtF.getText();
				if (customStringsFileName.isEmpty()) {
					// TODO msg string
					JOptionPane.showMessageDialog(configTabbedPane,
							"Please select strings file");
					return;
				}
				caseSensitive = srcUnivCaseSensitiveChk.isSelected();
			} else {
				JOptionPane.showMessageDialog(configTabbedPane, msgSelElemRepr);
				return;
			}

			if (srcUnivSepWsRadio.isSelected()) {
				separatorsFileName = null;
				whitespaceAlwaysSep = false; // dummy
			} else if (srcUnivSepFileRadio.isSelected()) {
				separatorsFileName = srcUnivSepFileTxtF.getText();
				if (separatorsFileName.isEmpty()) {
					// TODO msg string
					JOptionPane.showMessageDialog(configTabbedPane,
							"Please select separators file");
					return;
				}
				whitespaceAlwaysSep = srcUnivWsSepChk.isSelected();
			} else {
				JOptionPane.showMessageDialog(configTabbedPane, msgSelSepRepr);
				return;
			}

			if (srcUnivSingleElemRadio.isSelected()) {
				eventRepr = EventRepresentation.SINGLE_ELEMENT;
				extractedElems = null; // dummy
				orderMatters = false; // dummy
				multipleOccurrences = false; // dummy
			} else if (srcUnivLotteryRadio.isSelected()) {
				eventRepr = EventRepresentation.LOTTERY;
				extractedElems = getBigInteger(srcUnivSelectedElemsTxtF);
				orderMatters = srcUnivOrderMattersChk.isSelected();
				multipleOccurrences = srcUnivMultipleOccurrChk.isSelected();
			} else {
				JOptionPane
						.showMessageDialog(configTabbedPane, msgSelEventRepr);
				return;
			}
		} catch (NumberFormatException e) {
			showMsgInvalidNr(e.getMessage());
			return;
		}

		// check that no Repeat workers are running
		if (crtRepeatsCheckWorker != null || crtSearchMinRepWorker != null
				|| crtSearchTargetErrWorker != null) {
			JOptionPane.showMessageDialog(configTabbedPane,
					msgWaitForRepWorkersToFinish);
			return;
		}

		// get SwingWorker
		BackgroundTask<EventRepresConverter, Void> bgTask = new BackgroundTask<EventRepresConverter, Void>() {
			@Override
			public EventRepresConverter compute(
					Worker<EventRepresConverter, Void> worker) throws Exception {
				return EventRepresConverter.getInstance(
						new ElementRepresConverter(elemRepr, nr,
								customStringsFileName, caseSensitive,
								separatorsFileName, whitespaceAlwaysSep),
						eventRepr, extractedElems, orderMatters,
						multipleOccurrences);
			}
		};
		ResultHandler<EventRepresConverter, Void> resultHandler = new ResultHandler<EventRepresConverter, Void>() {
			@Override
			public void exception(Worker<EventRepresConverter, Void> worker,
					ExecutionException e) {
				configSrcWrkException(worker, e);
			}

			@Override
			public void result(Worker<EventRepresConverter, Void> worker,
					EventRepresConverter result) {
				configSrcWrkDone(worker, result);
			}
		};

		crtSrcUnivWorker = new Worker<EventRepresConverter, Void>(bgTask,
				resultHandler, null);

		// update GUI elements
		displayExecRunning(srcUnivStatusTxtF);
		srcUnivSetBtn.setEnabled(false);
		srcUnivCancelBtn.setEnabled(true);

		crtSrcUnivWorker.execute();
	}

	private void performSrcUnivCancelBtnFunction() {
		if (crtSrcUnivWorker == null) {
			return;
		}
		// TODO get return value and log it
		// TODO decide what param (true or false) to supply
		crtSrcUnivWorker.cancel(false);
		crtSrcUnivWorker = null;
		// update GUI elements
		displayExecCancelled(srcUnivStatusTxtF);
		srcUnivCancelBtn.setEnabled(false);
		srcUnivSetBtn.setEnabled(true);
	}

	private void performDestUnivSetBtnFunction() {
		final ElementRepresentation elemRepr;
		final EventRepresentation eventRepr;
		final String customStringsFileName, separatorsFileName;
		final BigInteger nr, extractedElems;
		final boolean caseSensitive, whitespaceAlwaysSep;
		final boolean orderMatters, multipleOccurrences;

		// get data from config window
		try {
			if (destUnivElemNrRadio.isSelected()) {
				elemRepr = ElementRepresentation.NUMBER;
				nr = getBigInteger(destUnivElemNrTxtF);
				customStringsFileName = null; // dummy
				caseSensitive = false; // dummy
			} else if (destUnivElemStringsRadio.isSelected()) {
				elemRepr = ElementRepresentation.CUSTOM_STRINGS;
				nr = null; // dummy
				customStringsFileName = destUnivStringsFileTxtF.getText();
				if (customStringsFileName.isEmpty()) {
					// TODO msg string
					JOptionPane.showMessageDialog(configTabbedPane,
							"Please select strings file");
					return;
				}
				caseSensitive = destUnivCaseSensitiveChk.isSelected();
			} else {
				JOptionPane.showMessageDialog(configTabbedPane, msgSelElemRepr);
				return;
			}

			if (destUnivSepWsRadio.isSelected()) {
				separatorsFileName = null;
				whitespaceAlwaysSep = false; // dummy
			} else if (destUnivSepFileRadio.isSelected()) {
				separatorsFileName = destUnivSepFileTxtF.getText();
				if (separatorsFileName.isEmpty()) {
					// TODO msg string
					JOptionPane.showMessageDialog(configTabbedPane,
							"Please select separators file");
					return;
				}
				whitespaceAlwaysSep = destUnivWsSepChk.isSelected();
			} else {
				JOptionPane.showMessageDialog(configTabbedPane, msgSelSepRepr);
				return;
			}

			if (destUnivSingleElemRadio.isSelected()) {
				eventRepr = EventRepresentation.SINGLE_ELEMENT;
				extractedElems = null; // dummy
				orderMatters = false; // dummy
				multipleOccurrences = false; // dummy
			} else if (destUnivLotteryRadio.isSelected()) {
				eventRepr = EventRepresentation.LOTTERY;
				extractedElems = getBigInteger(destUnivSelectedElemsTxtF);
				orderMatters = destUnivOrderMattersChk.isSelected();
				multipleOccurrences = destUnivMultipleOccurrChk.isSelected();
			} else {
				JOptionPane
						.showMessageDialog(configTabbedPane, msgSelEventRepr);
				return;
			}
		} catch (NumberFormatException e) {
			showMsgInvalidNr(e.getMessage());
			return;
		}

		// check that no Repeat workers are running
		if (crtRepeatsCheckWorker != null || crtSearchMinRepWorker != null
				|| crtSearchTargetErrWorker != null) {
			JOptionPane.showMessageDialog(configTabbedPane,
					msgWaitForRepWorkersToFinish);
			return;
		}

		// get SwingWorker
		BackgroundTask<EventRepresConverter, Void> bgTask = new BackgroundTask<EventRepresConverter, Void>() {
			@Override
			public EventRepresConverter compute(
					Worker<EventRepresConverter, Void> worker) throws Exception {
				return EventRepresConverter.getInstance(
						new ElementRepresConverter(elemRepr, nr,
								customStringsFileName, caseSensitive,
								separatorsFileName, whitespaceAlwaysSep),
						eventRepr, extractedElems, orderMatters,
						multipleOccurrences);
			}
		};
		ResultHandler<EventRepresConverter, Void> resultHandler = new ResultHandler<EventRepresConverter, Void>() {
			@Override
			public void exception(Worker<EventRepresConverter, Void> worker,
					ExecutionException e) {
				configDestWrkException(worker, e);
			}

			@Override
			public void result(Worker<EventRepresConverter, Void> worker,
					EventRepresConverter result) {
				configDestWrkDone(worker, result);
			}
		};

		crtDestUnivWorker = new Worker<EventRepresConverter, Void>(bgTask,
				resultHandler, null);

		// update GUI elements
		displayExecRunning(destUnivStatusTxtF);
		destUnivSetBtn.setEnabled(false);
		destUnivCancelBtn.setEnabled(true);

		crtDestUnivWorker.execute();
	}

	private void performDestUnivCancelBtnFunction() {
		if (crtDestUnivWorker == null) {
			return;
		}
		// TODO get return value and log it
		// TODO decide what param (true or false) to supply
		crtDestUnivWorker.cancel(false);
		crtDestUnivWorker = null;
		// update GUI elements
		displayExecCancelled(destUnivStatusTxtF);
		destUnivCancelBtn.setEnabled(false);
		destUnivSetBtn.setEnabled(true);
	}

	private void performSrcUnivElemFileBrowseBtnFunction() {
		int returnVal = fileChooser.showOpenDialog(configTabbedPane);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			srcUnivStringsFileTxtF.setText(file.toString());
		}
	}

	private void performDestUnivElemFileBrowseBtnFunction() {
		int returnVal = fileChooser.showOpenDialog(configTabbedPane);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			destUnivStringsFileTxtF.setText(file.toString());
		}
	}

	private void performSrcUnivSepFileBrowseBtnFunction() {
		int returnVal = fileChooser.showOpenDialog(configTabbedPane);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			srcUnivSepFileTxtF.setText(file.toString());
		}
	}

	private void performDestUnivSepFileBrowseBtnFunction() {
		int returnVal = fileChooser.showOpenDialog(configTabbedPane);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			destUnivSepFileTxtF.setText(file.toString());
		}
	}

	private void performRepSetBtnFunction() {
		// make sure both src and dest workers are not running
		if (crtSrcUnivWorker != null || crtDestUnivWorker != null) {
			JOptionPane.showMessageDialog(configTabbedPane,
					msgWaitForUnivWorkersToFinish);
			return;
		}

		if (configWindowSrcUniv == null || configWindowDestUniv == null) {
			JOptionPane.showMessageDialog(configTabbedPane,
					msgSrcAndDestUnivMustBeSet);
			return;
		}

		// get data from config window
		final int repNr;
		try {
			repNr = Integer.parseInt(repRepeatsTxtF.getText().trim());
		} catch (NumberFormatException e) {
			showMsgInvalidNr(e.getMessage());
			return;
		}

		// get worker
		BackgroundTask<EventMapper, Void> bgTask = new BackgroundTask<EventMapper, Void>() {
			@Override
			public EventMapper compute(Worker<EventMapper, Void> worker)
					throws Exception {
				return new EventMapper(configWindowSrcUniv,
						configWindowDestUniv, repNr);
			}
		};
		ResultHandler<EventMapper, Void> resultHandler = new ResultHandler<EventMapper, Void>() {
			@Override
			public void exception(Worker<EventMapper, Void> worker,
					ExecutionException e) {
				configRepeatsCheckWrkException(worker, e);
			}

			@Override
			public void result(Worker<EventMapper, Void> worker,
					EventMapper result) {
				configRepeatsCheckWrkDone(worker, result);
			}
		};
		crtRepeatsCheckWorker = new Worker<EventMapper, Void>(bgTask,
				resultHandler, null);

		// update GUI elements
		displayExecRunning(repStatusTxtF);
		repSetBtn.setEnabled(false);
		repCancelBtn.setEnabled(true);

		crtRepeatsCheckWorker.execute();
	}

	private void performRepCancelBtnFunction() {
		if (crtRepeatsCheckWorker == null)
			return;

		// TODO: get value and log it
		// TODO: decide what param (true or false) to pass
		crtRepeatsCheckWorker.cancel(false);
		crtRepeatsCheckWorker = null;
		// update GUI elements
		displayExecCancelled(repStatusTxtF);
		repCancelBtn.setEnabled(false);
		repSetBtn.setEnabled(true);
	}

	private void performRepSearchMinRepBtnFunction() {
		// make sure both src and dest workers are not running
		if (crtSrcUnivWorker != null || crtDestUnivWorker != null) {
			JOptionPane.showMessageDialog(configTabbedPane,
					msgWaitForUnivWorkersToFinish);
			return;
		}

		if (configWindowSrcUniv == null || configWindowDestUniv == null) {
			JOptionPane.showMessageDialog(configTabbedPane,
					msgSrcAndDestUnivMustBeSet);
			return;
		}

		BackgroundTask<RepeatsError, Void> bgTask = new BackgroundTask<RepeatsError, Void>() {
			@Override
			public RepeatsError compute(Worker<RepeatsError, Void> worker)
					throws Exception {
				// this is just a precaution: I'm avoiding the situation where
				// the worker thread accesses member fields (the fields might
				// point to different objects for different invocations).
				final EventRepresConverter srcUniv = configWindowSrcUniv, destUniv = configWindowDestUniv;
				return EventMapper.getMinRepeats(srcUniv, destUniv, worker);
			}
		};
		crtSearchMinRepWorker = new Worker<RepeatsError, Void>(bgTask,
				searchMinRepResultHandler, null);

		// update GUI elements
		displayExecRunning(repMinRepStatusTxtF);
		repSearchMinRepBtn.setEnabled(false);
		repCancelMinRepBtn.setEnabled(true);

		crtSearchMinRepWorker.execute();
	}

	private void performRepCancelMinRepBtnFunction() {
		if (crtSearchMinRepWorker == null)
			return;

		// TODO: get value and log it
		// TODO: decide what param (true or false) to pass
		displayExecCancelled(repMinRepStatusTxtF);
		crtSearchMinRepWorker.cancel(false);
		crtSearchMinRepWorker = null;

		// update GUI elements
		repCancelMinRepBtn.setEnabled(false);
		repSearchMinRepBtn.setEnabled(true);
	}

	private void performRepSearchTargetErrBtnFunction() {
	}

	private void performRepCancelTargetErrBtnFunction() {
		if (crtSearchTargetErrWorker == null) {
			return;
		}

		// TODO: get value and log it
		// TODO: decide what param (true or false) to pass
		crtSearchTargetErrWorker.cancel(false);
		crtSearchTargetErrWorker = null;

		// update GUI elements
		repCancelTargetErrBtn.setEnabled(false);
		repSearchTargetErrBtn.setEnabled(true);
	}

	private void cancelAllConfigDialogWorkers() {
		performSrcUnivCancelBtnFunction();
		performDestUnivCancelBtnFunction();
		performRepCancelBtnFunction();
		performRepCancelMinRepBtnFunction();
		performRepCancelTargetErrBtnFunction();
	}

	private void performComputeMappingBtnFunction() {
		// check that the mapper has been set
		if (mainMapper == null) {
			JOptionPane.showMessageDialog(mainFrame, msgMapperMustBeSet);
			return;
		}

		// check the selected data input source
		final InputSourceType inpSrcType;
		if (dataSrcTaRadio.isSelected())
			inpSrcType = InputSourceType.USER_READER;
		else if (dataSrcRandomOrgRadio.isSelected())
			inpSrcType = InputSourceType.RANDOM_ORG;
		else if (dataSrcSysRandRadio.isSelected())
			inpSrcType = InputSourceType.SYS_RAND_GEN;
		else {
			JOptionPane.showMessageDialog(mainFrame, msgSelectInputSrc);
			return;
		}

		// get worker
		crtMappingInputReader = new StringReader(inputTA.getText());

		BackgroundTask<Mapping, String> bgTask = new BackgroundTask<Mapping, String>() {
			@Override
			public Mapping compute(Worker<Mapping, String> worker)
					throws Exception {
				final Reader r = crtMappingInputReader;
				return mainMapper.map(inpSrcType, r, worker);
			}
		};

		ResultHandler<Mapping, String> resultHandler = new ResultHandler<Mapping, String>() {
			@Override
			public void exception(Worker<Mapping, String> worker,
					ExecutionException e) {
				mappingWrkException(worker, e);
			}

			@Override
			public void result(Worker<Mapping, String> worker, Mapping result) {
				mappingWrkDone(worker, result, inpSrcType);
			}
		};

		InterimHandler<Mapping, String> interimHandler = new InterimHandler<Mapping, String>() {
			@Override
			public void process(Worker<Mapping, String> worker,
					List<String> chunks) {
				int len = chunks.size();
				if (len > 0)
					processDirectMappingWrkStatus(worker, chunks.get(len - 1));
			}
		};

		crtMappingWorker = new Worker<Mapping, String>(bgTask, resultHandler,
				interimHandler);

		// update GUI elements
		displayExecRunning(mappingStatusTxtF);
		computeMappingBtn.setEnabled(false);
		computeReverseMappingBtn.setEnabled(false);
		cancelMappingBtn.setEnabled(true);

		crtMappingWorker.execute();
	}

	private void performCancelMappingBtnFunction() {
		if (crtMappingWorker == null) {
			return;
		}

		// TODO: get value and log it
		// TODO: decide what param (true or false) to pass
		crtMappingWorker.cancel(false);
		crtMappingWorker = null;
		crtMappingInputReader = null;

		// update GUI elements
		displayExecCancelled(mappingStatusTxtF);
		cancelMappingBtn.setEnabled(false);
		computeMappingBtn.setEnabled(true);
		computeReverseMappingBtn.setEnabled(true);
	}

	private void performComputeReverseMappingBtnFunction() {
		// check that the mapper has been set
		if (mainMapper == null) {
			JOptionPane.showMessageDialog(mainFrame, msgMapperMustBeSet);
			return;
		}

		final int solCount;
		// get desired result count
		try {
			solCount = Integer.parseInt(reverseMappingSolTxtF.getText().trim());
		} catch (NumberFormatException e) {
			showMsgInvalidNr(e.getMessage());
			return;
		}

		// check the selected data input source
		final InputSourceType inpSrcType;
		if (dataSrcTaRadio.isSelected())
			inpSrcType = InputSourceType.USER_READER;
		else if (dataSrcRandomOrgRadio.isSelected())
			inpSrcType = InputSourceType.RANDOM_ORG;
		else if (dataSrcSysRandRadio.isSelected())
			inpSrcType = InputSourceType.SYS_RAND_GEN;
		else {
			JOptionPane.showMessageDialog(mainFrame, msgSelectInputSrc);
			return;
		}

		// get worker
		crtMappingInputReader = new StringReader(inputTA.getText());

		BackgroundTask<ReverseMapping, String> bgTask = new BackgroundTask<ReverseMapping, String>() {
			@Override
			public ReverseMapping compute(Worker<ReverseMapping, String> worker)
					throws Exception {
				final Reader r = crtMappingInputReader;
				return mainMapper.reverseMap(inpSrcType, r, solCount, worker);
			}
		};

		ResultHandler<ReverseMapping, String> resultHandler = new ResultHandler<ReverseMapping, String>() {
			@Override
			public void exception(Worker<ReverseMapping, String> worker,
					ExecutionException e) {
				reverseMappingWrkException(worker, e);
			}

			@Override
			public void result(Worker<ReverseMapping, String> worker,
					ReverseMapping result) {
				reverseMappingWrkDone(worker, result, inpSrcType);
			}
		};

		InterimHandler<ReverseMapping, String> interimHandler = new InterimHandler<ReverseMapping, String>() {
			@Override
			public void process(Worker<ReverseMapping, String> worker,
					List<String> chunks) {
				int len = chunks.size();
				if (len > 0)
					processReverseMappingWrkStatus(worker, chunks.get(len - 1));
			}
		};

		crtReverseMappingWorker = new Worker<ReverseMapping, String>(bgTask,
				resultHandler, interimHandler);

		// update GUI elements
		displayExecRunning(mappingStatusTxtF);
		computeReverseMappingBtn.setEnabled(false);
		computeMappingBtn.setEnabled(false);
		cancelReverseMappingBtn.setEnabled(true);

		crtReverseMappingWorker.execute();
	}

	private void performCancelReverseMappingBtnFunction() {
		if (crtReverseMappingWorker == null)
			return;

		crtReverseMappingWorker.cancel(false);
		crtReverseMappingWorker = null;
		crtMappingInputReader = null;

		// update GUI elements
		displayExecCancelled(mappingStatusTxtF);
		cancelReverseMappingBtn.setEnabled(false);
		computeReverseMappingBtn.setEnabled(true);
		computeMappingBtn.setEnabled(true);
	}

	private void writePropertiesToFile() throws IOException {
		try (Writer osw = new OutputStreamWriter(
				new FileOutputStream(propsFile), charset)) {
			props.store(osw, "DiceLottery properties file");
		}
	}

	private void performapplyPrefsBtnFunction() {
		try {
			int selIndex = themeCmbBox.getSelectedIndex();
			if (selIndex == -1)
				return;

			UIManager.setLookAndFeel(getAvailableLookAndFeels()[selIndex]
					.getClassName());
			SwingUtilities.updateComponentTreeUI(mainFrame);
			mainFrame.pack();
			SwingUtilities.updateComponentTreeUI(configDialog);
			configDialog.pack();
			SwingUtilities.updateComponentTreeUI(fileChooser);
			setLafRelatedProperties();

			props.setProperty(lafKey, getLookAndFeelNames()[selIndex]);
			writePropertiesToFile();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	/**
	 * Creates and shows the GUI. This method must be invoked from the event
	 * dispatching thread.
	 */
	private static void createAndShowGUI() {
		// try to load Properties from user's home dir
		if (propsFile.exists()) {
			try (Reader r = new InputStreamReader(
					new FileInputStream(propsFile), charset)) {
				props.load(r);
			} catch (IOException e) {
				System.err.println(e);
			}
		}

		String storedClassName = getLafClassName(props
				.getProperty(lafKey, null));
		startupLafClassName = applyStartupLookAndFeel(storedClassName);
		DiceLottery dl = new DiceLottery();
		dl.initialize();
		dl.mainFrame.pack();
		dl.mainFrame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
