package Target;
/**
 * PCS RTP Project
 * Copyright (C) 2013 QQting ^_<b
 * Wireless Mobile Networking Laboratory
 * National Tsing Hua University, Taiwan
 */


import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;

public class PCS_UI {
	
	// for Java UI
	public final int width = 400;
	public final int height = 300;
	
	private JFrame frame;
	
	private JLabel remoteIpLabel;
	private JLabel remoteRtpLabel;
	private JLabel remoteRtcpLabel;
	private JLabel localRtpLabel;
	private JLabel localRtcpLabel;
	
	private JTextField remoteIpText;
	private JTextField remoteRtpText;
	private JTextField remoteRtcpText;
	private JTextField localRtpText;
	private JTextField localRtcpText;

	private JButton btnDial;
	private JButton btnCancel;
	
	// PCS_UI constructor
	public PCS_UI(String title) {
		frame = new JFrame();
		
		remoteIpLabel = new JLabel("Remote  IP");
		remoteRtpLabel = new JLabel("Remote  RTP  port");
		remoteRtcpLabel = new JLabel("Remote  RTCP  port");
		localRtpLabel = new JLabel("Local  RTP  port");;
		localRtcpLabel = new JLabel("Local  RTCP  port");;
		
		remoteIpText = new JTextField("0.0.0.0");
		remoteRtpText = new JTextField("0");
		remoteRtcpText = new JTextField("0");
		localRtpText = new JTextField("0");
		localRtcpText = new JTextField("0");
		
		btnDial = new JButton("Dial");
		btnDial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnCancel = new JButton("Cancel");
		
		frame.setLocationRelativeTo(null);
		frame.setTitle("Gateway");
		initUI();
	}
	
	public void initUI() {
		remoteIpLabel.setHorizontalAlignment(JLabel.CENTER);
		remoteRtpLabel.setHorizontalAlignment(JLabel.CENTER);
		remoteRtcpLabel.setHorizontalAlignment(JLabel.CENTER);
		localRtpLabel.setHorizontalAlignment(JLabel.CENTER);
		localRtcpLabel.setHorizontalAlignment(JLabel.CENTER);
		btnCancel.setHorizontalAlignment(JLabel.CENTER);
		remoteIpText.setHorizontalAlignment(JTextField.CENTER);
		remoteRtpText.setHorizontalAlignment(JTextField.CENTER);
		remoteRtcpText.setHorizontalAlignment(JTextField.CENTER);
		localRtpText.setHorizontalAlignment(JTextField.CENTER);
		localRtcpText.setHorizontalAlignment(JTextField.CENTER);
		
		frame.getContentPane().setLayout(new GridLayout(6, 2));
		frame.getContentPane().add(remoteIpLabel);
		frame.getContentPane().add(remoteIpText);
		frame.getContentPane().add(remoteRtpLabel);
		frame.getContentPane().add(remoteRtpText);
		frame.getContentPane().add(remoteRtcpLabel);
		frame.getContentPane().add(remoteRtcpText);
		frame.getContentPane().add(localRtpLabel);
		frame.getContentPane().add(localRtpText);
		frame.getContentPane().add(localRtcpLabel);
		frame.getContentPane().add(localRtcpText);
		frame.getContentPane().add(btnDial);
		frame.getContentPane().add(btnCancel);

		frame.setSize(width, height);
		frame.setVisible(true);
	} // end setUI()

	public void setWindowLocation(int x, int y) {
		frame.setLocation(x, y);
	}
	public Point getWindowLocation() {
		return frame.getLocation();
	}
	
	public void setWindowListener(WindowAdapter adapter) {
		frame.addWindowListener(adapter);
	}

	public void setButtonActionListener(ActionListener listener) {
		btnDial.addActionListener(listener);
	}

	public String getButtonText() {
		return btnDial.getText();
	}
	
	public void setButtonText(String text) {
		btnDial.setText(text);
	}
	
	
	
	public String getRemoteIP() {
		return remoteIpText.getText();
	}
	
	public int getRemoteRtpPort() {
		return Integer.parseInt(remoteRtpText.getText());
	}
	
	public int getRemoteRtcpPort() {
		return Integer.parseInt(remoteRtcpText.getText());
	}
	
	public int getLocalRtpPort() {
		return Integer.parseInt(localRtpText.getText());
	}
	
	public int getLocalRtcpPort() {
		return Integer.parseInt(localRtcpText.getText());
	}
	
	public void setStateText(String text) {
		btnCancel.setText(text);
	}
	
}
