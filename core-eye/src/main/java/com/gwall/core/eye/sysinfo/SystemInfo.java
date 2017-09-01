package com.gwall.core.eye.sysinfo;

/**
 * 系统信息收集
 * @ClassName: ISystemInfo 
 * @author: Administrator-yangkai
 * @date: 2016年11月23日 下午2:08:18 
 * @version: V1.0
 */
public interface SystemInfo {
	
	/**
	 * 获取IP-进程ID
	 * @return
	 */
	public String getKey();
	/**
	 * 获取当前所有可以获取的数据拼接到json
	 * @return
	 */
	public String getAllJson();
	/**
	 * 获取cpu使用率
	 * @return
	 */
	public String getCpuInfo();
	
	/**
	 * 获取系统内存使用量
	 * @return
	 */
	public String getRamInfo();

	/**
	 * 获取网络使用流量
	 * @return {name:'',hostname:'',ip:'',mac:'',dns:''，dns1:''，send:0，read:0}
	 */
	public String getNetworkInfo();
	
	/**
	 * 获取磁盘信息
	 * @return: [{dirname:'',devname:'',typename:'',filetype:'',total:'100G',free:'30G',used:'70G',usePercent:'32.44%'}]
	 */
	public String getFileSystemInfo();

	/**
	 * 获取操作系统信息
	 * @return
	 */
	public String getSystemInfo();

	/**
	 * 获取运行环境信息
	 * @return
	 */
	public String getRunLoadInfo();

	/**
	 * 获取虚拟机剩余内存
	 * @return
	 */
	public String getJvmMemory();
}
