/**
 * 
 */
package myFTPClient;

import java.io.BufferedOutputStream;
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

import javax.swing.JOptionPane;
/**
 * @author HP
 * create time: 2020年4月16日下午9:33:59
 */
public class MultiDownload {
	
	private ExecutorService pool; //线程池
	private int poolSize; //线程池大小
	private FTPUtil ftp; //控制FTP，用于查询远端文件大小 
	private ArrayList<long[]> downloadRecords;
	private String remotePath; 
	private String localDir;
	private String fileName;
	private long fileSize;
	private boolean isMerged = false; //是否合并完成
	
	/**
	 * 初始化多线程下载对象：新建线程池，开启FTP连接并获取下载信息
	 * @param poolSize 线程池大小
	 * @param remotePath 要下载的文件在远端的存放路径（包含文件名）
	 * @param 要下载到的目录（不包含文件名）
	 * @param 要下载的文件名
	 */
	public MultiDownload(int poolSize, String remotePath, String localDir, String fileName){
		this.poolSize = poolSize;
		this.remotePath = remotePath;
		this.localDir = localDir;
		this.fileName = fileName;
		pool = Executors.newFixedThreadPool(poolSize);
		ftp = new FTPUtil();
		ftp.setRemoteHost("62.234.12.47");
		ftp.setRemotePort(21);
		ftp.connect();
		ftp.setUser("ubuntu");
		ftp.setPW("Ubuntu111!");
		ftp.threadLogin();
		try {
			ftp.cwd("/var/ftp/test");
			this.fileSize = ftp.getFileSize(remotePath);
			createRecord();
			downloadRecords = readRecord();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isMerged() {
		return this.isMerged;
	}
	
	/**
	 * 获取路径分隔符
	 * @param path
	 * @return
	 */
	public String getSlash(String path) {
		String slash = "\\";
		if(path.indexOf(slash) == -1)
			slash = "/";
		return slash;
	}
	
	/**
	 * 创建本地记录文件
	 */
	public void createRecord() {
		File recordFile = new File(localDir + getSlash(localDir) + fileName.replace(".", "") + ".record");
		if(recordFile.exists())
			return;
		else {
			try {
				recordFile.createNewFile();
				long partSize1 = (long) Math.ceil(fileSize/poolSize); //前n-1个线程的下载数据量
				long partSize2 = fileSize - partSize1 * (poolSize - 1); //第n个线程的下载数据量
				int i = 0;
				String record;
				BufferedOutputStream recordOut = new BufferedOutputStream(new FileOutputStream(recordFile));
				while(i < poolSize - 1) {
					long startPos = i * partSize1;
					long endPos = startPos + partSize1;
					record = "part:" + i + ", startPos:" +  startPos + ", endPos:" + endPos + "\n";
					recordOut.write(record.getBytes());
					recordOut.flush();
					i++;
				}
				
				if(i == poolSize - 1) {
					long startPos = i * partSize1;
					long endPos = startPos + partSize2;
					record = "part:" + i + ", startPos:" +  startPos + ", endPos:" + endPos + "\n";
					recordOut.write(record.getBytes());
					recordOut.flush();
				}
				recordOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 *从本地文件中读取下载记录
	 * @return
	 */
	public ArrayList<long[]> readRecord() {
		ArrayList<long[]> res = new ArrayList<long[]>();
		String slash = getSlash(localDir);
		String recordPath = localDir + slash + fileName.replace(".", "") +".record";
		try {
			BufferedReader br = new BufferedReader(new FileReader(recordPath));
			String record;
			try {
				while((record = br.readLine()) != null) {
					String[] elements = record.split(", ");
					int id = Integer.parseInt(elements[0].substring(5,elements[0].length()));
					long startPos = Long.parseLong(elements[1].substring(9,elements[1].length()));
					long endPos = Long.parseLong(elements[2].substring(7,elements[2].length()));
					res.add(new long[] {id, startPos, endPos});
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * 多线程下载，允许断点续传（未考虑线程安全）
	 * @throws IOException 
	 */
	public void download() throws IOException {
    	//获取远程文件名 (xxx.a)
        String remoteFileName = "";
    	int i = remotePath.lastIndexOf("/");
        if (i == -1) {
            i = remotePath.lastIndexOf("\\");
            if(i == -1)
            	remoteFileName = remotePath;
        }
        if (i != -1) {
        	remoteFileName = remotePath.substring(i + 1);
        }  
        //获取本地路径分隔符
        String sep = "/";
        if (localDir.indexOf(sep) == -1)
        	sep = "\\";
        File targetFile = new File(localDir + sep + remoteFileName);
        if(targetFile.exists()) 
        	return;
		//是否有临时文件
        boolean hasTemp = false;
        String localFileName = remoteFileName.replace(".", "") + "0.temp";
        File localDir = new File(this.localDir);
        File[] tempList = localDir.listFiles();
        File localFile;
        for (File f : tempList) {
        	if (f.getName().equals(localFileName)) {
        		hasTemp = true;
        	}   		
        }
        //如果没有临时文件，从头下载
        DownloadTask downloadTask;
		if(!hasTemp) {
			for(int j = 0; j < downloadRecords.size(); j++) {
				long[] record = downloadRecords.get(j);
				int id = (int) record[0];
				long startPos = record[1];
				long  endPos = record[2];
				downloadTask = new DownloadTask(startPos, endPos, remotePath, this.localDir, id);
				pool.submit(downloadTask);
			}
		}
		//如果有本地下载记录，断点续传
		else {
			File tempFile;
			for(int k = 0; k < downloadRecords.size(); k++) {
				long[] record = downloadRecords.get(k);
				int id = (int) record[0];
				long startPos = record[1];
				long  endPos = record[2];
				tempFile = new File(this.localDir + remoteFileName.replace(".", "") + id + ".temp");
				long offset = startPos + tempFile.length(); //本次下载 该线程的偏移量
				downloadTask = new DownloadTask(offset,endPos,remotePath,this.localDir,id);
				pool.submit(downloadTask);
			}
		}
		pool.shutdown(); //全部线程执行结束后关闭线程池
	}
	
	/**
	 * 合并各线程下载好的文件部分
	 * @throws IOException 
	 */
	public void mergeFileParts() throws IOException { 
		if (poolSize < 1)
			return;
		byte[] buffer = new byte[1024];
		//获取本地路径分隔符
        String sep = "/";
        if (localDir.indexOf(sep) == -1)
        	sep = "\\";
		File targetFile = new File(localDir + sep + fileName);
		if(targetFile.exists())
			return;
        //建立文件对象
        String basePath = localDir + sep + fileName.replace(".", "") + 0 + ".temp";
        File baseFile = new File(basePath); //基准文件
        long allSize = baseFile.length();
		String nextPath;
		File nextFile;
		RandomAccessFile baseRAF = new RandomAccessFile(baseFile, "rw");
		FileInputStream nextIn = null;
		for(int i = 1; i < poolSize; i++) {
			//获取被合并文件
			nextPath = localDir + sep + fileName.replace(".", "") + i + ".temp";
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
			for (int j = 1; j < poolSize; j++) {
				tempPath = localDir + sep + fileName.replace(".", "") + j + ".temp";
				tempF = new File(tempPath);
				tempF.delete();
			}
			//删除记录文件
			String recordPath = localDir + sep + fileName.replace(".", "") + ".record";
			File recordF = new File(recordPath);
			recordF.delete();
			isMerged = true;
			System.out.println("文件"+ localDir + sep + fileName + "合并完成");
		}
		else {
			System.out.println("文件"+ localDir + sep + fileName + "合并失败");
		}
		
	}
	
	/**
	 * 各线程是否已执行完毕（需要先执行shutdown方法）
	 * @return
	 */
	public boolean isTerminated() {
		return pool.isTerminated();
	}
	
	/**
	 * 线程安全的多线程下载
	 */
	public void safeDownload() {
		try {	
			download(); //下载
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true) {
			if(isTerminated()) {
				try {	
					mergeFileParts(); //合并
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * 获取下载进度（各线程进度以及总进度）
	 * @return
	 */
	public double[] getProgress() {
		double[] res = new double[poolSize + 1];
		if(isMerged) {
			for(int i = 0; i < poolSize + 1; i++) {res[i] = 100;}
			return res;
		}
		//读取已下载大小并计算进度
		long downloadSize = 0;
		File tempFile;
		for(int i = 0; i < poolSize; i++) {
			tempFile = new File(localDir + getSlash(localDir) + fileName.replace(".", "") + i + ".temp");
			long partSize = tempFile.length();
			long needSize = downloadRecords.get(i)[2] - downloadRecords.get(i)[1];
			res[i] = partSize / needSize * 100;
			downloadSize += partSize;
		}
		return res;
	}

	/**
	 * 暂停所有正在进行的任务
	 */
	public void pause() {
		
	}

}

/**
 * 下载线程类
 * 每个线程下载一个固定大小（固定起始位置-固定终止位置）的文件块
 * @author HP
 * create time: 2020年4月17日上午12:20:01
 */
class DownloadTask extends Thread{
	private long startPos; //该线程下载的起始位置
	private long endPos; //该线程下载的终止位置
	private String remotePath; //服务器文件路径（相对于工作目录）
	private String localDir; //本地存放路径
	private int part; //该线程下载的文件块编号
	
	private FTPUtil ftp; //每个线程持有一个ftp对象
	
	public DownloadTask(long startPos, long endPos, String remotePath, String localDir, int part) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.remotePath = remotePath;
		this.localDir = localDir;
		this.part = part;
	}
	
	/**
	 * 初始化该线程的FTP连接并登录
	 */
	public void initFTP() {
		ftp = new FTPUtil();
		ftp.setRemoteHost("62.234.12.47");
		ftp.setRemotePort(21);
		ftp.connect();
		ftp.setUser("ubuntu");
		ftp.setPW("Ubuntu111!");
		ftp.threadLogin();
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
		long downloadSize = 0;
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



