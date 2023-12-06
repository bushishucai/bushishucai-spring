package com.bushishucai.user;

import com.bushishucai.springframework.Component;
import com.bushishucai.springframework.Transactional;

@Component
//@Transactional
public class OrderService {

    public void test(){
        System.out.println("orderService#test");
        //throw new RuntimeException();
    };
}
