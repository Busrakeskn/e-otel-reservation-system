package com.eotel;

import com.eotel.ui.ConsoleUI;
import com.eotel.ui.javafx.HotelApp;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--konsol")) {
            new ConsoleUI().baslat();
        } else {
            Application.launch(HotelApp.class, args);
        }
    }
}
