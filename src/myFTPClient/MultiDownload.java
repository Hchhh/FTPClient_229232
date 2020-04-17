/**
 * 
 */
package myFTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
/**
 * @author HP
 * create time: 2020年4月16日下午9:33:59
 */
public class MultiDownload {
	
	private ExecutorService pool; //线程池
	private int poolSize; //线程池大小
	private FTPUtil ftp; //控制FTP，用于查询远端文件大小 
	private RandomAccessFile recordRAF; //记录文件对象
	
	/**
	 * 给定线程池大小，初始化多线程下载对象
	 * @param poolSize 线程池大小
	 */
	MultiDownload(int poolSize){
		this.poolSize = poolSize;
		pool = Executors.newFixedThreadPool(poolSize);
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

	/**
	 * 多线程下载，允许断点续传
	 * @param remoteFilePath 远端文件路径（相对于当前工作目录）
	 * @param localFileDir
	 * @throws IOException 
	 */
	public void download(String remoteFilePath, String localFileDir) throws IOException {
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
        File targetFile = new File(localFileDir + sep + remoteFileName);
        if(targetFile.exists()) {
        	return;
        }
		//获取远端文件大小
		long fullSize = ftp.getFileSize(remoteFilePath);
		ftp.close();
		long partSize1 = (long) Math.ceil(fullSize/poolSize); //前n-1个线程的下载数据量
		long partSize2 = fullSize - partSize1 * (poolSize - 1); //第n个线程的下载数据量
		//获取本地下载记录文件
		String recordFilePath = localFileDir + sep + remoteFileName.replace(".", "") + ".record";
		File recordFile = new File(recordFilePath);
		//如果没有本地下载记录，从头开始下载
		if(!recordFile.exists()) {
			long startPos;
			long endPos;
			recordFile.createNewFile();
			//写入记录文件并启动下载
			recordRAF = new RandomAccessFile(recordFilePath, "rw");
			int j = 0;
			String record;
			while (j < poolSize - 1) { //前n-1个线程
				startPos = j * partSize1;
				endPos = startPos + partSize1;
				record = "part:" + j + ", startPos:" +  startPos + ", endPos:" + endPos + "\n";
				recordRAF.seek(recordRAF.length());
				recordRAF.write(record.getBytes());
				pool.submit(new Thread(new MyThread(startPos, endPos, remoteFilePath, localFileDir, j)));
				j++;
			}
			if(j == poolSize - 1) {
				startPos = j * partSize1;
				endPos = startPos + partSize2;
				record = "part:" + j + ", startPos:" +  startPos + ", endPos:" + endPos + "\n";
				recordRAF.seek(recordRAF.length());
				recordRAF.write(record.getBytes());
				pool.submit(new Thread(new MyThread(startPos, endPos, remoteFilePath, localFileDir, j)));
			}
		}
		//如果有本地下载记录，断点续传
		else {
			//查询本地下载记录
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(recordFilePath));
				String record = "";
				RandomAccessFile tempRAF;
				while((record = br.readLine()) != null){
					//创建临时文件实例
					String[] elements = record.split(", ");
					int elemLen = elements[0].length();
					int id = Integer.parseInt(elements[0].substring(5, elemLen));
					elemLen = elements[1].length();
					long startPos = Long.parseLong(elements[1].substring(9, elemLen));
					elemLen = elements[2].length();
					long endPos = Long.parseLong(elements[2].substring(7, elemLen));
					//启动下载
					tempRAF = new RandomAccessFile(localFileDir + remoteFilePath.replace(".", "") + id + ".temp", "rw");
					long offset = startPos + tempRAF.length(); //本次下载 该线程的偏移量
					tempRAF.close();
					pool.submit(new MyThread(offset,endPos,remoteFilePath,localFileDir,id));
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("无本地下载记录！");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("本地下载记录中无该部分信息！");
				e.printStackTrace();
			}
		}
		
		pool.shutdown(); //全部线程执行结束后关闭线程池
	}
	
	/**
	 * 合并各线程下载好的文件部分
	 * @param fileDir 本地文件目录
	 * @param fileName 源文件名
	 * @param nPart 该文件拆分的数量
	 * @throws IOException 
	 */
	public synchronized void mergeFileParts(String fileDir, String fileName, int nPart) throws IOException { 
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
        File baseFile = new File(basePath); //基准文件
        long allSize = baseFile.length();
		String nextPath;
		File nextFile;
		RandomAccessFile baseRAF = new RandomAccessFile(baseFile, "rw");
		FileInputStream nextIn = null;
		for(int i = 1; i < nPart; i++) {
			//获取被合并文件
			nextPath = fileDir + sep + fileName.replace(".", "") + i + ".temp";
			nextFile = new File(nextPath);
			nextIn = new FileInputStream(nextFile);
			//开始合并
			baseRAF.seek(baseRAF.length());
			int length = 0;
			do {
				length = nextIn.read(buffer);
				if(length != -1) {
					baseRAF.write(buffer, 0, length);
				}
			} while(length != -1);
			nextIn.close();
			allSize += nextFile.length();
		}
		//如果数据大小正确（各部分文件之和等于基准文件）
		if(allSize == baseRAF.length()) {
			//重命名
			baseRAF.close();
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
			//删除记录文件
			String recordPath = fileDir + sep + fileName.replace(".", "") + ".record";
			File recordF = new File(recordPath);
			recordF.delete();
			System.out.println("文件"+ fileDir + sep + fileName + "合并完成");
		}
		else {
			System.out.println("文件"+ fileDir + sep + fileName + "合并失败");
		}
		
		
	}
	
	/**
	 * 各线程是否已执行完毕（需要先执行shutdown方法）
	 * @return
	 */
	public boolean isTerminated() {
		return pool.isTerminated();
	}
	
	public void clear() {
		try {
			recordRAF.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//开一个大小为3的固定线程池
		int poolSize = 3;
		MultiDownload md = new MultiDownload(poolSize); 
		String remotePath = "timg.gif";
		String localDir = "F:\\shared";
		try {
			md.download(remotePath, localDir);
			while(true) {
				if(md.isTerminated()) {
					md.clear();
					md.mergeFileParts(localDir, remotePath, 3);
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		
//		Thread t1 = new Thread(new MyThread(0, 3000000, remotePath, localDir, 0));
//		Thread t2 = new Thread(new MyThread(3000000, 6000000, remotePath, localDir, 1));
//		Thread t3 = new Thread(new MyThread(6000000, 9451142, remotePath, localDir, 2));
//		t1.start();
//		t2.start();
//		t3.start();
//		try {
//			md.mergeFileParts(localDir, remotePath, 3);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}

/**
 * 下载线程类
 * 每个线程下载一个固定大小（固定起始位置-固定终止位置）的文件块
 * @author HP
 * create time: 2020年4月17日上午12:20:01
 */
class MyThread extends Thread{
	private long startPos; //该线程下载的起始位置
	private long endPos; //该线程下载的终止位置
	private String remotePath; //服务器文件路径（相对于工作目录）
	private String localDir; //本地存放路径
	private int part; //该线程下载的文件块编号
	
	private FTPUtil ftp; //每个线程持有一个ftp对象
	
	public MyThread(long startPos, long endPos, String remotePath, String localDir, int part) {
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
			ftp.close();
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



