package project.controllers;

import project.annotations.Bean;
import project.annotations.Component;
import project.annotations.Qualifier;

@Qualifier(value = "aaa")
@Bean
public class Klasa1 implements Interfejs1{
    public Klasa1(){

    }
}
