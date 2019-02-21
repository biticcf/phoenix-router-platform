package com.beyond.phoenix.router.config;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.beyond.phoenix.router.model.RouterInfoModel;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   上午8:56:09
 *
 */
@Primary
@Component
public class VoyagerRouteProvider implements SwaggerResourcesProvider {
    public static final String API_URI = "/v2/api-docs";
    
    private final RouteLocator routeLocator;
    private final GatewayProperties gatewayProperties;
    
    private final RouterInfoConfig routerInfoConfig;
    private final Map<String, RouterInfoModel> routerMap;
    
    public VoyagerRouteProvider(
    		RouteLocator routeLocator, 
    		GatewayProperties gatewayProperties,
    		RouterInfoConfig routerInfoConfig) {
    	this.routeLocator = routeLocator;
    	this.gatewayProperties = gatewayProperties;
    	
    	this.routerInfoConfig = routerInfoConfig;
    	this.routerMap = new HashMap<>();
    	List<RouterInfoModel> routerList = this.routerInfoConfig.getRoutes();
    	if(routerList != null && !routerList.isEmpty()) {
    		for(RouterInfoModel _info : routerList) {
    			this.routerMap.put(_info.getId(), _info);
    		}
    	}
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
        gatewayProperties.getRoutes().stream().filter(routeDefinition -> routes.contains(routeDefinition.getId()))
                .forEach(routeDefinition -> routeDefinition.getPredicates().stream()
                        .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName()))
                        .forEach(predicateDefinition -> resources.add(swaggerResource(routeDefinition.getId(),
                                predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0")
                                        .replace("/**", API_URI)))));
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
    	String _id = name;
    	String _name = null;
    	if(this.routerMap.containsKey(_id)) {
    		RouterInfoModel _info = this.routerMap.get(_id);
    		if(_info != null) {
    			_name = _info.getName();
    		}
    	}
    	if(_name == null || _name.trim().equals("")) {
    		_name = name;
    	}
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(_name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.9.2");
        
        return swaggerResource;
    }
}
