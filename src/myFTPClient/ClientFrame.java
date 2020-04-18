package myFTPClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ClientFrame {

	JFrame frame;
	private JTextField tf_host;
	private JTextField tf_user;
	private JPasswordField tf_pwd;
	private JTextField tf_port;

	// 新建连接对象
	private FTPUtil ftpUtil = new FTPUtil();

	// 本地文件树，服务器文件树
	JTree localFileTree;
	JTree serverFileTree;

	// 预存本地文件路径，windows修改第一项路径改为系统根目录
	String localSystemPath = "F:\\shared";
	String serverFilePath = "/var/ftp";

	// 传输文件时获取到的本地文件路径以及服务器文件路径
	String temp_localPath;
	String temp_fileName;
	String temp_serverPath;

	// 控制台文本框
	JTextArea consoleTxt;
	JTextArea fileConsoleTxt;
	boolean upload_OK = true;
	boolean download_OK = true;

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
//			ftpUtil.upload("/Users/hjq/Documents/CMMTokenAnalysis/cmm.txt","/var/ftp/test2");
//			ftpUtil.download("/var/ftp/test/cmm.txt", "/Users/hjq/Documents/cmm.txt");
			initialize(host, user, pwd, port);

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
		frame.setBounds(100, 100, 1280, 1150);
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

		tf_port = new JTextField(port + "");
		tf_port.setEditable(false);
		loginPane.add(tf_port);
		tf_port.setColumns(10);

		// 控制台文本框 用于显示接收到的Socket返回值
		consoleTxt = new JTextArea();
		consoleTxt.setEditable(false);
//		consoleTxt.setText("Console\n\n\n\n\n");
		consoleTxt.setMaximumSize(new Dimension(1280, 300));
		//consoleTxt.setText(ftpUtil.commuteInfo);
		frame.getContentPane().add(new JScrollPane(consoleTxt));

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

		serverPane.add(scrollPane, BorderLayout.CENTER);
		// 文件传输控制台文本框
				fileConsoleTxt = new JTextArea();
				fileConsoleTxt.setEditable(false);
//				fileConsoleTxt.setText("File Console\n\n\n\n\n");
				fileConsoleTxt.setMaximumSize(new Dimension(1280, 200));
				frame.getContentPane().add(new JScrollPane(fileConsoleTxt));

		// 本地文件树
		String filePath = localSystemPath;
		localFileTree = new JTree(getLocalFiles(filePath));
		panel.add(new JScrollPane(localFileTree), BorderLayout.CENTER);

		// 服务器文件树
		serverFileTree = new JTree(getServerFiles(serverFilePath));
		scrollPane.setViewportView(serverFileTree);

		// 树节点拖拽实现文件上传下载
		localFileTree.setDragEnabled(true);
		serverFileTree.setDragEnabled(true);

		// 本地文件上传
		DragSource dg_localtoserver = DragSource.getDefaultDragSource();
		dg_localtoserver.createDefaultDragGestureRecognizer(localFileTree, DnDConstants.ACTION_COPY,
				new DragGestureListener() {

					@Override
					public void dragGestureRecognized(DragGestureEvent dge) {
						// TODO Auto-generated method stub
						DefaultMutableTreeNode localNode = (DefaultMutableTreeNode) localFileTree
								.getLastSelectedPathComponent();
//						FileTreeNode node = (FileTreeNode) localNode.getUserObject();
//						String localRoute = node.getRoute();
						// 本地文件路径
						String temp_path = localSystemPath;
						TreeNode[] parents = localNode.getPath();
						for (int i = 1; i < parents.length; i++) {
							temp_path += "/" + ((DefaultMutableTreeNode) parents[i]).getUserObject();
						}

						// 当前选择传输的本地文件路径
						temp_localPath = temp_path;
						System.out.println(temp_localPath);

						// 若选中树节点为文件夹：
						if (new File(temp_localPath).isDirectory()) {
							JOptionPane.showConfirmDialog(null, " 暂不支持文件夹传输！ ", " 上传文件", JOptionPane.CLOSED_OPTION,
									JOptionPane.WARNING_MESSAGE);
//							upload_OK = false;
						}
//						String fileName = (String) localNode.getUserObject();
//						System.out.println("local :    " + fileName);
//						Transferable transferable = new StringSelection(fileName);
//						dge.startDrag(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), transferable);

					}

				});
		new DropTarget(serverFileTree, DnDConstants.ACTION_COPY, new LocaltoServerListener());

		DragSource dg_servertolocal = DragSource.getDefaultDragSource();
		dg_servertolocal.createDefaultDragGestureRecognizer(serverFileTree, DnDConstants.ACTION_COPY,
				new DragGestureListener() {

					@Override
					public void dragGestureRecognized(DragGestureEvent dge) {
						// TODO Auto-generated method stub
						DefaultMutableTreeNode serverNode = (DefaultMutableTreeNode) serverFileTree
								.getLastSelectedPathComponent();
						String serverFileName = (String) serverNode.getUserObject();
						temp_fileName = serverFileName;
						System.out.println(serverFileName);

						if (serverFileisDirectory(serverNode, serverFileName)) {
							JOptionPane.showConfirmDialog(null, " 暂不支持文件夹传输！ ", " 下载文件", JOptionPane.CLOSED_OPTION,
									JOptionPane.WARNING_MESSAGE);
						}
						// 选择的是服务器文件树节点
						else {
							DefaultMutableTreeNode parent = (DefaultMutableTreeNode) serverNode.getParent();
							temp_serverPath = parent.getUserObject() + "/" + serverFileName;
							System.out.println(temp_serverPath);
						}

					}

				});
		new DropTarget(localFileTree, DnDConstants.ACTION_COPY, new ServertoLocalListener());
	}

	// 上传文件拖动至服务器面板相应内部类
	class LocaltoServerListener extends DropTargetAdapter implements DropTargetListener {

		@Override
		public void drop(DropTargetDropEvent dtde) {
			// TODO Auto-generated method stub
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			Transferable transferable = dtde.getTransferable();
			String s = null;
			try {
				s = (String) transferable.getTransferData(DataFlavor.stringFlavor);
//				System.out.println("s :    "+s);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(s);
			TreePath tp = serverFileTree.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
//			System.out.println(serverFileTree.getPathForLocation(dtde.getLocation().x,dtde.getLocation().y).getLastPathComponent());
			DefaultMutableTreeNode serverPathNode = (DefaultMutableTreeNode) serverFileTree
					.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y).getLastPathComponent();
			int serverchildCount = serverPathNode.getChildCount();
			String serverPath = (String) ((DefaultMutableTreeNode) serverPathNode).getUserObject();
			temp_serverPath = serverPath;
			
			System.out.println(serverPath + "   " + serverchildCount);

			// 服务器路径选择正确
			if (serverFileisDirectory(serverPathNode, serverPath)) {

				// System.out.println(temp_localPath+" "+temp_serverPath);
				// 是否上传弹窗，是则开始上传否则取消

				if (JOptionPane.showConfirmDialog(null, "是否上传:\n" + temp_localPath + "\n至服务器路径：\n" + temp_serverPath,
						"上传文件", JOptionPane.YES_NO_OPTION) == 0) {

//					System.out.println("Yes");
//					try {
////						ftpUtil.upload(temp_localPath, temp_serverPath);
//						consoleTxt.setText(ftpUtil.commuteInfo);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}

					((DefaultMutableTreeNode) tp.getLastPathComponent()).add(newNode);
					serverFileTree.updateUI();

//					Date date = new Date();
//					String preText = fileConsoleTxt.getText();
//					fileConsoleTxt.setText(preText + "文件传输成功！\n操作时间：" + date.toString() + "\n本地文件路径：" + temp_localPath
//							+ "\n服务器文件路径：" + temp_serverPath + "\n\n");
//					fileConsoleTxt.updateUI();
				} else {
					// 取消上传
//					Date date = new Date();
//					String preText = fileConsoleTxt.getText();
//					fileConsoleTxt.setText(preText + "文件传输失败！\n操作时间：" + date.toString() + "\n失败原因：用户取消传输。\n\n");
//					fileConsoleTxt.updateUI();
				}

			} else {
				JOptionPane.showConfirmDialog(null, " 请选择传输至服务器文件夹中！ ", " 上传文件", JOptionPane.CLOSED_OPTION,
						JOptionPane.WARNING_MESSAGE);
//				Date date = new Date();
//				String preText = fileConsoleTxt.getText();
//				fileConsoleTxt.setText(preText + "文件传输失败！\n操作时间：" + date.toString() + "\n失败原因：选择了错误的服务器目的文件路径。\n\n");
//				fileConsoleTxt.updateUI();

			}
		}

	}

	// 上传文件拖动至服务器面板相应内部类
	class ServertoLocalListener extends DropTargetAdapter implements DropTargetListener {

		@Override
		public void drop(DropTargetDropEvent dtde) {
			// TODO Auto-generated method stub
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			Transferable transferable = dtde.getTransferable();
			String s = null;
			try {
				s = (String) transferable.getTransferData(DataFlavor.stringFlavor);
//				System.out.println("s :    "+s);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(s);
			TreePath tp = localFileTree.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
//			System.out.println(serverFileTree.getPathForLocation(dtde.getLocation().x,dtde.getLocation().y).getLastPathComponent());
			DefaultMutableTreeNode localPathNode = (DefaultMutableTreeNode) localFileTree
					.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y).getLastPathComponent();
			//选中文件名
			String localName = (String) ((DefaultMutableTreeNode) localPathNode).getUserObject();
			System.out.println(localName);
			//获得本地文件全路径
			String temp_path = localSystemPath;
			TreeNode[] localparents = localPathNode.getPath();
			for(int i=1; i< localparents.length;i++) {
				temp_path += "/" + ((DefaultMutableTreeNode) localparents[i]).getUserObject();
			}
			temp_localPath = temp_path;
			System.out.println(temp_localPath);
			if(!new File(temp_path).isDirectory()) {
				JOptionPane.showConfirmDialog(null, " 请选择传输至本地文件夹中！ ", " 下载文件", JOptionPane.CLOSED_OPTION,
						JOptionPane.WARNING_MESSAGE);
				Date date = new Date();
//				String preText = fileConsoleTxt.getText();
//				fileConsoleTxt.setText(preText + "文件下载失败！\n操作时间：" + date.toString() + "\n失败原因：选择了错误的本地目的文件路径。\n\n");
//				fileConsoleTxt.updateUI();
			}
			else {
				if (JOptionPane.showConfirmDialog(null, "是否下载:\n" + temp_serverPath + "\n至本地路径：\n" + temp_localPath,
						"下载文件", JOptionPane.YES_NO_OPTION) == 0) {
					//temp_localPath += "/" + temp_fileName;
					temp_localPath = temp_localPath.replace("/", "\\");
					try {
						ftpUtil.downloadContinue(temp_serverPath, temp_localPath);;
						//consoleTxt.setText(ftpUtil.commuteInfo);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					((DefaultMutableTreeNode) tp.getLastPathComponent()).add(newNode);
					localFileTree.updateUI();

					Date date = new Date();
//					String preText = fileConsoleTxt.getText();
//					fileConsoleTxt.setText(preText + "文件下载成功！\n操作时间：" + date.toString() + "\n本地文件路径：" + temp_localPath
//							+ "\n服务器文件路径：" + temp_serverPath + "\n\n");
//					fileConsoleTxt.updateUI();
				}
				else {
					// 取消下载
					Date date = new Date();
//					String preText = fileConsoleTxt.getText();
//					fileConsoleTxt.setText(preText + "文件下载失败！\n操作时间：" + date.toString() + "\n失败原因：用户取消下载。\n\n");
//					fileConsoleTxt.updateUI();
				
				}
			}
		}

	}

	// 获取本地文件树
	private DefaultMutableTreeNode getServerFiles(String mask) throws IOException {
//			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileTreeNode(mask));
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(mask);
		ArrayList<String> fileList = ftpUtil.list(mask);
		//consoleTxt.setText(ftpUtil.commuteInfo);
		String fileName;
		int fileSize;
		char isD;
		for (int i = 0; i < fileList.size(); i++) {

			String[] eachInfo = fileList.get(i).split("\\s+");

			fileName = eachInfo[8];
			fileSize = Integer.parseInt(eachInfo[3]);
			isD = eachInfo[0].charAt(0);
//				root = new DefaultMutableTreeNode(tf_host.getText());
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
//				System.out.println(i + ":   "+ fileList.get(i)+ "   ** "+ eachInfo[0].charAt(0) + "  size: "+ eachInfo[3]+ "   name:  "+ eachInfo[8]);
		}
		return root;
	}

	// 获取服务器文件树
	private DefaultMutableTreeNode getLocalFiles(String path) {

//			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileTreeNode(new File(path).getName()).getRoute());
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

	// 判断服务器文件是否为文件夹
	private boolean serverFileisDirectory(DefaultMutableTreeNode serverPathNode, String serverPath) {

		if (serverPathNode.getChildCount() != 0)
			return true;
		else {
			if (serverPath.charAt(0) == '/')
				return true;
			return false;
		}

	}
}
