to run this you need to start the server that is running on the port 35754

then as in the images, run the server. once it is run, then try and run the client on the same port
once done you will be shown the grid that is different for both the terminal clients, 

It is for terminal vs terminal, you will see and run as expected and the player will win if it reaches the score 9
or if it reaches round 5 and no player scored a point, or even if any player has scored a point, basically round 5 will 
not be played.
I used sb.append commands i could have made it a 2d array but tried to do it another way, that worked (took a lot of time)

The GUI version wasn't working for me i tried but it wasn't showing the proper grids and images weren't being displayed 
on the pop, so i skipped that part.

Rest the code is working you can run on IntelliJ, mobaxterm as well


Number of classes:
i have basic client-server class that runs client and the server

the main logic is in game state where everything works and the grid is been drawn, a little bit big class but includes all the 
messages to appear in dialog box, and the content for the cards, and the framing of the cards
encapsulates the logic and data for a two-player strategic game involving units like Axe, Hammer, Sword, and Arrow, which are arranged on separate boards for each player. 
It manages the game's progression by handling player moves, tracking scores, 
and updating the game board based on the rules defined for unit interactions. 
The class also supports resetting the game to a new round when both players pass their turn and determines the game's end based on rounds or scores. Additionally, 
it generates a textual representation of the game state for display purposes, helping players visualize the current status of the game

the game session thread class processes the threads to deal with, and is responsible for managing the 
flow of the game between two networked players, processing their inputs, updating the game state, 
and ensure both players have a current view of the game throughout their session.