import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.Scanner;

class Connect{
    public static Connection connection;

    Connect(){}

    public static int stablish(){
        //Descripiton:
        // Loads the driver and then Stablishes connection with the DB
        //Returns:
        // 1 -> if it stablished the connection well
        // 2 -> if it couldn't stablish the connection, writes the error in the connection_log file

        try {
            //Loading driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //establishing connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost/bd1","root" ,"");
            return 1;
        } catch (SQLException | ClassNotFoundException e) {

            System.err.println("FAILED TO STABLISH THE CONNECTION OR LOAD DEPENDENCIES -> FOR MORE INFO CHECK connection_log.txt");

            // Writing the stack trace to a file before returning
            try (FileWriter fileWriter = new FileWriter("connection_log.txt", true);  // 'true' for appending
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {

                // Stack trace to string
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();

                // Write st to the file
                printWriter.println("----- ERROR OCCURRED -----");
                printWriter.println(stackTrace);
                printWriter.println("--------------------------");
            } catch (IOException ioException) {
                System.err.println("Failed to write to the log file: " + ioException.getMessage());
            }
            return 0;
        }
    }

    public static void end(){
        /*
        * Closes the conection
        * */
        try {
            connection.close();
            //System.out.println("CONECTION CLOSED"); //DEBUG -> Works as intended
        } catch (SQLException e) {
            System.err.println("OPERATION TO CLOSE THE CONEXION FAILED.");
            try (FileWriter fileWriter = new FileWriter("connection_log.txt", true);  // 'true' for appending
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {

                // Stack trace to string
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();

                // Write st to the file
                printWriter.println("----- ERROR OCCURRED -----");
                printWriter.println(stackTrace);
                printWriter.println("--------------------------");
            } catch (IOException ioException) {
                System.err.println("Failed to write to the log file: " + ioException.getMessage());
            }
        }
    }

}

class Query{
    Query(){}
    static Statement command = null;
    static int worked = 0; //0 = didn't work 1 = worked fine 2 = error

    public static void ask(int user_code){
        /*
        * Creates a statement for quering, then executes the query and saves the register in a result set, if the register retrieved info
        * it prints it, else there's no article with that code
        *
        * It handles exceptions
        */

        try {
            command = Connect.connection.createStatement();
            ResultSet register = command.executeQuery("select descripcion,precio from articulos where codigo="+String.valueOf(user_code));

            if (register.next()==true) {
                System.out.println("The name of the item is: " + register.getString("descripcion"));
                System.out.println("The price tag for the item is: " + register.getString("precio"));
                worked = 1;
            } else {
                System.out.println("There's no article with this code");
                worked = 0;
            }
        } catch (SQLException e) {
            System.err.println("OPERATION FAILED");
            worked = 2;
        }
        Connect.end();
    }

    public static void add(String name, double price){
        Statement command = null;
        try{
            command = Connect.connection.createStatement();
            command.executeUpdate("insert into articulos(descripcion,precio) values ('"+name+"',"+price+")");
            Connect.connection.close();
        } catch (SQLException e){
            System.err.println("OPERATION FAILED");
            e.printStackTrace();
        }
        System.out.println("Data registered");
    }

    public static void update(String name, double price, int code) {
        if (worked != 1) {
            System.err.println("LOG: The item was not found");
        } else {
            try {
                Connect.stablish();
                Statement newcommand = Connect.connection.createStatement();
                int cuantity = newcommand.executeUpdate("update articulos set descripcion='" + name + "'," +
                        "precio=" + price + " where codigo=" + code);
                if (cuantity == 1) {
                    System.out.println("The Item was succesfully modified");
                } else {
                    System.out.println("The Item was not found");
                }
                Connect.connection.close();
            } catch (SQLException e) {
                System.err.println("OPERATION FAILED");
                e.printStackTrace();
            }
        }
    }
}

class Menu{

    //Constructor
    Menu(){}

    //Main Menu
    public static void show_Menu(){
        Scanner sc = new Scanner(System.in);
        boolean loop = true;
        int malCont = 0;

        while(loop && malCont < 3){
            System.out.println("-----------------MAIN MENU-----------------");
            System.out.println("Select the operation you want to perform: ");
            System.out.println("1. Show an Item.\n2. Add an Item\n3. Modify Item\n4. Exit.");
            System.out.println("-------------------------------------------");
            try {
                int userOption = Integer.parseInt(sc.nextLine());
                switch (userOption){
                    case 1:
                        if(Connect.connection.isClosed()){ Connect.stablish(); }

                        System.out.println("-----------------You selected show an Item-----------------");
                        System.out.println("Please introduce the code of the Item you want to query");
                        System.out.println("-----------------------------------------------------------");
                        try{
                            userOption = Integer.parseInt(sc.nextLine());
                            try{
                                Query.ask(userOption);
                            } catch (Exception e){
                                System.err.println("QUERYING OPERATION FAILED");
                            }
                        } catch(Exception e){
                            System.err.println("Please Introduce a Numeric Value");
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        if(Connect.connection.isClosed()){ Connect.stablish(); }
                        try {
                            System.out.println("-----------------You selected adding an Item-----------------");
                            System.out.println("Please introduce the name of the Item you want to add");
                            System.out.println("-----------------------------------------------------------");
                            String name = sc.nextLine();
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("Please introduce the price of the Item you want to add");
                            System.out.println("-----------------------------------------------------------");
                            double price = Double.parseDouble(sc.nextLine());
                            Query.add(name, price);
                        } catch (Exception e){
                            System.err.println("OPERATION TO CERTIFY THE INPUT FAILED");
                        }
                    case 3:
                        try {
                            if (Connect.connection.isClosed()) {
                                Connect.stablish();
                            }
                            System.out.println("-----------------You selected modifying an Item-----------------");
                            System.out.println("Please introduce the new name of the Item you want to modify");
                            System.out.println("-----------------------------------------------------------");
                            String name = sc.nextLine();
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("Please introduce the new price of the Item you want to modify");
                            System.out.println("-----------------------------------------------------------");
                            double price = Double.parseDouble(sc.nextLine());
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("Please introduce the actual code of the Item you want to modify");
                            System.out.println("-----------------------------------------------------------");
                            int code = Integer.parseInt(sc.nextLine());
                            Query.ask(code);
                            Query.update(name, price, code);
                        } catch (Exception e){
                            System.err.println("OPERATION TO CERTIFY THE INPUT FAILED");
                        }

                    case 4:
                        loop = false;

                    default:
                        malCont++;
                        if(malCont == 3){
                            System.err.println("Too many attempts, shutting down");
                        }
                }
            } catch (Exception e){
                malCont++;
                if(malCont == 3){
                    System.err.println("Too many attempts, shutting down");
                } else {
                    System.err.println("Please Introduce a valid Option");
                }
            }
        }

    }
}

public class Main {
    public static void main(String[] args) {
        if(Connect.stablish() == 1){
            Menu.show_Menu();
        }
    }
}