package application;

public final  class Constants {
	
	public static final String JOIN     = "J";
	public static final String LIST     = "L";
	public static final String PLAY     = "P";
	public static final String ACCEPT   = "A";
	public static final String DECLINE  = "D";
	public static final String STEP     = "S";
	public static final String CHAT     = "C";
	public static final String WIN      = "W";
	public static final String BREAK    = "B";
	public static final String OK	    = "O";
	public static final String NOTOK    = "N";
	public static final String REGISTER = "R";
	
	public static final int SOCKET = 10001;
	
	public static int CSIZE = 20;       //a mezõk oldala
	public static int BSIZE = 20;       //a tábla oldala = mezõk száma
	public static int BMAX = BSIZE - 1; //a tábla mezõinek maximális indexe
	
	public static int OWNSTEP   =  1;   //a mezõn saját pötty van
	public static int OTHERSTEP = -1;   //a mezõn az ellenfél pöttye van
	public static int FREECELL  =  0;   //a mezõn nincs pötty
	
	public static boolean INPLAY = true;
}
