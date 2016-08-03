package application;

public class BMPFileHeader {
	public short ID;		// 2 Bytes
	public int size;		// 4 Bytes
	public short resv1;		// 2 Bytes
	public short resv2;		// 2 Bytes
	public int offset;		// 4 Bytes
	
	BMPFileHeader(){
		ID 		= 0;
		size 	= 0;
		resv1	= 0;
		resv2	= 0;
		offset	= 0;
	}
	
	BMPFileHeader(short id,int ssize,short r1,short r2,int off){
		ID 		= id;
		size 	= ssize;
		resv1	= r1;
		resv2	= r2;
		offset	= off;
	}
}
