import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;


public class MainWindow {

    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private JPanel homePanel;
    private JPanel gamePanel;
    private BackgroundPanel gameOverPanel;

    private JLabel winnerLabel;

    private Model gameworld;
    private Viewer canvas;
    
    private JLabel winnerImage;

    private Clip clip;
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
        ImageIcon bg = new ImageIcon("res/homebg.png");
        JLabel background = new JLabel(bg);
        background.setLayout(null);
        background.setBounds(0,0,1000,1000);

        homePanel = new JPanel(null);
        homePanel.setLayout(new BorderLayout());

        JButton startButton = new JButton("Start Game");
        startButton.setBounds(350,500,300,70);

        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(0,170,60));
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,4,true));

        startButton.addActionListener(e -> {
            playMusic("res/dod3.wav");

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

            SwingUtilities.invokeLater(() -> {
                canvas.requestFocusInWindow();
            });
        });

        background.add(startButton);
        homePanel.add(background);
    }

    private void createGameScreen(){

        gamePanel = new JPanel(null);
    }

    private void createGameOverScreen(){
 
        gameOverPanel = new BackgroundPanel();
        gameOverPanel.setLayout(null);

        winnerLabel = new JLabel("",SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setBounds(300,400,400,60);

        JButton homeButton = new JButton("Back to Home");
        homeButton.setBounds(400,500,200,50);
        styleButton(homeButton);

        homeButton.addActionListener(e -> {
        	if(clip != null)
        	{
        	    clip.stop();
        	}

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
                       	if(clip != null)
                    	{
                    	    clip.stop();
                    	}

                    	String winner = gameworld.getWinner();
                    	winnerLabel.setText(winner);

                    	if(winner.contains("Player 1")){
                            playMusic("res/win1.wav");
                    	    gameOverPanel.setImage("res/mario.png");
                    	}
                    	else{            
                    		playMusic("res/win2.wav");
                    	    gameOverPanel.setImage("res/luigi.png");
                    	}
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
    
    class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public void setImage(String path){
            backgroundImage = new ImageIcon(path).getImage();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);

            if(backgroundImage != null){
                g.drawImage(backgroundImage,0,0,getWidth(),getHeight(),this);
            }
        }
    }
    
    public void playMusic(String filepath)
    {
        try
        {
            File musicPath = new File(filepath);

            if(musicPath.exists())
            {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);

                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void styleButton(JButton button){
    	JButton startButton = new JButton("Start Game");
    	startButton.setBounds(350,500,300,70);

    	startButton.setFont(new Font("Arial", Font.BOLD, 20));
    	startButton.setForeground(Color.WHITE);

    	startButton.setBackground(new Color(0,170,60));

    	startButton.setFocusPainted(false);
    	startButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,4,true));
    }

    public static void main(String[] args){

        SwingUtilities.invokeLater(() -> new MainWindow());

    }
}