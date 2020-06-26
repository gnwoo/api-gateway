package com.gnwoo.apigateway;

import com.gnwoo.apigateway.filter.post.PostChangePasswordFilter;
import com.gnwoo.apigateway.filter.post.PostLoginFilter;
import com.gnwoo.apigateway.filter.pre.PreJWTTokenFilter;
import com.gnwoo.apigateway.filter.pre.PreLogoutFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@EnableZuulProxy
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public PreJWTTokenFilter preJWTTokenFilter() {
		return new PreJWTTokenFilter();
	}

	@Bean
	public PreLogoutFilter preLogoutFilter() {
		return new PreLogoutFilter();
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
