package checkersGUI;

import checkersMain.CheckersPlayerInterface;
import utilsGUI.Constants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class ChangePlayerNameDialog extends JDialog implements ActionListener,
        KeyListener, ItemListener {

    public static final Font FONT = new Font(Constants.FONT_ARIAL, Font.BOLD, 20);
    private final JButton btnSaveChanges, btnCancel;
    private final JTextField changePlayerName1, changePlayerName2;
    private final CheckersPlayerInterface player1, player2;

    public ChangePlayerNameDialog(
            JFrame parent,
            CheckersPlayerInterface player1,
            CheckersPlayerInterface player2) {
        super(parent, "Change players nickname", true);

        this.player1 = player1;
        this.player2 = player2;

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(CheckersGUI.NEUTRAL_BG_COLOR);

        JComponent componentChangeNamePlayer1 = new JComponent() {
        };
        JComponent componentChangeNamePlayer2 = new JComponent() {
        };

        componentChangeNamePlayer1.setLayout(new GridBagLayout());
        componentChangeNamePlayer2.setLayout(new GridBagLayout());
        createBorders(componentChangeNamePlayer1, "Change player 1 name", CheckersGUI.PLAYER1_COLOR);
        createBorders(componentChangeNamePlayer2, "Change player 2 name", CheckersGUI.PLAYER2_COLOR);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        changePlayerName1 = new JTextField();
        componentChangeNamePlayer1.add(changePlayerName1, c);

        changePlayerName2 = new JTextField();
        componentChangeNamePlayer2.add(changePlayerName2, c);

        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.weighty = 5;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(componentChangeNamePlayer1, c);

        c.gridx++;
        panel.add(componentChangeNamePlayer2, c);

        c.gridwidth = 1;
        c.weighty = 1;
        c.gridy++;

        btnSaveChanges = new JButton("Save changes");
        btnSaveChanges.addActionListener(this);
        panel.add(btnSaveChanges, c);

        c.gridx--;
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        panel.add(btnCancel, c);

        add(panel);

        btnSaveChanges.addKeyListener(this);
        btnCancel.addKeyListener(this);

        setMinimumSize(new Dimension(400, 300));
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void createBorders(JComponent component, String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
                CheckersGUI.BORDER, title);
        border.setTitleFont(FONT);
        border.setTitleColor(color);
        component.setBorder(border);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSaveChanges) {
            player1.setName(getNewNamePlayer1());
            player2.setName(getNewNamePlayer2());
        }
        setVisible(false);
    }

    public String getNewNamePlayer1() {
        if (changePlayerName1.getText().isEmpty())
            return "Human";
        return changePlayerName1.getText();
    }

    public String getNewNamePlayer2() {
        if (changePlayerName2.getText().isEmpty())
            return "Human";
        return changePlayerName2.getText();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            btnCancel.doClick();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
