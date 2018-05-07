/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterAllocation;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Kitti Chiewchan
 */
public class FarmerGUI extends JFrame{
    //Farming agent class
    private Farmer myAgent;
	
    //Creating setter and getter for passing parameters.
    public static String sFileDir;
    public static Double sActualReduc;
    public static int sEtSeason;
    public static String sAgentStatus;
    
    public void setFileDir(String fileDir){
        sFileDir = fileDir;
    }
    public static String getFileDir(){
        return sFileDir;
    }
    
    public void setActualReduc(Double actualReduc){
        sActualReduc = actualReduc;
    }
    
    public static Double getActualReduc(){
        return sActualReduc;
    }
    
    public void setEtSeason(int etSeason){
        sEtSeason = etSeason;
    }
    public static int getEtSeason(){
        return sEtSeason;
    }
    
    public void setAgentStatus(String agentStatus){
        sAgentStatus = agentStatus;
    }
    public static String getAgentStatus(){
        return sAgentStatus;
    }
    
    //GUI design preferences
    private JTextField actualReducField;
    private JButton calculateButton, textDirButton;
    private JFileChooser choosingDir;
    private JTextArea log;
	
    FarmerGUI(Farmer a) {
	super(a.getLocalName());
	
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
	myAgent = a;
        
        //Create a file chooser
        choosingDir = new JFileChooser();
	
        //Open text directory button preference.
        textDirButton = new JButton("Open file");
        calculateButton = new JButton("Calculate");
        
        
        //Calculation button action Listerner
	calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String actualReduc = actualReducField.getText().trim();
                    setActualReduc(Double.parseDouble(actualReduc));
                    myAgent.farmerInput(getFileDir(), getActualReduc(),getEtSeason());
                    //fileDirField.setText("");
                    actualReducField.setText("");
		}
		catch (Exception e) {
                    JOptionPane.showMessageDialog(FarmerGUI.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
		}
            }
        } );
        
        //Open file action listerner
        textDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(FarmerGUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                String filename = f.getAbsolutePath();
                System.out.println("Farming scheduale uploaded");
                    //System.out.println(filename);
                setFileDir(filename);
                }
            }
        });
	
        //Combobox Buyer/Seler preferences.
        String[] agentWorkStirng = {"Seller","Buyer"};
        JComboBox stageList = new JComboBox(agentWorkStirng);
        stageList.setSelectedIndex(1);
        stageList.setEditable(false);
        stageList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (stageList.getSelectedIndex()==0) {
                    setAgentStatus("Agent status updated to seller");
                    myAgent.farmerInfo.agentType = "seller";
                } else {
                    setAgentStatus("buyer");
                    System.out.println("Agent status updated to buyer");
                    myAgent.farmerInfo.agentType = "buyer";
                }
            }
        });

        //Combobox ET0 preference.
        String[] etListStrings = { "ET0-Spring", "ET0-Summer", "ET0-Autumn", "ET0-Winter"};
        JComboBox etList = new JComboBox(etListStrings);
        etList.setSelectedIndex(3);
        etList.setEditable(false);
        etList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(etList.getSelectedIndex()==0){
                    setEtSeason(0);
                    System.out.println("Spring ET0 choosed" + getEtSeason());
                }else if(etList.getSelectedIndex()==1){
                    setEtSeason(1);
                    System.out.println("Summer ET0 choosed");
                }else if(etList.getSelectedIndex()==2){
                    setEtSeason(2);
                    System.out.println("Autumn ET0 choosed");
                }else {
                    setEtSeason(3);
                    System.out.println("Winter ET0 choosed");
                }
            }
        });
        
        JPanel controls = new JPanel();
        controls.add(stageList);
        controls.add(textDirButton);
        controls.add(new JLabel("actual water reduction (%)"));
	actualReducField = new JTextField(15);
	controls.add(actualReducField);
        controls.setBorder(BorderFactory.createTitledBorder("Farmer input"));
        
        //For layout purposes, put the buttons in a separate panel
        JPanel p = new JPanel();
	p.setLayout(new GridLayout(3, 3));
	//p.add(stageList); 
	getContentPane().add(p, BorderLayout.CENTER);
        p = new JPanel();
	//p.add(textDirButton);
        p.add(controls);
        p.add(etList);
        p.add(calculateButton);
       
        
	getContentPane().add(p, BorderLayout.SOUTH);
		
	// Make the agent terminate when the user closes 
	// the GUI using the button on the upper right corner	
	addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
	} );
	setResizable(false);
    }
	
    public void show() {
        pack();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int centerX = (int)screenSize.getWidth() / 2;
	int centerY = (int)screenSize.getHeight() / 2;
	setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
	super.show();
    }	
}
