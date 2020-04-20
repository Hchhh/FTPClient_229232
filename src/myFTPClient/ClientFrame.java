package myFTPClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
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
	
	//多线程下载对象
	private int poolSize = 3; //线程池大小
	private MultiDownload md;

	// 本地文件树，服务器文件树
	JTree localFileTree;
	JTree serverFileTree;

	// 预存本地文件路径，windows修改第一项路径改为系统根目录

	String localSystemPath = "F:\\shared";

//	String localSystemPath = "/Users/hjq/Documents";

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

	//进度信息
	private JTextField tf_localPath;
	private JTextField tf_direction;
	private JTextField tf_serverPath;
	JLabel lbl_thread1;
	JLabel lbl_thread2;
	JLabel lbl_thread3;
	JLabel lbl_all;
	JButton btn_continue;
	JList list;
	private DefaultListModel dlm = new DefaultListModel();
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
	public ClientFrame(String host, String user, String pwd, int port, String localWorkPath) {
		try {
			ftpUtil.setRemoteHost(host);
			ftpUtil.setRemotePort(port);
			ftpUtil.connect();
			ftpUtil.setUser(user);
			ftpUtil.setPW(pwd);
			ftpUtil.login();
			this.localSystemPath = localWorkPath;
//			ftpUtil.mkdir("/var/ftp/test/test1");
//			ftpUtil.upload("/Users/hjq/Documents/CMMTokenAnalysis/cmm.txt","/var/ftp/test2");
//			ftpUtil.download("/var/ftp/test/cmm.txt", "/Users/hjq/Documents/cmm.txt");
//			ftpUtil.deleteFile("/var/ftp/test/test.txt");
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

		consoleTxt.setMaximumSize(new Dimension(1280, 400));
		consoleTxt.setText(ftpUtil.commuteInfo);

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
		//传输界面
				JPanel transimitPane = new JPanel();
				frame.getContentPane().add(transimitPane);
				transimitPane.setLayout(null);
				
				JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
				tabbedPane.setBounds(10, 10, 1246, 193);
				transimitPane.add(tabbedPane);
				
				JPanel panel_InProgress = new JPanel();
				tabbedPane.addTab("正在传输", panel_InProgress);	
				panel_InProgress.setLayout(null);
				
				JLabel label_4 = new JLabel("本地存放路径");
				label_4.setFont(new Font("宋体", Font.PLAIN, 14));
				label_4.setBounds(49, 10, 169, 15);
				panel_InProgress.add(label_4);
				
				JLabel label_5 = new JLabel("传输方向");
				label_5.setFont(new Font("宋体", Font.PLAIN, 14));
				label_5.setBounds(207, 10, 70, 15);
				panel_InProgress.add(label_5);
				
				JLabel label_6 = new JLabel("线程 1 进度");
				label_6.setFont(new Font("宋体", Font.PLAIN, 14));
				label_6.setBounds(345, 10, 79, 15);
				panel_InProgress.add(label_6);
				
				JLabel label_7 = new JLabel("线程 2 进度");
				label_7.setFont(new Font("宋体", Font.PLAIN, 14));
				label_7.setBounds(488, 10, 87, 15);
				panel_InProgress.add(label_7);
				
				JLabel label_8 = new JLabel("线程 3 进度");
				label_8.setFont(new Font("宋体", Font.PLAIN, 14));
				label_8.setBounds(645, 10, 87, 15);
				panel_InProgress.add(label_8);
				
				JLabel label_9 = new JLabel("总进度");
				label_9.setFont(new Font("宋体", Font.PLAIN, 14));
				label_9.setBounds(860, 10, 70, 15);
				panel_InProgress.add(label_9);
				
				JLabel label_10 = new JLabel("服务器存放路径");
				label_10.setFont(new Font("宋体", Font.PLAIN, 14));
				label_10.setBounds(1046, 10, 101, 15);
				panel_InProgress.add(label_10);
				
				tf_localPath = new JTextField();
				tf_localPath.setEditable(false);
				tf_localPath.setBounds(10, 47, 155, 21);
				panel_InProgress.add(tf_localPath);
				tf_localPath.setColumns(10);
				
				tf_direction = new JTextField();
				tf_direction.setEditable(false);
				tf_direction.setColumns(10);
				tf_direction.setBounds(202, 47, 66, 21);
				panel_InProgress.add(tf_direction);
				
				tf_serverPath = new JTextField();
				tf_serverPath.setEditable(false);
				tf_serverPath.setColumns(10);
				tf_serverPath.setBounds(1015, 47, 161, 21);
				panel_InProgress.add(tf_serverPath);
				
				lbl_thread1 = new JLabel("");
				lbl_thread1.setBounds(352, 50, 58, 15);
				panel_InProgress.add(lbl_thread1);
				
				lbl_thread2 = new JLabel("");
				lbl_thread2.setBounds(498, 50, 58, 15);
				panel_InProgress.add(lbl_thread2);
				
				lbl_thread3 = new JLabel("");
				lbl_thread3.setBounds(661, 50, 58, 15);
				panel_InProgress.add(lbl_thread3);
				
				lbl_all = new JLabel("");
				lbl_all.setBounds(853, 50, 80, 15);
				panel_InProgress.add(lbl_all);
						
				JPanel panel_Compeleted = new JPanel();
				tabbedPane.addTab("传输完成", panel_Compeleted);	
				panel_Compeleted.setLayout(null);
				
				JScrollPane scrollPane_1 = new JScrollPane();
				scrollPane_1.setBounds(10, 10, 1221, 154);
				panel_Compeleted.add(scrollPane_1);
				
				list = new JList();
				scrollPane_1.setViewportView(list);

		// 本地文件树
		String filePath = localSystemPath;
		localFileTree = new JTree(getLocalFiles(filePath));
		panel.add(new JScrollPane(localFileTree), BorderLayout.CENTER);

		// 服务器文件树
		serverFileTree = new JTree(getServerFiles(serverFilePath));
		scrollPane.setViewportView(serverFileTree);

		// 添加右键响应事件实现删除文件以及重命名
		serverFileTree.addMouseListener(new MouseAdapter() {
			DefaultMutableTreeNode rightNode;
			String temp_fileName;

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					rightNode = (DefaultMutableTreeNode) serverFileTree.getPathForLocation(e.getX(), e.getY())
							.getLastPathComponent();
					temp_fileName = (String) rightNode.getUserObject();
					if (temp_fileName != null) {
						// 选择的是目录
						if (serverFileisDirectory(rightNode, temp_fileName)) {
							JPopupMenu dicMenu = directoryDeleteandRename(rightNode, temp_fileName);
							dicMenu.show(e.getComponent(),e.getX(),e.getY());
						}
						// 选择的是文件
						else {
							JPopupMenu fileMenu = fileDeleteandRename(rightNode, temp_fileName);
							fileMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			}

		});

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
					upload(); //包装上传方法
					consoleTxt.setText(ftpUtil.commuteInfo);

					((DefaultMutableTreeNode) tp.getLastPathComponent()).add(newNode);
					serverFileTree.updateUI();
				} else {
					// 取消上传
					fileLog("文件传输失败！","失败原因：用户取消传输。");
				}

			} else {
				JOptionPane.showConfirmDialog(null, " 请选择传输至服务器文件夹中！ ", " 上传文件", JOptionPane.CLOSED_OPTION,
						JOptionPane.WARNING_MESSAGE);
				fileLog("文件传输失败！","失败原因：选择了错误的服务器目的文件路径。");


			}
		}
		
		/**
		 * 
		 * 上传并绘制传输界面，注意修改slash
		 */
		@SuppressWarnings("unchecked")
		public synchronized void upload() {
				//开始上传
				try {
					String slash = "\\";
					temp_localPath = temp_localPath.replace("/", slash);
					tf_localPath.setText(temp_localPath);
					tf_direction.setText("上传");
					tf_serverPath.setText(temp_serverPath);
					lbl_thread1.setForeground(Color.BLUE);
					lbl_thread1.setText("上传中...");
					lbl_thread2.setText("");
					lbl_thread3.setText("");
					lbl_all.setForeground(Color.BLUE);
					lbl_all.setText("上传中...");
					ftpUtil.uploadContinue(temp_localPath, temp_serverPath);
					lbl_thread1.setForeground(Color.GREEN);
					lbl_thread1.setText("上传完成");
					lbl_all.setForeground(Color.green);
					lbl_all.setText("上传完成");
					Date date = new Date();
					dlm.addElement("本地文件路径："+temp_localPath
								+"------------传输方向：上传"
								+"------------文件大小：" + new File(temp_localPath).length() + "字节"
								+"------------服务器文件路径：" + temp_serverPath + "/" + temp_fileName
								+"------------完成时间："+date);
					list.setModel(dlm);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
//					System.out.println("s :    "+s);
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(s);
				TreePath tp = localFileTree.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
//				System.out.println(serverFileTree.getPathForLocation(dtde.getLocation().x,dtde.getLocation().y).getLastPathComponent());
				DefaultMutableTreeNode localPathNode = (DefaultMutableTreeNode) localFileTree
						.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y).getLastPathComponent();
				// 选中文件名
				String localName = (String) ((DefaultMutableTreeNode) localPathNode).getUserObject();
				System.out.println(localName);
				// 获得本地文件全路径
				String temp_path = localSystemPath;
				TreeNode[] localparents = localPathNode.getPath();
				for (int i = 1; i < localparents.length; i++) {
					temp_path += "/" + ((DefaultMutableTreeNode) localparents[i]).getUserObject();
				}
				temp_localPath = temp_path;
				System.out.println(temp_localPath);
				if (!new File(temp_path).isDirectory()) {
					JOptionPane.showConfirmDialog(null, " 请选择传输至本地文件夹中！ ", " 下载文件", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
					fileLog("文件下载失败！","失败原因：选择了错误的本地目的文件路径");
				} else {
					if (JOptionPane.showConfirmDialog(null, "是否下载:\n" + temp_serverPath + "\n至本地路径：\n" + temp_localPath,
							"下载文件", JOptionPane.YES_NO_OPTION) == 0) {
//						temp_localPath += "/" + temp_fileName;
						download();
						consoleTxt.setText(ftpUtil.commuteInfo);
						((DefaultMutableTreeNode) tp.getLastPathComponent()).add(newNode);
						localFileTree.updateUI();

						fileLog("文件下载成功！","本地文件路径：" + temp_localPath + "\n服务器文件路径：" + temp_serverPath );
					} else {
						// 取消下载
						fileLog("文件下载失败！","失败原因：用户取消下载。");
					}
				}
			}
			
			/**
			 * 下载文件并绘制传输界面，注意修改slash
			 */
			@SuppressWarnings("unchecked")
			public synchronized void download() {
				String slash = "\\";
				if(new File(temp_localPath+slash+temp_fileName).exists()) {
					JOptionPane.showConfirmDialog(null, "该文件已存在");
					return;
				}
				temp_localPath = temp_localPath.replace("/", slash);
				tf_localPath.setText(temp_localPath + slash + temp_fileName);
				tf_direction.setText("下载");
				tf_serverPath.setText(temp_serverPath + slash + temp_fileName);
				lbl_thread1.setForeground(Color.BLUE);
				lbl_thread1.setText("下载中...");
				lbl_thread2.setForeground(Color.BLUE);
				lbl_thread2.setText("下载中...");
				lbl_thread3.setForeground(Color.BLUE);
				lbl_thread3.setText("下载中...");
				lbl_all.setForeground(Color.BLUE);
				lbl_all.setText("多线程下载中...");
				//开一个线程启动多线程下载
				md = new MultiDownload(poolSize, temp_serverPath, temp_localPath, temp_fileName);
				Thread downloadTask = new Thread(() -> {
					md.safeDownload();
				});
				//另开一个线程统计动态下载信息并进行显示
				Thread displayTask = new Thread(() -> {					
					double[] progress;
					while(true) {
						progress = md.getProgress();
						if(progress[0] == 100) {
							lbl_thread1.setForeground(Color.GREEN); 
							lbl_thread1.setText("下载完成");
						} 
						if(progress[1] == 100) {
							lbl_thread2.setForeground(Color.GREEN); 
							lbl_thread2.setText("下载完成");
						} 
						if(progress[2] == 100) {
							lbl_thread3.setForeground(Color.GREEN); 
							lbl_thread3.setText("下载完成");
						} 
						if(progress[3] == 100) {
							lbl_all.setForeground(Color.GREEN); 
							lbl_all.setText("合并完成");
							Date date = new Date();
							dlm.addElement("本地文件路径："+temp_localPath+slash+temp_fileName
									+"------------传输方向：下载"
									+"------------文件大小：" + (new File(temp_localPath+slash+temp_fileName).length() + "字节")
									+"------------服务器文件路径：" + temp_serverPath
									+"------------完成时间："+date);
							list.setModel(dlm);
							break;
						} 
						
					}
				});

				ExecutorService executorService = Executors.newFixedThreadPool(2);
				executorService.submit(downloadTask);
				executorService.submit(displayTask);
				executorService.shutdown();
			}


		}

	// 获取服务器文件树
	private DefaultMutableTreeNode getServerFiles(String mask) throws IOException {
//			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileTreeNode(mask));
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(mask);
		ArrayList<String> fileList = ftpUtil.list(mask);
		consoleTxt.setText(ftpUtil.commuteInfo);
		String fileName="";
		int fileSize;
		char isD;
		for (int i = 0; i < fileList.size(); i++) {

			String[] eachInfo = fileList.get(i).split("\\s+");
//			String test = "-rw-------    1 500      500         58200 Apr 17 16:17 ";
//			String test2="-rw-------    1 500      500      12717913 Apr 16 18:06 ";
//			System.out.println(test.length() + "   " +test2.length());
//			
//			
//			
//			for(int n=8; n<eachInfo.length;n++)
//				fileName += eachInfo[n];
			fileName = fileList.get(i).substring(56, fileList.get(i).length());
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
			fileName ="";
		}
		return root;
	}

	// 获取本地文件树
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

	//服务器文件夹新建 删除 操作
	private JPopupMenu directoryDeleteandRename(DefaultMutableTreeNode node, String fileName) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem newItem = new JMenuItem("新建文件夹");
		JMenuItem deleteItem = new JMenuItem("删除文件夹");
		menu.add(deleteItem);
		menu.add(newItem);
		
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String fullPath = fileName;
				
				String newFolderName=JOptionPane.showInputDialog("新建文件夹：");
				
				if(JOptionPane.showConfirmDialog(null,
							"是否在服务器文件夹：" + fullPath + " 下\n新建文件夹：" +newFolderName, "新建文件夹",
							JOptionPane.YES_NO_OPTION) == 0) {
					fullPath += "/"+newFolderName;
					try {
						//新建成功
						if(ftpUtil.mkdir(fullPath)) {
							node.add(new DefaultMutableTreeNode(fullPath));
							serverFileTree.updateUI();
							fileLog("新建文件夹成功","新建文件夹路径："+fullPath);
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		deleteItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String fullPath = fileName;
				if(fullPath.equals("/var/ftp")) {
					JOptionPane.showConfirmDialog(null, " 不能对服务器根目录进行删除操作！ ", " 删除文件夹", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}else {
					if(JOptionPane.showConfirmDialog(null,
							"是否在服务器删除文件夹：" + fullPath + "\n（此操作不可逆！）" , "删除文件夹",
							JOptionPane.YES_NO_OPTION) == 0) {
						try {
							if(ftpUtil.rmdir(fullPath)) {
								((DefaultMutableTreeNode) node.getParent()).remove(node);
								serverFileTree.updateUI();
								fileLog("文件夹删除成功！","服务器原文件夹路径："+ fullPath);
							}
							// 删除失败
							else {
								fileLog("文件夹删除失败！","服务器原文件夹路径：" +fullPath +  "\n请在上方控制台查看失败信息！");
								
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}else {
						fileLog("文件夹删除失败","失败原因：用户取消了删除操作。");
					}
				}
			}
			
		});
		
		return menu;
	}
	
	// 文件右键弹出窗体
	private JPopupMenu fileDeleteandRename(DefaultMutableTreeNode node, String fileName) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem deleteItem = new JMenuItem("删除");
		JMenuItem renameItem = new JMenuItem("重命名");
		menu.add(deleteItem);
		menu.add(renameItem);

		// 删除文件操作
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String fullPath = (String) ((DefaultMutableTreeNode) node.getParent()).getUserObject() + "/" + fileName;
//				System.out.println(fullPath);
				try {
					if (JOptionPane.showConfirmDialog(null,
							"是否在服务器删除文件:\n" + fullPath + "\n（此操作不可逆！）" , "删除文件",
							JOptionPane.YES_NO_OPTION) == 0) {
						// 删除成功:删除树节点
						if (ftpUtil.deleteFile(fullPath) == true) {
							((DefaultMutableTreeNode) node.getParent()).remove(node);
							serverFileTree.updateUI();
							fileLog("文件删除成功！","服务器原文件路径：" +fullPath );
						}
						// 删除失败
						else {
							fileLog("文件删除失败！","服务器原文件路径：" +fullPath +  "\n请在上方控制台查看失败信息！");
							
						}
					}
					else {
						fileLog("文件删除失败","失败原因：用户取消了删除操作。");
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		// 重命名操作
		renameItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String fullPath = (String) ((DefaultMutableTreeNode) node.getParent()).getUserObject() + "/" + fileName;
//				System.out.println(fullPath);
//				try {
//					if (JOptionPane.showConfirmDialog(null,
//							"是否重命名:\n" + fileName , "重命名文件",
//							JOptionPane.YES_NO_OPTION) == 0) {
//						// 重命名成功:重命名树节点
//						if (ftpUtil.deleteFile(fullPath) == true) {
//							((DefaultMutableTreeNode) node.getParent()).remove(node);
//							serverFileTree.updateUI();
//							Date date = new Date();
//							String preText = fileConsoleTxt.getText();
//							fileConsoleTxt.setText(preText + "文件重命名成功！\n操作时间：" + date.toString() + "\n服务器原文件名：" +fullPath + "\n新文件名：" +"!!!!!!!!! " + "\n\n");
//							fileConsoleTxt.updateUI();
//						}
//						// 删除失败
//						else {
//							Date date = new Date();
//							String preText = fileConsoleTxt.getText();
//							fileConsoleTxt.setText(preText + "文件删除失败！\n操作时间：" + date.toString() + "\n服务器原文件路径：" +fullPath +  "\n请在上方控制台查看失败信息！" +"\n\n");
//							fileConsoleTxt.updateUI();
//						}
//					}
//					else {
//						Date date = new Date();
//						String preText = fileConsoleTxt.getText();
//						fileConsoleTxt.setText(preText + "文件删除失败！\n操作时间：" + date.toString() + "\n失败原因：用户取消了删除操作。\n\n");
//						fileConsoleTxt.updateUI();
//					}
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			}

		});

		return menu;
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
	
	//文件控制台写入操作
	private void fileLog(String title,String content) {
		Date date = new Date();
		String preText = consoleTxt.getText();
		consoleTxt.setText(preText + title + "\n操作时间："+ date.toString() +"\n" + content + "\n\n");
		consoleTxt.updateUI();
	}
}
