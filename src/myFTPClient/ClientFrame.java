package myFTPClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

public class ClientFrame {

	JFrame frame;
	private JTextField tf_host;
	private JTextField tf_user;
	private JPasswordField tf_pwd;
	private JTextField tf_port;

	// 新建连接对象
	private FTPUtil ftpUtil = new FTPUtil();

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					ClientFrame window = new ClientFrame();
//					window.frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the application.
	 */
	public ClientFrame(String host, String user, String pwd, int port) {
		try {
			ftpUtil.setRemoteHost(host);
			ftpUtil.setRemotePort(port);
			ftpUtil.connect();
			ftpUtil.setUser(user);
			ftpUtil.setPW(pwd);
			ftpUtil.login();
			initialize(host,user,pwd,port);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize(String host, String user, String pwd, int port) throws IOException {
		frame = new JFrame();
		frame.setBounds(100, 100, 1280, 960);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

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
		loginPane.setMaximumSize(new Dimension(1280, 50));

		JLabel label = new JLabel("主机：");
		loginPane.add(label);

		tf_host = new JTextField(host);
		tf_host.setEditable(false);
		loginPane.add(tf_host);
		tf_host.setColumns(10);

		JLabel label_1 = new JLabel("用户名：");
		loginPane.add(label_1);

		tf_user = new JTextField(user);
		tf_user.setEditable(false);
		loginPane.add(tf_user);
		tf_user.setColumns(10);

		JLabel label_2 = new JLabel("密码：");
		loginPane.add(label_2);

		tf_pwd = new JPasswordField(pwd);
		tf_pwd.setEditable(false);
		loginPane.add(tf_pwd);
		tf_pwd.setColumns(10);

		JLabel label_3 = new JLabel("端口：");
		loginPane.add(label_3);

		tf_port = new JTextField(port+"");
		tf_port.setEditable(false);
		loginPane.add(tf_port);
		tf_port.setColumns(10);

		JTextArea consoleTxt = new JTextArea();
		consoleTxt.setText("Console\n\n\n\n\n");
		consoleTxt.setMaximumSize(new Dimension(1280, 200));
		frame.getContentPane().add(consoleTxt);
		
		JLabel lblNewLabel = new JLabel("文件传输");
		frame.getContentPane().add(lblNewLabel);

		JPanel filePane = new JPanel();
		frame.getContentPane().add(filePane);
		filePane.setMaximumSize(new Dimension(1280, 500));
		filePane.setMaximumSize(new Dimension(1280, 600));
		filePane.setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panel = new JPanel();
		filePane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel serverPane = new JPanel();
		serverPane.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		filePane.add(serverPane);
		
		
		serverPane.add(scrollPane,BorderLayout.CENTER);

//		JTree serverFileTree = new JTree(getServerFiles(""));

		JTextArea fileConsoleTxt = new JTextArea();
		fileConsoleTxt.setText("File Console\n\n\n\n\n");
		fileConsoleTxt.setMaximumSize(new Dimension(1280, 200));
		frame.getContentPane().add(fileConsoleTxt);

		//本地文件树
		String filePath = "/Users/hjq/Documents";
		JTree localFileTree = new JTree(getLocalFiles(filePath));
		panel.add(new JScrollPane(localFileTree), BorderLayout.CENTER);
		
		//服务器文件树
		JTree serverFileTree = new JTree(getServerFiles("/var/ftp"));
		scrollPane.setViewportView(serverFileTree);
		
		
	}

	private DefaultMutableTreeNode getServerFiles(String mask) throws IOException {
		DefaultMutableTreeNode root =  new DefaultMutableTreeNode(mask);
		ArrayList<String> fileList = ftpUtil.list(mask);
		String fileName;
		int fileSize;
		char isD;
		for (int i = 0; i < fileList.size(); i++) {

			String[] eachInfo = fileList.get(i).split("\\s+");

			fileName = eachInfo[8];
			fileSize = Integer.parseInt(eachInfo[3]);
			isD = eachInfo[0].charAt(0);
//			root = new DefaultMutableTreeNode(tf_host.getText());
			// 判断是文件还是文件夹
			if (isD == 'd') {
				System.out.println(mask + "/" + fileName);
				root.add(getServerFiles(mask + "/" + fileName));

			} else {
				if (fileSize == 0) {

				} else {
					DefaultMutableTreeNode temp = new DefaultMutableTreeNode(fileName);
					root.add(temp);
				}
			}
//			System.out.println(i + ":   "+ fileList.get(i)+ "   ** "+ eachInfo[0].charAt(0) + "  size: "+ eachInfo[3]+ "   name:  "+ eachInfo[8]);
		}
		return root;
	}
		

	private DefaultMutableTreeNode getLocalFiles(String path) {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new File(path).getName());
		File file = new File(path);

		if (file.exists()) {
			File[] files = file.listFiles();
			if (files.length == 0) {
				if (file.isDirectory()) {
					// 如果是空文件夹
					DefaultMutableTreeNode dn = new DefaultMutableTreeNode(file.getName(), false);
					return dn;
				}
			} else {
				for (File file2 : files) {
					if (file2.isDirectory()) {
						// 是目录的话，生成节点，并添加里面的节点
						root.add(getLocalFiles(file2.getAbsolutePath()));
					} else {
						// 是文件的话直接生成节点，并把该节点加到对应父节点上
						DefaultMutableTreeNode temp = new DefaultMutableTreeNode(file2.getName());
						root.add(temp);
					}
				}
			}
		} else {
			// 文件不存在
			return null;
		}
		return root;
	}

}
