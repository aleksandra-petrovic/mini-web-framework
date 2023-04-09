package project.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
public class MyAspect {
// NE RADI NISTA ;(
    // ne koristim aspektno uopste, ali nisam htela da obrisem jer me zanima
    // zasto nije radilo...
    @Pointcut("within(@project.annotations.Controller *)")
    public void classAnnotatedWithController() {}

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}

    @Pointcut("publicMethod() && classAnnotatedWithController()")
    public void publicMethodInsideAClassMarkedWithController() {}

    @Pointcut("execution(public Map<String, Object> project.server.ServerThread.generateResponseMap(..))")
    void makeResponse(){

    }

    @Pointcut("execution (public Map<String,Method> project.reflection.MyReflection.mapControllerMethods(..))")
    void daliradi(){

    }

    @Around("daliradi()")
    public void aroundReflection(ProceedingJoinPoint jp) throws Throwable{
        System.out.println("RADI PRE");
        jp.proceed();
        System.out.println("RADI POSLE");
    }

    @Around("makeResponse()")
    public Map<String, Object> around(ProceedingJoinPoint jp) throws Throwable {
        System.out.println("-> Before calling make response");
        //float total = (float) jp.proceed();
        Map<String,Object> response = new HashMap<>();
        response = (Map<String, Object>) jp.proceed();
       // Map<String,Object> response = new HashMap<>();
        response.put("route_location", "B");
        response.put("route_method", "B");
        response.put("parameters", "B");
        System.out.println("<- After calling make response");

        return response;
    }


    @Around("publicMethodInsideAClassMarkedWithController()")
    public void aroundControllerMethods(ProceedingJoinPoint jp) throws Throwable {
        System.out.println(" project.aspect za metode kontrolera ");

        jp.proceed();

        Method mtd = jp.getTarget().getClass().getDeclaredMethod("m1()");
        mtd.setAccessible(true);
        mtd.invoke(null,null);
    }

}
