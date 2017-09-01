package com.gwall.core.eye.sysinfo;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.gwall.core.eye.utils.JacksonUtils;

/**
 * 系统信息获取
 * @ClassName: SystemInfoImpl 
 * @author: Administrator-yangkai
 * @date: 2016年11月23日 下午2:07:50 
 * @version: V1.0
 */
public class SystemInfoImpl implements SystemInfo {
	/**
	 * 创建sigar对象用来获取系统信息
	 */
	private Sigar sigar = new Sigar();
	//作为本次获取信息的唯一标识,由IP+进程ID标识
	private String onlyKey = null;
	private Properties props = System.getProperties();
	private String netInfoStart = "", netName = "";
	private long networkUpRSize = 0, networkUpTSize = 0;
	private DecimalFormat df = new DecimalFormat("0.00");
	private final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

	public SystemInfoImpl() {
		try {
			System.out.println(sigar.getCpu().getIdle());
			String name = ManagementFactory.getRuntimeMXBean().getName();  
			String pid = name.split("@")[0];
			NetInterfaceConfig config = this.sigar.getNetInterfaceConfig(null);
			netName = config.getName();
			netInfoStart = "{name:'" + config.getName() + "',ip:'" + config.getAddress() + "/" + config.getNetmask()
					+ "',mac:'" + config.getHwaddr() + "'";
			//当前IP+进程
			this.onlyKey = config.getAddress()+"-"+pid;
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取key
	 */
	public String getKey(){
		return this.onlyKey;
	}

	/**
	 * 获取系统信息
	 */
	public String getAllJson() {
		String time = sdf.format(new Date());
		StringBuilder sb = new StringBuilder("{time:'" + time + "',");
		try {
			// 操作系统
			sb.append("system:'"+getSystemInfo()+"',");
			// 运行环境
			sb.append("java:"+getRunLoadInfo()+",");
			// JVM信息
			sb.append("jvm:" + getJvmMemory()+",");
			// CPU信息
			String cpuInfo = getCpuInfo();
			sb.append("cpu:'" + cpuInfo + "',");
			// 内存信息
			String ramInfo = getRamInfo();
			sb.append("ramInfo:" + ramInfo + ",");
			// 网络信息
			String netInfo = getNetworkInfo();
			sb.append("netInfo:" + netInfo + ",");
			// 硬盘信息
			String fileSystemInfo = getFileSystemInfo();
			sb.append("fileSystemInfo:"+fileSystemInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.append("}").toString();
	}

	/**
	 * 获取硬盘信息
	 * [{dirname:'',devname:'',typename:'',filetype:'',total:'100G',used:'70G',usePercent:'32.44%'}]
	 */
	public String getFileSystemInfo() {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try {
			FileSystem[] fileSystemArray = sigar.getFileSystemList();
			Map<String,Object> map = new HashMap<String,Object>();
			for (FileSystem fileSystem : fileSystemArray) {
				FileSystemUsage fileSystemUsage = null;
				try {
					fileSystemUsage = sigar.getFileSystemUsage(fileSystem.getDirName());
				} catch (SigarException e) {// 当fileSystem.getType()为5时会出现该异常——此时文件系统类型为光驱
					continue;
				}
				if(fileSystemUsage != null){
					map = new HashMap<String,Object>();
					map.put("dirname", fileSystem.getDirName());
					map.put("devname", fileSystem.getDevName());
					map.put("filetype", fileSystem.getSysTypeName());
					map.put("total", df.format(fileSystemUsage.getTotal()/1024/1024));
					map.put("used", df.format(fileSystemUsage.getUsed()/1024/1024));
					map.put("usePercent",df.format(fileSystemUsage.getUsePercent() * 100));
					list.add(map);
				}
			}
		} catch (SigarException e1) {
			e1.printStackTrace();
		}
		return JacksonUtils.toJson(list);
	}

	/**
	 * CPU的使用率:20.00
	 */
	public String getCpuInfo() {
		try {
			CpuPerc cpuCerc = sigar.getCpuPerc();
			// 获取当前cpu的占用率
			return df.format(cpuCerc.getCombined() * 100);
		} catch (SigarException e) {
			e.printStackTrace();
		}
		return "error";
	}

	/**
	 * 获取系统内存信息
	 * {total:'100MB',used:'90MB',usedPercent:'90%'}
	 */
	public String getRamInfo() {
		try {
			Mem mem = sigar.getMem();
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("ram", mem.getTotal() / 1024 / 1024);
			map.put("used", mem.getUsed() / 1024 / 1024);
			map.put("usedPercent", df.format(mem.getUsedPercent()));
			map.put("type", "MB");
			return JacksonUtils.toJson(map);
		} catch (SigarException e) {
			e.printStackTrace();
		}
		return "{}";
	}

	/**
	 * 返回网卡流量等信息
	 * {name:'etf1',hostname:'yk',ip:'192.168.1.136/255.255.255.0',mac:'',dns:''
	 * ，dns1:''，send:0，read:0}
	 */
	public String getNetworkInfo() {
		StringBuilder sb = new StringBuilder();
		try {
			NetInfo info = this.sigar.getNetInfo();
			sb.append(netInfoStart);
			sb.append(",hostname:'" + info.getHostName() + "'");
			sb.append(",dns:'" + info.getPrimaryDns() + "'");
			sb.append(",dns1:'" + info.getSecondaryDns() + "'");
			/**
			 * 获取网络流量
			 */
			NetInterfaceStat ifstat = sigar.getNetInterfaceStat(netName);
			/**
			 * 获取发送的总字节数
			 */
			if (networkUpTSize == 0) {
				networkUpTSize = ifstat.getTxBytes();
			}
			if (networkUpRSize == 0) {
				networkUpRSize = ifstat.getRxBytes();
			}
			long t = ifstat.getTxBytes() - networkUpTSize;
			long r = ifstat.getRxBytes() - networkUpRSize;

			networkUpRSize = ifstat.getRxBytes();
			networkUpTSize = ifstat.getTxBytes();
			sb.append(",send:" + t + ",read:" + r + "}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 获取操作系统名称
	 */
	public String getSystemInfo() {
		Properties props=System.getProperties(); //获得系统属性集    
		return props.getProperty("os.name");
	}

	/**
	 * 获取运行环境信息
	 * {version:"1.8",home:"C:\Program Files\Java\jre1.8.0_31",dir:"D:\Work\Gwall\project\Gwall3.0\core-eye"}
	 */
	public String getRunLoadInfo() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("version", props.getProperty("java.version"));
		map.put("home", props.getProperty("java.home"));
		map.put("dir", props.getProperty("user.dir"));
		return JacksonUtils.toJson(map);
	}

	/**
	 * 获取虚拟机剩余内存
	 */
	public String getJvmMemory() {
		return (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "";
	}
}
