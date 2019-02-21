package com.beyond.phoenix.router.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.beyond.phoenix.router.config.VoyagerRouteProvider;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   上午8:53:49
 *
 */
public class SwaggerHeaderFilter extends AbstractGatewayFilterFactory<Object> {
    private static final String HEADER_NAME = "X-Forwarded-Prefix";

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (!StringUtils.endsWithIgnoreCase(path, VoyagerRouteProvider.API_URI)) {
                return chain.filter(exchange);
            }
            
            String basePath = path.substring(0, path.lastIndexOf(VoyagerRouteProvider.API_URI));
            
            ServerHttpRequest newRequest = request.mutate().header(HEADER_NAME, basePath).build();
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
            
            return chain.filter(newExchange);
        };
    }
}
