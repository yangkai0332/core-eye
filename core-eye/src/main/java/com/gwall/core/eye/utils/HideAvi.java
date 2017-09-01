package com.gwall.core.eye.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.codec.digest.DigestUtils;

public class HideAvi {

	public static void main(String[] args) {
		try {
			String s = "111111111122222A222B444444F44433";
			//System.out.println(s.length()+" "+s+" "+s.getBytes("UTF-8").length);
			new HideAvi().testWriteData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testWriteData(){
		File file = new File("D:/test/ytp.png");
		String data = "1这里是测试数据,{obj:[{value:1,name:2}]},完成end123";
		RandomAccessFile randomFile = null;
		try {
			String fmd5 = DigestUtils.md5Hex(new FileInputStream(file));
			System.out.println("file-1md5:"+fmd5);
			// 打开一个随机访问文件流，按读写方式
			randomFile = new RandomAccessFile(file, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			System.out.println(fileLength);
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength-11);
			byte ds[] = data.getBytes("UTF-8");
			byte b[] = new byte[11];
			randomFile.read(b);
			randomFile.seek(fileLength-11);
			randomFile.write(ds);
			randomFile.write(b);
			System.out.println("写入完成");
			fmd5 = DigestUtils.md5Hex(new FileInputStream(file));
			System.out.println("file-2md5:"+fmd5);
			
			FileInputStream in = new FileInputStream(file);
			byte ddss[] = new byte[ds.length];
			in.skip(fileLength-11);
			int n = in.read(ddss);
			System.out.println(n+""+new String(ddss,"UTF-8"));
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (randomFile != null) {
				try {
					randomFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void test1() throws Exception {

		// 1.找到合并的图片
		File fileImg = new File("D:\\test/ytp.png");
		// 找到需要隐藏的avi
		File fileAvi = new File("D:\\test/icon.png");

		// 2.建立文件的输入输出流
		// 读取图片流（二进制）
		FileInputStream imgIn = new FileInputStream(fileImg);
		// 读取视频流（二进制）
		FileInputStream aviIn = new FileInputStream(fileAvi);

		// 将合并后的文件输出
		FileOutputStream out = new FileOutputStream(new File("D:\\test/4.png"));
		/**
		 * 第二种方法：使用Vector集合 最终目的都是要获取取出枚举对象
		 */
		// 3.创建集合，添加字节流对象
		Vector<FileInputStream> vector = new Vector<FileInputStream>();
		vector.add(imgIn);
		vector.add(aviIn);

		// 4.获取集合的枚举对象，理解为迭代------是因为序列流中需要Enumeration类型的参数
		/**
		 * 查看api，发现迭代器与枚举有两点不同 1.迭代器允许调用者利用定义良好的语义在迭代期间从迭代器所指向的 collection 移除元素。
		 * 2.方法名称得到了改进。 迭代器就是多了个remove()方法，从迭代器指向的 collection 中移除迭代器返回的最后一个元素。
		 */
		Enumeration<FileInputStream> en = vector.elements();

		// 5.创建序列流对象，接受集合的枚举对象，完成对流的合并
		SequenceInputStream sis = new SequenceInputStream(en);

		// 6.缓冲字节数组
		byte[] buf = new byte[4024];
		int len = 0;
		while ((len = sis.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
		// 7.关闭资源
		sis.close();
		out.close();
	}
}