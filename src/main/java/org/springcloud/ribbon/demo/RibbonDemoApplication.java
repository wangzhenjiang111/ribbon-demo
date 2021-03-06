package org.springcloud.ribbon.demo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;

import rx.Observable;

@SpringBootApplication
public class RibbonDemoApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(RibbonDemoApplication.class, args);
		
		// 服务列表
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(new Server("localhost", 8081));
		serverList.add(new Server("localhost", 8082));
		// 构建负载均衡实例
		ILoadBalancer loadBalancer = 
				LoadBalancerBuilder.newBuilder().buildFixedServerListLoadBalancer(serverList);
		
		// 调用5次来测试效果
		for(int i = 0; i < 5; i++) {
			String result = 
					LoadBalancerCommand
					.<String>builder()
					.withLoadBalancer(loadBalancer)
					.build()
					.submit(
							new ServerOperation<String>() {
								public Observable<String> call(Server server){
									try {
										String addr = "http://"+server.getHost()+":"+server.getPort()+"/user/hello";
										System.out.println("调用地址："+addr);
										
										URL url = new URL(addr);
										HttpURLConnection conn = (HttpURLConnection)url.openConnection();
										conn.setRequestMethod("GET");
										conn.connect();
										
										InputStream in = conn.getInputStream();
										byte[] data = new byte[in.available()];
										in.read(data);
										
										return Observable.just(new String(data));
									}catch (Exception e) {
										return Observable.error(e);
									}
								}
							})
					.toBlocking().first();
			System.out.println("调用结果："+result);
		}
	}

}
