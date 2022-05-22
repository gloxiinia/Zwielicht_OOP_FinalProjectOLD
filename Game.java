import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import gameobjects.Actor;
import gameobjects.Item;
import gameobjects.Scene;
import gameobjects.Thing;
import gameobjects.ThingList;
import globals.Direction;
import globals.Methods;



public class Game {

        //Creating the needed assets
        private ArrayList<Scene> map; 
        private ArrayList<String> allItemNames;
        private ThingList allItems;
        private ArrayList<String> sceneNames;
        private Actor player;
        List<String> commands = new ArrayList<>(Arrays.asList("examine", "move", "talk", "take", "drop", "use", "yes", "no", "quit"));
        List<String> directions = new ArrayList<>(Arrays.asList("north", "south", "east", "west"));
        List<String> miscNouns = new ArrayList<>(Arrays.asList("inventory", "i", "around"));

        //methods to run the intended commands
        //EXAMINING
        private String examineItem(String itemName){
            String msg = "";
            Thing aSceneItem = getPlayer().getScene().getThings().thisThing(itemName);
            Thing anInventoryItem = getPlayer().getThings().thisThing(itemName);

            //flags for whether a sceneitem or aninventoryitem is found
            boolean sceneItemFound = true;
            boolean inventoryItemFound = true;

            //checking inventory
            if((itemName.equals("inventory") || itemName.equals("i"))){ // check the i command
                showInventory();

            }
            //examining the active scene
            else if(itemName.equals("around")){
                msg = getPlayer().getScene().getExamination();

            }
            else if(anInventoryItem == null){
                msg = "There's nothing named " + itemName + " in your inventory.";
                inventoryItemFound = false;
            }
            else if(aSceneItem == null){
                msg = "There's nothing named " + itemName + " in this location.";
                sceneItemFound = false;
            }
            if (!sceneItemFound && inventoryItemFound){
                msg = anInventoryItem.getExamination();
            }
            if(sceneItemFound && !inventoryItemFound){
                msg = aSceneItem.getExamination();
            }
            
            return msg;
        }

        //TAKING AND DROPPING
        private void transferItem(Thing aThing, ThingList fromList, ThingList toList){
            fromList.remove(aThing);
            toList.add(aThing);

        }

        private void showInventory(){
            System.out.println("-----------------INVENTORY-----------------\n");
            System.out.println(getPlayer().getThings().describeThings());
        }

        public String takeItem(String itemName){
            String msg = "";
            Thing t = getPlayer().getScene().getThings().thisThing(itemName);
            if (itemName.equals("")){
                itemName = "nothing";
            }if(t == null){
                msg = "There's nothing named " + itemName + " in here.";
            }else{
                if(t.isThingPickupable() == true){
                    transferItem(t, player.getScene().getThings(), player.getThings());
                    msg =  Methods.capitalizeString(itemName) + " has been taken.";
                }else{
                    msg = "I don't think you can pick that up.";
                }
            }

            return msg;
        }

        public String dropItem(String itemName){
            String msg = "";
            Thing t = getPlayer().getThings().thisThing(itemName);
            if (itemName.equals("")){
                msg = "You have to *name* the object. I can't read your mind.";
            }if(t == null){
                msg = "There's nothing named " + itemName + " in your inventory.";
            }else{
                if(t.isThingKey() == false){
                    transferItem(t, player.getThings(), player.getScene().getThings());
                    msg =  Methods.capitalizeString(itemName) + " has been dropped.";
                }else{
                    msg = "I don't think you should drop that.";
                }
            }

            return msg;
        }

        //MOVEMENT
        //methods for moving the player around the map
        private void goN() {
            moveHandler(movePlayerTo(Direction.NORTH));
        }
    
        private void goS() {
            moveHandler(movePlayerTo(Direction.SOUTH));
        }
    
        private void goW() {
            moveHandler(movePlayerTo(Direction.WEST));
        }
    
        private void goE() {
            moveHandler(movePlayerTo(Direction.EAST));
        }

        //constructor
        public Game(){
            //creating the game map by reading the Scenes file
            this.map = ReadFile.createScenes();
            this.allItems = ReadFile.createItems();
            this.allItemNames = allItems.allThingNamesList(allItems);
            this.sceneNames = getAllSceneNames();
            ThingList playerInventory = new ThingList();
            player = new Actor("player", "Not worth getting into my backstory.", "Really, nothing noteworthy here.", map.get(0), playerInventory);
            
        }

        //method to move an Actor (NPC, PC) to a scene
        int moveTo(Actor anActor, Direction aDirection){
            Scene activeScene = anActor.getScene();
            int exit;
            switch(aDirection){
                case NORTH:
                    exit = activeScene.getNorth();
                    break;
                case SOUTH:
                    exit = activeScene.getSouth();
                    break;
                case EAST:
                    exit = activeScene.getEast();
                    break;
                case WEST:
                    exit = activeScene.getWest();
                    break;
                default:
                    exit = Direction.NOEXIT; 
                    break;
            }
            if (exit != Direction.NOEXIT){
                moveActorTo(anActor, map.get(exit));
            }
            return exit;
        }
        
        //method to try a scene change
        //returns a special message if unsuccessful
        //returns the scene name + description if successful
        void moveHandler(int sceneNumber) {      
            String msg;
            if (sceneNumber == Direction.NOEXIT) {
                msg = "You can't travel that way, sorry.\n";
            } else {
                Scene activeScene = getPlayer().getScene();
                msg = "You are now in the " + activeScene.getName() + ". " + '\n'+ activeScene.getDescription();
            }
            System.out.print(msg);
        }


        //method to call the moveTo method for an Actor (NPC, PC)
        void moveActorTo(Actor aPerson, Scene aScene) {
            aPerson.setScene(aScene);
        }

        //method to call the moveTo method for the player character (PC)
        public int movePlayerTo(Direction dir){
            return moveTo(player, dir);
            
        }

        //method to return the player object
        public Actor getPlayer() {
            return player;
        }

        //method to return each of the scene's names
        public ArrayList<String> getAllSceneNames(){
            ArrayList<String> allSceneNames = new ArrayList<>();
            for(Scene s : this.map){
                allSceneNames.add(s.getName().toLowerCase());
            }
            return allSceneNames;
        }

        //Might delete
        public void printMap(){
            for(int i = 0; i < this.map.size(); i++){
                System.out.println(this.map.get(i).getName());
                System.out.println(this.map.get(i).getDescription());
                System.out.println(this.map.get(i).getExamination());
                System.out.println(this.map.get(i).getNorth());
                System.out.println(this.map.get(i).getSouth());
                System.out.println(this.map.get(i).getEast());
                System.out.println(this.map.get(i).getWest());
            }
        }
        public void printSceneObjects(){
            for(Scene s : this.map){
                System.out.println(s.getThings());
            }
        }
        //Might Delete

        //method for processing one word commands
        public String ProcessVerb(List<String> wordList){
            String verb;
            String msg = "";
            verb = wordList.get(0);
            if(verb.equals("quit")){

            }if(verb.equals("yes")){

            }if(verb.equals("no")){

            }
            return msg;
        }

        //method for processing two word commands
        public String ProcessVerbNoun(List<String> wordlist){
            String verb;
            String noun;
            String msg = "";
            boolean error = false;
            verb = wordlist.get(0);
            noun = wordlist.get(1);
            if(!commands.contains(verb)){
                msg = "You either made a typo, or " + verb +" isn't a recognized action.\n";
                error = true;
            }if(!allItemNames.contains(noun) && !directions.contains(noun) && !miscNouns.contains(noun)){
                msg += "You either made a typo, or " + noun + " isn't a recognized noun.\n";
                error = true;
            }if(!error){
                if(directions.contains(noun)){ //processing move commands
                    switch(noun){
                        case "north":
                            goN();
                            break;
                        case "south":
                            goS();
                            break;
                        case "east":
                            goE();
                            break;
                        case "west":
                            goW();
                            break;
                        default:
                            msg = "You either made a typo, or " + noun + " isn't a recognized direction.\n";
                            break;
                    }
                }if(verb.equals("examine")){ //processing examine commands
                    msg = examineItem(noun);
                }if(verb.equals("talk")){ //processing talk commands
    
                }if(verb.equals("take")){ //processing take commands
                    msg = takeItem(noun);

                }if(verb.equals("drop")){ //processing drop commands
                    msg = dropItem(noun);

                }if(verb.equals("use")){ //processing use commands
    
                }
            }

            return msg;
        }

        //method to call the processVerbNoun method and run the player's intended command
        public String RunCommand(List<String> inputstr) {
            String s;      
            s = ProcessVerbNoun(inputstr);
            return s;
        }

      
        // Main driver method
        public static void main(String[] args) throws IOException
        {


            Game works = new Game();
            BufferedReader in;
            String input;
            List<String> outputs;
            String output;
            in = new BufferedReader(new InputStreamReader(System.in));
            do {
                System.out.println();
                System.out.print("> ");
                input = in.readLine();
                input = TextParser.FilterDelims(input);
                outputs = TextParser.parseInput(input);
                output = works.RunCommand(outputs);
                System.out.println(output);
            } while (!"q".equals(input));

        }
}

        // Declaring ANSI_RESET so that we can reset the color
        //public static final String ANSI_RESET = "\u001B[0m";
  
        // Declaring the color
        // Custom declaration
        //public static final String ANSI_RED = "\u001B[31m";

        // Printing the text on console prior adding
        // the desired color
        //System.out.println(ANSI_RED + "This text is red" + ANSI_RESET + " this text is not");
        
        //randomizer
        //String[] available_cards = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Queen", "King", "Jack", "Ace"};  
        //java.util.Random random = new java.util.Random();
        //int random_computer_card = random.nextInt(available_cards.length);
        //System.out.println(available_cards[random_computer_card]);
