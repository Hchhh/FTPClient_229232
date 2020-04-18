package myFTPClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class LoginWindow {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JPasswordField textField_2;
	private JTextField textField_3;
	private JTextField localWorkPath;

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
		frame.setBounds(0, 0, 400, 350);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("FTP Client");
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 21));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(100, 22, 189, 53);
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
				String host = textField.getText(), user = textField_1.getText(), pwd = textField_2.getText(),localFilePath = localWorkPath.getText();
				int port = Integer.parseInt(textField_3.getText());
				if(host.equals("")) {
					JOptionPane.showConfirmDialog(null, " 主机地址不能为空！ ", "登录失败", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}else if(user.equals("")) {
					JOptionPane.showConfirmDialog(null, " 服务器登录用户名不能为空！ ", "登录失败", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}else if(pwd.equals("")) {
					JOptionPane.showConfirmDialog(null, " 服务器登录密码不能为空！ ", "登录失败", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}else if(textField_3.getText().equals("")) {
					JOptionPane.showConfirmDialog(null, " 服务器端口不能为空！ ", "登录失败", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}else if(localFilePath.equals("")) {
					JOptionPane.showConfirmDialog(null, " 请选择本地工作路径！ ", "登录失败", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}else {
					ClientFrame window = new ClientFrame(host, user, pwd, port,localFilePath);
					window.frame.setVisible(true);
					button.setEnabled(false);	
				}
			}
		});
		button.setBounds(138, 274, 117, 29);
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

		JLabel lblNewLabel_1 = new JLabel("本地工作路径：");
		lblNewLabel_1.setBounds(60, 208, 117, 21);
		frame.getContentPane().add(lblNewLabel_1);

		localWorkPath = new JTextField("");
		localWorkPath.setEditable(false);
		localWorkPath.setBounds(160, 205, 130, 26);
		frame.getContentPane().add(localWorkPath);
		localWorkPath.setColumns(10);

		JButton openBtn = new JButton("", new ImageIcon("src/img/open_icon.png"));
		openBtn.setBounds(294, 205, 34, 24);
		openBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
//				String osName = System.getProperty("os.name");
//				JFileChooser fileChooser = null;
//				if(osName.contains("Mac")) {
//					fileChooser = new JFileChooser();
//				}else if(osName.contains("Windows")) {
//					fileChooser = new JFileChooser("/");
//				}
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showOpenDialog(fileChooser);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String filePath = fileChooser.getSelectedFile().getAbsolutePath();// 这个就是你选择的文件夹的路径
					localWorkPath.setText(filePath);
				}
			}

		});
		frame.getContentPane().add(openBtn);

	}
}
