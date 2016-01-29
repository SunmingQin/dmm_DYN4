/*
!+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
!> DMM TECH Servo drive control software
!>
!> Author: Victor Petrov
!> Date: 01/16/2016
!> Version: v0.02
!> 
!> Revisions:
!> (Date) - Author
!>  
!>  
!+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
package javafx_servomotor;


import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;


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
      private Label Label_MotorSpeed1;
      @FXML
      private Label Label_PistonVel  ;
      @FXML
      private Label Label_JetVel;
      @FXML
      private Label Label_ReNum;
       @FXML
      private Label Label_PistonVel1  ;
      @FXML
      private Label Label_JetVel1;
      @FXML
      private Label Label_ReNum1;
      @FXML
      private Label LableStroke;
      @FXML
      private Label LableStroke1;
      @FXML
      private static Label crtpos;
      @FXML
      private static Label crtpos1;
      @FXML
      private static Label crtpos2;
      
      static int[] return_data_st = new int[2];
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
      private Button MovePistonBtn1;
      @FXML
      private Button RetrivePistonBtn1;
      @FXML
      private Button ccw_move;
      @FXML
      private Button cw_move;
      @FXML
      private Button reset_pos;
      @FXML
      private Button stop;
      @FXML
      private Button stop1;
      @FXML
      private Button stop2;
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
      private Slider SliderStroke1;
      @FXML
      private Slider rpmSlider;
      @FXML
      private RadioButton rb16;
      @FXML
      final ToggleGroup myToggleGroup = new ToggleGroup();
      @FXML
      private Tab tab1;
      @FXML
      private Tab tab2;
      @FXML
      private Tab tab3;
      
      static private volatile boolean killMe=false;
              
        //Define number format
        DecimalFormat numb_form = new DecimalFormat("#0.00");
        //Define number format
        DecimalFormat numb_form1 = new DecimalFormat("#00");
        //Piston diameter
        double d_piston=8*25.4/1000;
        //Jet diameter
        double d_jet=0.5*25.4/1000;  
        //Calculate dimater ratio
        double D_ratio=Math.pow(d_piston/d_jet,2);  
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
        
        //initial slider position
        double SliderPos=0;
        double SliderPos1=0;
        double SliderRpm=0;
      //Global variables
      static int[] return_data =new int[2];
      public static volatile int[] MY_INT = new int[3];
 //===============================================================================================          
    @FXML
        private void SetComPort(ActionEvent event) {
        //Get selected COM port and add it to the lable text
        String output = (String) ComPorts.getSelectionModel().getSelectedItem(); 
        ComPortLable.setText("Selected port: "+output);
    }
//===============================================================================================    
    @FXML
    private void ConnectToPort(ActionEvent event) {
        //Process connection buttom
        //Get selected value
        String output = (String) ComPorts.getSelectionModel().getSelectedItem(); 
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
            //Enable tabs
             tab1.setDisable(false);
             tab2.setDisable(false);
             tab3.setDisable(false);
             stop.setDisable(false);
             //Check position
             int[] position={0,0x0e,0x1b};                   
             serialPort.writeBytes(SendToMotor(position[0],position[1],position[2]));//Write data to port
            
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
//===============================================================================================     
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
        String output = (String) ComPorts.getSelectionModel().getSelectedItem(); 
        ComPortLable.setText("Selected port: "+output);
        MovePistonBtn.setDisable(true);
        SliderStroke.setDisable(true);
        tab1.setDisable(true);
        tab2.setDisable(true);
        tab3.setDisable(true);
        stop.setDisable(true);
    }
//===============================================================================================
    @FXML
    private void stop(ActionEvent event) throws SerialPortException {
         //Stop command
       int[] Stop = {0,3,0};
       serialPort.writeBytes(SendToMotor(Stop[0],Stop[1],Stop[2]));//Write data to port
       int[] position={0,0x0e,0x1b};                   
       serialPort.writeBytes(SendToMotor(position[0],position[1],position[2]));//Write data to port
       killMe=true;
       tab1.setDisable(false);
       tab2.setDisable(false);
       tab3.setDisable(false);
                                    crtpos.setText(" "+MY_INT[1]);
                             crtpos1.setText(" "+MY_INT[1]);
                             crtpos2.setText(" "+MY_INT[1]);
       
        }
//===============================================================================================    
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
//===============================================================================================    
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

                                RetrivePistonBtn.setDisable(true);
                //Disable radio buttons
                        for (Toggle t : myToggleGroup.getToggles()) {  
                    if (t instanceof RadioButton) {  
                        ((RadioButton) t).setDisable(true);  
                        }  
                    }
                    SliderStroke.setDisable(true);
                    tab1.setDisable(true);
                    tab2.setDisable(true);
                    tab3.setDisable(true);
                    stop.setDisable(true);
      
        //Listener toget stroke slider value and indicate it on the screen
        SliderStroke.valueProperty().addListener(new ChangeListener<Number>() 
        {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            LableStroke.setText("Stroke length : " + newValue.intValue() + " mm");
            SliderPos=newValue.intValue();
        }
        });
        
        //Listener toget stroke slider value and indicate it on the screen
        SliderStroke1.valueProperty().addListener(new ChangeListener<Number>() 
        {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            LableStroke1.setText("Stroke length : " + newValue.intValue() + " mm");
            SliderPos1=newValue.intValue();
        }
        });
        
        
        //Listener toget rpm slider value and indicate it on the screen
        rpmSlider.valueProperty().addListener(new ChangeListener<Number>() 
        {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            Label_MotorSpeed1.setText("Motor speed  = " + numb_form1.format(newValue.intValue()) + " rpm");
            SliderRpm=newValue.intValue();
            
             //piston speed (millimetres per second)
        double Piston_Max_speed1 = (SliderRpm/60)*Ball_screw;
        //Jet velocity (meters per second)
        double Jet_velocity1 = Piston_Max_speed1*D_ratio/1000;
        //Jet Re number
        double Jet_Re1=WaterDensity*Jet_velocity1*d_jet/WaterVis;

        //Set lables values for: Motor speed, Piston velocity, Jet velocity and Re number 
     
        Label_PistonVel1.setText("Piston velocity = " +numb_form.format(Piston_Max_speed1)+" mm/s");
        Label_JetVel1.setText("Jet velocity = " +numb_form.format(Jet_velocity1)+" m/s");
        Label_ReNum1.setText("Re = "+numb_form.format(Jet_Re1));
        }
        });
        //change properties after button pressed    
        MovePistonBtn.setOnAction(new EventHandler<ActionEvent>() {  
        
        @Override  
        public void handle(ActionEvent event) {  
                        SliderStroke.setDisable(true);
                        MovePistonBtn.setDisable(true);
                        RetrivePistonBtn.setDisable(false);
                        tab2.setDisable(true);
                        tab3.setDisable(true);
    
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
                     //move to requested travel 
                     int Position=(int)((SliderPos/Ball_screw)*65535);
                     serialPort.writeBytes(SendToMotor(motor_ID,0x01,Position));//Write data to port
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }  
            });  
        
        //Retrive piston hange properties after button pressed    
        RetrivePistonBtn.setOnAction(new EventHandler<ActionEvent>() {  
        @Override  
        public void handle(ActionEvent event) {  
                        SliderStroke.setDisable(false);
                        MovePistonBtn.setDisable(false);
                        RetrivePistonBtn.setDisable(true);
                        tab2.setDisable(false);
                        tab3.setDisable(false);
    
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
            
        //Move piston with selected rpm
        MovePistonBtn1.setOnAction(new EventHandler<ActionEvent>() {  
        @Override  
        public void handle(ActionEvent event) {  
                        SliderStroke.setDisable(true);
                        MovePistonBtn.setDisable(true);
                        RetrivePistonBtn.setDisable(false);
                        tab1.setDisable(true);
                        tab3.setDisable(true);
                        
                        int motor_ID =0;
                     //move with selected rpm
                     int rpm=(int)(SliderRpm);
                    int rpm_command =0x0a;
                    int[] CurrentPos = {0x0e,0x1b};
            try {
                           
                       
        
            
                //Setcurrent position as 0
                        int ZeroPosition =0x00;
                        serialPort.writeBytes(SendToMotor(motor_ID,ZeroPosition,ZeroPosition));
                        
                        
                serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));
                try {
                                TimeUnit.MILLISECONDS.sleep(5);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                int current_pos = MY_INT[1];
                System.out.println("current_pos "+ current_pos);
                int Distination_Position=(int)((SliderPos1/Ball_screw)*65535)+current_pos;
                System.out.println("Distination_Position "+ Distination_Position);
                //For return
                MY_INT[2]=current_pos;
                serialPort.writeBytes(SendToMotor(motor_ID,rpm_command,rpm));
                
                //Run runnable class for position motion (see the end of the file)
                Thread t = new Thread(new ShotTask(Distination_Position));
                t.start();    
                }
             catch (SerialPortException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                    }
                });
            
        RetrivePistonBtn1.setOnAction(new EventHandler<ActionEvent>() {  
            
         @Override  
        public void handle(ActionEvent event) {  
                        
                     int motor_ID =0;
                     
                     //move with selected rpm
                     int rpm=(int)(SliderRpm);
                    int rpm_command =0x0a;
                    int[] CurrentPos = {0x0e,0x1b};
            try {
                serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));
                try {
                                TimeUnit.MILLISECONDS.sleep(10);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                int current_pos1 = MY_INT[1];
                int Distination_Position1 = (current_pos1-MY_INT[2])*-1;
                
                System.out.println("current_pos1 "+ current_pos1);
                
                System.out.println("Distination_Position1 "+ Distination_Position1);
                
                //Setcurrent position as 0
                        int ZeroPosition =0x00;
                        serialPort.writeBytes(SendToMotor(motor_ID,ZeroPosition,ZeroPosition));
                        
                        //Write data to port
                    //limit retrive speed to MaxSpd = 7;
                    int Set_HighSpeed =0x14;
                    int MaxSpd = 7;
                    serialPort.writeBytes(SendToMotor(motor_ID,Set_HighSpeed,MaxSpd));
                 serialPort.writeBytes(SendToMotor(motor_ID,0x01,Distination_Position1));//Write data to port
                }
             catch (SerialPortException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                    }
                });
        //move piston 1mm
        ccw_move.setOnAction(new EventHandler<ActionEvent>() {  
        @Override  
        public void handle(ActionEvent event) {  
                       
                  int motor_ID =0;
                try {
                    
                    //check current position
                    int[] CurrentPos = {0x0e,0x1b};

                    serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));//Write data to port
                    
                    //limit retrive speed to MaxSpd = 7;
                    int Set_HighSpeed =0x14;
                    int MaxSpd = 7;
                    serialPort.writeBytes(SendToMotor(motor_ID,Set_HighSpeed,MaxSpd));
                    
                    //Move motor for 1mm calculated by ballscrew value
                    serialPort.writeBytes(SendToMotor(motor_ID,0x03,(int)(65535/Ball_screw)));
                    //add after positioning check
                    
                } catch (SerialPortException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                }  
            });  
        
        //move piston -1mm
        cw_move.setOnAction(new EventHandler<ActionEvent>() {  
        @Override  
        public void handle(ActionEvent event) {  
                       
                  int motor_ID =0;
                try {
                    //check current position
                    int[] CurrentPos = {0x0e,0x1b};

                    serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));//Write data to port
                    
                    //limit retrive speed to MaxSpd = 7;
                    int Set_HighSpeed =0x14;
                    int MaxSpd = 7;
                    serialPort.writeBytes(SendToMotor(motor_ID,Set_HighSpeed,MaxSpd));
                    
                    //Move motor for 1mm calculated by ballscrew value
                    serialPort.writeBytes(SendToMotor(motor_ID,0x03,(int)(-65535/Ball_screw)));
                    //add after positioning check
                    
                } catch (SerialPortException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                }  
            });  
            
        //reset encoder position 
        reset_pos.setOnAction(new EventHandler<ActionEvent>() {  
        @Override  
        public void handle(ActionEvent event) {  
                       
                  int motor_ID =0;
                try {
                    
                    //check current position
                    int[] CurrentPos = {0x00,0x00};

                    serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));//Write data to port
                    
                    //check position
                    serialPort.writeBytes(SendToMotor(motor_ID,0x0e,0x1b));
                    
                } catch (SerialPortException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                }  
            });  
    }    
//===============================================================================================    
    public static SerialPort serialPort;
    static class SerialPortReader implements SerialPortEventListener {
        //Define output byte array
            byte[] out_line = new byte[7];
            //Counter for bytes
            int j=0;
            //Default trigger value
            boolean byte_triger=false;
            //Innitial sum value
            byte sum=0;
            //Receved parameter value
             int value;
            
             //Pakage length 
             int pakage_length;
             //Trigger to reset J counter when check summ is met
             boolean jtr=false;
            @Override
       public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){//If data is available
 try {
        byte buffer[] = serialPort.readBytes();
        //For each byte in receved data (if recived data contain more then 1 byte it will be splited
    for (int i = 0; i < buffer.length; i++) {
        //if receved byte is equal to 0
        if (buffer[i]==0){
                
                //change trigger value 
                byte_triger=!byte_triger;
                //clear counter
                //j=0;
                        }
        //perform action if trigger is "true"
        if (byte_triger==true){
            //Fill output array with bytes
            out_line[j]=buffer[i]; 
                    //Chek if sum of previusly recived bytes is equal to last recived byte
                    if (sum==out_line[j] && out_line[j]!=0){
                        

                        //System.out.println("Right sum "+"j="+j);
                        byte_triger=!byte_triger;
                        //set pakage length
                        pakage_length=j+1;
                        //System.out.println("pakage_length "+ pakage_length + " j= "+j);
                         //Innitial Param value
                        byte[] Param = new byte[pakage_length];
                        //copy one array in to another
                System.arraycopy(out_line, 0, Param, 0, pakage_length);
                    
                        return_data = ProcBytes(Param);
                        //pass data to global variables
                        MY_INT[0]=return_data[0];
                        MY_INT[1]=return_data[1];
                        //If receved UserFunction is equal to 27 (position) send it to main window label.
                        if (return_data[0]==27)
                                {
                        Platform.runLater(new Runnable() {
                        @Override 
                        public void run() {
                             crtpos.setText(" "+MY_INT[1]);
                            crtpos1.setText(" "+MY_INT[1]);
                             crtpos2.setText(" "+MY_INT[1]);
                                                    }
                                                         });
                                }
                        
                        jtr=!jtr;
                    }

                    //start to summ bytes 
                    //Sum with previus byte but substracting MSB before summation
                    sum+=(byte) (out_line[j]-(char)0x80);    
                    //make sure that MSB is 0
                    sum= (byte) (sum & 0x7F);
                    //Add MSB to finall sum
                    sum+=(char)0x80;    
                    if (jtr==true){
                        j=0;
                        sum=0;
                        jtr=!jtr;
                                  }
                            else {
                            //Add to counter
                            j=j+1;}
                                          }
                               }
       
    }
    catch (SerialPortException ex) {
        System.out.println(ex);
    }
                    //    System.out.println("number " + j);
                
            }
            
        }
    }
//===============================================================================================        
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
//===============================================================================================        
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
Bt[Package_Length-1]+= (char)((sum & 0x7F));
        byte[] Bt_fin= new byte[Package_Length];
        for (int i = 0; i < Package_Length; i++) {
           Bt_fin[i]=(byte) Bt[i];
           }
        return Bt_fin;
}
//===============================================================================================        
    private  static int[] ProcBytes(byte[] values1 ) {
    
    //Receved parameter value
             int value;
    
                        String s1 = String.format("%6s", Integer.toBinaryString(values1[2]&0x3F)).replace(' ', '0');
                        //Calculate parameter value
                        for (int l = 1; l < values1.length-3; l++) 
                                        {
                                            s1+=String.format("%7s", Integer.toBinaryString(values1[l+2]&0x7F)).replace(' ', '0');
                                        }
                        
                        //verefy value sign byt checking bin 6 in third byte
                        if ((values1[2]&0x40) == 0x40)
                                {
                                 //if sign is negative calculate param value using 2 compliment
                                 //value=binary value-1 and then swap zerows and ones 
                                 //Considering all bytes befor check sum byte   
                                 
                                if (Integer.parseInt(s1,2)==0)
                                    //if bit 7 in byte 3 is the only bit 
                                    //then it should be considered for such values like -64,-8192,-1048576,-134217728
                                    {
                                    s1="1"+s1;
                                    }
                                    
                                value= (Integer.parseInt(s1,2));
                                int length = 32-Integer.toBinaryString(value).length();
                                value= ~(value);
                                //previus command will use 32 bit this will cause  few extra ones 
                                value= Integer.parseInt(Integer.toBinaryString(value).substring(length),2);
                                //set negative sign
                                value=(value+1)*(-1);
                                //System.out.println("Negative "+ value);
                                    long end_time = System.nanoTime();


                                }
                        else 
                        {
                            value= (Integer.parseInt(s1,2));
                            //System.out.println("Positive "+ value);
                        }
    //check received command 
    int value1;
    value1=values1[1] & 0x7F;
    value1=value1-((values1.length-4)*32);
    //System.out.println("command "+value1);
    int[] output_value ={value1,value};
    return output_value;
}
    //===============================================================================================        
    //Runnable class to make stop button active while "while" loop is running
    //used for operation in RPM mode
   public static class ShotTask implements Runnable {
        int Distination_Position;
        ShotTask(int s) { Distination_Position = s; }
        @Override
        public void run() {
            killMe=false;
              
              int motor_ID =0;
             int[] CurrentPos = {0x0e,0x1b};
             try{
              //50 is used to prevent motor over ratation
              //this is required due to delay
                while (MY_INT[1]<Distination_Position-50)
                {
                    if (killMe == true) {
                    break;
    }
                       serialPort.writeBytes(SendToMotor(motor_ID,CurrentPos[0],CurrentPos[1]));
                                                                  
                                
                            try {
                                TimeUnit.MILLISECONDS.sleep(2);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                }
                             //Stop command
                            int[] Stop = {0,3,0};
                            serialPort.writeBytes(SendToMotor(Stop[0],Stop[1],Stop[2]));//Write data to port
                }
             catch (SerialPortException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                            }
        
        }
    }
}
