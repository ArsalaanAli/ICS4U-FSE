import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*;
import java.util.*; 

public class gym extends JFrame{
	javax.swing.Timer myTimer;   
	DodgeBall game;
		
    public gym() {
		super("Move the Box");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1280,720);

		myTimer = new javax.swing.Timer(10, new TickListener());	 // trigger every 100 ms
		myTimer.start();

        game = new DodgeBall();
        add(game);
		setResizable(false);
		setVisible(true);
    }
    
	class TickListener implements ActionListener{
		public void actionPerformed(ActionEvent evt){
			if(game!= null && game.ready){
                game.move();
                game.controlBalls();
                game.checkCollisions();
                game.repaint();
                game.gameController();
			}			
		}
	}
	
    public static void main(String[] arguments) {
		gym frame = new gym();		
    }
}

class DodgeBall extends JPanel {
    public static final int RIGHT = 1, LEFT = 0, WAIT = 5;
    //94x60
    private int timer = 0, levelTime = 0, difficulty = 100, mark = 100;
    private double timeRemaining = 50;
    private float transparent = 0;
	public boolean ready=false, hurt = false, gameOver = false;
    private boolean []keys;
    private Scarrow scarrow = new Scarrow();
    private Jim jim = new Jim(100, 550, 3, 100);
    private ArrayList<Ball> balls = new ArrayList<Ball>();
    private Image BG = new ImageIcon("Assets/BossMaps/GymBG.png").getImage();
	public DodgeBall(){
		keys = new boolean[KeyEvent.KEY_LAST+1];
		addKeyListener(new moveListener());
        setSize(800,600);
	}
	
    public void addNotify() {
        super.addNotify();
        requestFocus();
        ready = true;
    }
    public void controlBalls(){
        if(timer%difficulty==0){
            scarrow.throwAnimation();
            addBall();
            timer = 0;
        }
        timer++;
        moveBalls();
    }
    private void addBall(){
        Ball b = new Ball(1170, randint(500, 600), 3);
        balls.add(b);
    }
    private void moveBalls(){
        for(Ball b : balls){
            b.move();
        }
    }
    public void checkCollisions(){
        for(int b = balls.size()-1; b>-1; b--){
            if(balls.get(b).getX() <= jim.getX()+jim.getWidth()-10 && balls.get(b).getX() >= jim.getX() && balls.get(b).getY() >= jim.getY()-30 && balls.get(b).getY() <= jim.getY()+jim.getHeight()){
                balls.remove(b);
                mark-=5;
                hurt = true;
            }
        }
    }
    public void move(){
		jim.jump(keys[KeyEvent.VK_UP]);
		if (keys[KeyEvent.VK_RIGHT]){
			jim.move(RIGHT);
		}
		else if(keys[KeyEvent.VK_LEFT]){
			jim.move(LEFT);
		}
    }
    public void gameController(){
        if(mark<50){
            gameOver = true;
        }
        if(levelTime>4000){
            difficulty = 50;
        }
        if(levelTime>2500){
            difficulty = 60;
        }
        else if(levelTime>1000){
            difficulty = 75;
        }
        if(levelTime%10 == 0){
            timeRemaining-= 0.1;
        }
        levelTime++;
    }
    public void paint(Graphics g){
        if(gameOver){
            g.setColor(Color.black);
            g.fillRect(0, 0, 1280, 720);
        }
        else{
            //Drawing Background
            g.drawImage(BG, 0, 0, null);

            //Drawing Text
            String tempTime = String.format("%.1f", timeRemaining);
            g.setColor(Color.black);
            g.setFont(new Font(null, Font.BOLD, 25));
            g.drawString("Time Remaining: " + tempTime, 895, 100);

            //Drawing HealthBar
            g.setColor(Color.black);
            g.fillRect(10, 10, 300, 30);
            g.fillRect(10, 40, 120, 20);
            g.setColor(Color.white);
            g.setFont(new Font(null, Font.BOLD, 20));
            g.drawString("Mark: " + mark + "%", 20, 55);
            g.fillRect(15, 15, 290, 20);
            g.setColor(Color.red);
            g.fillRect(15, 15, (int)((double)((mark-50)/50.0)*290), 20);
            
            //Drawing Balls
            for(Ball b : balls){
                b.paint(g);
            }

            //Drawing Characters
            scarrow.paint(g);
            jim.draw(g);

            //Hurt Effect
            if(hurt){
                transparent+=0.05;
                if(transparent>0.5){
                    hurt = false;
                }
            }
            else if(transparent>0.05){
                transparent-=0.05;        
            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.red);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent));
            g2d.fillRect(0, 0, 1290, 730);
        }
    }
    private int randint(int low, int high){
        return (int)(Math.random()*(high-low+1)+low);
    }
    class moveListener implements KeyListener{
	    public void keyTyped(KeyEvent e) {}
	
	    public void keyPressed(KeyEvent e) {
	        keys[e.getKeyCode()] = true;
	    }
	    
	    public void keyReleased(KeyEvent e) {
	        keys[e.getKeyCode()] = false;
	    }
    }    
}

class Ball{
    private int x, y, speed; 
    private static Image ball = new ImageIcon("Assets/Sprites/Dodgeball/ball.png").getImage().getScaledInstance(40, 40,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
    public Ball(int x, int y, int speed){
        this.x = x;
        this.y = y;
        this.speed = speed;
    }
    public void move(){
        x -= speed;
    }
    public void paint(Graphics g){
        g.drawImage(ball, x, y, null);
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
}
class Scarrow{
    private int x = 1200, y = 550, currentPic = 0, timer = 0;
    private boolean throwing = false;
    private Image[] scarrowPics = new Image[6];
    public Scarrow(){
        for(int i = 0; i<6; i++){
            Image temp = new ImageIcon("Assets/Sprites/Scarrow/" + i + ".png").getImage();
            temp = temp.getScaledInstance(temp.getWidth(null)*2, temp.getHeight(null)*2,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
            scarrowPics[i] = temp;
        }
    }
    public void throwAnimation(){
       throwing = true;
    }
    public void paint(Graphics g){
        if(throwing){
            if(timer%10 == 0){
                currentPic++;
                if(currentPic>5){
                    throwing = false;
                    currentPic = 0;
                }
            }
            timer++;
        }
        g.drawImage(scarrowPics[currentPic], x, y, null);
    }
}