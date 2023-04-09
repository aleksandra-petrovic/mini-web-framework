package project.reflection;

import project.annotations.Controller;
import project.annotations.Qualifier;
import project.framework.request.Request;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MyReflection {

    private Map<String, Method> pathAndMethodMap = new HashMap<>();
    private Map<Object,Class> uniqueQualifiersMap = new HashMap<>();
    private Map<Object, Class> dependencyContainerMap = new HashMap<>();

    private Request request = null;

    public MyReflection(){
//        mapControllerMethods();
//        mapQualifiers();
//        mapDependencyContainer();
        //this.pathAndMethodMap = mapControllerMethods();
       // System.out.println(pathAndMethodMap.toString());
        this.uniqueQualifiersMap = mapQualifiers();
        System.out.println(uniqueQualifiersMap.toString());
        this.dependencyContainerMap = mapDependencyContainer();
        System.out.println(dependencyContainerMap.toString());
    };

    public MyReflection(Request request){
        this.request = request;
        this.pathAndMethodMap = mapControllerMethods();
    }

    public Map<String, Object> findResponse(){

        String path = request.getMethod().toString() + " " + request.getLocation();
        Map<String, Object> responseMap = new HashMap<>();
        System.out.println("REQUEST PATH :" + path + ".");
        System.out.println("path and method map : " + this.pathAndMethodMap.toString());
        if (this.pathAndMethodMap.get(path) == null) {
            responseMap.put("route_location", request.getLocation());
            responseMap.put("route_method", request.getMethod().toString());
            responseMap.put("parameters", request.getParameters());
        } else {
            Method method = pathAndMethodMap.get(path);
            method.setAccessible(true);
            Object obj = null;
            Set<Class> classes = findAllClassesUsingClassLoader("project.controllers");
            for(Class c : classes){
                try {
                    if (c.getAnnotation(Controller.class) != null) {
                        obj = Class.forName(c.getName()).getDeclaredConstructor().newInstance();
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                try {

                    Method[] methods = c.getDeclaredMethods();
                    for(Method m : methods){
                        if(m.equals(method)){
                            responseMap.put("method_activated", method.invoke(obj, null));
                        }
                    }

                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            }

            responseMap.put("route_location", request.getLocation());
            responseMap.put("route_method", request.getMethod().toString());
            responseMap.put("parameters", request.getParameters());
        }

        return responseMap;
    }

    public Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    public Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        return null;
    }
    public Map<String,Method> mapControllerMethods(){
        System.out.println("sakupljam metode");
        Set<Class> classes = findAllClassesUsingClassLoader("project.controllers");
        for(Class c1 : classes){
            System.out.println("klasa: " + c1.getName());
            if(c1.getAnnotation(Controller.class) != null){
                System.out.println("KONTROLER: " + c1.getName());
                Method[] methods = c1.getDeclaredMethods();

                for(Method m1 : methods){
                    System.out.println("METODA " + m1.getName());
                    String path = "";
                    Annotation[] annotations = m1.getAnnotations();
                    for (Annotation a1 : annotations){
                        if(a1.toString().contains("GET")) {
                            path = path + "GET ";
                        }else if(a1.toString().contains("POST")){
                            path = path + "POST ";
                        }else{
                            String[] split = a1.toString().split("\"");
                            path = path + split[1];
                        }
                    }
                    System.out.println("PUTANJA :" + path);
                    pathAndMethodMap.put(path, m1);
                }

            }
        }
        return pathAndMethodMap;
    }

    public Map<Object,Class> mapQualifiers() {
        System.out.println("sakupljam qualifiere");
        //Map<Object,Object> dependencyContainer = new HashMap<>();
        uniqueQualifiersMap = new HashMap<>();
        Set<Class> classes = findAllClassesUsingClassLoader("project.controllers");
        for (Class c1 : classes) {
            System.out.println("klasa: " + c1.getName());
            if(c1.getAnnotation(Qualifier.class) != null){
                //dependencyContainer.put()
                String annotation = c1.getAnnotation(Qualifier.class).toString();
                String[] help = annotation.split("\"");
                annotation = help[1];
                System.out.println("anotacija value " + annotation);
                if(uniqueQualifiersMap.get(annotation) == null) {
                    uniqueQualifiersMap.put(annotation, c1);
                }else{
                    throw new RuntimeException("There is already a class with qualifier " + annotation + " present in dependency container." +
                            "No duplicates allowed.");
                }
            }
        }
        return uniqueQualifiersMap;
    }

    public Map<Object,Class> mapDependencyContainer() {
        System.out.println("sakupljam container");
        //Map<Object, Object> dependencyContainer = new HashMap<>();
        Set<Class> classes = findAllClassesUsingClassLoader("project.controllers");
        for (Class c1 : classes) {
            System.out.println("klasa: " + c1.getName());
            if (c1.getAnnotation(Controller.class) != null) {
                Field[] fields = c1.getDeclaredFields();
                for(Field f : fields){
                    if(f.getAnnotation(Qualifier.class) != null){
                        String qualifier = f.getAnnotation(Qualifier.class).toString();
                        String[] help = qualifier.split("\"");
                        qualifier = help[1];
                        if (uniqueQualifiersMap.get(qualifier) != null){
                            dependencyContainerMap.put(f.getType(),uniqueQualifiersMap.get(qualifier));
                        }else{
                            //throw exception
                        }

                    }
                }

            }
        }
        return dependencyContainerMap;
    }

    public Map<Object, Class> getDependencyContainerMap() {
        return dependencyContainerMap;
    }


}
