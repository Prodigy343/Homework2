package application;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

public class MainController implements Initializable{

	@FXML private StackPane DnDListener;
	@FXML private ImageView img;
	@FXML private LineChart<String, Number> chartHistogram;
	@FXML private Label Bpp;
	@FXML private Label FileSize;
	@FXML private Label Dimension;
	@FXML private Label FileName;
	@FXML private TextField threshold;
	@FXML private TextField bright;
	@FXML private TextField contrast;
	@FXML private TextField degree;
	@FXML private TextField outBMP;
	@FXML private TextField scaleX;
	@FXML private TextField scaleY;
	@FXML private RadioButton Nearest;
	@FXML private RadioButton Bilinear;
	@FXML private ToggleGroup group;

	private CategoryAxis xAxis;
    private NumberAxis yAxis;
	private String filePath;
	private BMPFile bmp;
	private boolean imgd,init;
	private int type; // 0 - Nearest | 1 - Billinear
	
	@SuppressWarnings("rawtypes")
	private XYChart.Series sRed;
	
	@SuppressWarnings("rawtypes")
	private XYChart.Series sGreen;
	
	@SuppressWarnings("rawtypes")
	private XYChart.Series sBlue;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		filePath = null;
		bmp = new BMPFile();
		imgd = false;
		xAxis = new CategoryAxis();
	    yAxis = new NumberAxis();
	    LineChart<String, Number> chartHistogram = new LineChart<>(xAxis, yAxis);
	    chartHistogram.setCreateSymbols(false);
	    sRed = sGreen = sBlue = new XYChart.Series();
	    init = false;
	    type = 0;
	    
	    group = new ToggleGroup();
	    
		Nearest = new RadioButton("Nearest");
		Nearest.setToggleGroup(group);
		Nearest.setSelected(true);

		Bilinear = new RadioButton("Bilinear");
		Bilinear.setToggleGroup(group);
	}
	
	public void dragOver(DragEvent event) {
	    Dragboard db = event.getDragboard();
	    if (db.hasFiles()) event.acceptTransferModes(TransferMode.COPY);
	    else event.consume();
	}
	
	public String clean(String x){
		return "file:"+x.replace("\\", "/");
	}
	
	public void dragDropped(DragEvent event) throws IOException {
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			success = true;
			for (File file:db.getFiles())filePath = file.getAbsolutePath();
		}
		displaysome();
		event.setDropCompleted(success);
		event.consume();
	}
	
	public void displaysome() throws IOException{
		if(filePath.substring(filePath.length()-3).equals("bmp")){
			bmp = new BMPFile(filePath);
			img.setImage(bmp.getImage());
			bmp.updateBuffers();
			bmp.updateSeries();
			updateHistogram();
			updateLabelsInfo();
			imgd = true;
		}else
			JOptionPane.showMessageDialog(null,"Please drop a BMP File");
	}
	
	public void selectNearest(ActionEvent e){
		type = 0;
	}
	
	public void selectBilinear(ActionEvent e){
		type = 1;
	}
	
	public void Negative(ActionEvent e){
		System.out.println(type);
		if(imgd){
			bmp.Negative();
			bmp.updateBuffers();
			bmp.updateSeries();
			img.setImage(bmp.getImage());
			updateHistogram();
		}
	}
	
	public void Brightness(ActionEvent e){
		if(imgd){
			bmp.Brightness(Integer.parseInt(bright.getText()));
			bmp.updateBuffers();
			bmp.updateSeries();
			img.setImage(bmp.getImage());
			updateHistogram();
		}
	}
	
	public void Contrast(ActionEvent e){
		if(imgd){
			bmp.Contrast(Float.parseFloat(contrast.getText()));
			bmp.updateBuffers();
			bmp.updateSeries();
			img.setImage(bmp.getImage());
			updateHistogram();
		}
	}
	
	public void Thresholding(ActionEvent e){
		if(imgd){
			int th = Integer.parseInt(threshold.getText());
			if(th >= 0 && th < 256) {
				bmp.Thresholding(th);
				bmp.updateBuffers();
				bmp.updateSeries();
				img.setImage(bmp.getImage());
				updateHistogram();
			}else{
				JOptionPane.showMessageDialog(null,"Threshold range [0 - 255]");
			}
		}
	}
	
	public void Rotation(ActionEvent e){
		if(imgd){
			bmp.Rotation(Integer.parseInt(degree.getText()));
			bmp.updateBuffers();
			bmp.updateSeries();
			img.setImage(bmp.getImage());
		}
	}
	
	public void Scale(ActionEvent e){
		if(imgd){
			bmp.Scale(Integer.parseInt(scaleX.getText()),Integer.parseInt(scaleY.getText()),type);
			bmp.updateBuffers();
			bmp.updateSeries();
			updateLabelsInfo();
		}
	}
	
	public void ReflectionX(ActionEvent e){
		if(imgd){
			bmp.ReflecX();
			bmp.updateBuffers();
			img.setImage(bmp.getImage());
		}
	}
	
	public void ReflectionY(ActionEvent e){
		if(imgd){
			bmp.ReflecY();
			bmp.updateBuffers();
			img.setImage(bmp.getImage());
		}
	}
	
	public void equalization(ActionEvent e){
		if(imgd){
			bmp.Equal();
			bmp.updateSeries();
			bmp.updateBuffers();
			img.setImage(bmp.getImage());
			updateHistogram();
		}
	}
	
	public void updateLabelsInfo(){
		int i = bmp.path.length()-4;
		for(; i>=0;i--)if(bmp.path.charAt(i)=='\\')break;
		FileName.setText(bmp.path.substring(i+1));
		FileSize.setText(Integer.toString(bmp.Header.size) + " Bytes");
		Bpp.setText(Short.toString(bmp.InfoHeader.bpp) + " bpp");
		Dimension.setText(Integer.toString(bmp.InfoHeader.height) + "px x " + Integer.toString(bmp.InfoHeader.width) + "px");
	}
	
	public void saveBMP(ActionEvent e) throws HeadlessException, IOException{
		if(imgd){
			String dir ="BMPFilesSaved";
			if(outBMP.getText().length()==0){
				JOptionPane.showMessageDialog(null,"Please insert a name");
				return ;
			}
			
			if(!init){
				if(!new File(System.getProperty("user.dir")+"\\"+dir).exists()){
					boolean success = (new File(dir)).mkdir();
					if (success) {
						init = true;
					}else{
						JOptionPane.showMessageDialog(null,"ERROR: Creating folder");
						return ;
					}
				}
			}
			
			if(bmp.saveBMP(outBMP.getText(),dir)){
				JOptionPane.showMessageDialog(null,"BMP saved successfully");
			}else{
				JOptionPane.showMessageDialog(null,"ERROR: writting bmp in 'save'");
			}
		}
			
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateHistogram(){
		chartHistogram.getData().clear();
		chartHistogram.setCreateSymbols(false);
		sRed = new XYChart.Series();
	    sGreen = new XYChart.Series();
	    sBlue = new XYChart.Series();
		//bmp.updateSeries();
		
		for(int i=0;i<256;i++){
			sRed.getData().add(new XYChart.Data(String.valueOf(i), bmp.red[i]));
			sGreen.getData().add(new XYChart.Data(String.valueOf(i), bmp.green[i]));
			sBlue.getData().add(new XYChart.Data(String.valueOf(i), bmp.blue[i]));
		}
		
		sRed.setName("red");
        sGreen.setName("green             ");
        sBlue.setName("blue");
        
		chartHistogram.getData().addAll(sRed,sGreen,sBlue);
	}
	
}
