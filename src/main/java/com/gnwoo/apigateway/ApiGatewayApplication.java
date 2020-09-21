package com.gnwoo.apigateway;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.gnwoo.apigateway.filter.post.PostChangePasswordFilter;
import com.gnwoo.apigateway.filter.post.PostLoginFilter;
import com.gnwoo.apigateway.filter.pre.PreSessionFilter;
import com.gnwoo.apigateway.filter.pre.PreLogoutFilter;
import com.gnwoo.apigateway.filter.pre.PreSessionInfoFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableRedisRepositories
@EnableRedisHttpSession
@EnableZuulProxy
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Value("${ws-server.host}")
	private String host;

	@Value("${ws-server.port}")
	private Integer port;

	@Bean
	public SocketIOServer socketIOServer() {
		Configuration config = new Configuration();
		config.setHostname(host);
		config.setPort(port);
		return new SocketIOServer(config);
	}

	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		config.addExposedHeader("Content-Type");
		config.addExposedHeader("Authorization");
		config.addExposedHeader("Accept");
		config.addExposedHeader("Origin");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public PreSessionFilter preSessionFilter() {
		return new PreSessionFilter();
	}

	@Bean
	public PreLogoutFilter preLogoutFilter() {
		return new PreLogoutFilter();
	}

	@Bean
	public PreSessionInfoFilter preSessionInfoFilter() {
		return new PreSessionInfoFilter();
	}

	@Bean
	public PostLoginFilter postLoginFilter() {
		return new PostLoginFilter();
	}

	@Bean
	public PostChangePasswordFilter postChangePasswordFilter() {
		return new PostChangePasswordFilter();
	}
}
