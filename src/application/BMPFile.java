package application;

import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BMPFile {
	
	//InfoHeader.bpp 1,4,8,24 bits
	public String path;
	public BMPFileHeader Header;
	public BMPInfoHeader InfoHeader;
	public int[] Palette;
	public int[] ImageData;
	public int[] WrittableImageData;
	public long[] red;
	public long[] green;
	public long[] blue;
	public ArrayList<String> operations;
	private DataOutputStream dos;
	
	
	BMPFile(){
		this.Header = new BMPFileHeader();
		this.InfoHeader = new BMPInfoHeader();
		this.Palette = new int[0];
		this.ImageData = new int[0];
		this.red = new long[256];
		this.blue = new long[256];
		this.green = new long[256];
		for (int i = 0; i < 256; i++) {
	           red[i] = green[i] = blue[i] = 0;
		}
	}
	
	@SuppressWarnings("resource")
	BMPFile(String path) throws IOException{
		this.operations = new ArrayList<String>();
		this.red = new long[256];
		this.blue = new long[256];
		this.green = new long[256];
		
		for (int i = 0; i < 256; i++) {
			red[i] = green[i] = blue[i] = 0;
		}
		
		final FileChannel channel = new FileInputStream(path).getChannel();
		MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		
		
		this.Header = new BMPFileHeader();
		this.InfoHeader = new BMPInfoHeader();
		this.Palette = new int[0];
		this.path = path;
		
		this.Header.ID		= Short.reverseBytes(buffer.getShort());
		this.Header.size 	= Integer.reverseBytes(buffer.getInt());
		this.Header.resv1 	= Short.reverseBytes(buffer.getShort());
		this.Header.resv2 	= Short.reverseBytes(buffer.getShort());
		this.Header.offset 	= Integer.reverseBytes(buffer.getInt());
		
		this.InfoHeader.headersize	= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.width		= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.height		= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.planos		= Short.reverseBytes(buffer.getShort());
		this.InfoHeader.bpp			= Short.reverseBytes(buffer.getShort());
		this.InfoHeader.compress	= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.imgsize		= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.bmpx		= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.bmpy		= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.colors		= Integer.reverseBytes(buffer.getInt());
		this.InfoHeader.imxtcolors	= Integer.reverseBytes(buffer.getInt());
		
		//System.out.println("BEFORE------");
		//printData();
		
		ImageData = new int[10000000];
		int r,g,b,pos;
		
		if(this.InfoHeader.bpp == 1){ 
			//ImageData = new int[this.InfoHeader.width*this.InfoHeader.height*8];
			Palette = new int[2*3];
			buffer.get(new byte[this.InfoHeader.headersize-40]);
			readPalette(2,buffer);
			
			buffer.position(0);
			buffer.get(new byte[this.Header.offset]);
			
			if(((this.InfoHeader.width*this.InfoHeader.bpp)%32) == 0){	// NO PADDING
				for(int i=this.InfoHeader.height-1; i>=0 ;i--)
					for(int j=0; j<this.InfoHeader.width/8 ;j++)
						c1to24(Byte.toUnsignedInt(buffer.get()),i,j,this.InfoHeader.width/8);
			}else{
				int RD 	= this.InfoHeader.width; //easy math
				int M32	= 32*((RD/32)+1);
				int PD 	= M32 - RD;
				int BF 	= RD/8;
				int BP 	= PD/8;
				
				if((BF + BP) ==((RD + PD)/8)){
					for(int i=this.InfoHeader.height-1; i>=0 ;i--){
						for(int j=0; j<this.InfoHeader.width/8 ;j++)
							c1to24(Byte.toUnsignedInt(buffer.get()),i,j,this.InfoHeader.width/8);
						buffer.get(new byte[BP]);
					}
				}else{
					for(int i=this.InfoHeader.height-1; i>=0 ;i--){
						int act = 0;
						for(int j=0; j<this.InfoHeader.width/8 ;j++)
							act = p1to24(Byte.toUnsignedInt(buffer.get()),i,act,0,this.InfoHeader.width);
						p1to24(Byte.toUnsignedInt(buffer.get()),i,act,1,this.InfoHeader.width);
						buffer.get(new byte[BP]);
					}
				}
			}
			
		}else if(this.InfoHeader.bpp == 4){
			
			//ImageData = new int[this.InfoHeader.width*this.InfoHeader.height*3];
			Palette = new int[16*3];
			buffer.get(new byte[this.InfoHeader.headersize-40]);
			readPalette(16,buffer);
			
			buffer.position(0);
			buffer.get(new byte[this.Header.offset]);
			
			if(((this.InfoHeader.width*this.InfoHeader.bpp)%32) == 0){	// NO PADDING
				for(int i=this.InfoHeader.height-1; i>=0 ;i--)
					for(int j=0; j<this.InfoHeader.width/2 ;j++)
						c4to24(Byte.toUnsignedInt(buffer.get()),i,j,this.InfoHeader.width/2);
			}else{
				int RD 	= this.InfoHeader.width*4; //easy math
				int M32	= 32*((RD/32)+1);
				int PD 	= M32 - RD;
				int BF 	= RD/8;
				int BP 	= PD/8;
			
				if((BF + BP) ==((RD + PD)/8)){ //Soft stuff
					for(int i=this.InfoHeader.height-1; i>=0 ;i--){
						for(int j=0; j<this.InfoHeader.width/2 ;j++)
							c4to24(Byte.toUnsignedInt(buffer.get()),i,j,this.InfoHeader.width/2);
						buffer.get(new byte[BP]);
					}
				}else{ //HARD PADDING!!
					for(int i=this.InfoHeader.height-1; i>=0 ;i--){
						int act = 0 ;
						for(int j=0; j<this.InfoHeader.width/2 ;j++)
							act = p4to24(Byte.toUnsignedInt(buffer.get()),i,act,0,this.InfoHeader.width);
						p4to24(Byte.toUnsignedInt(buffer.get()),i,act,1,this.InfoHeader.width);
						buffer.get(new byte[BP]);	
					}
				}
			}
			
		}else if(this.InfoHeader.bpp == 8){
			//ImageData = new int[this.InfoHeader.width*this.InfoHeader.height*3];
			Palette = new int[256*3];
			buffer.get(new byte[this.InfoHeader.headersize-40]);
			readPalette(256,buffer);
			
			buffer.position(0);
			buffer.get(new byte[this.Header.offset]);
			int x;
			
			if(((this.InfoHeader.width*this.InfoHeader.bpp)%32) == 0){// NO MADNESS
				for(int i=this.InfoHeader.height-1; i>=0 ;i--){
					for(int j=0; j<this.InfoHeader.width ;j++){
						x = Byte.toUnsignedInt(buffer.get());
						pos = ((i * this.InfoHeader.width) + j)*3;
						ImageData[pos] = Palette[x*3];
						ImageData[pos+1] = Palette[x*3+1];
						ImageData[pos+2] = Palette[x*3+2];
					}
				}
			}else{// MADNESS
				int madness = 4 - (this.InfoHeader.width%4);
				for(int i=this.InfoHeader.height-1; i>=0 ;i--){
					for(int j=0; j<this.InfoHeader.width ;j++){
						x = Byte.toUnsignedInt(buffer.get());
						pos = ((i * this.InfoHeader.width) + j)*3;
						ImageData[pos] = Palette[x*3];
						ImageData[pos+1] = Palette[x*3+1];
						ImageData[pos+2] = Palette[x*3+2];
					}
					for(int j=0;j<madness;j++) // BRING THE MADNESS!!
						buffer.get();
				}
			}
			
		}else if(this.InfoHeader.bpp == 24){	// NO PALETTE
			//ImageData = new int[this.InfoHeader.width*this.InfoHeader.height*3];
			for(int i=0;i<this.Header.offset - 54;i++)buffer.get();
			
			if(((this.InfoHeader.width*this.InfoHeader.bpp)%32) == 0){// NO MADNESS	
				for(int i=this.InfoHeader.height-1; i>=0 ;i--){
					for(int j=0; j<this.InfoHeader.width ;j++){
						pos = ((i * this.InfoHeader.width) + j)*3;
						b = Byte.toUnsignedInt(buffer.get());
						g = Byte.toUnsignedInt(buffer.get());
						r = Byte.toUnsignedInt(buffer.get());
						
						ImageData[pos] = r;
						ImageData[pos+1] = g;
						ImageData[pos+2] = b;
					}
				}
			}else{// MADNESS
				int madness = 4 - ((this.InfoHeader.width*3)%4);
				for(int i=this.InfoHeader.height-1; i>=0 ;i--){
					for(int j=0; j<this.InfoHeader.width ;j++){
						pos = ((i * this.InfoHeader.width) + j)*3;
						b = Byte.toUnsignedInt(buffer.get());
						g = Byte.toUnsignedInt(buffer.get());
						r = Byte.toUnsignedInt(buffer.get());
						
						ImageData[pos] = r;
						ImageData[pos+1] = g;
						ImageData[pos+2] = b;
					}
					for(int j=0; j<madness ;j++) // BRING THE MADNESS!!
						buffer.get();
				}
			}
		}
		
		WrittableImageData = new int[10000000];
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height*3);i++)WrittableImageData[i] = ImageData[i];
	}
	
	public void printData(){
		System.out.println("HEADER SIZE : " + this.Header.size);
		System.out.println("HEADER OFFSET : " + this.Header.offset);
		
		System.out.println("INFOHEADER HEADERSIZE : " + this.InfoHeader.headersize);
		System.out.println("INFOHEADER IMGSIZE : " + this.InfoHeader.imgsize);
		System.out.println("INFOHEADER BMPX : " + this.InfoHeader.bmpx);
		System.out.println("INFOHEADER BMPY : " + this.InfoHeader.bmpy);
		System.out.println("INFOHEADER COLORS : " + this.InfoHeader.colors);
		System.out.println("INFOHEADER IMXTCOLORS : " + this.InfoHeader.imxtcolors);
	}
	
	public int[] NearestNeighbor(float x_ratio,float y_ratio, int i, int j,int oldw){
		
		int [] RGB = new int[3];
		
		float px = (float) Math.floor((double)j*x_ratio);
		float py = (float) Math.floor((double)i*y_ratio);
		
		double R = Math.round((py*oldw+px)*3);
		double G = Math.round((py*oldw+px)*3+1);
		double B = Math.round((py*oldw+px)*3+2);
		
		RGB[0] = truncate(WrittableImageData[(int) R]);
		RGB[1] = truncate(WrittableImageData[(int) G]);
		RGB[2] = truncate(WrittableImageData[(int) B]);
		
		return RGB;
	}
	
	public boolean limits(int i,int j){
		if(i>=0&&i<this.InfoHeader.height&&j>=0&&j<this.InfoHeader.width)
			return true;
		else
			return false;
	}
	
	public int[] getPixel(int i,int j, boolean x){	
		int [] pix = new int[3];
		pix[0] = 0;
		pix[1] = 0;
		pix[2] = 0;
		if(!limits(i,j))
			x = false;
		else{
			x = true;
			pix[0] = truncate(this.WrittableImageData[(i*this.InfoHeader.width+j)*3]);
			pix[1] = truncate(this.WrittableImageData[(i*this.InfoHeader.width+j)*3+1]);
			pix[2] = truncate(this.WrittableImageData[(i*this.InfoHeader.width+j)*3+2]);
		}	
		return pix;
	}
	
	public int[] BilinearInterpolation(float x_ratio, float y_ratio,int i, int j,int oldw){
		int [] RGB = new int[3];
		
		int x = (int)x_ratio * j;
        int y = (int)y_ratio * i;
        float x_diff = (x_ratio * j) - x;
        float y_diff = (y_ratio * i) - y;
        
        int indexa = (y * oldw * 3 + x * 3);
        int indexb = (indexa + 3);
        int indexc = (indexa + oldw * 3);
        int indexd = (indexc + 3);
        
        System.out.println(i + " " + j + " :::: x " + x + " y " + y + " xdiff " + x_diff + " ydiff " + y_diff + " " + indexa + " " + indexb + " " + indexc + " " + indexd);
        
        RGB[0] = truncate(truncate(WrittableImageData[indexa])*(1-x_diff)*(1-y_diff) + truncate(WrittableImageData[indexb])*(x_diff)*(1-y_diff) + truncate(WrittableImageData[indexc])*(y_diff)*(1-x_diff) + truncate(WrittableImageData[indexd])*(x_diff*y_diff));
        RGB[1] = truncate(truncate(WrittableImageData[indexa+1])*(1-x_diff)*(1-y_diff) + truncate(WrittableImageData[indexb+1])*(x_diff)*(1-y_diff) + truncate(WrittableImageData[indexc+1])*(y_diff)*(1-x_diff) + truncate(WrittableImageData[indexd+1])*(x_diff*y_diff));
        RGB[2] = truncate(truncate(WrittableImageData[indexa+2])*(1-x_diff)*(1-y_diff) + truncate(WrittableImageData[indexb+2])*(x_diff)*(1-y_diff) + truncate(WrittableImageData[indexc+2])*(y_diff)*(1-x_diff) + truncate(WrittableImageData[indexd+2])*(x_diff*y_diff));
        
       // System.out.println(RGB[0]+"|"+RGB[1]+"|"+RGB[2]);
		return RGB;
	}
	
	public int truncate(float color){
		if(color>255.0)
			return 255;
		else if(color<0.0)
			return 0;
		else
			return (int)color;
	}
	
	public int truncate(int color){
		if(color>255)
			return 255;
		else if(color<0)
			return 0;
		else
			return color;
	}
	
	public int p1to24(int B,int i,int act,int top,int w){
		int mask = 128;
		
		for(int d=7,l=0;d>=top;d--,l++){
			int index = ((mask>>l)&B)>>d;
			this.ImageData[((i*w)+act)*3] = Palette[index*3];
			this.ImageData[((i*w)+act)*3+1] = Palette[index*3+1];
			this.ImageData[((i*w)+act)*3+2] = Palette[index*3+2];
			act++;
		}
		return act;
	}
	
	public int p4to24(int B,int i,int act,int top,int w){
		int mask = 240;
		
		for(int d=1,l=0;d>=top;d--,l++){
			int index = ((mask>>4*l)&B)>>(4*d);
			this.ImageData[((i*w)+act)*3] = Palette[index*3];
			this.ImageData[((i*w)+act)*3+1] = Palette[index*3+1];
			this.ImageData[((i*w)+act)*3+2] = Palette[index*3+2];
			act++;
		}
		return act;
	}
	
	public void c1to24(int B,int i,int j,int w){
		
		int mask = 128;
		
		for(int d=7,l=0;d>=0;d--,l++){
			int index = ((mask>>l)&B)>>d;
			this.ImageData[((i*w+j)*8+l)*3] = Palette[index*3];
			this.ImageData[((i*w+j)*8+l)*3+1] = Palette[index*3+1];
			this.ImageData[((i*w+j)*8+l)*3+2] = Palette[index*3+2];
		}
	}
	
	public void c4to24(int B,int i,int j,int w){
		int mask = 240;
		
		for(int d=1,l=0;d>=0;d--,l++){
			int index = ((mask>>4*l)&B)>>(4*d);
			this.ImageData[((i*w+j)*2+l)*3] = Palette[index*3];
			this.ImageData[((i*w+j)*2+l)*3+1] = Palette[index*3+1];
			this.ImageData[((i*w+j)*2+l)*3+2] = Palette[index*3+2];
		}
	}
	
	public void readPalette(int colors,MappedByteBuffer in){
		for(int i=0;i<colors;i++){
			this.Palette[i*3+2] = Byte.toUnsignedInt(in.get());
			this.Palette[i*3+1] = Byte.toUnsignedInt(in.get());
			this.Palette[i*3] 	= Byte.toUnsignedInt(in.get());
			in.get();
		}
	}
	
	public void updateSeries(){
		
		for(int i=0;i<=255;i++){
			red[i] = 0;
			green[i] = 0;
			blue[i] = 0;
		}
		
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height);i++){
			red[ImageData[i*3]]++;
			green[ImageData[i*3+1]]++;
			blue[ImageData[i*3+2]]++;
		}
	}
	
	public void Negative(){
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height*3);i++)WrittableImageData[i] = 255 - WrittableImageData[i];
	}
	
	public void ReflecX(){
		int r,g,b;
		for(int i=0; i<this.InfoHeader.height/2 ;i++)
			for(int j=0; j<this.InfoHeader.width ;j++){
				r = WrittableImageData[(i*this.InfoHeader.width+j)*3];
				g = WrittableImageData[(i*this.InfoHeader.width+j)*3+1];
				b = WrittableImageData[(i*this.InfoHeader.width+j)*3+2];
				WrittableImageData[(i*this.InfoHeader.width+j)*3] = WrittableImageData[((this.InfoHeader.height-i-1)*this.InfoHeader.width+j)*3];
				WrittableImageData[(i*this.InfoHeader.width+j)*3+1] = WrittableImageData[((this.InfoHeader.height-i-1)*this.InfoHeader.width+j)*3+1];
				WrittableImageData[(i*this.InfoHeader.width+j)*3+2] = WrittableImageData[((this.InfoHeader.height-i-1)*this.InfoHeader.width+j)*3+2];
				WrittableImageData[((this.InfoHeader.height-i-1)*this.InfoHeader.width+j)*3] = r;
				WrittableImageData[((this.InfoHeader.height-i-1)*this.InfoHeader.width+j)*3+1] = g;
				WrittableImageData[((this.InfoHeader.height-i-1)*this.InfoHeader.width+j)*3+2] = b;
			}
	}
	
	public void ReflecY(){
		int r,g,b;
		for(int i=0; i<this.InfoHeader.height ;i++)
			for(int j=0; j<this.InfoHeader.width/2 ;j++){
				r = WrittableImageData[(i*this.InfoHeader.width+j)*3];
				g = WrittableImageData[(i*this.InfoHeader.width+j)*3+1];
				b = WrittableImageData[(i*this.InfoHeader.width+j)*3+2];
				WrittableImageData[(i*this.InfoHeader.width+j)*3] 	= WrittableImageData[((i+1)*this.InfoHeader.width-1-j)*3];
				WrittableImageData[(i*this.InfoHeader.width+j)*3+1] = WrittableImageData[((i+1)*this.InfoHeader.width-1-j)*3+1];
				WrittableImageData[(i*this.InfoHeader.width+j)*3+2] = WrittableImageData[((i+1)*this.InfoHeader.width-1-j)*3+2];
				WrittableImageData[((i+1)*this.InfoHeader.width-1-j)*3]	= r;
				WrittableImageData[((i+1)*this.InfoHeader.width-1-j)*3+1] = g;
				WrittableImageData[((i+1)*this.InfoHeader.width-1-j)*3+2] = b;
			}
	}
	
	public void Equal(){
		long NM = this.InfoHeader.height * this.InfoHeader.width;
		long[] eqR = new long[256];long[] eqG = new long[256];long[] eqB = new long[256];
		eqR[0] = 0;eqR[255] = 255;eqG[0] = 0;eqG[255] = 255;eqB[0] = 0;eqB[255] = 255;
		long acR = red[0];long acG = green[0];long acB = blue[0];
		
		for(int i=1;i<255;i++){
			eqR[i] = (acR * 255) / NM;
			acR += red[i];
			
			eqG[i] = (acG * 255) / NM;
			acG += green[i];
			
			eqB[i] = (acB * 255) / NM;
			acB += blue[i];
		}
		
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height);i++){
			WrittableImageData[i*3] = (int)eqR[WrittableImageData[i*3]];
			WrittableImageData[i*3+1] = (int)eqG[WrittableImageData[i*3+1]];
			WrittableImageData[i*3+2] = (int)eqB[WrittableImageData[i*3+2]];
		}
	}
	
	public void Contrast(float contrast){
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height*3);i++)WrittableImageData[i] = Math.round(contrast*(WrittableImageData[i]-128)+128);
	}
	
	public void Brightness(int bright){
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height*3);i++)WrittableImageData[i]+=bright;
	}
	
	public void Thresholding(int threshold){
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height);i++){
			int grayVal = (WrittableImageData[i*3] + WrittableImageData[i*3+1] + WrittableImageData[i*3+2])/3;
			if(grayVal>threshold){
				WrittableImageData[i*3] = 255;
				WrittableImageData[i*3+1] = 255;
				WrittableImageData[i*3+2] = 255;
			}else{
				WrittableImageData[i*3] = 0;
				WrittableImageData[i*3+1] = 0;
				WrittableImageData[i*3+2] = 0;
			}
		}
	}
	
	public void Rotation(int degree){
		
	}
	
	public void Scale(int sx,int sy,int t){
		int oldW = this.InfoHeader.width;
		int oldH = this.InfoHeader.height;
		
		this.InfoHeader.width *= sx;
		this.InfoHeader.height *= sy;
		int [] nBuff = new int[this.InfoHeader.width*this.InfoHeader.height*3];
		
		//System.out.println(this.InfoHeader.width + " " + this.InfoHeader.height + " size: " + nBuff.length + " " + t);
		int [] RGB = new int[3];
		
		//System.out.println("BEFORE---SCALE");
		//printData();
		
		for(int i=0;i<InfoHeader.height;i++){
			for(int j=0;j<InfoHeader.width;j++){
				
				if(t==0){
					RGB = NearestNeighbor((oldW)/(float)this.InfoHeader.width, (oldH)/(float)this.InfoHeader.height, i, j, oldW);
				}else{
					RGB = BilinearInterpolation((oldW)/(float)this.InfoHeader.width, (oldH)/(float)this.InfoHeader.height, i, j, oldW);
				}
				
				//System.out.println(i+" "+j+"==>"+RGB[0]+"|"+RGB[1]+"|"+RGB[2]);
				
				nBuff[(i*InfoHeader.width+j)*3] = RGB[0];
				nBuff[(i*InfoHeader.width+j)*3+1] = RGB[1];
				nBuff[(i*InfoHeader.width+j)*3+2] = RGB[2];
			}
		}
		
		/*WrittableImageData = null;
		WrittableImageData = new int[this.InfoHeader.width*this.InfoHeader.height*3];*/
		
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height*3);i++)WrittableImageData[i] = nBuff[i];
	}
	
	public void updateBuffers(){
		for(int i=0;i<(this.InfoHeader.width*this.InfoHeader.height*3);i++)ImageData[i] = truncate(WrittableImageData[i]);
	}
	
	public Image getImage(){
		BufferedImage img = new BufferedImage(this.InfoHeader.width, this.InfoHeader.height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = (WritableRaster) img.getData();
	    raster.setPixels(0, 0, this.InfoHeader.width, this.InfoHeader.height, this.ImageData);
	    img.setData(raster);
	    img = resizeImage(img);
	    return SwingFXUtils.toFXImage(img, null);
	}
	
	public BufferedImage resizeImage(BufferedImage originalImage){
        BufferedImage resizedImage = new BufferedImage(520, 520, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 520, 520, null);
        g.dispose();
        return resizedImage;
    }
	
	
	public String cleanAll(String name,String folder){
		String cur = System.getProperty("user.dir");
		if(name.length()>4&&name.substring(name.length()-4).equals(".bmp"))
			return cur.replace("\\", "/")+"/"+folder+"/"+name;
		else 
			return cur.replace("\\", "/")+"/"+folder+"/"+name+".bmp";
	}
	
	public boolean saveBMP(String name, String folder) throws IOException{
		
		String saveplace = cleanAll(name,folder);
		System.out.println(saveplace);
		dos = new DataOutputStream(new FileOutputStream(saveplace));
		
		this.Header.size = this.Header.size - this.InfoHeader.imgsize;
		this.InfoHeader.imgsize = this.InfoHeader.width*this.InfoHeader.height*3;
		
		this.InfoHeader.bpp = 24;
		this.InfoHeader.colors = 0;
		this.InfoHeader.imxtcolors = 0;
		//this.InfoHeader.headersize = 40;
		
		int pad = 4 - ((this.InfoHeader.width*3)%4);
		if(pad!=0 && pad!=4){
			System.out.println("PADDING: " + pad);
			this.InfoHeader.imgsize += pad*this.InfoHeader.height;
		}
		
		//this.Header.offset 	= 54+4;
		//this.Header.size 	= 54+this.InfoHeader.imgsize;
		this.Header.size = this.Header.size + this.InfoHeader.imgsize;
		
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(Short.reverseBytes(this.Header.ID));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer = ByteBuffer.allocate(4);
		buffer.putInt(Integer.reverseBytes(this.Header.size));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer = ByteBuffer.allocate(2);
		buffer.putShort(Short.reverseBytes(this.Header.resv1));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putShort(Short.reverseBytes(this.Header.resv2));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer = ByteBuffer.allocate(4);
		buffer.putInt(Integer.reverseBytes(this.Header.offset));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.headersize));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.width));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.height));
		dos.write(buffer.array());

		buffer.clear();
		buffer = ByteBuffer.allocate(2);
		buffer.putShort(Short.reverseBytes(this.InfoHeader.planos));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer = ByteBuffer.allocate(2);
		buffer.putShort(Short.reverseBytes(this.InfoHeader.bpp));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer = ByteBuffer.allocate(4);
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.compress));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.imgsize));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.bmpx));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.bmpy));
		dos.write(buffer.array());
		
		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.colors));
		dos.write(buffer.array());

		buffer.clear();
		buffer.putInt(Integer.reverseBytes(this.InfoHeader.imxtcolors));
		dos.write(buffer.array());
		
		//System.out.println("AFTER--------------");
		//printData();
		
		dos.write(new byte[this.Header.offset-54]);
		
		for(int i=this.InfoHeader.height-1; i>=0 ;i--){
			for(int j=0; j<this.InfoHeader.width ;j++){
				dos.write(truncate(WrittableImageData[(i*this.InfoHeader.width+j)*3+2]));
				dos.write(truncate(WrittableImageData[(i*this.InfoHeader.width+j)*3+1]));
				dos.write(truncate(WrittableImageData[(i*this.InfoHeader.width+j)*3]));
			}
		}
		dos.close();
		
		return true;
	}
	
}
