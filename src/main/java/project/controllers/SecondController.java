package project.controllers;

import project.annotations.*;

@Controller
@Qualifier(value = "456")
@Service
public class SecondController {

    @Autowired
    @Qualifier(value = "bbb")
    private Interfejs2 number;

    public SecondController(){};

    @GET
    @Path(path = "/path-four")
    public String f(){ return "aktivirana metoda f()"; }

}
