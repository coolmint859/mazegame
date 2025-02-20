# About
This project was developed for CS5410 Game Development while at my final semester at USU. It uses LWJGL and OpenGL for the graphics libraries, as well as a graphics frontend designed my my instructure Dr. Dean Mathias. 
The code written by him is found in src/usu.edu. He also wrote the KeyboardInput class and the StarterProject class. All other code was written by me (which is the vast majority of the game anyways).

This project had the following requirements:
1. Randomized Maze Generation (I used Randomized Prim's)
2. Support for maze sizes of 5x5, 10x10, 15x15, and 20x20.
3. Player controls game through use of keyboard, and the player can move around via the WASD, IJKL, and arrow keys.
4. Implementation of a scoring system based on the cells the player travels to.
5. Timer showing when the player started working on a maze.
6. Ability to toggle a hint (based on the next best cell to move to) as well as a solution (implemented using BFS) and a breadcrumbs trail
7. Ability to toggle high scores (Ordered by maze size and score)
8. Ability to toggle credits

Scoring is based on the shortest path. If the player travels to a cell on the shortest path, they gain 5 points. If they find the goal, they gain 10 points. Otherwise they lose 6 points. The higher penalty for moving off the shortest path is to incentivize the player to stay on the shortest path.
If the penalty was instead, say -4, then the player could move on a cell on the shortest path, then off the cell, and then back on the same cell, which would give them a net positive 6 points, (more than the 5 points they would gain otherwise) This incentivizes them to travel to as many cells as possible, which is not good gameplay design. Thus, the higher penalty ensures they lose points if they leave the shortest path.

At the time of the due date for this program, menuing and design had not yet been discussed so the graphics and code that creates it are rough. This may be fixed in the future, but it's unlikely.
Nevertheless, all features enumerated above are implemented. As the game is written in Java, both Windows and macOS should be able to run it.
