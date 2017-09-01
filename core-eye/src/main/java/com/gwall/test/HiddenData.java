package com.gwall.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 将数据加密隐藏到图片文件中去
 * 
 * @author yangkai
 * 2016-12-30 10:22:33
 */
public class HiddenData {
	
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	public static void main(String[] args) {
		byte[] data;
		try {
			data = "test1234991429291410241204812894ce顺风顺水sdfljksdlf 阿斯顿发的说法阿萨德佛文件佛文件佛年底f123124124124".getBytes("UTF-8");
			String newfilePath = HiddenData.encrypt("D:/test/test1.png",data);
			System.out.println("加密前数据:"+new String(data,"UTF-8"));
			System.out.println("加密后文件:"+newfilePath);
			byte[] data2 = HiddenData.decrypt(newfilePath);
			String dt = new String(data2,"UTF-8");
			System.out.println("解密后数据:"+dt);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 是否可用于加密,文件未被加密,文件可用用于加密
	 * @param file
	 * @return
	 */
	private static boolean isAvailable(File file){
		RandomAccessFile rf = null;
		try {
			rf = new RandomAccessFile(file, "rw");
			long len = rf.length();
			if(len - 14 > 0){
				rf.seek(len-14);
				byte[] endD = new byte[3];
				rf.read(endD,0,3);
				String endS = new String(endD);
				if("GOD".equals(endS)){
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(rf != null){
				try {
					rf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	/**
	 * 生成数字
	 * @return
	 */
	private static int getRandomInt(){
		return RANDOM.nextInt(15191314);
	}
	/**
	 * 生成随机数字再MD5的秘钥
	 * @return
	 */
	private static String getMd5Key(){
		return DigestUtils.md5Hex(""+getRandomInt());
	}
	
	/**
	 * 将数据加密到指定文件中,必须是png图片格式文件
	 * @param filePath
	 * @param data	utf-8格式的二进制数据
	 * @return	返回新文件路径
	 */
	public static String encrypt(String filePath,byte[] data)throws Exception{
		filePath = backupFile(filePath);
		if(filePath == null){
			throw new Exception("备份文件失败,请确认文件路径!");
		}
		File file = new File(filePath);
		if(!file.isFile() || !file.getName().endsWith(".png")){
			throw new Exception("此文件不支持加密!");
		}
		RandomAccessFile rf = null;
		try {
			if(!isAvailable(file)){
				throw new Exception("此文件已经被加密过,无法再次加密!");
			}
			// 打开一个随机访问文件流，按读写方式
			rf = new RandomAccessFile(file, "rw");
			// 文件长度，字节数
			long len = rf.length();
			rf.seek(len-11);	//跳过图片结尾
			byte[] endData = new byte[11];
			rf.read(endData,0,11);
			rf.seek(len-11);
			String key = getMd5Key();
			DesUtils des = new DesUtils(key);// 自定义密钥
			byte[] newData = des.encrypt(data);
			//生成干扰数据
			String grdt = getMd5Key();
			int st = (int)(len-11);
			//图片文件数据32B干扰数据真实数据32B干扰数据4BF,4BSTART,4BF,4BEND,4BF,32BKEY,4BF,4BF
			//写入干扰数据32
			rf.write(grdt.getBytes());
			//写入真是数据
			int i=newData.length,n=0,m = 2048;
			while(i >= m){
				rf.write(newData,n,m);
				n = m;
				m += 2048;
			}
			if(m - i > 0){
				rf.write(newData,n,i);
			}
			int se = st+32+newData.length;
			//生成干扰数据
			grdt = getMd5Key();
			//写入干扰数据32
			rf.write(grdt.getBytes());
			rf.writeInt(getRandomInt());
			rf.writeInt(st);
			rf.writeInt(getRandomInt());
			rf.writeInt(se);
			rf.writeInt(getRandomInt());
			rf.write(key.getBytes());
			rf.writeInt(getRandomInt());
			rf.writeInt(getRandomInt());
			byte[] endd = "GOD".getBytes();
			rf.write(endd);
			rf.write(endData);
			//写入完毕
			return filePath;
		}catch(Exception e){
			throw new Exception("文件解密异常:"+e.getMessage());
		}finally{
			if (rf != null) {
				try {
					rf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 根据上面加密的文件,进行解密动作
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(String filePath)throws Exception{
		File file = new File(filePath);
		if(!file.isFile() || !file.getName().endsWith(".png")){
			throw new Exception("此文件不支持解密!");
		}
		RandomAccessFile rf = null;
		try {
			if(isAvailable(file)){
				throw new Exception("此文件不支持解密!");
			}
			// 打开一个随机访问文件流，按读写方式
			rf = new RandomAccessFile(file, "rw");
			// 文件长度，字节数
			long len = rf.length();
			rf.seek(len-14-60);	//跳过图片结尾  和尾部数据
			//图片文件数据32B干扰数据真实数据32B干扰数据4BF,4BSTART,4BF,4BEND,4BF,32BKEY,4BF,4BF
			rf.readInt();
			int start = rf.readInt();
			rf.readInt();
			int end = rf.readInt();
			rf.readInt();
			byte keys[] = new byte[32];
			rf.read(keys,0,32);
			rf.readInt();
			rf.readInt();
			String key = new String(keys);
			rf.seek(start);
			rf.read(keys,0,32);
			int size = end-start-32;
			byte data[] = new byte[size];
			int i=0,n=2048;
			while(i+n <= size){
				rf.read(data,i,n);
				i+=n;
			}
			if(i+n > size){
				rf.read(data,i,size);
			}
			DesUtils des = new DesUtils(key);// 自定义密钥
			byte[] ds = des.decrypt(data);
			return ds;
		}catch(Exception e){
			throw new Exception("解析文件异常:"+e.getMessage());
		}finally{
			if (rf != null) {
				try {
					rf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
     * 复制文件源文件到指定路径后,删除源文件
     * @param path
     * @return backFilePath
     */
    public static String backupFile(String path){
    	try {
    		File f1 = new File(path);
    		String[] sn = f1.getName().split("\\.");
        	File f2 = new File(f1.getParentFile()+"\\"+sn[0]+"-"+System.currentTimeMillis()+"."+sn[1]);
			f2.createNewFile();
			if(f1 != null && f1.exists() && f2 != null && f2.exists()){
	    		if(fileChannelCopy(f1,f2)){
	    			return f2.getAbsolutePath();
	    		}
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
	 * 使用文件通道的方式复制文件
	 * @param s
	 *            源文件
	 * @param t
	 *            复制到的新文件
	 */
	private static boolean fileChannelCopy(File s, File t){
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();// 得到对应的文件通道
			out = fo.getChannel();// 得到对应的文件通道
			in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}

