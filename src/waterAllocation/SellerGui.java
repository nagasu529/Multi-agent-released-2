package waterAllocation;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
  @author Giovanni Caire - TILAB
 */
class SellerGui extends JFrame {	
	private Farmer myAgent;
	
	private JTextField volumnFiled, priceField;
	
	SellerGui() {
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Water volume:"));
		volumnFiled = new JTextField(15);
		p.add(volumnFiled);
		p.add(new JLabel("Price per mm.:"));
		priceField = new JTextField(15);
		p.add(priceField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String volume = volumnFiled.getText().trim();
					String price = priceField.getText().trim();
                                        myAgent.volumeToSell = Double.parseDouble(volume);
					//myAgent.updateCatalogue(myAgent.getName(),Double.parseDouble(volume));
                                        myAgent.sellingPrice = Double.parseDouble(price);
                                        //Print out input
                                        System.out.println(myAgent.getLocalName());
                                        System.out.println(myAgent.volumeToSell);
                                        System.out.println(myAgent.sellingPrice);
					volumnFiled.setText("");
					priceField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(SellerGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
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
