/**
 * 
 */
package myFTPClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
/**
 * @author HP
 * create time: 2020年4月16日下午9:33:59
 */
public class MultiDownload {
	
	private ExecutorService pool; //线程池
	private int poolSize; //线程池大小
	
	MultiDownload(int poolSize){
		pool = Executors.newFixedThreadPool(poolSize);
	}
	
	/**
	 * 合并各线程下载好的文件部分
	 * @param fileDir 本地文件目录
	 * @param fileName 源文件名
	 * @param nPart 该文件拆分的数量
	 * @throws IOException 
	 */
	public static void mergeFileParts(String fileDir, String fileName, int nPart) throws IOException { 
		if (nPart < 1)
			return;
		byte[] buffer = new byte[1024];
		//获取本地路径分隔符
        String sep = "/";
        if (fileDir.indexOf(sep) == -1)
        	sep = "\\";
		File targetFile = new File(fileDir + sep + fileName);
		if(targetFile.exists())
			return;
        //建立文件对象
        String basePath = fileDir + sep + fileName.replace(".", "") + 0 + ".temp";
		RandomAccessFile baseRAF = new RandomAccessFile(basePath, "rw"); //基准文件
        long allSize = baseRAF.length();
		String nextPath;
		RandomAccessFile nextRAF = null;
		for(int i = 1; i < nPart; i++) {
			//获取被合并文件
			nextPath = fileDir + sep + fileName.replace(".", "") + i + ".temp";
			nextRAF = new RandomAccessFile(nextPath, "r");
			//开始合并
			long baseLen = baseRAF.length();
			baseRAF.seek(baseLen);
			int length;
			while((length = nextRAF.read(buffer)) != -1) {
				baseRAF.write(buffer, 0, length);
				allSize += length;
			}
		}
		//如果数据大小正确（各部分文件之和等于基准文件）
		if(allSize == baseRAF.length()) {
			//重命名
			baseRAF.close();
			nextRAF.close();
			File currentFile = new File(basePath);
			currentFile.renameTo(targetFile);
			//删除临时文件
			File tempF;
			String tempPath;
			for (int j = 1; j < nPart; j++) {
				tempPath = fileDir + sep + fileName.replace(".", "") + j + ".temp";
				tempF = new File(tempPath);
				tempF.delete();
			}
			System.out.println("文件"+ fileDir + fileName + "合并完成");
		}
		else {
			System.out.println("文件"+ fileDir + fileName + "合并失败");
		}
		
		
	}
	
	public static void main(String[] args) {
		//开一个大小为3的固定线程池
		int poolSize = 3;
		MultiDownload md = new MultiDownload(poolSize); 
		String remotePath = "网络编程实用教程.pdf";
		String localDir = "F:\\shared";
//		Thread t1 = new Thread(new MyThread(0, 3000000, remotePath, localDir, 0));
//		Thread t2 = new Thread(new MyThread(3000000, 6000000, remotePath, localDir, 1));
//		Thread t3 = new Thread(new MyThread(6000000, 9451142, remotePath, localDir, 2));
//		t1.start();
//		t2.start();
//		t3.start();
		try {
			mergeFileParts(localDir, remotePath, 3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/**
 * 下载线程类
 * 每个线程下载一个固定大小（固定起始位置-固定终止位置）的文件块
 * @author HP
 * create time: 2020年4月17日上午12:20:01
 */
class MyThread extends Thread{
	private int startPos; //该线程下载的起始位置
	private int endPos; //该线程下载的终止位置
	private String remotePath; //服务器文件路径（相对于工作目录）
	private String localDir; //本地存放路径
	private int part; //该线程下载的文件块编号
	
	private FTPUtil ftp; //每个线程持有一个ftp对象
	
	public MyThread(int startPos, int endPos, String remotePath, String localDir, int part) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.remotePath = remotePath;
		this.localDir = localDir;
		this.part = part;
	}
	
	/**
	 * 初始化FTP连接并登录
	 */
	public void initFTP() {
		ftp = new FTPUtil();
		ftp.setRemoteHost("62.234.12.47");
		ftp.setRemotePort(21);
		ftp.connect();
		ftp.setUser("ubuntu");
		ftp.setPW("Ubuntu111!");
		ftp.login();
		try {
			ftp.cwd("/var/ftp/test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("线程"+part+"启动");
		initFTP();
		long downloadSize = 0; //实际下载的文件大小
		try {
			downloadSize = ftp.downloadPart(startPos, endPos, remotePath, localDir, part);
			if (downloadSize == (endPos - startPos)) {
				System.out.println("文件块"+part+"下载成功");
			}
			else {
				System.out.println("文件块"+part+"下载失败");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}



/**
 * 下载线程类
 * 每个线程下载一个固定大小（固定起始位置-固定终止位置）的文件块
 * @author HP
 * create time: 2020年4月17日上午12:20:01
 */
class Task implements Runnable{
	private int startPos; //该线程下载的起始位置
	private int endPos; //该线程下载的终止位置
	private String remotePath; //服务器文件路径（相对于工作目录）
	private String localDir; //本地存放路径
	private int part; //该线程下载的文件块编号
	
	private FTPUtil ftp; //每个线程持有一个ftp对象
	
	public Task(int startPos, int endPos, String remotePath, String localDir, int part) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.remotePath = remotePath;
		this.localDir = localDir;
		this.part = part;
	}
	
	/**
	 * 初始化FTP连接并登录
	 */
	public void initFTP() {
		ftp.setRemoteHost("62.234.12.47");
		ftp.setRemotePort(21);
		ftp.connect();
		ftp.setUser("ubuntu");
		ftp.setPW("Ubuntu111!");
		ftp.login();
		try {
			ftp.cwd("/var/ftp/test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("线程"+part+"启动");
		initFTP();
		long downloadSize = 0; //实际下载的文件大小
		try {
			downloadSize = ftp.downloadPart(startPos, endPos, remotePath, localDir, part);
			if (downloadSize == (endPos - startPos)) {
				System.out.println("文件块"+part+"下载成功");
			}
			else {
				System.out.println("文件块"+part+"下载失败");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
