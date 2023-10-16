package com.denote.client.config.other

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
import com.denote.client.dto.APIResponse
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Type

class ConfigFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {
    @Override
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (o != null && o.getClass() == APIResponse.class) {
            APIResponse oo = o;
            if (!oo.resOk()) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletResponse response = requestAttributes.getResponse();
                response.setStatus(500)
            }
        }
        // writing data
        super.write(o, type, contentType, outputMessage);
    }
}