package project.reflection;

import project.annotations.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DIEngine {

    private MyReflection reflection;
    private List<Class> singletonClasses = new ArrayList<>();
    private Map<Class, Object> instances = new HashMap<>();
    public DIEngine(){
        this.reflection = new MyReflection();
        this.singletonClasses = collectSingletons();
        System.out.println(this.singletonClasses);
        inject();
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

    public List<Class> collectSingletons() {
        System.out.println("sakupljam singltone");
        List<Class> singletons = new ArrayList<>();
        Set<Class> classes = findAllClassesUsingClassLoader("project.controllers");
        for (Class c1 : classes) {
            if (c1.getAnnotation(Bean.class) != null){
             //   System.out.println("bean nije null");
                String bean = c1.getAnnotation(Bean.class).toString();
                String[] help = bean.split("\"");
                bean = help[1];
              //  System.out.println("BEAN VALUE : " + bean);
                if(bean.equals("singleton")){
                //    System.out.println("bean je singleton");
                    singletons.add(c1.getClass());
               //     System.out.println("jeste " + c1.getName());
                }
            }
            if(c1.getAnnotation(Service.class) != null){
               // System.out.println("anotacija servis");
                singletons.add(c1.getClass());
              //  System.out.println("jeste " + c1.getName());
            }
        }
        System.out.println("singletons: " + singletons.toString());
        return singletons;
    }

    public void inject() {
        System.out.println("injectujem klase");
        Object obj = null;
        Set<Class> classes = findAllClassesUsingClassLoader("project.controllers");
        for (Class c1 : classes) {
            if (c1.getAnnotation(Controller.class) != null) {
                try {
                    if(instances.get(c1) == null) {
                        obj = c1.getDeclaredConstructor().newInstance();
                        injectFields(c1,obj);
                    }else{
                        injectFields(c1, instances.get(c1));
                    }

                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

        }
    }
    }

    public void injectFields(Class c1, Object obj){

        Field[] fields = c1.getDeclaredFields();
        for(Field f : fields){
            if(f.getAnnotation(Autowired.class) != null){
                if(f.getAnnotation(Qualifier.class) == null){
                    throw new RuntimeException("Qualifier required when using Autowired annotation!");
                }

                Class childClass = null;
                try {
                    childClass = Class.forName(f.getType().getName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                if(childClass.getAnnotation(Bean.class) == null && childClass.getAnnotation(Service.class) == null && childClass.getAnnotation(Component.class) == null){
                    throw new RuntimeException("Can't use Autowired on fields with type that has no @Bean/@Service/@Component annotations!");
                }

                    Field[] childFields = childClass.getDeclaredFields();
//                System.out.println("ime klase koja je tip polja : " + f.getType().getName());
                    for (Field f1 : childFields) {
                        System.out.println("child field : " + f1.getName());
                        if (f1.getAnnotation(Autowired.class) != null) {
                            System.out.println("rekurzivno pozivam klasu " + childClass.getName() + " iz klase " + c1.getName());
                            if(childClass.isInterface() == false) {
                                this.injectFields(childClass, makeInstance(childClass));
                            }
                        }
                    }
                    f.setAccessible(true);
                    if (reflection.getDependencyContainerMap().get(f.getType()) != null) {
                        Class myclass = reflection.getDependencyContainerMap().get(f.getType());
                        Object instance = makeInstance(myclass);
                        try {
                            f.set(obj, instance);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        boolean verbose = f.getAnnotation(Autowired.class).verbose();
                        if (verbose == true) {
                            Date date = new Date();
                            SimpleDateFormat DateFor = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                            String stringDate = DateFor.format(date);
                            System.out.println("Initialized <" + f.getType().getName() + "> <"
                                    + f.getName() + "> in <" + c1.getName() + "> on <" + stringDate + "> with <"
                                    + instance.toString() + ">");
                        }
                        System.out.println("kreirana nova instanca " + instance + "za interfejs " + f.getType());

                    } else {
                        System.out.println("ne postoji namapirana implementacija u dep containeru");
                    }
                }
            }
        }


    public Object makeInstance(Class myclass){
        Object instance;
        if(singletonClasses.contains(myclass) | myclass.getAnnotation(Controller.class) != null){
            if(instances.get(myclass) == null){
                try {
                    instance = myclass.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                instances.put(myclass,instance);
            }else{
                instance = instances.get(myclass);
            }
        }else{
            try {
                instance = myclass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }



}
