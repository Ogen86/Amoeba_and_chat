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
	
	public static int CSIZE = 20;       //a mez�k oldala
	public static int BSIZE = 20;       //a t�bla oldala = mez�k sz�ma
	public static int BMAX = BSIZE - 1; //a t�bla mez�inek maxim�lis indexe
	
	public static int OWNSTEP   =  1;   //a mez�n saj�t p�tty van
	public static int OTHERSTEP = -1;   //a mez�n az ellenf�l p�ttye van
	public static int FREECELL  =  0;   //a mez�n nincs p�tty
	
	public static boolean INPLAY = true;
}
