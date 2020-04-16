package myFTPClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginWindow {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JPasswordField textField_2;
	private JTextField textField_3;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow window = new LoginWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 400, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("FTP Client");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(159, 29, 72, 44);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel label = new JLabel("主机地址：");
		label.setBounds(60, 90, 72, 21);
		frame.getContentPane().add(label);
		
		JLabel label_1 = new JLabel("用户名：");
		label_1.setBounds(60, 120, 61, 16);
		frame.getContentPane().add(label_1);
		
		JLabel label_2 = new JLabel("密码：");
		label_2.setBounds(60, 150, 61, 16);
		frame.getContentPane().add(label_2);
		
		JLabel label_3 = new JLabel("端口：");
		label_3.setBounds(60, 180, 61, 16);
		frame.getContentPane().add(label_3);
		
		
		JButton button = new JButton("登录");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String host=textField.getText(),user=textField_1.getText(),pwd=textField_2.getText();
				int port=Integer.parseInt(textField_3.getText());
				ClientFrame window = new ClientFrame(host,user,pwd,port);
				window.frame.setVisible(true);
				button.setEnabled(false);
			}
		});
		button.setBounds(138, 224, 117, 29);
		frame.getContentPane().add(button);
		
		textField = new JTextField("62.234.12.47");
		textField.setBounds(160, 90, 130, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField("ubuntu");
		textField_1.setBounds(160, 120, 130, 20);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		textField_2 = new JPasswordField("Ubuntu111!");
		textField_2.setBounds(160, 150, 130, 20);
		frame.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField("21");
		textField_3.setBounds(160, 180, 130, 20);
		frame.getContentPane().add(textField_3);
		textField_3.setColumns(10);
		
		
		
		
	}
}
