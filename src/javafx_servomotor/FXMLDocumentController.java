/*
!+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
!> DMM TECH Servo drive control software
!>
!> Author: Victor Petrov
!> Date: 01/16/2016
!> Version: v0.01
!> 
!> Revisions:
!> (Date) - Author
!>  
!>  
!+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
package javafx_servomotor;

import static com.sun.org.apache.xalan.internal.lib.ExsltMath.power;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javax.xml.bind.DatatypeConverter;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import static sun.util.calendar.CalendarUtils.mod;

/**
 *
 * @author petrov
 */
public class FXMLDocumentController implements Initializable {
    
      @FXML
      private Label ComPortLable;
      @FXML
      private Label Label_MotorSpeed;
      @FXML
      private Label Label_PistonVel  ;
      @FXML
      private Label Label_JetVel;
      @FXML
      private Label Label_ReNum;
      @FXML
      private Label LableStroke;
      @FXML
      private ComboBox<String> ComPorts; // Value injected by FXMLLoader
      @FXML
      private Button ConnectToPort;
      @FXML
      private Button DisconnectFromPort;
      @FXML
      private Button MovePistonBtn;
      @FXML
      private Button RetrivePistonBtn;
      @FXML
      private RadioButton rb01;
      @FXML
      private RadioButton rb02;
      @FXML
      private RadioButton rb03;
      @FXML
      private RadioButton rb04;
      @FXML
      private RadioButton rb05;
      @FXML
      private RadioButton rb06;
      @FXML
      private RadioButton rb07;
      @FXML
      private RadioButton rb08;
      @FXML
      private RadioButton rb09;
      @FXML
      private RadioButton rb10;
      @FXML
      private RadioButton rb11;
      @FXML
      private RadioButton rb12;
      @FXML
      private RadioButton rb13;
      @FXML
      private RadioButton rb14;
      @FXML
      private RadioButton rb15;
      @FXML
      private Slider SliderStroke;
      @FXML
      private RadioButton rb16;
      @FXML
      final ToggleGroup myToggleGroup = new ToggleGroup();
      //@FXML
      //final Slider slider = new Slider();
      
      
      
              //Define number format
        DecimalFormat numb_form = new DecimalFormat("#0.00");
        //Piston diameter
        double d_piston=8*25.4/1000;
        //Jet diameter
        double d_jet=0.5*25.4/1000;  
        //Calculate dimater ratio
        double D_ratio=power(d_piston/d_jet,2);  
        //Linear actuator ball screw
        double Ball_screw=6.35;
        //ServoMotor GearNumber
        double Gear_number = 16384;
        //Gear ratio
        double Gear_ratio = 4096/Gear_number;
        //Fluid properties
        //Density
        double WaterDensity = 997.07;
        //Viscosity
        double WaterVis = 8.9E-4;
      
      
           
    @FXML
    private void SetComPort(ActionEvent event) {
        //Get selected COM port and add it to the lable text
        String output = (String) ComPorts.getSelectionModel().getSelectedItem().toString(); 
        ComPortLable.setText("Selected port: "+output);
        
        
    }
    
    
    @FXML
    private void MovePistonAct(ActionEvent event) {
        //Make buttom desable
            
    }
    
    
    @FXML
    private void RetrivePistonAct(ActionEvent event) {
        //Make buttom desable
            RetrivePistonBtn.setDisable(true);
            MovePistonBtn.setDisable(false);
    }
    
    //initial slider position
    double SliderPos=0;
    
    @FXML
    public void SliderStrokeAct(ActionEvent event) {
        
        double Slider_value = SliderStroke.getValue();
        LableStroke.setText("Requested stroke: "+Slider_value+ " mm");
        
    }
    
    
    @FXML
    private void myToggleGroupAct(ActionEvent event) {

       
        //Define constructor to check which radio button is selected
        GroupButtonUtils CallBtn = new GroupButtonUtils();  
        String str = CallBtn.RadioSelected();
        
        //Get MaxSpeed value from selected radio button
        int MaxSpd = Integer.parseInt(CallBtn.RadioSelected());
        //Calculate max motor rpm
        double Max_motor_rpm=(MaxSpd+3)*(MaxSpd+3)*12.21*Gear_ratio/16;
        //Motor speed (revolutions per second)
        double Max_motor_rps = Max_motor_rpm/60;
        //piston speed (millimetres per second)
        double Piston_Max_speed = Max_motor_rps*Ball_screw;
        //Jet velocity (meters per second)
        double Jet_velocity = Piston_Max_speed*D_ratio/1000;
        //Jet Re number
        double Jet_Re=WaterDensity*Jet_velocity*d_jet/WaterVis;

        //Set lables values for: Motor speed, Piston velocity, Jet velocity and Re number 
        Label_MotorSpeed.setText("Motor speed = " +numb_form.format(Max_motor_rpm)+" rpm");
        Label_PistonVel.setText("Piston velocity = " +numb_form.format(Piston_Max_speed)+" mm/s");
        Label_JetVel.setText("Jet velocity = " +numb_form.format(Jet_velocity)+" m/s");
        Label_ReNum.setText("Re = "+numb_form.format(Jet_Re));
        //enable button
        MovePistonBtn.setDisable(false);
        
        
        
        
        
    }
  
    @FXML
    private void ConnectToPort(ActionEvent event) {
        //Process connection buttom
        //Get selected value
        String output = (String) ComPorts.getSelectionModel().getSelectedItem().toString(); 
        //Set serial port
        serialPort = new SerialPort(output); 
        try {
            //Open selected port
            serialPort.openPort();//Open port
            //Define port parameters
            serialPort.setParams(38400, 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
            //Change COM port lable to connected
            ComPortLable.setText("Connected to port: "+output);
            //Make buttom desable
            ComPorts.setDisable(true);
            //Make ComboBos desable
            ConnectToPort.setDisable(true);
            //Make disconnect buttom enable
            DisconnectFromPort.setDisable(false);
            
            for (Toggle t : myToggleGroup.getToggles()) {  
                    if (t instanceof RadioButton) {  
                        ((RadioButton) t).setDisable(false);  
                        }  
                    }  
            SliderStroke.setDisable(false);

            }
        catch (SerialPortException ex) 
                    {
                        //Convert ex to line using toString()and split it using delimeters.
                        String delims = "[-;]+";
                        //Process output message
                        String[] tokens = ex.toString().split(delims);
                        //System.out.println(tokens[1]+" "+tokens[5]);
                        ComPortLable.setText(tokens[1]+" "+tokens[5]);
                    }
    }
     
     @FXML
     private void DisconnectFromPort(ActionEvent event) {
         try {
        serialPort.closePort();//Close serial port
        for (Toggle t : myToggleGroup.getToggles()) {  
                    if (t instanceof RadioButton) {  
                        ((RadioButton) t).setDisable(true);  
                        }  
                    }  
         }
          catch (SerialPortException ex) {
                        //Convert ex to line using toString()and split it using delimeters.
                        String delims = "[-;]+";
                        String[] tokens = ex.toString().split(delims);
                        System.out.println(tokens[1]+" "+tokens[5]);
        }
        ConnectToPort.setDisable(false);
        ComPorts.setDisable(false);
        DisconnectFromPort.setDisable(true);
        String output = (String) ComPorts.getSelectionModel().getSelectedItem().toString(); 
        ComPortLable.setText("Selected port: "+output);
        MovePistonBtn.setDisable(true);
        
 
    }
    
    
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        //Get available portnames  
        String[] portNames = SerialPortList.getPortNames();
           //Clear ComPorts combobox         
           ComPorts.getItems().clear();
           //Set ComPorts list to variable portNames  
           ComPorts.getItems().addAll(portNames);
           ComPorts.getSelectionModel().selectFirst();
           DisconnectFromPort.setDisable(true);
           
           MovePistonBtn.setDisable(true);
        
                rb01.setToggleGroup(myToggleGroup);
                rb02.setToggleGroup(myToggleGroup);
                rb03.setToggleGroup(myToggleGroup);
                rb04.setToggleGroup(myToggleGroup);
                rb05.setToggleGroup(myToggleGroup);
                rb06.setToggleGroup(myToggleGroup);
                rb07.setToggleGroup(myToggleGroup);
                rb08.setToggleGroup(myToggleGroup);
                rb09.setToggleGroup(myToggleGroup);
                rb10.setToggleGroup(myToggleGroup);
                rb11.setToggleGroup(myToggleGroup);
                rb12.setToggleGroup(myToggleGroup);
                rb13.setToggleGroup(myToggleGroup);
                rb14.setToggleGroup(myToggleGroup);
                rb15.setToggleGroup(myToggleGroup);
                rb16.setToggleGroup(myToggleGroup);
                //SliderStroke.setText("Selected port: ");
                
                
                //Disable radio buttons
                RetrivePistonBtn.setDisable(true);
                
                        for (Toggle t : myToggleGroup.getToggles()) {  
                    if (t instanceof RadioButton) {  
                        ((RadioButton) t).setDisable(true);  
                        }  
                    }
                    SliderStroke.setDisable(true);
      /*         
    SliderStroke.setShowTickMarks(true);
    SliderStroke.setShowTickLabels(true);
    SliderStroke.setMajorTickUnit(25.0f);
    SliderStroke.setMinorTickCount(4);
    SliderStroke.setBlockIncrement(5f);
    SliderStroke.setSnapToTicks(true);
        */        
      
      
      
      
      
      
                
                SliderStroke.valueProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            
            LableStroke.setText("Stroke length : " + newValue.intValue() + " mm");
            SliderPos=newValue.intValue();
        }
        
        });
        
                
            //change properties after button pressed    
            MovePistonBtn.setOnAction(new EventHandler<ActionEvent>() {  
            @Override  
            public void handle(ActionEvent event) {  
                        SliderStroke.setDisable(true);
                        MovePistonBtn.setDisable(true);
                        RetrivePistonBtn.setDisable(false);
    
                for (Toggle t : myToggleGroup.getToggles()) {  
                    if (t instanceof RadioButton) {  
                        ((RadioButton) t).setDisable(true);  
                        }  
                    }  
                
                
                
                
                
                 try {
                        //Setcurrent position as 0
                        int motor_ID =0;               
                        int ZeroPosition =0x00;
                        serialPort.writeBytes(SendToMotor(motor_ID,ZeroPosition,ZeroPosition));

                     
                     
       //Max speed bit 0x14
       int Set_HighSpeed =0x14;
       //Max speed value
       //Define constructor to check which radio button is selected
       GroupButtonUtils CallBtn = new GroupButtonUtils();  
       String str = CallBtn.RadioSelected();
       int MaxSpd = Integer.parseInt(CallBtn.RadioSelected());
  
  
       //check current position
      int[] CurrentPos = {0x0e,0x1b};
      
      
      
       serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));//Write data to port
       
       

       
       
                     serialPort.writeBytes(SendToMotor(motor_ID,Set_HighSpeed,MaxSpd));//Write data to port
                     
                     
                     //move to 65535
                     int Position=(int)((SliderPos/Ball_screw)*65535);
                                                             
                     
                     serialPort.writeBytes(SendToMotor(motor_ID,0x01,Position));//Write data to port
                         
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                
                }  
            });  
            
            
            
            
             //change properties after button pressed    
            RetrivePistonBtn.setOnAction(new EventHandler<ActionEvent>() {  
            @Override  
            public void handle(ActionEvent event) {  
                        SliderStroke.setDisable(false);
                        MovePistonBtn.setDisable(false);
                        RetrivePistonBtn.setDisable(true);
    
                for (Toggle t : myToggleGroup.getToggles()) {  
                    if (t instanceof RadioButton) {  
                        ((RadioButton) t).setDisable(false);  
                        }  
                    }  
                  int motor_ID =0;
                try {
                     //check current position
                    int[] CurrentPos = {0x0e,0x1b};
                    serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));//Write data to port
                    
                    
                    //Write data to port
                    //limit retrive speed to MaxSpd = 7;
                    int Set_HighSpeed =0x14;
                    int MaxSpd = 7;
                    serialPort.writeBytes(SendToMotor(motor_ID,Set_HighSpeed,MaxSpd));
                    
                    
                    //Move motor back to 0
                    serialPort.writeBytes(SendToMotor(motor_ID,0x01,0));
                    
                    
                } catch (SerialPortException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                
                
                
                }  
            
            });  
            
            
            
            
            
            
            
            
            
            
            
               
    }    
    
    static SerialPort serialPort;
    
    static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){//If data is available
                
                    try {
                        byte buffer[] = serialPort.readBytes();
                    System.out.println((buffer).length);

                    for (int i = 0; i < buffer.length; i++) {
           String s1 = String.format("%8s", Integer.toBinaryString(buffer[i] & 0xFF)).replace(' ', '0');
           System.out.print(s1+" "); 
	}
                    
                    
                         System.out.println(Arrays.toString(buffer));
  
                        
                         //serialPort.writeBytes("This is a test string".getBytes());//Write data to port
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                
            }
            
            
        }
    }
    
    public class GroupButtonUtils
    {
    public  String RadioSelected()
        {
            if(rb01.isSelected())
                {
                    return rb01.getText();
                }
            if(rb02.isSelected())
                {
                return rb02.getText();
                }
            if(rb03.isSelected())
                {
                    return rb03.getText();
                }
            if(rb04.isSelected())
                {
                return rb04.getText();
                }
            if(rb05.isSelected())
                {
                return rb05.getText();
                }
            if(rb06.isSelected())
                {
                return rb06.getText();
                }
            if(rb07.isSelected())
                {
                return rb07.getText();
                }
            if(rb08.isSelected())
                {
                return rb08.getText();
                }
            if(rb09.isSelected())
                {
                return rb09.getText();
                }
            if(rb10.isSelected())
                {
                return rb10.getText();
                }
            if(rb11.isSelected())
                {
                return rb11.getText();
                }
            if(rb12.isSelected())
                {
                return rb12.getText();
                }
            if(rb13.isSelected())
                {
                return rb13.getText();
                }
            if(rb14.isSelected())
                {
                return rb14.getText();
                }
            if(rb15.isSelected())
                {
                return rb15.getText();
                }
            if(rb16.isSelected())
                {
                return rb16.getText();
                }
            return null;
        }
    }
    
    
      
        
    
        
    
    
    
    public  static byte[] SendToMotor(int motor_ID, int UsrFunction,int ParamValue)
{
    
int[] Bt = new int[7];
//define 0 byte
Bt[0] = motor_ID&0x7f;

Bt[1] = Bt[2] = Bt[3] = Bt[4] = Bt[5] = Bt[6] = (char)0x80;
    
int Package_Length;
    if (ParamValue > 1048575 || ParamValue <-1048576)
    {
        Package_Length = 7;
                Bt[5]+=ParamValue& 0x7F;
                ParamValue=ParamValue>>7;
                Bt[4]+=ParamValue& 0x7F;
                ParamValue=ParamValue>>7;
                Bt[3]+=ParamValue& 0x7F;
                ParamValue=ParamValue>>7;
                if (ParamValue>=0){
                Bt[2]+=ParamValue& 0x3F;}
                else{Bt[2]+=ParamValue& 0x7F;}
    }
    else if (ParamValue > 8191 || ParamValue <-8192)
    {
        Package_Length = 6;

                Bt[4]+=ParamValue& 0x7F;
                ParamValue=ParamValue>>7;
                Bt[3]+=ParamValue& 0x7F;
                ParamValue=ParamValue>>7;
               if (ParamValue>=0){
                Bt[2]+=ParamValue& 0x3F;}
                else{Bt[2]+=ParamValue& 0x7F;}
    }
    else if (ParamValue > 63 || ParamValue <-64)
    {
        Package_Length = 5;

                Bt[3]+=ParamValue& 0x7F;
                ParamValue=ParamValue>>7;
               if (ParamValue>=0){
                Bt[2]+=ParamValue& 0x3F;}
                else{Bt[2]+=ParamValue& 0x7F;}
    }
    else     
    {
        Package_Length = 4;

       if (ParamValue>=0){
                Bt[2]+=ParamValue& 0x3F;}
                else{Bt[2]+=ParamValue& 0x7F;}
    }
    //Calculate byte 1 considering total nymber of bytes

Bt[1] += (Package_Length-4)*32 + UsrFunction;
byte sum=0;
for (int i =0; i <= Package_Length-2; i++) {
           sum+=(byte) (Bt[i]);
            }
Bt[Package_Length-1]+= (char)(mod(sum,128));
        byte[] Bt_fin= new byte[Package_Length];
        for (int i = 0; i < Package_Length; i++) {
           Bt_fin[i]=(byte) Bt[i];
           
           }
           
            return Bt_fin;
}
    
    
    
}
