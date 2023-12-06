package com.bushishucai.user;

import com.bushishucai.springframework.*;

@Component
@Scope("singleton")
@Lazy
public class MemberService implements ApplicationContextAware{
    @Autowired
    private OrderService orderService;

    private ApplicationContext applicationContext;

    public void test(){
        System.out.println("memberService#test");
        orderService.test();
    }

    @Override
    public void setApplicationContextAware(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
