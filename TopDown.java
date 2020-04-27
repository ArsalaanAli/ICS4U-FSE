import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*; 
import java.util.*;
public class TopDown extends JFrame{
	javax.swing.Timer myTimer;
	//CREATING JPanels
	TopDownGame game;
	CutScene intro, intro2;

	JPanel cards;//a panel that uses CardLayout
	private int currentScene = 0;
	private boolean[] keys;
    CardLayout cLayout = new CardLayout(); 
    public TopDown() {
		super("Move the Box");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1280,720);

		myTimer = new javax.swing.Timer(10, new TickListener());	 // trigger every 100 ms
		myTimer.start();
		keys = new boolean[KeyEvent.KEY_LAST+1];
		addKeyListener(new moveListener());
		intro = new CutScene(0, 7, true, keys);
		intro2 = new CutScene(1, 2, false, keys);
		game = new TopDownGame(keys);
		cards = new JPanel(cLayout);
		cards.add(intro, "intro");
		cards.add(intro2, "intro2");
		cards.add(game, "td");
		add(cards);
		setResizable(false);
		setVisible(true);
	}
	class moveListener implements KeyListener{
	    public void keyTyped(KeyEvent e) {
		}
	
	    public void keyPressed(KeyEvent e) {
			keys[e.getKeyCode()] = true;
	    }
	    
	    public void keyReleased(KeyEvent e) {
	        keys[e.getKeyCode()] = false;
	    }
    }
	class TickListener implements ActionListener{
		public void actionPerformed(ActionEvent evt){
			if(game!= null && game.ready){
				if(currentScene == 0){
					cLayout.show(cards, "intro");
					if(intro.sceneFinished()){
						currentScene = 1;
					}
					intro.repaint();
				}
				if(currentScene == 1){
					cLayout.show(cards, "intro2");
					if(intro2.sceneFinished()){
						currentScene = 2;
					}
					intro2.repaint();
				}
				if(currentScene == 2){
					cLayout.show(cards, "td");
					game.move();
					game.checkEntrances();
					game.repaint();
					if(game.checkCutScene()){
						currentScene = game.getCutScene();
						System.out.println(currentScene);
					}
				}
				if(currentScene ==  3){
					cLayout.show(cards, "cutScene1");
				}
			}
		}
	}
    public static void main(String[] arguments) {
		TopDown frame = new TopDown();		
    }
}

class TopDownGame extends JPanel {
	public boolean ready=false;
	public final int UP = 0, LEFT = 1, DOWN = 2,  RIGHT = 3;
	private TopDownCharacter jim;
	private boolean[] keys;
	private int speed = 2, gameState = 0;
	private int[] cutScenes = new int[]{3};
	private BufferedImage mask;
	private String currentMap = "Outside";
	private Hashtable<Integer, String> entrances = new Hashtable<Integer, String>();
	private Hashtable<String, int[]> mapStartLocations = new Hashtable<String, int[]>();
	private String[] cutSceneLocations = new String[]{"BusinessHall"};
	Image map = new ImageIcon("Assets/Maps/Outside.png").getImage();//loading map of Massey
	public TopDownGame(boolean[] keyListen){
		try {
    		mask = ImageIO.read(new File("Assets/Maps/OutsideMask.png"));
		} 
		catch (IOException e) {
			System.out.println(e);
		}
		loadEntrances();
		loadStartLocations();
		jim = new TopDownCharacter(630, 620, speed);
		keys = keyListen;
		setSize(1280,720);
	}
    public void addNotify() {
        super.addNotify();
        ready = true;
	}
	private void loadStartLocations(){
		mapStartLocations.put("GuidanceHall", new int[]{1200, 350});
		mapStartLocations.put("FrontRow", new int[]{35, 180});
		mapStartLocations.put("BusinessHall", new int[]{1200, 400});
	}
	private int[] getMapStartLocation(String prev, String next){
		if(prev == "Outside" && next == "FrontRow"){
			return new int[]{640, 650};
		}
		else{
			return mapStartLocations.get(next);
		}
	}
	private void loadEntrances(){
		Color c = new Color(0, 255, 0);
		int val = c.getRGB();
		entrances.put(val, "GuidanceHall");
		c = new Color(255, 0, 0);
		val = c.getRGB();
		entrances.put(val, "FrontRow");
		c = new Color(0, 255, 255);
		val = c.getRGB();
		entrances.put(val, "FrontRow");
		c = new Color(100, 255, 0);
		val = c.getRGB();
		entrances.put(val, "BusinessHall");
	}
	private boolean entranceAtLocation(){
		int c = mask.getRGB(jim.getX(), jim.getY());
		if (entrances.containsKey(c)){
			return true;
		}
		return false;
	}
	public void checkEntrances(){
		if(entranceAtLocation()){
			String newMap = entrances.get(mask.getRGB(jim.getX(), jim.getY()));
			map = new ImageIcon("Assets/Maps/" + newMap + ".png").getImage();
			try {
				mask = ImageIO.read(new File("Assets/Maps/"+ newMap + "Mask.png"));
			} 
			catch (IOException e) {
				System.out.println(e);
			}
			int[] startLocations = getMapStartLocation(currentMap, newMap);
			jim.setX(startLocations[0]);
			jim.setY(startLocations[1]);
			currentMap = newMap;
		}
	}
	public boolean checkCutScene(){
		if(currentMap == cutSceneLocations[gameState]){
			int c = mask.getRGB(jim.getX(), jim.getY());
			int check = (new Color(255, 255, 0)).getRGB();
			if (c == check){
				return true;
			}
		}
		return false;
	}
	public int getCutScene(){
		return cutScenes[gameState];
	}
	private boolean clear(int x, int y){
		Color pink = new Color(246, 0, 255);
		if(x<0 || x>= mask.getWidth(null) || y<0 || y>= mask.getHeight(null)){
			return false;
		}
		int WALL = pink.getRGB();
		int c = mask.getRGB(x, y);
		return c != WALL;
	}
    public void move() {
		if(keys[KeyEvent.VK_RIGHT] && clear(jim.getX() + jim.getWidth() + 2*speed, jim.getY()) && clear(jim.getX() + jim.getWidth() + 2*speed, jim.getY()+jim.getHeight())){
			jim.move(RIGHT);
		}
		else if(keys[KeyEvent.VK_LEFT] && clear(jim.getX() - 2*speed, jim.getY()) && clear(jim.getX() - 2*speed, jim.getY()+jim.getHeight())){
			jim.move(LEFT);
		}
		else if(keys[KeyEvent.VK_UP] && clear(jim.getX(), jim.getY() - 2*speed)){
			jim.move(UP);
		}
		else if(keys[KeyEvent.VK_DOWN] && clear(jim.getX(), jim.getY() + jim.getHeight() + 2*speed)){
			jim.move(DOWN);
		}
	}
    public void paint(Graphics g){
		g.drawImage(map, 0, 0, null);
		g.setColor(new Color(0, 190, 190));
        jim.draw(g);
    }
}

class TopDownCharacter{
	public final int UP = 0, LEFT = 1, DOWN = 2, RIGHT = 3, WAIT = 5;
	private int x, y, speed, dir, frame = 0, delay = 0;
	Image playerPics[], map;//60-96
	public TopDownCharacter(int x, int y, int speed){
		this.x = x;
		this.y = y;
		this.speed = speed;
		playerPics = new Image[36];
		for(int i = 0; i<36; i++){
			playerPics[i] = new ImageIcon("Assets/Sprites/Jim/" + (i+60) + ".png").getImage();
		}
	}
	public void move(int d){
		if(d == UP){
			dir = UP;
			y -= speed;
			delay+=1;
			if(delay%WAIT == 0){
			frame = (frame+1)%9;
			}
		}
		if(d == DOWN){
			dir = DOWN;
			y+=speed;
			delay+=1;
			if(delay%WAIT == 0){
			frame = (frame+1)%9;
			}
		}
		if(d == LEFT){
			dir = LEFT;
			x-=speed;
			delay+=1;
			if(delay%WAIT == 0){
			frame = (frame+1)%9;
			}
		}
		if(d == RIGHT){
			dir = RIGHT;
			x+=speed;
			delay+=1;
			if(delay%WAIT == 0){
			frame = (frame+1)%9;
			}
		}
	}
	public void draw(Graphics g){
		g.drawImage(playerPics[frame+(dir*9)], x, y, null);
	}
	public int getHeight(){
		return playerPics[frame+(dir*9)].getHeight(null);
	}
	public int getWidth(){
		return playerPics[frame+(dir*9)].getWidth(null);
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public void setX(int set){
		x = set;
	}
	public void setY(int set){
		y = set;
	}
}
class CutScene extends JPanel {
	public boolean ready=false;
	private boolean[] keys;
	private Image[] scene;
	private int current = 0, delay =  0, length;
	private boolean animated, fade = false, finished = false;
	private float transparent;
	public CutScene(int set, int length, boolean animated,boolean[] keyListen){
		keys = new boolean[KeyEvent.KEY_LAST+1];
		keys = keyListen;
		scene = new Image[length+1];
		for(int i = 1; i<=length; i++){
			scene[i-1] = new ImageIcon("Scenes/Scene" + set + "/" + i + ".png").getImage();
		}
		this.length = length;
		this.animated = animated;
	}
    public void addNotify() {
        super.addNotify();
        ready = true;
	}
	public boolean sceneFinished(){
		if(finished){
			return true;
		}
		if(animated){
			if(keys[KeyEvent.VK_ENTER]){
				fade = true;
			}
		}
		else{
			if(current == length){
				return true;
			}
		}
		return false;
	}

	public void paint(Graphics g){
		if(fade){
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(new Color(0, 0, 0));
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent));
			g2d.fillRect(0, 0, 1290, 730);
			transparent+=0.005;
			if(transparent>0.5){
				finished = true;
			}
		}
		else{
			if(animated){
				delay+=1;
				if(delay%10 == 0){
					current = (current+1)%length;
				}
			}
			else{
				if(keys[KeyEvent.VK_ENTER] && delay>50){
					current+=1;
					delay = 0;
				}
				delay+=1;
			}
			g.drawImage(scene[current], 0, 0, null);
		}
	}
}