package myFTPClient;


import java.net.Socket;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class FTPUtil {

    private Socket connectSocket;//控制连接，用于传送和响应命令
    private Socket dataSocket;//数据连接，用于数据传输
    private BufferedReader inData;//控制连接中用于读取返回信息的数据流
    private BufferedWriter outData;//控制连接中用于传送用户命令的数据流
    private String response = null;//将返回信息封装成字符串
    private String remoteHost;//远程主机名
    private int remotePort;//通信端口号
    private String remotePath;//远程路径
    private String user;//用户名
    private String passWord;//用户口令
    File rootPath = new File("/");//根路径
    File currentPath = rootPath;//当前路径
    private boolean logined;//判断是否登录服务器的标志
    private boolean debug;
    private RandomAccessFile localRAF; //本地文件
    private RandomAccessFile remoteRAF; //远端文件
    

    public FTPUtil() {
        remoteHost = "localhost";
        remotePort = 21;
        remotePath = "/";
        user = "user";
        passWord = "123";
        logined = false;
        debug = false;
    }

    //设置服务器域名（IP地址）
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    //返回服务器域名（IP地址）
    public String getRemoteHost() {
        return remoteHost;
    }
    //设置端口
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    //返回端口
    public int getRemotePort() {
        return remotePort;
    }
    //The remote directory path
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
    /// The current remote directory path.
    public String getRemotePath() {
        return remotePath;
    }
    //用户名
    public void setUser(String user) {
        this.user = user;
    }
    //密码
    public void setPW(String password) {
        this.passWord = password;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 与远程主机:端口建立连接，等价于telnet host:port
     * @return 控制连接socket
     */
    public Socket connect() {
        try {
            if (connectSocket == null) {

                connectSocket = new Socket(remoteHost, remotePort);
                inData = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));//输入信息(字符输入流)

                outData = new BufferedWriter(new OutputStreamWriter(connectSocket.getOutputStream()));//输出信息(字符输出流)
            }
            response = readLine();
          JOptionPane.showConfirmDialog(null,
                    "服务器已经成功连接",
                    "连接信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {           
             JOptionPane.showConfirmDialog(null,
                    " 连接失败",
                    " 连接信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return connectSocket;
    }

    /**
     * 使用用户名与密码登录，并根据remotePath更改当前工作目录
     */
    public void login() {
        try {
            if (connectSocket == null) {
                JOptionPane.showConfirmDialog(null,
                    " 服务器尚未连接，请先连接！",
                    " 连接信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);               
                return;
            }
            sendCommand("USER " + user);
            response = readLine();
            if (!response.startsWith("331")) {
                cleanup();
                 JOptionPane.showConfirmDialog(null,
                    " 用户名或密码错误！",
                    " 连接信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Error:用户名或密码错误！" + response);
                System.out.println(response);
                return;
            }
            sendCommand("PASS " + passWord);
            response = readLine();
            if (!response.startsWith("230")) {
                cleanup();
                 JOptionPane.showConfirmDialog(null,
                    " 用户名或密码错误！",
                    " 连接信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Error:用户名或密码错误！" + response);
                System.out.println(response);
                return;
            }
            logined = true;
             JOptionPane.showConfirmDialog(null,
                    " 登陆成功！",
                    " 连接信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            cwd(remotePath);
        } catch (Exception e) {
          JOptionPane.showConfirmDialog(null,
                    " 登陆失败！",
                    " 登陆信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 获取服务器目标目录下的所有文件以及目录
     * @param mask 目标目录
     * @return 文件及目录信息
     * @throws IOException
     */
    public ArrayList<String> list(String mask) throws IOException {
        if (!logined) {
            System.out.println("服务器尚未连接。");
            login();
        }
        ArrayList<String> fileList = new ArrayList<String>();
        try {
        	dataSocket = createDataSocket();
            if (mask == null || mask.equals("") || mask.equals(" ")) {
                sendCommand("LIST");
            } else {
                sendCommand("LIST " + mask);
            }
            response = readLine();
            if (!response.startsWith("1")) {
                System.out.println(response);
            }
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            String line;
            while ((line = dataIn.readLine()) != null) {
                fileList.add(line);
                System.out.println(line);
            }

            dataIn.close();//关闭数据流
            dataSocket.close();//关闭数据连接 
            response = readLine();
            System.out.println("List Complete.");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fileList;
    }
    
    /**
     * 关闭FTP连接
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        try {
            sendCommand("QUIT ");
        } finally {
            cleanup();
            System.out.println("正在关闭......");
        }
    }

    /**
     * 关闭socket及其输入输出流
     */
    private void cleanup() {
        try {
            inData.close();
            outData.close();
            connectSocket.close();
            logined = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 设置数据传输模式
     * @param mode true-二进制 false-ASCII
     * @throws IOException
     */
    public void setBinaryMode(Boolean mode) throws IOException {
        if (mode) {
            sendCommand("TYPE I ");
        } else {
            sendCommand("TYPE A ");
        }
        response = readLine();
        if (!response.startsWith("200")) {
            throw new IOException("Caught Error " + response);
        }
    }
    
    /**
     * 显示服务器的当前工作目录
     * @return 服务器的当前工作目录
     * @throws IOException
     */
    public synchronized String pwd() throws IOException {
        sendCommand("XPWD ");
        String dir = null;
        response = readLine();
        if (response.startsWith("257")) {         //服务器响应信息如：257 "/C:/TEMP/" is current directory.截取两引号之间的内容
            int fristQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', fristQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(fristQuote + 1, secondQuote);
            }
        }
        System.out.println(""+dir);
        return dir;
    }
   
    /**
     * 改变服务器的当前工作目录
     * @param dir 目标目录
     * @return
     * @throws IOException
     */
    public synchronized boolean cwd(String dir) throws IOException {
        if (dir.equals("/")) {//根路径
            System.out.println("当前路径是根目录！");
        }
        if (!logined) {
            login();
        }
        sendCommand("CWD " + dir);
        response = readLine();
        if (response.startsWith("250 ")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 获取远程服务器某个文件的大小
     * @param fileName 远端文件路径（相对于工作目录）
     * @return 远端文件的大小（字节数）, -1-获取失败
     * @throws IOException
     */
    public long getFileSize(String filePath) throws IOException {
    	if(!logined)
    		login();
    	sendCommand("SIZE "+filePath); //根据工作目录改写
    	response = readLine();
    	if(response.startsWith("213"))
    	{
    		long fileSize = Long.parseLong(response.split(" ")[1]);
        	return fileSize;
    	}
    	return -1;
    }

    /**
     * 删除远程服务器上的某个文件
     * @param FilePath 远端文件路径（相对于工作目录）
     * @return true-删除成功 false-删除失败
     * @throws IOException
     */
    public boolean deleteFile(String FilePath) throws IOException {
    	if(!logined)
    		login();
    	sendCommand("DELE " + FilePath);
    	response = readLine();
    	if(response.startsWith("250")) //250-delete operation successful
    		return true;
    	return false;
    }
    
    
    /**
     * 上传文件，包含断点续传功能
     * @param localFilePath 要上传的本地文件的路径（包含文件名）
     * @throws IOException
     */ 
    public synchronized void uploadContinue(String localFilePath) throws IOException{
    	//创建数据缓冲区，用于上传数据
    	byte[] buffer = new byte[4096];
    	//获取本地文件名 (xxx.a)
    	int i = localFilePath.lastIndexOf("/");
        if (i == -1) {
            i = localFilePath.lastIndexOf("\\");
        }
        String localFileName = "";
        if (i != -1) {
        	localFileName = localFilePath.substring(i + 1);
        }  
        //查找远端是否有对应的临时文件 (xxxa.temp)
        boolean hasTemp = false;
        String remoteFileName = localFileName.replace(".", "") + ".temp";
        ArrayList<String> files = list("");
        for(String s : files) {
        	if(s.indexOf(remoteFileName) != -1)
        		hasTemp = true;
        }
       //创建数据socket及其IO流
    	dataSocket = createDataSocket();
    	BufferedInputStream socketIn = new BufferedInputStream(dataSocket.getInputStream());
    	BufferedOutputStream socketOut = new BufferedOutputStream(dataSocket.getOutputStream()); 	
        //如果远端有临时文件，即断点续传
    	localRAF = new RandomAccessFile(localFilePath, "r"); //获取本地文件
    	long fileSize = getFileSize(remoteFileName); //获取远端文件的大小
    	if(hasTemp) {
        	if(fileSize == -1) {
        		System.out.println("cannot get size of remote temp file");
        		return;
        	}	
        	if(fileSize < localRAF.length()) {
        		//修改文件指针到剩余部分的开始
        		localRAF.seek(fileSize);
            	//传输该部分,每次传输一个buffer的数据
        		sendCommand("APPE " + remoteFileName);
            	int length1 = 0; //单次传输的数据大小（字节数）
            	while((length1 = localRAF.read(buffer)) > 0) {
            		socketOut.write(buffer, 0, length1);
            		socketOut.flush();
            	}
            	
            	//传输完成，重命名
            	sendCommand("RNFR " + remoteFileName);
            	sendCommand("RNTO " + localFileName);
             	socketOut.close();
                socketIn.close();
                dataSocket.close();//关闭此数据连接
                response = readLine();
                if (response.startsWith("226")) {
                    JOptionPane.showConfirmDialog(null,
                                " 文件上传成功！",
                                " 上传信息", JOptionPane.CLOSED_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);            
                }
        	}		
        }
        
        else {
        	 //如果远端没有对应的临时文件, 正常传输
            localRAF.seek(0);
            sendCommand("STOR " + remoteFileName); //创建远端临时文件
            response = readLine();
            int length2 = 0;
            while((length2 = localRAF.read(buffer)) > 0) {
            	socketOut.write(buffer, 0, length2);
            	socketOut.flush();
            }
            
            //传输完成，重命名
            sendCommand("RNFR " + remoteFileName);
        	sendCommand("RNTO " + localFileName);
        	socketOut.close();
            socketIn.close();
            dataSocket.close();//关闭此数据连接
            response = readLine();
            if (response.startsWith("226")) {
               JOptionPane.showConfirmDialog(null,
                            " 文件上传成功！",
                            " 上传信息", JOptionPane.CLOSED_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);            
            }
        	     
        }     	
    }
    
    /**
     * 下载文件，包含断点续传功能
     * @param remoteFilePath 要下载的远程文件路径 （包含文件名）
     * @param localFilePath 本地存放路径 （不包含文件名）
     * @throws IOException
     */
    public synchronized void downloadContinue(String remoteFilePath, String localFileDir) throws IOException {
    	//创建数据缓冲区，用于下载
    	byte[] buffer = new byte[4096];
    	//获取远程文件名 (xxx.a)
        String remoteFileName = "";
    	int i = remoteFilePath.lastIndexOf("/");
        if (i == -1) {
            i = remoteFilePath.lastIndexOf("\\");
            if(i == -1)
            	remoteFileName = remoteFilePath;
        }
        if (i != -1) {
        	remoteFileName = remoteFilePath.substring(i + 1);
        }  
        //获取本地路径分隔符
        String sep = "/";
        if (localFileDir.indexOf(sep) == -1)
        	sep = "\\";
        //查找本地是否有对应的临时文件 (xxxa.temp) 并获取本地文件对象
        boolean hasTemp = false;
        String localFileName = remoteFileName.replace(".", "") + ".temp";
        ArrayList<String> files = new ArrayList<String>();
        File localDir = new File(localFileDir);
        File[] tempList = localDir.listFiles();
        File localFile;
        for (File f : tempList) {
        	if (f.getName().equals(localFileName)) {
        		hasTemp = true;
        		localRAF = new RandomAccessFile(localFileDir + sep + localFileName, "rw");
        		localFile = f;
        	}   		
        }
       //创建数据socket及其IO流
    	dataSocket = createDataSocket();
    	BufferedInputStream socketIn = new BufferedInputStream(dataSocket.getInputStream());
    	BufferedOutputStream socketOut = new BufferedOutputStream(dataSocket.getOutputStream()); 	
        //如果本地有临时文件，即断点续传
    	if(hasTemp) {
        	Long localFileSize = localRAF.length(); //获取本地文件的大小
        	long remoteFileSize = getFileSize("网络编程实用教程.pdf");
        	if(localFileSize < remoteFileSize) {
        		int isEnd = 0; //是否读取完文件
        		int length = buffer.length; //每次传输所允许的最大缓冲区大小
        		int offset = localFileSize.intValue(); //设置传输偏移量，即本地文件大小
                sendCommand("RETR " + remoteFilePath); //获取远端文件，socket输入流开启
                response = readLine();
        		do {
        			socketIn.skip(offset);
        	        isEnd = socketIn.read(buffer);
        	        System.out.println(buffer[0]);
        	        if (isEnd != -1) {
        	            localRAF.write(buffer);
        	            System.out.println(buffer[0]);
        	        }
        	    } while (isEnd != -1); 	
       		 	response = readLine();
       		 	localRAF.close();
        		//传输完成，重命名
        		File newFilePath = new File(localFileDir + sep + remoteFileName);
        		localFile = new File(localFileDir + sep + localFileName);
        		localFile.renameTo(newFilePath);
        		socketOut.close();
                socketIn.close();
                dataSocket.close();//关闭此数据连接
                response = readLine();
                if (response.startsWith("226")) {
                    JOptionPane.showConfirmDialog(null,
                                    " 文件下载成功！",
                                    " 下载信息", JOptionPane.CLOSED_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE);            
                }
            }   
        }		
        
        else {
        	//如果本地没有对应的临时文件, 正常传输
            localFile = new File(localFileDir + sep + localFileName); //创建本地临时文件
            localRAF = new RandomAccessFile(localFileDir + sep + localFileName, "rwd");
            int isEnd = 0; //是否读取完文件
    		int length = buffer.length; //每次传输所允许的最大缓冲区大小
            sendCommand("RETR " + remoteFilePath); //获取远端文件，socket输入流开启
            response = readLine();
    		do {
    	        isEnd = socketIn.read(buffer);
    	        if (isEnd != -1) {
    	            localRAF.write(buffer);
    	        }
    	    } while (isEnd != -1); 	
    		 response = readLine();
     		localRAF.close();
            //传输完成，重命名
    		File newFilePath = new File(localFileDir + sep + remoteFileName);
    		localFile.renameTo(newFilePath);
    		socketOut.close();
            socketIn.close();
            dataSocket.close();//关闭此数据连接
            response = readLine();
            if (response.startsWith("226")) {
                JOptionPane.showConfirmDialog(null,
                                " 文件下载成功！",
                                " 下载信息", JOptionPane.CLOSED_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);            
            }
        }     	
    	
    }
    
    /**
     * 上传文件，无断点续传
     * @param localFileName 本地文件路径
     * @return
     * @throws IOException
     */
    public synchronized boolean upload(String localFileName) throws IOException {
        dataSocket = createDataSocket();
        int i = localFileName.lastIndexOf("/");
        if (i == -1) {
            i = localFileName.lastIndexOf("\\");
        }
        String element_1 = "";
        if (i != -1) {
            element_1 = localFileName.substring(i + 1);
        }
        sendCommand("STOR /var/ftp/test/" + element_1);
        response = readLine();
        if (!response.startsWith("1")) {
            System.out.println(response);
        }
        FileInputStream dataIn = new FileInputStream(localFileName);
        BufferedOutputStream dataOut = new BufferedOutputStream(dataSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        do {
            bytesRead = dataIn.read(buffer);
            if (bytesRead != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
        } while (bytesRead != -1);
        dataOut.flush();
        dataOut.close();
        dataIn.close();
        dataSocket.close();//关闭此数据连接
        response = readLine();

        if (response.startsWith("226")) {
            JOptionPane.showConfirmDialog(null,
                    " 文件上传成功！",
                    " 上传信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);            
        }
        return (response.startsWith("226"));
    }
    
    /**
     * 下载文件，无断点续传
     * @param remoteFile 远程文件路径（相对于当前工作目录）
     * @param localFile 本地存放路径
     * @return
     * @throws IOException
     */
    public synchronized boolean download(String remoteFile, String localFile) throws IOException {
        dataSocket = createDataSocket();
        sendCommand("RETR " + remoteFile);
        response = readLine();
        if (!response.startsWith("1")) {
            System.out.println(response);
        }
        System.out.println(localFile);
        BufferedInputStream dataIn = new BufferedInputStream(dataSocket.getInputStream());
        new File(localFile).createNewFile();
        FileOutputStream fileOut = new FileOutputStream(localFile);
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        do {
            bytesRead = dataIn.read(buffer);
            if (bytesRead != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
        } while (bytesRead != -1);
        fileOut.flush();
        fileOut.close();
        dataSocket.close();
        response = readLine();

        if (response.startsWith("226")) {
             JOptionPane.showConfirmDialog(null,
                    "下载成功",
                    " 下载信息", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);         
        }
        return (response.startsWith("226"));
    }
    
    /**
     * 在远程服务器的当前工作目录下创建目录
     * @param dirName 要创建的目录名
     * @throws IOException
     */
    public void mkdir(String dirName) throws IOException {

        if (!logined) {
            login();
        }

        sendCommand("XMKD " + dirName); // 创建目录
        response = readLine();
        if (!response.startsWith("257")) {      //FTP命令发送过程发生异常
            System.out.println( response);
        } else {
             JOptionPane.showConfirmDialog(null,
                    "创建目录"+dirName+"  成功！！",
                    " 创建目录", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);             //成功创建目录
        }

    }
    
    /**
     * 删除远程服务器当前工作目录下的某个目录
     * @param 要删除的目录名
     * @throws IOException
     */
    public void rmdir(String dirName) throws IOException {
        if (!logined) {                 //如果尚未与服务器连接，则连接服务器
            login();
        }

        sendCommand("XRMD " + dirName);
        response = readLine();
        if (!response.startsWith("250")) {     //FTP命令发送过程发生异常
            System.out.println(response);
        } else {
             JOptionPane.showConfirmDialog(null,
                    "删除目录"+dirName+"  成功！！",
                    " 删除目录", JOptionPane.CLOSED_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);          //成功删除目录
        }

    }
       
    /**
     * 建立与远程服务器的数据连接
     * @return 包含数据连接的socket
     * @throws IOException
     */
    private Socket createDataSocket() throws IOException {

        sendCommand("PASV ");               //采用Pasv模式（被动模式），由服务器返回数据传输的临时端口号，使用该端口进行数据传输
        response = readLine();
        if (!response.startsWith("227")) {      //FTP命令传输过程发生异常
            System.out.println(response);
        }
        String clientIp = "";
        int port = -1;
        int opening = response.indexOf('(');               //采用Pasv模式服务器返回的信息如“227 Entering Passive Mode (127,0,0,1,64,2)”
        int closing = response.indexOf(')', opening + 1);  //取"()"之间的内容：127,0,0,1,64,2 ，前4个数字为本机IP地址，转换成127.0.0.1格式
        if (closing > 0) {                                 //端口号由后2个数字计算得出：64*256+2=16386
            String dataLink = response.substring(opening + 1, closing);

            StringTokenizer arg = new StringTokenizer(dataLink, ",", false);
            clientIp = arg.nextToken();

            for (int i = 0; i < 3; i++) {
                String hIp = arg.nextToken();
                clientIp = clientIp + "." + hIp;
            }
            port = Integer.parseInt(arg.nextToken()) * 256 + Integer.parseInt(arg.nextToken());
        }

        return new Socket(clientIp, port);
    }

    /**
     * 从控制连接socket的输入流读取服务器的FTP响应信息
     * @return FTP响应信息 （响应码 详细信息）
     * @throws IOException
     */
    private String readLine() throws IOException {
        String line = inData.readLine();
        if (debug) {
            System.out.println("< " + line);
        }
        System.out.println(line);
        return line;
    }

    /**
     * 向控制连接socket的输入流域发送FTP命令行
     * @param line FTP命令行 （命令 参数）
     */
    private void sendCommand(String line) {
        if (connectSocket == null) {
            System.out.println("FTP尚未连接");         //未建立通信链接，抛出异常警告
        }
        try {
            outData.write(line + "\r\n");               //发送命令
            outData.flush();                            //刷新输出流
            System.out.println(line);
            if (debug) {
                System.out.println("> " + line);        //同时控制台输出相应命令信息，以便分析
            }
        } catch (Exception e) {
            connectSocket = null;
            System.out.println(e);
            return;
        }
    }
}
