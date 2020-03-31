package top.raisesunner.springmvc.controller;

import top.raisesunner.springmvc.annotation.TopAutowired;
import top.raisesunner.springmvc.annotation.TopController;
import top.raisesunner.springmvc.annotation.TopRequestMapping;
import top.raisesunner.springmvc.annotation.TopRequestParam;
import top.raisesunner.springmvc.service.RaisesunnerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@TopController("raisesunnerController")
@TopRequestMapping("/raisesunner")
public class RaisesunnerController {
    @TopAutowired("raisesunnerService")
    private RaisesunnerService raisesunnerService;

    @TopRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response, @TopRequestParam("name") String name, @TopRequestParam("age") int age) throws IOException {

        String result = raisesunnerService.query(name, age);

        PrintWriter pw = response.getWriter();
        pw.write(result);

    }
}
