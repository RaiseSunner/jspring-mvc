package top.raisesunner.springmvc.servlet;

import top.raisesunner.springmvc.annotation.*;
import top.raisesunner.springmvc.controller.RaisesunnerController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    List<String> classNames = new ArrayList<>();
    Map<String, Object> beans = new HashMap<>();
    Map<String, Object> handlerMap = new HashMap<>();

    public void init(ServletConfig config) {
        //1 实例化所有声明了注解的类之前，需要扫描哪些注解声明的类需要实例化。
        doScan("top.raisesunner");
        doInstance();

        doAutowired();

        UrlHandle();

    }

    private void UrlHandle() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(TopController.class)) {
                TopRequestMapping requestMapping = clazz.getAnnotation(TopRequestMapping.class);
                String classPath = requestMapping.value();
                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(TopRequestMapping.class)) {
                        TopRequestMapping annotation = method.getAnnotation(TopRequestMapping.class);
                        String methodPath = annotation.value();
                        handlerMap.put(classPath + methodPath, method);
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    private void doAutowired() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(TopController.class)) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(TopAutowired.class)) {
                        TopAutowired annotation = field.getAnnotation(TopAutowired.class);
                        String key = annotation.value();
                        Object object = beans.get(key);

                        field.setAccessible(true);
                        try {
                            field.set(instance, object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    private void doInstance() {
        for (String className : classNames) {
            String cn = className.replace(".class", "");

            try {
                Class<?> clazz = Class.forName(cn);
                Object instance = clazz.newInstance();
                String beanKey = null;
                if (clazz.isAnnotationPresent(TopController.class)) {
                    TopController annotation = clazz.getAnnotation(TopController.class);
                    beanKey = annotation.value();
                } else if (clazz.isAnnotationPresent(TopService.class)) {
                    TopService annotation = clazz.getAnnotation(TopService.class);
                    beanKey = annotation.value();
                } else {
                    continue;
                }
                beans.put(beanKey, instance);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScan(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);

        String[] filesStr = file.list();

        for (String path : filesStr) {
            File filePath = new File(fileStr + path);
            if (filePath.isDirectory()) {
                //递归进行处理
                doScan(basePackage + "." + path);
            } else {
                classNames.add(basePackage + "." + filePath.getName());
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String context = req.getContextPath();  //  /jspringmvc
        String path = uri.replace(context, ""); //   /jspring-mvc/raisesunner/query   -> /raisesunner/query

        Method method = (Method) handlerMap.get(path);

        // 获取 raisesunner这个名称
        RaisesunnerController instance = (RaisesunnerController) beans.get("/" + path.split("/")[1]);
        Object[] args = hand(req, resp, method);

        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static Object[] hand(HttpServletRequest request, HttpServletResponse response, Method method) {
        // 获取当前执行的方法所需的参数
        Class<?>[] paramClazzes = method.getParameterTypes();
        // 根据参数的个数，创建一个参数的数组，讲方法里的参数存储在数据中
        Object[] args = new Object[paramClazzes.length];

        int args_i = 0;
        int index = 0;
        for (Class<?> clazz : paramClazzes) {
            if (ServletRequest.class.isAssignableFrom(clazz)) {
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(clazz)) {
                args[args_i++] = response;
            }

            Annotation[] paramAnnos = method.getParameterAnnotations()[index];
            if (paramAnnos.length > 0) {
                for (Annotation paramAn : paramAnnos) {
                    if (TopRequestParam.class.isAssignableFrom(paramAn.getClass())){
                        TopRequestParam requestParam = (TopRequestParam) paramAn;
                        args[args_i++] = request.getParameter(requestParam.value());
                    }
                }
            }
            index++;
        }
        return  args;
    }
}