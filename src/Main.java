import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Main extends JFrame {

    private Robot robot;
    private JPanel colorPanel;
    private boolean mousePressed;
    private int gridSize = 9; 

    public Main() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 125));

        colorPanel = new JPanel(new GridLayout(gridSize, gridSize));
        for (int i = 0; i < gridSize * gridSize; i++) {
            JPanel pixelPanel = new JPanel();
            pixelPanel.setPreferredSize(new Dimension(11, 11)); 
            colorPanel.add(pixelPanel);
        }
        getContentPane().add(colorPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point mousePos = e.getPoint();
                displayColors(mousePos);
                mousePressed = true;
            }
        });

        getContentPane().setPreferredSize(new Dimension(100, 100));
        pack(); 

        JFrame transparentWindow = new JFrame();
        transparentWindow.setUndecorated(true);
        transparentWindow.setBackground(new Color(0, 0, 0, 1));
        transparentWindow.setSize(100, 100); 
        transparentWindow.setVisible(true);
        transparentWindow.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                try {
                    Robot robot = new Robot();
                    Color color = robot.getPixelColor(mousePos.x, mousePos.y);

                    Color maxBrightnessColor = new Color(color.getRed() + 1, color.getGreen() + 1, color.getBlue() + 1,
                            1);
                    System.out.println("Color under the mouse position with maximum brightness: " + maxBrightnessColor);
                    String hexColor = String.format("#%02X%02X%02X", maxBrightnessColor.getRed(),
                            maxBrightnessColor.getGreen(), maxBrightnessColor.getBlue());

                    StringSelection selection = new StringSelection(hexColor);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);

                    System.exit(0);
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Thread positionUpdater = new Thread(() -> {
            while (!mousePressed) {
                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                displayColors(mousePos);
                setLocation(mousePos.x + 5, mousePos.y + 5);
                try {
                    Thread.sleep(10); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                transparentWindow.setLocation(mousePos.x - 50, mousePos.y - 50);
            }
        });
        positionUpdater.start();

    }

    private void displayColors(Point point) {
        BufferedImage screenshot = robot
                .createScreenCapture(new Rectangle(point.x - gridSize / 2, point.y - gridSize / 2, gridSize, gridSize)); 

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JPanel pixelPanel = (JPanel) colorPanel.getComponent(i * gridSize + j);
                Color color = new Color(screenshot.getRGB(j, i));
                pixelPanel.setBackground(color);
                if (i == gridSize / 2 && j == gridSize / 2) { 

                    pixelPanel.setBorder(BorderFactory.createLineBorder(
                            new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue())));
                } else {

                    pixelPanel.setBorder(null); 
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main picker = new Main();
            picker.setVisible(true);
        });
    }
}