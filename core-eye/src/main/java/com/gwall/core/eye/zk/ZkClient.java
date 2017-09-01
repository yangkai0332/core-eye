package com.gwall.core.eye.zk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * zk客户端
 * 建立zk连接,处理数据监听和事件通知
 * @author yangkai
 *
 */
public class ZkClient {
	//tomcat的创建节点名称
	private String groupNode = "tomcats";
	//监听tomcat的节点
	private String tomcatPath = "/"+groupNode;
	//当前节点的名称,当前的IP和端口作为节点key=node
	private Map<String,Object> nodeData = new HashMap<String,Object>();
	private ZooKeeper zk;
	private Stat stat = new Stat();
	private volatile List<String> serverList;
	
	public ZkClient(String node,Map<String,Object> data) {
		if(data != null){
			this.nodeData = data;
		}
		this.nodeData.put("key_node", node);
	}
	
	/**
	 * 创建一个节点
	 * @param path	路径
	 * @param data	数据,请用UTF-8格式
	 * @param isls	是临时节点
	 * @return
	 */
	public String create(String path,byte data[],boolean isls){
		try{
			//如果节点不存在就创建
			if(zk.exists(path, false) == null){
				//如果没有这个节点，就创建，PERSISTENT永久保留，不会因断开连接而消失
				//EPHEMERAL临时节点;断开连接就自动删除
				return zk.create(path, data, Ids.OPEN_ACL_UNSAFE, 
						isls ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
			}else{
				return path;
			}
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 根据指定路径获取数据
	 * @param path
	 * @return
	 */
	public String getData(String path){
		byte[] datas;
		try {
			datas = zk.getData("/testRootPath/testChildPathOne", true, null);
			String str = new String(datas, "UTF-8");
			return str;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 设置指定路径的数据,请使用UTF-8,默认获取数据转换为UTF-8
	 * @param path
	 * @param data
	 * @return
	 */
	public boolean update(String path,byte data[]){
		// 修改子目录节点数据
		try {
			//根据路径,数据,进行修改,-1标识不检查版本号
			Stat stat = zk.setData(path, data, -1);
			if(stat != null){
				return true;
			}
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 删除指定目录数据
	 * @param path
	 * @return
	 */
	public boolean delete(String path){
		// 删除整个子目录 -1代表version版本号，-1是删除所有版本
		try {
			zk.delete(path, -1);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 根据指定路径;获取子集
	 * @param path
	 * @return
	 */
	public List<String> getChildren(String path){
		List<String> list = new ArrayList<String>();
		List<String> subList;
		try {
			subList = zk.getChildren(path, true);
			for (String subNode : subList) {
				// 获取每个子节点下关联的server地址
				byte[] data = zk.getData(path + "/" + subNode, false, stat);
				list.add(new String(data, "UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 连接zookeeper
	 */
	public void connectZookeeper() throws Exception {
		//zk = new ZooKeeper("localhost:2181,localhost:2182,localhost:2183", 5000, new Watcher() {
		zk = new ZooKeeper("localhost:2181", 5000, new Watcher() {
			public void process(WatchedEvent event) {
				// 如果发生了"/sgroup"节点下的子节点变化事件, 更新server列表, 并重新注册监听
				if (event.getType() == EventType.NodeChildrenChanged && (tomcatPath).equals(event.getPath())) {
					try {
						updateServerList();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		//如果tomcat节点不存在就创建
		if(zk.exists(tomcatPath, false) == null){
			//如果没有这个节点，就创建，永久保留，不会因断开连接而消失
			zk.create(tomcatPath, groupNode.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		//将自己添加到这个tomcats节点中去
		// 子节点的类型设置为EPHEMERAL_SEQUENTIAL, 表明这是一个临时节点, 且在子节点的名称后面加上一串数字后缀
		// 将server的地址数据关联到新创建的子节点上
		String createdPath = zk.create(tomcatPath+ "/" + nodeData.get("key_node").toString(), 
				//这里放json数据
				nodeData.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL);
		System.out.println("create: " + createdPath+" data:"+nodeData.toString().getBytes("UTF-8"));
		updateServerList();
	}

	/**
	 * 更新server列表
	 */
	private void updateServerList() throws Exception {
		// 替换server列表
		serverList = getChildren(tomcatPath);
		System.out.println("server list updated: " + serverList);
	}

	/**
	 * client的工作逻辑写在这个方法中 此处不做任何处理, 只让client sleep
	 */
	public void handle() throws InterruptedException {
		Thread.sleep(Long.MAX_VALUE);
	}

	public static void main(String[] args) throws Exception {
		ZkClient ac = new ZkClient("tset-3",null);
		ac.connectZookeeper();
		ac.handle();
	}
}