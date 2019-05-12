package md5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryMD5 {
	private static Set<String> existsMD5 = null;
	static {
		if (!new File("md5.txt").exists()) {
			System.out.println("文件md5.txt不存在，请创建");
			System.exit(-1);
		}
		initMD5();
	}

	public static boolean isExistsMD5(String instr) {
		boolean isExists = existsMD5.contains(instr);
		if (!isExists) {
			existsMD5.add(instr);
			
		} 
		return isExists;
	}

	public static void initMD5() {
		Set<String> md5List = new HashSet<String>();
		try {
			BufferedReader bfReader = new BufferedReader(new FileReader(new File("md5.txt")));
			String lineString = "";
			while ((lineString = bfReader.readLine()) != null) {
				md5List.add(lineString);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		existsMD5 = md5List;
	}
}
