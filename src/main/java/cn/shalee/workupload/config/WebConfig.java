package cn.shalee.workupload.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @author 31930
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前工作目录
        String currentDir = System.getProperty("user.dir");
        String uploadsPath = currentDir + File.separator + "uploads" + File.separator;
        
        log.info("配置静态资源映射 - 当前目录: {}", currentDir);
        log.info("配置静态资源映射 - 上传目录: {}", uploadsPath);
        
        // 检查目录是否存在
        File uploadsDir = new File(uploadsPath);
        if (uploadsDir.exists()) {
            log.info("上传目录存在: {}", uploadsDir.getAbsolutePath());
        } else {
            log.warn("上传目录不存在: {}", uploadsDir.getAbsolutePath());
        }
        
        // 配置上传文件的静态资源访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath)
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(true);
        
        log.info("静态资源映射配置完成: /uploads/** -> file:{}", uploadsPath);
    }
}