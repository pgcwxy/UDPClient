import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * UDP发送日志工具类，需要使用者做一些准备工作才能使用<br>
 *     1.修改下面静态代码块中的配置文件名称###为自己应用的配置文件名称，注意自己的配置文件要在根目录<br>
 *         2.配置文件中需要准备两个key，value，例子如下:<br>
 *              UDPClient_ADDRS=10.0.79.160:10011,10.0.79.160:10012（可以使用逗号分隔，代表发送多个目标端，也可以不分割）<br>
 *              environment=test             <br>
 *                  3.依赖问题，本工具类需要的依赖如下：<br>
 *                      <dependency>
                            <groupId>com.alibaba</groupId>
                            <artifactId>fastjson</artifactId>
                            <version>1.2.8</version>
						 </dependency>
 *                      <dependency>
                            <groupId>log4j</groupId>
                            <artifactId>log4j</artifactId>
                            <version>1.2.17</version>
                        </dependency>
 *
 *
 * @date 13:54 2017/7/24
 * @author 王新宇
 */
public class UDPClient {
	private static Logger logger = Logger.getLogger(UDPClient.class);
	private static Properties properties = null;
	static {
		properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("###"));
		} catch (IOException e) {
			logger.error("没有找到配置文件",e);
		}
	}
	public static void sendMessage(Map messageMap) throws IOException {
		if(messageMap==null||messageMap.isEmpty()||properties==null){
			return;
		}
		String addr = "";
		int port = 0;
		DatagramSocket datagramSocket = new DatagramSocket();
		String environment = properties.getProperty("environment");
		messageMap.put("所属环境",environment);
		String toJSONString = toJSONString(messageMap);
		byte[] data = toJSONString.getBytes(Charset.forName("UTF-8"));
		List<Map> addrs = getAddrs();
		for(Map map:addrs){
			addr = String.valueOf(map.get("addr"));
			port = Integer.parseInt(String.valueOf(map.get("port")));
			InetSocketAddress inetSocketAddress = new InetSocketAddress(addr,port);
			StringBuffer a = new StringBuffer("准备发送UDP日志，本次日志文本大小为：");
			a.append((double) (data.length)/1024d+"KB\n");
			a.append("本次发送的日志内容为：\n");
			a.append(toJSONString);
			//System.out.println(a);
			logger.info(a);
			datagramSocket.send(new DatagramPacket(data,0,data.length,inetSocketAddress));
		}
	}
	public static List<Map> getAddrs(){
		String udpClient_addrs = properties.getProperty("UDPClient_ADDRS");
		if (udpClient_addrs!=null) {
			String[] splitAddr = udpClient_addrs.split(",");
			if(splitAddr.length>0){
				List<Map> list = new ArrayList<Map>();
				for(String temp:splitAddr){
					String[] split = temp.split(":");
					Map<String,String> map = new HashMap<String,String>();
					map.put("addr",split[0]);
					map.put("port",split[1]);
					list.add(map);
				}
				return list;
			}
		}
		return new ArrayList<Map>();
	}
	public static String toJSONString(Object object){
		return JSON.toJSONString(object, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.DisableCircularReferenceDetect});
	}
}
