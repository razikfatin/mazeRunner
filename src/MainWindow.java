import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow {

    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private JPanel homePanel;
    private JPanel gamePanel;
    private JPanel gameOverPanel;

    private JLabel winnerLabel;

    private Model gameworld;
    private Viewer canvas;

    private Controller controller = new Controller();

    private boolean startGame = false;
    private boolean gameOverShown = false;

    private static int TargetFPS = 100;

    public MainWindow() {

        frame = new JFrame("Game");
        frame.setSize(1000,1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createHomeScreen();
        createGameScreen();
        createGameOverScreen();

        mainPanel.add(homePanel,"HOME");
        mainPanel.add(gamePanel,"GAME");
        mainPanel.add(gameOverPanel,"GAMEOVER");

        frame.add(mainPanel);
        frame.setVisible(true);

        runGameLoop();
    }

    private void createHomeScreen(){

        homePanel = new JPanel(null);

        JButton startButton = new JButton("Start Game");
        startButton.setBounds(400,500,200,40);

        startButton.addActionListener(e -> {
        	
        	controller.resetKeys();

            gameworld = new Model();
            canvas = new Viewer(gameworld);

            gamePanel.removeAll();

            canvas.setBounds(0,0,1000,1000);
            canvas.setBackground(Color.WHITE);

            canvas.addKeyListener(controller);
            canvas.setFocusable(true);

            gamePanel.add(canvas);

            startGame = true;
            gameOverShown = false;

            cardLayout.show(mainPanel,"GAME");

            // request focus AFTER switching panels
            SwingUtilities.invokeLater(() -> {
                canvas.requestFocusInWindow();
            });
        });

        homePanel.add(startButton);
    }

    private void createGameScreen(){

        gamePanel = new JPanel(null);
    }

    private void createGameOverScreen(){

        gameOverPanel = new JPanel(null);

        winnerLabel = new JLabel("",SwingConstants.CENTER);
        winnerLabel.setBounds(350,400,300,50);

        JButton homeButton = new JButton("Back to Home");
        homeButton.setBounds(400,500,200,40);

        homeButton.addActionListener(e -> {

            startGame = false;
            gameOverShown = false;

            cardLayout.show(mainPanel,"HOME");
        });

        gameOverPanel.add(winnerLabel);
        gameOverPanel.add(homeButton);
    }

    private void runGameLoop(){

        new Thread(() -> {

            while(true){

                int timeBetweenFrames = 1000 / TargetFPS;
                long frameCheck = System.currentTimeMillis() + timeBetweenFrames;

                if(startGame && gameworld != null){

                    if(!gameworld.isGameOver()){

                        gameworld.gamelogic();

                        if(canvas != null){
                            canvas.updateview();
                        }

                        frame.setTitle(
                                "Player1 Lives: " + gameworld.getPlayer1Lives() +
                                " | Player2 Lives: " + gameworld.getPlayer2Lives()
                        );
                    }

                    else if(!gameOverShown){

                        winnerLabel.setText(gameworld.getWinner());

                        cardLayout.show(mainPanel,"GAMEOVER");

                        gameOverShown = true;
                    }
                }

                while(frameCheck > System.currentTimeMillis()){
                    try{ Thread.sleep(1); }catch(Exception ignored){}
                }
            }

        }).start();
    }

    public static void main(String[] args){

        SwingUtilities.invokeLater(() -> new MainWindow());

    }
}