package project.controllers;

import project.annotations.*;

@Controller
@Qualifier(value = "123")
@Bean()
public class FirstController{

    @Autowired(verbose = true)
    @Qualifier(value = "aaa")
    private Interfejs1 something;

    @Autowired
    @Qualifier(value = "456")
    private SecondController sc;

    public FirstController(){};

    @GET
    @Path(path = "/path-one")
    public String m1(){

        System.out.println("aktivirana metoda 1");
        return "aktivirana metoda 1";
    }

    @GET
    @Path(path = "/path-two")
    public String m2(){

        System.out.println("aktivirana metoda 2");
        return "aktivirana metoda 2";
    }

    @GET
    @Path(path = "/path-three")
    public String m3(){

        System.out.println("aktivirana metoda 3");
        return "aktivirana metoda 3";
    }

}
