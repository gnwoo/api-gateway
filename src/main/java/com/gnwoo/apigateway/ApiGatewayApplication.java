package com.gnwoo.apigateway;

import com.gnwoo.apigateway.Filters.post.PostLoginFilter;
import com.gnwoo.apigateway.Filters.pre.PreAuthenticationFilter;
import com.gnwoo.apigateway.Filters.pre.PreLogoutFilter;
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
	public PreAuthenticationFilter preAuthenticationFilter() {
		return new PreAuthenticationFilter();
	}

	@Bean
	public PreLogoutFilter preLogoutFilter() {
		return new PreLogoutFilter();
	}

	@Bean
	public PostLoginFilter postLoginFilter() {
		return new PostLoginFilter();
	}
}
