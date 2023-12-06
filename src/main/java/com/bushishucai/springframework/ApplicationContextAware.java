package com.bushishucai.springframework;

//用于拿到当前spring容器
public interface ApplicationContextAware {
    void setApplicationContextAware(ApplicationContext applicationContext);
}
