package application;

public class BMPInfoHeader {
	public int headersize;	// 4 Bytes
	public int width;		// 4 Bytes
	public int height;		// 4 Bytes
	public short planos;	// 2 Bytes
	public short bpp;		// 2 Bytes
	public int compress;	// 4 Bytes
	public int imgsize;		// 4 Bytes
	public int bmpx;		// 4 Bytes
	public int bmpy;		// 4 Bytes
	public int colors;		// 4 Bytes
	public int imxtcolors;	// 4 Bytes
	
	BMPInfoHeader(){
		headersize 	= 0;
		width 		= 0;
		height		= 0;
		planos		= 0;
		bpp			= 0;
		compress	= 0;
		imgsize		= 0;
		bmpx		= 0;
		bmpy		= 0;
		colors		= 0;
		colors		= 0;
		imxtcolors	= 0;
	}
	
	BMPInfoHeader(int hs,int wi,int he,short pl,short bp,int com,int im,int bx,int by,int co,int imx){
		headersize 	= hs;
		width 		= wi;
		height		= he;
		planos		= pl;
		bpp			= bp;
		compress	= com;
		imgsize		= im;
		bmpx		= bx;
		bmpy		= by;
		colors		= co;
		colors		= imx;
		imxtcolors	= imx;
	}
}