package com.denote.client.config

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.alibaba.fastjson.support.config.FastJsonConfig
import com.denote.client.config.other.ConfigFastJsonHttpMessageConverter
import com.denote.client.core.AuthLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GLogger
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.config.annotation.InterceptorRegistration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.Charset

@Configuration
class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ConfigFastJsonHttpMessageConverter converter = new ConfigFastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(SerializerFeature.WriteMapNullValue);
        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(Charset.forName("UTF-8"));
        converters.add(converter);
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new HandlerExceptionResolver() {
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
                setCommonHeadersForResponse(response);
                response.setStatus(500);
                GLogger.g().error("facing an error while handling", e);
                APIResponse apiResponse = APIResponse.err(-1, "An error occurred while proceeding your request", null);
                try {
                    response.getWriter().write(JSON.toJSONString(apiResponse));
                } catch (IOException ex) {
                    GLogger.g().error(ex.getMessage());
                }
                return new ModelAndView();
            }
        });
    }

    private void setCommonHeadersForResponse(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
    }

    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(getHandleAdaptor());
        addInterceptor.excludePathPatterns("/recipient/**");
        addInterceptor.addPathPatterns("/**");
    }

    public HandlerInterceptorAdapter getHandleAdaptor() {
        new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                setCommonHeadersForResponse(response);
                APIRequest apiRequest = new APIRequest();
                apiRequest.getParam().put("req", request);
                apiRequest.getParam().put("res", response);
                def call = GlobalController.call(AuthLogicFunc.class, apiRequest);
                if (call != null && call.status == 1) {
                    return true;
                } else {
                    throw new RuntimeException("application has not configured yet")
                }
            }
        }
    }

}
