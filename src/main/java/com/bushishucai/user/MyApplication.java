package com.bushishucai.user;

import com.bushishucai.springframework.ApplicationContext;

public class MyApplication {
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();

        //MemberService memberService = (MemberService) applicationContext.getBean("memberService");
        //memberService.test();
    }
}
