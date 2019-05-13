package org.pxz.filedistinct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * 文件剔重 1、输入文件夹名称； 2、遍历目录；
 * 3、如果是文件，生成MD5摘要，并查询当前MD5文件中是否存在，如果已经存在，则将文件复制（可选）到当前目录下的exists文件夹，删掉源文件；
 * 4、如果是目录，回到步骤2；
 */
public class FileDistinctApp {
	public static Set<String> ExistsMD5 = new HashSet<String>();
	//1 删除文件前先复制文件到当前目录下的exists文件夹；0 删除文件前不复制文件
	public static int isCopyDeletedFile=1;
	public static int isDeletedNullFolder=1;
	static {
		File md5File = new File("md5.txt");
		if (!md5File.exists()) {
			System.out.println("文件md5.txt不存在，程序自动创建");
			try {
				md5File.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			bakMD5File();
			initMD5();
		}
	}

	/**
	 * 
	 * @param args 目录路径
	 */
	public static void main(String[] args) {
		String picDirStr = "";
		if (args.length > 0) {
			picDirStr = args[0];
			System.out.println("picDirStr  =" + picDirStr);
		} else {
			System.out.println("使用方法：");
			System.out.println("\t 必选入参1：文件夹路径，");
			System.out.println("\t 可选入参2：删除已经存在的文件前，是否复制文件，1复制，0不复制 ，默认复制");
			System.out.println("\t 可选入参3：是否删除空目录，是否复制文件，1是，0否 ，默认是");
			System.exit(-1);
		}
		
		if (args.length >= 2) {
			if (Integer.valueOf(args[1]) == 0)
			isCopyDeletedFile = 0;
			if (Integer.valueOf(args[1]) == 1)
			isCopyDeletedFile = 1;
		}
		

		if (args.length >= 3) {
			if (Integer.valueOf(args[2]) == 0)
				isDeletedNullFolder = 0;
			if (Integer.valueOf(args[2]) == 1)
				isDeletedNullFolder = 1;
		}
		

		File picDirFile = new File(picDirStr);
		if (!picDirFile.exists()) {
			System.out.println("程序退出：文件不存在：" + picDirFile.getAbsolutePath());
			System.exit(0);
		}
		
		if (picDirFile.isFile()) {
			process(picDirFile);
		} else {
			File[] listFiles = picDirFile.listFiles();
			for (File subFile : listFiles) {
				process(subFile);
			}
		}

		//文件处理完毕，将md5结果写入文件
		try {
			File md5File = new File("md5.txt");
			BufferedWriter fbWriter = new BufferedWriter(new FileWriter(md5File));
			for (String md5Str : ExistsMD5) {
				fbWriter.write("\r\n");
				fbWriter.write(md5Str);
			}
			fbWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		deleteNullFolder(picDirStr);
	}

	public static void  deleteNullFolder(String folderName) {
		File folder = new File(folderName);
		if(folder.exists() && folder.isDirectory())  {
			if(folder.list().length > 0) {
		    //目录不为空
				for(File subFolder:folder.listFiles()) {
					deleteNullFolder(subFolder.getAbsolutePath());
				}				
			} else {
			//目录为空
				folder.delete();
			}
		}
	}
	
	public static void process(File inFile) {
		if (!inFile.exists()) {
			System.out.println("文件不存在：" + inFile.getAbsolutePath());
			return;
		}
		if (inFile.isDirectory()) {
			File[] subFiles = inFile.listFiles();
			for (File innerFile : subFiles) {
				process(innerFile);
			}
		} else {
			String md5Str = generateMD5(inFile);
			if (ExistsMD5.contains(md5Str)) {
				if(isCopyDeletedFile == 1) {
					// 文件已经存在，则删除复制后再删除				
					copyFile(inFile.getAbsolutePath());
				}				
				boolean isDeleted = inFile.delete();
				if (isDeleted) {
					System.out.println("删除文件"+inFile.getAbsolutePath()+"; result true=" + isDeleted);
				} else {
					System.out.println("删除文件"+inFile.getAbsolutePath()+"; result false="  + isDeleted);
				}
			} else {
				//文件不存在
				ExistsMD5.add(md5Str);
			}
		}
	}

	private static void copyFile(String srcPathStr) {

		File destDir = new File("");
		String desPathStr = destDir.getAbsolutePath();
		System.out.println("复制文件："+srcPathStr);
		// 1.获取源文件的名称
		String newFileName = srcPathStr.substring(srcPathStr.lastIndexOf("\\") + 1);
		String tmpPath1 = srcPathStr.substring(0, srcPathStr.lastIndexOf("\\"));
		tmpPath1 = tmpPath1.substring(tmpPath1.lastIndexOf("\\") + 1).replaceAll(":", "");
		System.out.println("tmpPath1=" + tmpPath1);
		File newPath = new File(destDir.getAbsoluteFile() + File.separator + tmpPath1);
		if (!newPath.exists()) {
			System.out.println("mkdir:" + newPath.getAbsolutePath() + "=" + newPath.mkdir());
		}
		// 目标文件地址
		// System.out.println(newFileName);
		desPathStr = newPath.getAbsolutePath() + File.separator + newFileName;
		// 源文件地址
		// System.out.println(desPathStr);
		String copyCMD = "cmd /c copy  \"" + srcPathStr + "\"   \"" + newPath.getAbsolutePath() + "\"";
		System.out.println("copyCMD=" + copyCMD);
		try {

			Process pr = Runtime.getRuntime().exec(copyCMD);
			InputStream inputStream = pr.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream(), "gb2312"));
			// String line = null;
			// List<String> resultListString = new ArrayList<String>();
			String resultString = "";
			System.out.println("CMD 运行结果：");
			while ((resultString = br.readLine()) != null) {
				System.out.println(resultString);
			}
			;
			pr.destroy();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * try { // 2.创建输入输出流对象 FileInputStream fis = new FileInputStream(srcPathStr);
		 * FileOutputStream fos = new FileOutputStream(desPathStr); // 创建搬运工具 byte
		 * datas[] = new byte[1024 * 8]; // 创建长度 int len = 0; // 循环读取数据 while ((len =
		 * fis.read(datas)) != -1) { fos.write(datas, 0, len); } // 3.释放资源 fis.close();
		 * fos.close(); } catch (Exception e) { e.printStackTrace(); }
		 */
		System.out.println("srcPathStr = " + srcPathStr);
		System.out.println("desPathStr = " + desPathStr);
		System.out.println("\n");

	}

	private static void bakMD5File() {
		File md5file = new File("md5.txt");
		String srcPathStr = md5file.getAbsolutePath();
		File destDir = new File("md5." + System.currentTimeMillis() + ".txt");
		String desPathStr = destDir.getAbsolutePath();
		try {
			// 2.创建输入输出流对象
			FileInputStream fis = new FileInputStream(srcPathStr);
			FileOutputStream fos = new FileOutputStream(desPathStr);
			// 创建搬运工具
			byte datas[] = new byte[1024 * 8];
			// 创建长度
			int len = 0;
			// 循环读取数据
			while ((len = fis.read(datas)) != -1) {
				fos.write(datas, 0, len);
			}
			// 3.释放资源
			fis.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("srcPathStr = " + srcPathStr);
		System.out.println("desPathStr = " + desPathStr);
		System.out.println("\n");
	}

	public static String generateMD5(File inFile) {
		String md5String = "";
		Runtime rn = Runtime.getRuntime();
		try {
			System.out.println("generateMD5 = "+inFile.getAbsolutePath());
			Process pr = rn.exec("certutil -hashfile  \"" + inFile.getAbsolutePath() + "\"  MD5");
			InputStream inputStream = pr.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream(), "gb2312"));
			// String line = null;
			// List<String> resultListString = new ArrayList<String>();
			br.readLine();
			md5String = br.readLine();// 第二行
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return md5String;
	}

	private static void initMD5() {
		Set<String> md5List = new HashSet<String>();
		try {
			BufferedReader bfReader = new BufferedReader(new FileReader(new File("md5.txt")));
			String lineString = "";
			while ((lineString = bfReader.readLine()) != null) {
				md5List.add(lineString);
			}
			ExistsMD5 = md5List;

			bfReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
