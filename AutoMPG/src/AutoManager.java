import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class AutoManager {



    public static void assembleDatabase() throws SQLException, ClassNotFoundException  //Method that will handle the entire assembly of the database. Will check if it worked in the MySQL Shell later.
    {
        String mpg;           //Series of variables relating to each car, and all of its different specifications.
        Integer cylinders;
        String displacement;
        String horses;
        Integer weight;
        Double accel;
        Integer year;
        String origin;
        String carName;


        Class.forName("com.mysql.cj.jdbc.Driver"); //Load SQL Driver
        System.out.println("Driver loaded.");

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/auto","hwuser","Pa$$word"); //Log into database
        System.out.println("Database connected.");

        Statement statement = connection.createStatement(); //Make a statement to create a table if it doesn't exist yet.
        statement.executeUpdate("create table if not exists carData( mpg decimal(10,2), cylinders int(2), displacement decimal(10,2), horsepower decimal(10,2), weight decimal(10,2), acceleration decimal(10, 2), modelYear int(4), origin varChar(10), carName varChar(100), primary key(mpg, horsepower, carName));");
        try
        {
            Scanner scanner = new Scanner(new FileReader("auto-mpg.data-original")); //Find file of data
            while(scanner.hasNextLine()) //Parse each line and iterate, adding to the database until end of file is reached
            {
                String row = scanner.nextLine();
                String[] rowArray = row.split("\\s+");
                if(rowArray[0].equals("NA")) //If there is an NA, set MPG to null
                {
                    mpg = "NULL";
                }
                else
                {
                    mpg = rowArray[0]; //Set value otherwise
                }
                cylinders = Integer.parseInt(rowArray[1].substring(0, rowArray[1].length()-1)); //Parse integer for number of cylinders
                displacement = rowArray[2];
                if(rowArray[3].equals("NA")) //If horsepower is empty, set to NULL
                {
                    horses = "NULL";
                }
                else
                {
                    horses = rowArray[3]; //Set value otherwise
                }

                weight = Integer.parseInt(rowArray[4].substring(0, rowArray[4].length()-1)); //Parse integer for weight

                accel = Double.parseDouble(rowArray[5]); //Parse double for acceleration time

                year = Integer.parseInt(rowArray[6].substring(0, rowArray[6].length()-1)); //Parse integer for model year

                switch(Integer.parseInt(rowArray[7].substring(0, rowArray[7].length()-1))) //Set region according to the integer found in the 8th column
                {
                    case 1:
                        origin = "'USA'";

                        break;
                    case 2:
                        origin = "'Europe'";

                        break;
                    case 3:
                        origin = "'Japan'";

                        break;
                    default:
                        origin = "NULL";

                        break;
                }
                carName = rowArray[8];
                for(int i = 9; i < rowArray.length; i++)
                {
                    carName = carName + " " + rowArray[i];
                }

                carName = carName.replace("'"," ");
                String command = "Insert Ignore into carData (mpg, cylinders, displacement, horsepower, weight, acceleration, modelYear, origin, carName) values ("+mpg+","+cylinders.toString()+","+displacement.toString()+","+horses+","+weight.toString()+","+accel.toString()+","+year.toString()+","+origin+",'"+carName+"');";

                int doThing = statement.executeUpdate(command);
            }
            scanner.close();
        }
        catch(Exception e)
        {
            System.out.println("If you're seeing this in the console, the plan didn't work.");
        }
        finally
        {
            System.out.println("It worked!");
            connection.close();
        }
    }
    public static void assembleWindow() throws SQLException, ClassNotFoundException//The GUI that will handle going through the records.
    {
        JButton searchButton;       //Series of Swing objects to be used in the GUI.
        JTextArea listOfCars;
        JSlider mpgSlider;
        JSlider powerSlider;
        JFrame autoFrame;
        JFormattedTextField searchBar;



        Class.forName("com.mysql.cj.jdbc.Driver"); //Load SQL Driver
        System.out.println("Driver loaded.");

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/auto","hwuser","Pa$$word"); //Log into database
        System.out.println("Database connected.");

        searchBar = new JFormattedTextField(); //Initialize a JFormattedTextField
        searchBar.setSize(200,50);
        searchBar.setEditable(true);



        listOfCars = new JTextArea(); //Make a JTextArea
        listOfCars.setSize(720,640);
        listOfCars.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(listOfCars); //Make listOfCars scrollable
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mpgSlider = new JSlider(0,50,0); //Set up the bounds of the JSliders
        powerSlider = new JSlider(0,250,0);

        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            int getMPG = mpgSlider.getValue(); //Values for search. Used as Strings to simplify the searching process.
            int getPower = powerSlider.getValue();
            String carSearch = searchBar.getText();


            ArrayList<String> searchResults = new ArrayList<>();
            String command = "Select * from carData "; //Start assembling the String for the SQL query

            if(getMPG != 0) //If the MPG slider is more than 0, set a minimum and maximum, and search within the range
            {
                Integer minMPG = getMPG - 1;
                Integer maxMPG = getMPG + 1;
                command = command + "where mpg between " + minMPG.toString() + " and " + maxMPG.toString() + " ";
            }
            if(getPower != 0) //If the Power slider is more than 0, set a minimum and a maximum and search within the range
            {
                Integer minPower = getPower - 10;
                Integer maxPower = getPower + 10;
                if(getMPG == 0)
                {
                    command = command + "where horsepower between " + minPower.toString() + " and " + maxPower.toString() + " "; //Add where clause if the MPG slider is at 0
                }
                else
                {
                    command = command + "and horsepower between " + minPower.toString() + " and " + maxPower.toString() + " "; //Add AND clause if the MPG slider is not at 0
                }
            }
            if(!carSearch.isBlank() || !carSearch.equalsIgnoreCase("All")) //If the search isn't blank or the JTextField doesn't say ALL
            {
                if(getMPG == 0 && getPower != 0)
                {
                    command = command + "and carName like '%" + carSearch + "%' " ; //Append to the query if only the power slider is more than 0
                }
                else if(getPower == 0 && getMPG != 0)
                {
                    command = command + "and carName like '%" + carSearch + "%' " ; //Append to the query if only the mpg slider is more than 0
                }
                else if(getPower == 0 && getMPG == 0)
                {
                    command = command + "where carName like '%" + carSearch + "%' " ; //Append where clause to query if the sliders are both at 0
                }
                else
                {
                    command = command + "and carName like '%" + carSearch + "%' " ; //Append and clause to query if MPG and Power sliders are both not at 0
                }
            }
            command = command + ";"; //Add semicolon at the end for SQL query
            try
            {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(command);

                while(resultSet.next()) //Add every row of the results to an ArrayList
                {
                    searchResults.add(resultSet.getString(1) + "\t" + resultSet.getString(2) + "\t" + resultSet.getString(3) + "\t" + resultSet.getString(4) + "\t" + resultSet.getString(5) + "\t" + resultSet.getString(8) + "\t" + resultSet.getString(9));

                }
                for(int i = 0; i < searchResults.size(); i++)
                {
                    listOfCars.append(searchResults.get(i) + "\n"); //Add the list of cars one row at a time until all search results are added
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        });
        //Assemble the window, pack it, and show it.
        autoFrame = new JFrame();
        autoFrame.setLayout(new GridLayout(3,3,5,5));
        autoFrame.setSize(1280,720);
        autoFrame.setLocationRelativeTo(null);

        autoFrame.add(searchBar, BorderLayout.NORTH);
        autoFrame.add(scrollPane, BorderLayout.CENTER);
        autoFrame.add(searchButton, BorderLayout.SOUTH);
        mpgSlider.setMajorTickSpacing(5);
        mpgSlider.setMinorTickSpacing(1);
        mpgSlider.setPaintTicks(true);
        mpgSlider.setPaintLabels(true);
        autoFrame.add(mpgSlider, BorderLayout.WEST);
        powerSlider.setMajorTickSpacing(50);
        powerSlider.setMinorTickSpacing(10);
        powerSlider.setPaintTicks(true);
        powerSlider.setPaintLabels(true);
        autoFrame.add(powerSlider, BorderLayout.EAST);


        autoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        autoFrame.setVisible(true);


    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException
    {
        assembleDatabase();
        assembleWindow();
    }
}
