Lab1.java implements an A* search algorithm to find the shortest path between provided points
on a given terrain image. It takes into consideration the terrain types and the costs that
correspond to each type based on how easy/difficult it is to move through them, the elevation of
each pixel, and the longitude and latitude of each pixel. The code takes in a terrain image, an
elevation file, a path file, and an output file to print the path on. It returns the output file image
with the path drawn on it and the total distance of the shortest path found between points.
There are two HashMaps created to keep track of each terrain type and their corresponding
colors and costs. Each terrain type is linked to a color in one HashMap and linked to a cost for
that type (determined by how fast and slow to get through them) which is later used in
determining which way is the shortest path. The higher the cost the harder it is to move through.
The code uses a pixel class to keep track of each pixel and query them easily. Each pixel has
an x, y coord, a terrain type, a terrain color, an elevation and a cost. The terrain type is
determined by the terrain color which is determined by finding the color of the pixel on the
terrain image at the specified x, y value. The cost of each pixel is determined by its terrain type.
The F score function in the pixel class finds the F score for each pixel by combining the G score
and the heuristic value.
The heuristic function calculates the H score between two pixels. It uses the manhattan distance
between the coordinates of the pixel (includes elevations) to calculate because it is admissible
and non-monotonic so it never overestimates the true cost, which would slow down the code.
The cost calculation calculates the cost moving from one pixel to another. It does so by
determining which way the movement is (up/down or left/right) and then multiplying either the
given pixel latitude or longitude by the pixels terrain cost and adds the difference in elevation
times a weight of 0.5. This is so it takes into account that going across a pixel takes longer, the
higher cost of a terrain the longer it take to go through, and the greater the elevation the higher
the cost.
The get neighbors function finds all neighbors up, down, left, and right of the current pixel and
determines if they are valid by checking if they are in bounds and not a terrain type that is
impassible and returns them as possible path movement from current pixel.
The A* algorithm uses a priority queue to keep track of the pixels and a closed set to keep track
of the pixels already explored while also tracking scores and paths. It explores all the
neighboring pixels and determines the shortest path by comparing their costs and heuristic
value. If the path is found then it returns the list of pixels that represents the path and otherwise
just returns an empty list. Therefore it always returns a solution if such exists and guarantees an
optimal path by using the admissible heuristic.
The reconstruct path uses the came from map made in the A* function and the current and goal
pixel to put all the pixels the A* used to get from the current to goal pixel in the path list and
returns it.
The main function reads in all the args correctly and stores them as needed. When the path is
found it draws the path onto the output file and calculates the distance by iterating over the
found path and between each pixel in the path calculates the sqrt of the change in x *
PIXEL_WIDTH ^ 2 + change in y * PIXEL_HEIGHT ^ 2 + change in elevation ^ 2. This is so the
elevation is taken into account and also the direction of movement through each pixel (up/down
or left/right).
