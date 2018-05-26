package waterAllocation;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MonitorUI extends JFrame {

	private JPanel contentPane;
	private JTextArea log;
	private MonitorAgent monitorAgent;
	/**
	 * Launch the application.
	 */
	

	/**
	 * Create the frame.
	 */
	public MonitorUI(String a) {
		super(a = "Agent service monitoring");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
	}
	
	public void displayUI(String displayUI) {
    	log.append(displayUI);
    	log.setCaretPosition(log.getDocument().getLength());
    }

}
