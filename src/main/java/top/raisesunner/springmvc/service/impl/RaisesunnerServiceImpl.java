package top.raisesunner.springmvc.service.impl;

import top.raisesunner.springmvc.annotation.TopService;
import top.raisesunner.springmvc.service.RaisesunnerService;

@TopService("raisesunnerService")
public class RaisesunnerServiceImpl  implements RaisesunnerService {
    @Override
    public String query(String name, int age) {
        return "name====" + name + "; age===" + age ;
    }
}
