package checkersGUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import checkersMain.*;
import checkersMain.CheckersBoard.Ply;
import checkersPlayer.Human;
import checkersSound.CheckerSound;
import utilsGUI.Config;
import utilsGUI.Constants;
import utilsGUI.DefinitionJLabelDTO;

/**
 * A simple graphical user interface to allow humans and AI CheckerPlayers to
 * play each other. It will display the {@link CheckersBoard} in 2D and players'
 * names. It also keeps track of the number of pieces each player has and all
 * the plies that have been taken. It allows the user to set the players using
 * the {@link PlayerSetupDialog} and change settings in the
 * {@link CheckersGameManager} using the {@link GameOptionsDialog}.
 * <p>
 * <b>Note:</b> The javadoc, comments, and code names use the proper game theory
 * terms as defined in {@link CheckersGameManager}'s javadoc. However, the GUI
 * will display "Turn" instead of "Ply", i.e. "Turn time left" instead of
 * "Ply time left".
 *
 * @author Amos Yuen
 * @version {@value #VERISON} - 6 November 2009
 */

@SuppressWarnings("serial")
public class CheckersGUI extends JFrame implements MouseListener,
		MouseMotionListener, KeyListener, ComponentListener, WindowListener,
		ListSelectionListener, CheckersGameListener, ContainerListener {
	/**
	 * A class that handles drawing and interaction with the CheckersBoard
	 *
	 * @author Amos Yuen
	 * @version 1.20 - 25 July 2008
	 */

	public CheckerSound checkerSound = new CheckerSound("./config/sound_background.wav");
	public JCheckBoxMenuItem useBackgroundSoundMenuItem = new JCheckBoxMenuItem();

	private class CheckersPanel extends JPanel implements ComponentListener {
		private Font pausedFont, subPausedFont, kingFont;
		private int pausedTextX, pausedTextY, subPausedTextX, subPausedTextY;
		private Dimension preferredSize;
		private int tileSize, offsetX, offsetY;

		private Color selectedColor = TILE2_COLOR;
		private Color colorSaved = TILE2_COLOR;

		public CheckersPanel() {
			setBackground(NEUTRAL_BG_COLOR);
			addComponentListener(this);
			tileSize = 60;
			int size = tileSize * 8;

			preferredSize = new Dimension(size, size);

			Config config = new Config();

			int codeColor = config.getConfig("COLOR_THEME", -16743896);
			this.colorSaved = Color.decode(String.valueOf(codeColor));
			this.selectedColor = Color.decode(String.valueOf(codeColor));
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			update();
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		/**
		 * Draws a Checkers piece given its parameters.
		 *
		 * @param g      the Graphics
		 * @param color1 Player1's color
		 * @param color2 Player2's color
		 * @param x      the x-coordinate where the left of the Checkers piece
		 *               should be drawn
		 * @param y      the y-coordinate where the top of the Checkers piece
		 *               should be drawn
		 * @param size   the width and height the Checkers piece should be drawn in
		 *               pixels
		 * @param piece  the Checkers piece type (King or Checker)
		 * @return
		 */
		public boolean drawCheckersPiece(Graphics g, Color color1,
				Color color2, int x, int y, int size, byte piece) {
			if (piece == CheckersBoard.EMPTY || piece == CheckersBoard.OFFBOARD)
				return false;

			boolean player1 = (piece == CheckersBoard.PLAYER1_CHECKER || piece == CheckersBoard.PLAYER1_KING);
			// Draw Outline in Opposite color
			if (!player1)
				g.setColor(color1);
			else
				g.setColor(color2);
			g.fillOval(x, y, size, size);

			// Draw the actual checker in normal color
			if (player1)
				g.setColor(color1);
			else
				g.setColor(color2);
			g.fillOval(x + checkersBorderSize, y + checkersBorderSize, size
					- checkersBorderSize * 2, size - checkersBorderSize * 2);

			// Draw 'Q' in opposite color if checker is a king
			if (piece == CheckersBoard.PLAYER2_KING
					|| piece == CheckersBoard.PLAYER1_KING) {
				if (!player1)
					g.setColor(color1);
				else
					g.setColor(color2);
				g.setFont(kingFont);
				int fontSize = kingFont.getSize() * 2 / 3;
				g.drawString("Q", x + (size - fontSize) / 2, y
						+ (size + fontSize) / 2);
			}

			return true;
		}

		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}

		@Override
		public synchronized void paint(Graphics graphic) {
			super.paint(graphic);

			if (selectedState == null)
				return;

			drawBoard(graphic);
			drawLastMove(graphic);
			drawCurrentMovePiece(graphic);
			drawGamePausedScreen(graphic);
		}

		private boolean setColorMain(Graphics graphic, int x, int y, boolean heldPiece) {
			boolean colorSet = false;

			int index = CheckersBoard.getIndex(y, x);
			if (currMove == null) {
				colorSet = setColorCurrMove(graphic, colorSet, index);
			} else if (currMove.plies.get(0).get(0) == index) {
				colorSet = setColorIndex(graphic, colorSet);
				heldPiece = true;
			} else if (showMoves && !showingOldPly) {
				colorSet = setColorNewPly(graphic, colorSet, index);
			}

			if (!colorSet) {
				graphic.setColor(
						selectedColor != null ? selectedColor : TILE3_COLOR);
			}
			return heldPiece;
		}

		private boolean setColorCurrMove(Graphics graphic, boolean colorSet, int index) {
			if (showMoves && !showingOldPly) {
				for (PossiblePly ply : sortedPlies) {
					if (ply.plies.get(0).get(0) == index) {
						graphic.setColor(MOVE_ENDS_COLOR);
						colorSet = true;
						break;
					}
				}
			}
			return colorSet;
		}

		private boolean setColorNewPly(Graphics graphic, boolean colorSet, int index) {
			for (Ply ply : currMove.plies) {
				if (ply.get(ply.size() - 1) == index) {
					graphic.setColor(MOVE_ENDS_COLOR);
					colorSet = true;
				} else {
					for (int i = 1; i < ply.size() - 1; i++) {
						if (ply.get(i) == index) {
							graphic.setColor(JUMP_INTERMEDIATE_COLOR);
							colorSet = true;
							break;
						}
					}
				}
			}
			return colorSet;
		}

		private boolean setColorIndex(Graphics graphic, boolean colorSet) {
			if (showMoves && !showingOldPly) {
				graphic.setColor(MOVE_ENDS_COLOR);
				colorSet = true;
			}
			return colorSet;
		}

		private void drawBoard(Graphics graphic) {
			for (int x = 0; x < 8; x++) {
				int sqX = offsetX + x * tileSize;
				for (int y = 0; y < 8; y++) {
					int sqY = offsetY + y * tileSize;
					boolean heldPiece = false;

					if ((x + y) % 2 == 0)
						graphic.setColor(TILE1_COLOR);
					else {
						heldPiece = setColorMain(graphic, x, y, heldPiece);
					}

					graphic.fillRect(sqX, sqY, tileSize, tileSize);

					if (!heldPiece) {
						drawCheckersPiece(graphic, PLAYER1_COLOR, PLAYER2_COLOR, sqX
								+ checkersFillOffset, sqY + checkersFillOffset,
								tileSize - checkersFillOffset * 2,
								selectedState.board.getPiece(y, x));
					}
				}
			}
		}

		private void drawLastMove(Graphics graphic) {
			if (selectedState.lastMove != null) {
				for (int i = 0; i < selectedState.lastMove.size(); i++) {
					if (i == 0 || i == selectedState.lastMove.size() - 1)
						graphic.setColor(OLD_MOVE_ENDS_COLOR);
					else
						graphic.setColor(OLD_JUMP_INTERMEDIATE_COLOR);

					int index = selectedState.lastMove.get(i);
					int x = index % 4 * 2;
					int y = index / 4;
					if (y % 2 == 0)
						x++;
					x = offsetX + x * tileSize;
					y = offsetY + y * tileSize;

					for (int j = 0; j < tileBorderSize; j++)
						graphic.drawRect(x + j, y + j, tileSize - 2 * j, tileSize - 2 * j);
				}
			}
		}

		private void drawCurrentMovePiece(Graphics graphic) {
			if (currMove != null) {
				int index = currMove.plies.get(0).get(0);
				int size = tileSize - checkersFillOffset * 2;

				drawCheckersPiece(graphic, PLAYER1_ALPHA_COLOR, PLAYER2_ALPHA_COLOR,
						offsetX + oldMouseX - size / 2, offsetY + oldMouseY - size / 2,
						size, selectedState.board.getPiece(index));
			}
		}

		private void drawGamePausedScreen(Graphics graphic) {
			if (gameManager.isPaused()) {
				setScreenToGamePaused(graphic);
			}
		}

		public void setScreenToGamePaused(Graphics graphic) {
			graphic.setColor(NEUTRAL_FG_COLOR);
			graphic.setFont(pausedFont);
			graphic.drawString(PAUSED_TEXT, pausedTextX, pausedTextY);

			graphic.setFont(subPausedFont);
			graphic.drawString(SUB_PAUSED_TEXT, subPausedTextX, subPausedTextY);
		}

		public void setTileSize(int tileSize) {
			this.tileSize = tileSize;
			update();
		}

		/**
		 * Updates the size the text size and location based on the components
		 * size.
		 */
		public void update() {
			int width = getWidth(), height = getHeight();
			offsetX = Math.max(0, (width - tileSize * 8) / 2);
			offsetY = Math.max(0, (height - tileSize * 8) / 2);
			int size = tileSize * 8;
			tileBorderSize = Math.max(1, tileSize / 20);
			checkersFillOffset = tileBorderSize;
			checkersBorderSize = tileBorderSize;
			kingFont = new Font(Constants.FONT_ARIAL, Font.BOLD, tileSize * 3 / 4);

			pausedFont = new Font(Constants.FONT_ARIAL, Font.BOLD, size / 5);
			subPausedFont = new Font(Constants.FONT_ARIAL, Font.BOLD, size / 20);

			Graphics g = getGraphics();
			g.setFont(pausedFont);
			int strWidth = g.getFontMetrics().stringWidth(PAUSED_TEXT);
			int strHeight = g.getFontMetrics().getHeight();
			pausedTextX = (width - strWidth) / 2;
			pausedTextY = (height + strHeight / 4) / 2;

			g.setFont(subPausedFont);
			strWidth = g.getFontMetrics().stringWidth(SUB_PAUSED_TEXT);
			subPausedTextX = (width - strWidth) / 2;
			subPausedTextY = (height + strHeight * 2 / 3) / 2;

			size = tileSize * 8;
			preferredSize.setSize(size, size);

			board.updateUI();
		}
	}

	/**
	 * A data class to hold the data for the state of the board, players, and
	 * game for a certain ply.
	 *
	 * @author Amos Yuen
	 * @version 1.00 - 28 July 2008
	 */
	private class GameState {
		public CheckersBoard board;
		public Ply lastMove;
		public int move;
		public CheckersPlayerInterface player1, player2;

		public GameState() {
			this(gameManager.getDisplayBoard(), gameManager.getLastPly(),
					gameManager.getMoveCount(), gameManager.getPlayer1(),
					gameManager.getPlayer2());
		}

		public GameState(CheckersBoard board, Ply lastMove, int move,
				CheckersPlayerInterface player1, CheckersPlayerInterface player2) {
			super();
			this.board = board;
			this.lastMove = lastMove;
			this.move = move;
			this.player1 = player1;
			this.player2 = player2;
		}

		@Override
		public String toString() {
			StringBuffer strBuff = new StringBuffer("Move ");
			strBuff.append(move);
			strBuff.append(" - ");
			if (stateModel.indexOf(this) % 2 == 0)
				strBuff.append(player1.getName());
			else
				strBuff.append(player2.getName());

			return strBuff.toString();
		}
	}

	/**
	 * A wrapper class to sort the listof possible moves by the index of the
	 * piece that is moved.
	 *
	 * @author Amos Yuen
	 * @version 1.00 - 8 July 2008
	 */
	private class PossiblePly {
		private final List<Ply> plies;

		public PossiblePly(List<Ply> plies) {
			super();
			this.plies = plies;
		}
	}

	/**
	 * A simple formatted dialog that holds a JTextArea component. Used to
	 * display text for information purposes.
	 *
	 * @author Amos Yuen
	 * @version 1.1 - 17 August 2008
	 */
	private class TextDialog extends JDialog {
		private Action quit;
		private JTextArea textArea;

		public TextDialog(JFrame parent, String name, String text,
				boolean wordWrap) {
			super(parent, name, true);
			textArea = new JTextArea(text);
			textArea.setForeground(NEUTRAL_FG_COLOR);
			textArea.setBackground(NEUTRAL_BG_COLOR);
			textArea.setSelectedTextColor(NEUTRAL_BG_COLOR);
			textArea.setSelectionColor(NEUTRAL_FG_COLOR);
			textArea.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			textArea.setEditable(false);
			if (wordWrap) {
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
			}

			add(new JScrollPane(textArea));

			quit = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			};
			quit.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_ESCAPE, 0));
			textArea.getKeymap().addActionForKeyStroke(
					(KeyStroke) quit.getValue(Action.ACCELERATOR_KEY), quit);

			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setFocusable(false);
			setMinimumSize(new Dimension(200, 200));
			setSize(400, 400);
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}

	public static final int ALPHA = 150;
	public static final CompoundBorder BORDER = BorderFactory
			.createCompoundBorder(BorderFactory.createBevelBorder(
					BevelBorder.RAISED, Color.LIGHT_GRAY, Color.DARK_GRAY),
					BorderFactory.createBevelBorder(BevelBorder.LOWERED,
							Color.LIGHT_GRAY, Color.DARK_GRAY));

	public static final String CHANGE_LOG_FILE_PATH = "EBFCheckers_Change_Log.txt";
	public static final String CHANGE_LOG_TEXT;

	public static final String HELP_FILE_PATH = "EBFCheckers_Help.txt";
	public static final String HELP_TEXT;

	public static final String LICENSE_FILE_PATH = "GNUGPL.txt";
	public static final String LICENSE_TEXT;

	public static final String ABOUT_FILE_PATH = "EBFCheckers_About.txt";
	public static final String ABOUT_TEXT;

	public static final Color HIGHLIGHT_COLOR = new Color(0, 0, 0),
			TILE1_COLOR = new Color(240, 220, 130),
			TILE2_COLOR = new Color(0, 130, 40),

			TILE3_COLOR = new Color(21, 86, 182),
			NEUTRAL_FG_COLOR = new Color(150, 150, 150),
			NEUTRAL_BG_COLOR = Color.BLACK, MOVE_ENDS_COLOR = Color.GREEN,
			JUMP_INTERMEDIATE_COLOR = Color.CYAN,
			OLD_MOVE_ENDS_COLOR = Color.BLUE,
			OLD_JUMP_INTERMEDIATE_COLOR = Color.MAGENTA,

			PLAYER1_COLOR = Color.BLACK,
			PLAYER2_COLOR = Color.WHITE,

			PLAYER1_ALPHA_COLOR = new Color(PLAYER1_COLOR.getRed(),
					PLAYER1_COLOR.getGreen(), PLAYER1_COLOR.getBlue(), ALPHA),
			PLAYER2_ALPHA_COLOR = new Color(PLAYER2_COLOR.getRed(),
					PLAYER2_COLOR.getGreen(), PLAYER2_COLOR.getBlue(), ALPHA);

	private static final Border LIST_SELECTION_BORDER = BorderFactory
			.createLineBorder(NEUTRAL_FG_COLOR, 2);
	public static final String PAUSED_TEXT = "PAUSED";
	public static final long SLEEP_TIME = 250;
	public static final String VERISON = "1.27";

	static {
		HELP_TEXT = loadTextFile(new File(HELP_FILE_PATH));
		CHANGE_LOG_TEXT = loadTextFile(new File(CHANGE_LOG_FILE_PATH));
		LICENSE_TEXT = loadTextFile(new File(LICENSE_FILE_PATH));
		ABOUT_TEXT = loadTextFile(new File(ABOUT_FILE_PATH));
	}

	/**
	 * @param time in milliseconds
	 * @return a string representing the time in mm:ss.ms
	 */
	public static String formatTime(long time) {
		StringBuffer strBuffer = new StringBuffer();
		long minutes, seconds, milliseconds = time;

		seconds = milliseconds / 1000;
		minutes = seconds / 60;
		milliseconds %= 1000;
		seconds %= 60;

		if (minutes < 10)
			strBuffer.append('0');
		strBuffer.append(String.valueOf(minutes));

		strBuffer.append(':');
		if (seconds < 10)
			strBuffer.append('0');
		strBuffer.append(String.valueOf(seconds));

		strBuffer.append('.');
		if (milliseconds < 10)
			strBuffer.append("00");
		else if (milliseconds < 100)
			strBuffer.append('0');
		strBuffer.append(String.valueOf(milliseconds));

		return strBuffer.toString();
	}

	/**
	 * Creates and formats a string to represent a KeyStroke.
	 *
	 * @param stroke the KeyStroke
	 * @return the text displayed
	 */
	public static String getKeyStrokeText(KeyStroke stroke) {
		StringBuffer strBuff = new StringBuffer(KeyEvent
				.getKeyModifiersText(stroke.getModifiers()));
		if (strBuff.length() > 0)
			strBuff.append('+');
		strBuff.append(KeyEvent.getKeyText(stroke.getKeyCode()));

		return strBuff.toString();
	}

	/**
	 * Loads the Help File as a String.
	 *
	 * @return a String of the loaded Help file
	 */
	public static String loadTextFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

			StringBuffer strBuff = new StringBuffer(br.readLine());
			String line = br.readLine();
			while (line != null) {
				strBuff.append('\n');
				strBuff.append(line);
				line = br.readLine();
			}

			br.close();
			isr.close();
			fis.close();
			return strBuff.toString();
		} catch (Exception e) {
			System.out.println("Error Loading File: " + e.getMessage());
		}
		return null;
	}

	public static void main(String[] args) {
		new CheckersGUI();
	}

	private CheckersPanel board;

	private JComponent boardContainer;

	private PossiblePly currMove;
	private CheckersGameManager gameManager;

	private Action newGame, pause, quit, playerSetup, gameOptions, changePlayersNickName,
			createTrainer, help, changeLog, license, about, changeTheme, useBackgroundSound;
	private JLabel player1Label;
	private JLabel player2Label;

	private JLabel plyTime;
	private JLabel gameTime;
	private JLabel moveCount;
	private GameState selectedState;

	private boolean showingOldPly, autoSwitch, showMoves, switchPlayers;
	private LinkedList<PossiblePly> sortedPlies;
	private JSplitPane splitPane;
	private JList stateList;
	private DefaultListModel stateModel;
	private JScrollPane stateScrollPane, boardScrollPane;
	public final String SUB_PAUSED_TEXT;
	private int tileBorderSize, checkersFillOffset, checkersBorderSize,
			oldMouseX, oldMouseY, oldMouseTileX, oldMouseTileY;
	private CheckersTrainer trainer;

	public CheckersGUI() {
		this(new CheckersGameManager());

		Config config = new Config();
		boolean palySound = config.getConfig("PLAY_SOUND", false);

		if (palySound) {
			useBackgroundSoundMenuItem.setSelected(true);
			checkerSound.play();
		} else {
			useBackgroundSoundMenuItem.setSelected(false);
		}
	}

	public JLabel defineJLabel(DefinitionJLabelDTO definition) {
		JLabel newLabel = new JLabel();

		newLabel.setOpaque(definition.isOpaque);
		newLabel.setHorizontalAlignment(definition.horizontalAlignment);
		newLabel.setToolTipText(definition.tooltipText);
		newLabel.setForeground(definition.foregroundColor);
		newLabel.setBackground(definition.backgroundColor);
		newLabel.setBorder(definition.border);

		return newLabel;
	}

	public CheckersGUI(CheckersGameManager gameManager) {
		this.gameManager = gameManager;
		gameManager.addCheckersGameListener(this);
		sortedPlies = new LinkedList<PossiblePly>();
		autoSwitch = showMoves = true;

		stateModel = new DefaultListModel();
		stateList = new JList(stateModel);
		stateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		stateList.addListSelectionListener(this);
		stateList.addContainerListener(this);
		stateList.setToolTipText("A list of every turn that has"
				+ " been taken and the player that took it");
		stateList.setForeground(CheckersGUI.NEUTRAL_BG_COLOR);
		stateList.setBackground(CheckersGUI.NEUTRAL_BG_COLOR);

		stateScrollPane = new JScrollPane(stateList);
		stateScrollPane.setOpaque(false);
		stateScrollPane.setBackground(CheckersGUI.NEUTRAL_BG_COLOR);

		TitledBorder border = BorderFactory.createTitledBorder(BORDER, "Turns");
		border.setTitleJustification(TitledBorder.CENTER);
		border.setTitleColor(CheckersGUI.NEUTRAL_FG_COLOR);
		stateScrollPane.setBorder(border);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		boardContainer = new JComponent() {
		};
		boardContainer.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		border = BorderFactory.createTitledBorder(BORDER, "Player 2 - "
				+ gameManager.getPlayer2().getName());
		border.setTitleColor(PLAYER2_COLOR);
		border.setTitleJustification(TitledBorder.CENTER);
		player2Label = defineJLabel(new DefinitionJLabelDTO(
				false,
				SwingConstants.CENTER,
				"Player 2's Name and Checker & King Count",
				PLAYER2_COLOR,
				null,
				border));
		boardContainer.add(player2Label, c);

		c.weighty = 10;
		c.gridy++;
		board = new CheckersPanel();
		board.addMouseListener(this);
		board.addMouseMotionListener(this);
		board.selectedColor = board.colorSaved;
		boardScrollPane = new JScrollPane(board);
		boardScrollPane.setBorder(null);
		boardContainer.add(boardScrollPane, c);

		c.weighty = 0;

		c.gridy++;

		border = BorderFactory.createTitledBorder(BORDER, "Player 1 - "
				+ gameManager.getPlayer1().getName());
		border.setTitleColor(PLAYER1_COLOR);
		border.setTitlePosition(TitledBorder.BOTTOM);
		border.setTitleJustification(TitledBorder.CENTER);
		player1Label = defineJLabel(new DefinitionJLabelDTO(
				false,
				SwingConstants.CENTER,
				"Player 1's Name and Checker & King Count",
				PLAYER1_COLOR,
				null,
				border));
		boardContainer.add(player1Label, c);

		c.gridwidth = 3;
		c.weighty = 1;
		c.gridy = 0;
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				stateScrollPane, boardContainer);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(null);
		splitPane.setBackground(NEUTRAL_BG_COLOR);
		add(splitPane, c);

		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 0;

		c.gridy++;
		moveCount = defineJLabel(new DefinitionJLabelDTO(
				true,
				SwingConstants.CENTER,
				"The current move count",
				NEUTRAL_FG_COLOR,
				NEUTRAL_BG_COLOR,
				BORDER));
		add(moveCount, c);

		c.gridx++;

		plyTime = defineJLabel(new DefinitionJLabelDTO(
				true,
				SwingConstants.CENTER,
				"Shows the remaining time for the current player to take his turn.",
				PLAYER1_COLOR,
				NEUTRAL_BG_COLOR,
				BORDER));
		add(plyTime, c);

		c.gridx++;

		gameTime = defineJLabel(new DefinitionJLabelDTO(
				true,
				SwingConstants.CENTER,
				"The total time that has elapsed since the start of the game",
				NEUTRAL_FG_COLOR,
				NEUTRAL_BG_COLOR,
				BORDER));
		add(gameTime, c);

		initActions();
		initMenu();
		gameStarted(null);
		plyTimeChanged(null);
		sortedPlies.clear();

		SUB_PAUSED_TEXT = "Press '"
				+ getKeyStrokeText((KeyStroke) pause
						.getValue(Action.ACCELERATOR_KEY))
				+ "' to Unpause";

		String displayName = CheckersPlayerLoader.getPlayerDisplayName(0);
		for (int i = 1; i < CheckersPlayerLoader.getNumCheckersPlayers(); i++) {
			String name = CheckersPlayerLoader.getPlayerDisplayName(i);
			if (name.length() > displayName.length())
				displayName = name;
		}
		stateModel.addElement("Move 999 - " + displayName);

		board.addKeyListener(this);
		stateList.addKeyListener(this);

		addComponentListener(this);
		addWindowListener(this);
		setMinimumSize(new Dimension(400, 300));
		setSize(650, 600);
		setLocationRelativeTo(null);
		setTitle("EBFCheckers v" + VERISON);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		doLayout();
		stateList.setSelectedValue(null, false);
		setVisible(true);
		board.setTileSize(getSize().height / 12);

		new Thread() {
			@Override
			public void run() {
				try {
					while (!CheckersGUI.this.isActive())
						Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				splitPane.setDividerLocation(stateList.getWidth());
				stateModel.clear();
				stateList.setForeground(CheckersGUI.NEUTRAL_FG_COLOR);
				stateList.setCellRenderer(new DefaultListCellRenderer.UIResource() {

					@Override
					public Component getListCellRendererComponent(
							JList list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						JComponent c = (JComponent) super.getListCellRendererComponent(list,
								value, index, isSelected,
								cellHasFocus);

						if (!isSelected) {
							if (index % 2 == 0)
								c.setForeground(PLAYER1_COLOR);
							else
								c.setForeground(PLAYER2_COLOR);
						} else {
							c
									.setBorder(CheckersGUI.LIST_SELECTION_BORDER);
							if (index % 2 == 0)
								c.setBackground(PLAYER1_COLOR);
							else
								c.setBackground(PLAYER2_COLOR);
						}
						return c;
					}
				});
			}
		}.start();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (e.getSource() == this) {
			int size = Math.min(getWidth() / 10, getHeight()) / 4;
			Font font = new Font(Constants.FONT_ARIAL, Font.BOLD, size);
			moveCount.setFont(font);
			plyTime.setFont(font);
			gameTime.setFont(font);
			player1Label.setFont(font);
			player2Label.setFont(font);
			((TitledBorder) player1Label.getBorder()).setTitleFont(font);
			((TitledBorder) player2Label.getBorder()).setTitleFont(font);
			((TitledBorder) stateScrollPane.getBorder()).setTitleFont(font);

			size = Math.min(getWidth() / 10, getHeight()) / 5;
			stateList.setFont(new Font(Constants.FONT_ARIAL, Font.PLAIN, size));

			setVisible(true);
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void gameEnded(CheckersGameEvent ce) {
		if (gameManager.getGameOutcome() == CheckersGameManager.PLAYER1_WINS) {
			plyTime.setText(gameManager.getPlayer1().getName() + " WINS!");
			plyTime.setForeground(PLAYER1_COLOR);
		} else if (gameManager.getGameOutcome() == CheckersGameManager.PLAYER2_WINS) {
			plyTime.setText(gameManager.getPlayer2().getName() + " WINS!");
			plyTime.setForeground(PLAYER2_COLOR);
		} else if (gameManager.getGameOutcome() == CheckersGameManager.DRAW) {
			plyTime.setText("The Game is called a DRAW!");
			plyTime.setForeground(NEUTRAL_FG_COLOR);
		}
		sortedPlies.clear();
		switchPlayers = autoSwitch
				&& gameManager.getGameOutcome() != CheckersGameManager.INTERRUPTED;
		repaint();
	}

	@Override
	public void gameStarted(CheckersGameEvent ce) {
		selectedState = new GameState();
		stateModel.clear();
		stateList.getUI().installUI(stateList);

		update();
		showingOldPly = false;
		repaint();

		if (trainer == null)
			newGame.setEnabled(true);
	}

	private void initActions() {
		newGame = createNewGameAction();
		pause = createPauseAction();
		quit = createQuitAction();
		playerSetup = createPlayerSetupAction();
		changePlayersNickName = createChangePlayersNickNameAction();
		changeTheme = createChangeThemeAction();
		useBackgroundSound = createUseBackgroundSoundAction();
		gameOptions = createGameOptionsAction();
		createTrainer = createCreateTrainerAction();
		help = createHelpAction();
		changeLog = createChangeLogAction();
		license = createLicenseAction();
		about = createAboutAction();
	}

	private AbstractAction createNewGameAction() {
		return createAction("New Game", KeyEvent.VK_N, this::handleNewGame);
	}

	private AbstractAction createPauseAction() {
		return createAction("Pause", KeyEvent.VK_P, this::handlePause);
	}

	private AbstractAction createQuitAction() {
		return createAction("Quit", KeyEvent.VK_ESCAPE, this::handleQuit);
	}

	private AbstractAction createPlayerSetupAction() {
		return createAction("Player Setup", KeyEvent.VK_S, this::handlePlayerSetup);
	}

	private AbstractAction createChangePlayersNickNameAction() {
		return createAction("Change Players NickName", KeyEvent.VK_C, this::handleChangePlayersNickName);
	}

	private AbstractAction createChangeThemeAction() {
		return createAction("Change Theme", KeyEvent.VK_2, this::handleChangeTheme);
	}

	private AbstractAction createUseBackgroundSoundAction() {
		return createAction("Use Background Sound", KeyEvent.VK_3, this::handleUseBackgroundSound);
	}

	private AbstractAction createGameOptionsAction() {
		return createAction("Game Options", KeyEvent.VK_O, this::handleGameOptions);
	}

	private AbstractAction createCreateTrainerAction() {
		return createAction("CheckersTrainer", KeyEvent.VK_T, this::handleCreateTrainer);
	}

	private AbstractAction createHelpAction() {
		return createAction("Help", KeyEvent.VK_F1, this::handleHelp);
	}

	private AbstractAction createChangeLogAction() {
		return createAction("Change Log", KeyEvent.VK_F2, this::handleChangeLog);
	}

	private AbstractAction createLicenseAction() {
		return createAction("GNU General Public License", KeyEvent.VK_F11, this::handleLicense);
	}

	private AbstractAction createAboutAction() {
		return createAction("About", KeyEvent.VK_F12, this::handleAbout);
	}

	private AbstractAction createAction(String name, int acceleratorKey, ActionListener listener) {
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseHandler();
				listener.actionPerformed(e);
			}
		};

		action.putValue(Action.NAME, name);
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey, 0));

		return action;
	}

	private void pauseHandler() {
		boolean paused = gameManager.isPaused();
		if (!paused) gameManager.setPaused(true);
		else gameManager.setPaused(false);
		repaint();
	}

	private void handlePause(ActionEvent e) {
	}

	private void handleNewGame(ActionEvent e) {
		newGame.setEnabled(false);
		gameManager.stop();

		if (switchPlayers) {
			CheckersPlayerInterface player1 = gameManager.getPlayer1();
			gameManager.setPlayer1(gameManager.getPlayer2());
			gameManager.setPlayer2(player1);
		}

		gameManager.newGame();
		showingOldPly = false;
	}

	private void handleQuit(ActionEvent actionEvent) {
		gameManager.removeCheckersGameListener(CheckersGUI.this);
		if (trainer == null) {
			gameManager.stop();
			System.exit(0);
		} else if (gameManager.isPaused()) {
			gameManager.setPaused(false);
		}
	}

	private void handlePlayerSetup(ActionEvent actionEvent) {
		CheckersPlayerInterface player1 = gameManager.getPlayer1();
		CheckersPlayerInterface player2 = gameManager.getPlayer2();

		PlayerSetupDialog dialog = new PlayerSetupDialog(CheckersGUI.this, player1, player2);

		if (dialog.isAccepted()) {
			CheckersPlayerInterface newPlayer1 = dialog.getPlayer1();
			CheckersPlayerInterface newPlayer2 = dialog.getPlayer2();
			if (!player1.getClass().equals(newPlayer1.getClass())) {
				gameManager.setPlayer1(newPlayer1);
			}
			if (!player2.getClass().equals(newPlayer2.getClass())) {
				gameManager.setPlayer2(newPlayer2);
			}
			updatePlayerLabels();
			switchPlayers = false;
		}
		dialog.dispose();
	}

	private void handleChangePlayersNickName(ActionEvent actionEvent) {
		CheckersPlayerInterface player1 = gameManager.getPlayer1();
		CheckersPlayerInterface player2 = gameManager.getPlayer2();

		ChangePlayerNameDialog dialog = new ChangePlayerNameDialog(CheckersGUI.this, player1, player2);
		updatePlayerLabels();
		dialog.dispose();
	}

	private void handleChangeTheme(ActionEvent actionEvent) {
		ChangeTheme dialog = new ChangeTheme(CheckersGUI.this, board.selectedColor);
		if (dialog.isAccepted()) {
			board.selectedColor = dialog.getColorTheme();
			int codeColor = board.selectedColor.getRGB();
			Config config = new Config();
			config.setConfig("COLOR_THEME", codeColor);
			repaint();
		}

		dialog.dispose();
	}


	private void handleUseBackgroundSound(ActionEvent actionEvent) {
		boolean isMarked = useBackgroundSoundMenuItem.isSelected();

		// Setando as configurações:
		Config config = new Config();
		config.setConfig("PLAY_SOUND", !isMarked);

		if (isMarked) {
			useBackgroundSoundMenuItem.setSelected(false);
			checkerSound.stop();
		} else {
			useBackgroundSoundMenuItem.setSelected(true);
			checkerSound.play();
		}
	}

	private void handleGameOptions(ActionEvent actionEvent) {
		GameOptionsDialog dialog = new GameOptionsDialog(
				CheckersGUI.this,
				gameManager.getPlyTime() / 1000,
				gameManager.getMaxMoves(),
				gameManager.getWaitTime(),
				board.tileSize, autoSwitch, showMoves);

		if (dialog.isAccepted()) {
			gameManager.setPlyTime(dialog.getTurnTime() * 1000);
			gameManager.setMaxMoves(dialog.getMaxMoves());
			gameManager.setWaitTime(dialog.getWaitTime());
			board.setTileSize(dialog.getTileSize());
			autoSwitch = dialog.useAutoSwitch();
			showMoves = dialog.showMoves();
		}

		dialog.dispose();
	}

	private void handleCreateTrainer(ActionEvent actionEvent) {
		setTrainer(new CheckersTrainer(gameManager));
		trainer.setGUI(CheckersGUI.this);
	}

	private void handleHelp(ActionEvent actionEvent) {
		new TextDialog(CheckersGUI.this, getTitle() + " Help", HELP_TEXT, true);
	}

	private void handleChangeLog(ActionEvent actionEvent) {
		new TextDialog(CheckersGUI.this, getTitle() + " Change Log", CHANGE_LOG_TEXT, true);
	}

	private void handleLicense(ActionEvent actionEvent) {
		new TextDialog(CheckersGUI.this, "GNU General Public License", LICENSE_TEXT, false);
	}

	private void handleAbout(ActionEvent actionEvent) {
		new TextDialog(CheckersGUI.this, getTitle() + " About", ABOUT_TEXT, true);
	}

	private void initMenu() {
		JMenu file = new JMenu("File");
		file.add(new JMenuItem(newGame));
		file.add(new JMenuItem(pause));
		file.add(new JMenuItem(quit));

		JMenu settings = new JMenu("Settings");
		settings.add(new JMenuItem(playerSetup));
		settings.add(new JMenuItem(gameOptions));
		settings.add(new JMenuItem(changePlayersNickName));
		settings.add(new JMenuItem(changeTheme));
		settings.add(new JCheckBoxMenuItem(useBackgroundSound));

		JMenu toolsM = new JMenu("Tools");
		toolsM.add(new JMenuItem(createTrainer));

		JMenu helpM = new JMenu("Help");
		helpM.add(new JMenuItem(help));
		helpM.add(new JMenuItem(changeLog));
		helpM.add(new JMenuItem(license));
		helpM.add(new JMenuItem(about));

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(settings);
		menuBar.add(toolsM);
		menuBar.add(helpM);
		setJMenuBar(menuBar);
	}

	@Override
	public void keyPressed(KeyEvent ke) {
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		/*
		 * The newGame and pause actions are disabled when first pressed, and
		 * are only re-enabled upon release of the keys.
		 */
		KeyStroke stroke = KeyStroke.getKeyStroke(ke.getKeyCode(), ke
				.getModifiers());
		if (stroke.equals(pause.getValue(Action.ACCELERATOR_KEY)))
			pause.setEnabled(true);
	}

	@Override
	public void keyTyped(KeyEvent ke) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		oldMouseX = e.getX() - board.offsetX;
		oldMouseY = e.getY() - board.offsetY;
		oldMouseTileX = oldMouseX / board.tileSize;
		oldMouseTileY = oldMouseY / board.tileSize;
		repaint();
		e.consume();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		oldMouseX = e.getX() - board.offsetX;
		oldMouseY = e.getY() - board.offsetY;
		oldMouseTileX = oldMouseX / board.tileSize;
		oldMouseTileY = oldMouseY / board.tileSize;
		repaint();
		e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (gameManager.hasGameRunning() && !showingOldPly
				&& !gameManager.isPaused()
				&& gameManager.getCurrentPlayer() instanceof Human) {
			int index = CheckersBoard.getIndex(oldMouseTileY, oldMouseTileX);

			for (PossiblePly move : sortedPlies) {
				for (Ply moveIndices : move.plies) {
					if (moveIndices.get(0) == index) {
						currMove = move;
						break;
					}
				}
			}
		}
		e.consume();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (currMove != null) {
			int boardIndex = CheckersBoard.getIndex(oldMouseTileY,
					oldMouseTileX);

			for (Ply ply : currMove.plies) {
				if (ply.get(ply.size() - 1) == boardIndex) {
					if (gameManager.isPlayer1Turn())
						((Human) gameManager.getPlayer1())
								.setMove(ply.plyIndex);
					else
						((Human) gameManager.getPlayer2())
								.setMove(ply.plyIndex);
					return;
				}
			}
			currMove = null;
		}
		e.consume();
	}

	@Override
	public void pauseStateChanged(CheckersGameEvent ce) {
	}

	@Override
	public void player1Changed(CheckersGameEvent ce) {
		updatePlayerLabels();
	}

	@Override
	public void player2Changed(CheckersGameEvent ce) {
		updatePlayerLabels();
	}

	@Override
	public void plyTaken(CheckersGameEvent ce) {
		stateModel.addElement(new GameState());

		update();
		repaint();
	}

	@Override
	public void plyTimeChanged(CheckersGameEvent ce) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Turn Time Left: ");
		if (gameManager.hasGameRunning())
			strBuffer.append(formatTime(gameManager.getRemainingPlyTime()));
		else
			strBuffer.append(formatTime(0));
		plyTime.setText(strBuffer.toString());

		gameTime.setText("Game Time: "
				+ formatTime(gameManager.getElapsedTime()));
	}

	/**
	 * Stores each possible ply by the checkers piece that is moved. This is
	 * used for showing the current player's possible plies.
	 */
	private synchronized void processPlies() {
		sortedPlies.clear();

		if (!shouldProcessPlies()) {
			return;
		}

		CheckersBoard board = gameManager.getBoard();
		List<Ply> plies = extractPlies(board);

		splitAndAddPlies(plies);
	}

	private boolean shouldProcessPlies() {
		if (gameManager.getGameOutcome() != CheckersGameManager.GAME_IN_PROGRESS) {
			return false;
		}

		return showMoves || gameManager.getCurrentPlayer() instanceof Human;
	}

	private List<Ply> extractPlies(CheckersBoard board) {
		int numPlies = board.getNumPlies();

		if (numPlies <= 0) {
			return Collections.emptyList();
		}

		List<Ply> plies = new ArrayList<>();

		for (int i = 0; i < numPlies; i++) {
			Ply ply = board.getPly(i);

			if (!gameManager.isPlayer1Turn()) {
				ply = Ply.getInvertedPly(ply);
			}

			plies.add(ply);
		}

		return plies;
	}

	private void splitAndAddPlies(List<Ply> plies) {
		List<Ply> currentPlyGroup = new ArrayList<>();
		Ply firstPly = plies.get(0);

		for (Ply ply : plies) {
			if (ply.get(0) != firstPly.get(0)) {
				addPlyGroup(currentPlyGroup);
				currentPlyGroup = new ArrayList<>();
				firstPly = ply;
			}

			currentPlyGroup.add(ply);
		}

		addPlyGroup(currentPlyGroup);
	}

	private void addPlyGroup(List<Ply> plyGroup) {
		if (!plyGroup.isEmpty()) {
			sortedPlies.add(new PossiblePly(plyGroup));
		}
	}

	public void setTrainer(CheckersTrainer trainer) {
		if (this.trainer != null || trainer == null)
			return;

		this.trainer = trainer;
		trainer.addWindowListener(CheckersGUI.this);

		newGame.setEnabled(false);
		playerSetup.setEnabled(false);
		createTrainer.setEnabled(false);
		repaint();
	}

	private void update() {
		currMove = null;
		// Update list items
		stateList.getUI().installUI(stateList);

		updatePlayerScores();

		// Change the background of the turnTime field
		// to match the current player's color
		if (gameManager.isPlayer1Turn())
			plyTime.setForeground(PLAYER1_COLOR);
		else
			plyTime.setForeground(PLAYER2_COLOR);

		// Format and set the current move count
		StringBuffer strBuffer = new StringBuffer("Move: ");
		int moveCount = gameManager.getMoveCount();
		if (moveCount < 10)
			strBuffer.append("00");
		else if (moveCount < 100)
			strBuffer.append('0');
		strBuffer.append(moveCount);
		this.moveCount.setText(strBuffer.toString());

		// Used for showing possible player moves
		processPlies();
	}

	/**
	 * Updates the player label borders to show each player's name.
	 */
	public void updatePlayerLabels() {
		CheckersPlayerInterface player1, player2;
		if (stateList.getSelectedIndex() == stateModel.getSize() - 1) {
			player1 = gameManager.getPlayer1();
			player2 = gameManager.getPlayer2();
		} else {
			player1 = selectedState.player1;
			player2 = selectedState.player2;
		}

		((TitledBorder) player1Label.getBorder()).setTitle("Player 1 - "
				+ player1.getName());
		((TitledBorder) player2Label.getBorder()).setTitle("Player 2 - "
				+ player2.getName());
	}

	/**
	 * Calculates the number of checkers and kings for each player.
	 */
	public void updatePlayerScores() {
		StringBuffer strBuffer = new StringBuffer("Checkers: ");
		strBuffer.append(selectedState.board
				.getCount(CheckersBoard.PLAYER1_CHECKER));
		strBuffer.append("   Kings: ");
		strBuffer.append(selectedState.board
				.getCount(CheckersBoard.PLAYER1_KING));
		player1Label.setText(strBuffer.toString());

		strBuffer = new StringBuffer("Checkers: ");
		strBuffer.append(selectedState.board
				.getCount(CheckersBoard.PLAYER2_CHECKER));
		strBuffer.append("   Kings: ");
		strBuffer.append(selectedState.board
				.getCount(CheckersBoard.PLAYER2_KING));
		player2Label.setText(strBuffer.toString());
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (stateList.getSelectedIndex() == -1
				|| stateList.getSelectedIndex() >= stateModel.size())
			stateList.setSelectedIndex(stateModel.size() - 1);
		else {
			selectedState = (GameState) stateList.getSelectedValue();
			showingOldPly = stateList.getSelectedIndex() < stateModel.getSize() - 1;
			updatePlayerLabels();
			updatePlayerScores();
			stateList.ensureIndexIsVisible(stateList.getSelectedIndex());
			repaint();
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (e.getSource() == trainer) {
			trainer = null;

			newGame.setEnabled(true);
			playerSetup.setEnabled(true);
			createTrainer.setEnabled(true);
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getSource() == this)
			quit.actionPerformed(null);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		if (e.getSource() == stateList) {
			// If the last selected board state is the current
			// board state, select the new current board state
			int size = stateModel.getSize();
			if (size > 0 && !showingOldPly)
				stateList.setSelectedIndex(size - 1);
		}
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
	}
}