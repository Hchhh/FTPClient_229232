package myFTPClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;

public class ClientFrame {

	private JFrame frame;
	private JTextField tf_host;
	private JTextField tf_user;
	private JTextField tf_pwd;
	private JTextField tf_port;

	//新建连接对象
	private FTPUtil ftpUtil = new FTPUtil();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientFrame window = new ClientFrame();
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
	public ClientFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1280, 960);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.Y_AXIS));
		
		JToolBar toolBar = new JToolBar();
//		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		frame.getContentPane().add(toolBar);
		
		JButton btn_file = new JButton("文件");
		toolBar.add(btn_file);
		
		JButton btn_edit = new JButton("编辑");
		btn_edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		toolBar.add(btn_edit);
		
		JButton btn_help = new JButton("帮助");
		toolBar.add(btn_help);
		
		JPanel loginPane = new JPanel();
		loginPane.setSize(800, 5);
		
		frame.getContentPane().add(loginPane);
//		loginPane.setLayout(new GridLayout(1, 8, 0, 0));
		loginPane.setLayout(new FlowLayout());
		loginPane.setMaximumSize(new Dimension(1280,50));
		
		JLabel label = new JLabel("主机：");
		loginPane.add(label);
		
		tf_host = new JTextField("62.234.12.47");
		loginPane.add(tf_host);
		tf_host.setColumns(10);
		
		JLabel label_1 = new JLabel("用户名：");
		loginPane.add(label_1);
		
		tf_user = new JTextField("ubuntu");
		loginPane.add(tf_user);
		tf_user.setColumns(10);
		
		JLabel label_2 = new JLabel("密码：");
		loginPane.add(label_2);
		
		tf_pwd = new JTextField("Ubuntu111!");
		loginPane.add(tf_pwd);
		tf_pwd.setColumns(10);
		
		JLabel label_3 = new JLabel("端口：");
		loginPane.add(label_3);
		
		tf_port = new JTextField("21");
		loginPane.add(tf_port);
		tf_port.setColumns(10);
		
		JButton btn_connect = new JButton("连接");
		loginPane.add(btn_connect);
		btn_connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String host = tf_host.getText();
				int port = Integer.parseInt(tf_port.getText());
				ftpUtil.setRemoteHost(host);
				ftpUtil.setRemotePort(port);
				ftpUtil.setRemotePath("/var/ftp/test");
				ftpUtil.connect();
				String user = tf_user.getText();
				String pwd = tf_pwd.getText();
				ftpUtil.setUser(user);
				ftpUtil.setPW(pwd);
				ftpUtil.login();
			}		
		});
		
		
		JTextArea consoleTxt = new JTextArea();
		consoleTxt.setText("Console\n\n\n\n\n");
		consoleTxt.setMaximumSize(new Dimension(1280,200));
		frame.getContentPane().add(consoleTxt);
		
		JPanel filePane = new JPanel();
		frame.getContentPane().add(filePane);
		filePane.setMaximumSize(new Dimension(1280,500));
		filePane.setMaximumSize(new Dimension(1280,600));
		filePane.setLayout(new GridLayout(0, 2, 0, 0));
		
		JSplitPane myFileTreePane = new JSplitPane();
		myFileTreePane.setResizeWeight(0.5);
		myFileTreePane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		filePane.add(myFileTreePane);
		
		JSplitPane serverFileTree = new JSplitPane();
		serverFileTree.setResizeWeight(0.5);
		serverFileTree.setOrientation(JSplitPane.VERTICAL_SPLIT);
		filePane.add(serverFileTree);
		
		
		JTextArea fileConsoleTxt = new JTextArea();
		fileConsoleTxt.setText("File Console\n\n\n\n\n");
		fileConsoleTxt.setMaximumSize(new Dimension(1280,200));
		frame.getContentPane().add(fileConsoleTxt);
	}

}
