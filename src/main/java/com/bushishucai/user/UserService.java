package com.bushishucai.user;

import com.bushishucai.springframework.*;

@Component
@Scope("singleton")
@Lazy
@Transactional
public class UserService implements ApplicationContextAware {

    @Autowired
    private OrderService orderService;

    private ApplicationContext applicationContext;

    public void test(){
        System.out.println("userService#test");
        orderService.test();
        throw new RuntimeException();
    }

    @Override
    public void setApplicationContextAware(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
